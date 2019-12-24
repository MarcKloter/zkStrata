package zkstrata.domain.visitor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import zkstrata.compiler.Arguments;
import zkstrata.domain.Proposition;
import zkstrata.domain.Statement;
import zkstrata.domain.conjunctions.Conjunction;
import zkstrata.domain.data.Selector;
import zkstrata.domain.data.accessors.SchemaAccessor;
import zkstrata.domain.data.accessors.ValueAccessor;
import zkstrata.exceptions.*;
import zkstrata.parser.ast.Node;
import zkstrata.parser.ast.connectives.Connective;
import zkstrata.parser.ast.predicates.Predicate;
import zkstrata.parser.ast.types.*;
import zkstrata.utils.BinaryTree;
import zkstrata.utils.ReflectionHelper;
import zkstrata.utils.SchemaHelper;
import zkstrata.domain.data.types.wrapper.Null;
import zkstrata.domain.data.schemas.wrapper.Instance;
import zkstrata.domain.data.schemas.wrapper.StructuredData;
import zkstrata.domain.data.schemas.wrapper.Witness;
import zkstrata.domain.data.types.wrapper.InstanceVariable;
import zkstrata.domain.data.types.wrapper.Variable;
import zkstrata.domain.gadgets.Gadget;
import zkstrata.domain.data.schemas.Schema;
import zkstrata.parser.ast.AbstractSyntaxTree;
import zkstrata.parser.ast.Subject;

import java.lang.reflect.Field;
import java.util.*;

public class ASTVisitor {
    private static final Logger LOGGER = LogManager.getRootLogger();
    private static final List<String> reservedAliases = List.of("private", "public");

    private final String parentAlias;
    private final Map<String, ValueAccessor> witnessData;
    private final Map<String, ValueAccessor> instanceData;
    private final Map<String, Schema> schemas;
    private final MapListener<String, StructuredData> subjects;

    private AbstractSyntaxTree ast;

    public ASTVisitor(Arguments.SubjectData subjectData) {
        this(subjectData, null);
    }

    public ASTVisitor(Arguments.SubjectData subjectData, String parentAlias) {
        this.witnessData = subjectData.getWitnessData();
        this.instanceData = subjectData.getInstanceData();
        this.schemas = subjectData.getSchemas();
        this.subjects = new MapListener<>(new HashMap<>());
        this.parentAlias = parentAlias;
    }

    public Statement visit(AbstractSyntaxTree ast) {
        this.ast = ast;

        LOGGER.debug("Starting visit of AST for {}", ast.getSource());

        for (Subject subject : ast.getSubjects()) {
            String alias = subject.getAlias().getName();
            if (subjects.containsKey(alias))
                throw new CompileTimeException(String.format("Alias `%s` is already defined.", alias),
                        pinPosition(subject.getAlias()));

            this.subjects.put(alias, visitSubject(subject));
        }

        Proposition predicate = visitNode(ast.getRoot());

        this.checkUnusedSubjects();

        LOGGER.debug("Finishing visit of AST for {}", ast.getSource());

        return new Statement(subjects.getUsedMap(), predicate);
    }

    private StructuredData visitSubject(Subject subject) {
        String alias = subject.getAlias().getName();

        if (reservedAliases.contains(alias)) {
            if (parentAlias == null)
                throw new CompileTimeException(String.format("Reserved keyword `%s` used as alias.", alias),
                        pinPosition(subject.getAlias()));

            alias = parentAlias;
        }

        String schemaName = subject.getSchema().getName();
        Schema schema = schemas.getOrDefault(schemaName, SchemaHelper.resolve(schemaName));

        if (schema == null)
            throw new CompileTimeException(String.format("Undefined schema %s.", schemaName),
                    pinPosition(subject.getSchema()));

        if (subject.isWitness()) {
            if (!witnessData.isEmpty() && !witnessData.containsKey(alias))
                throw new CompileTimeException(String.format("Missing witness data for subject %s.", alias),
                        pinPosition(subject.getAlias()));

            ValueAccessor accessor = witnessData.getOrDefault(alias, new SchemaAccessor(alias, schema));
            return new Witness(alias, schema, accessor);
        } else
            return new Instance(alias, schema, instanceData.get(alias));
    }

    private Proposition visitNode(Node node) {
        if (node instanceof Connective)
            return visitConnective((Connective) node);

        if (node instanceof Predicate)
            return visitPredicate((Predicate) node);

        throw new InternalCompilerException("Missing visitor for AST node of type %s.", node.getClass());
    }

    private Proposition visitConnective(Connective connective) {
        List<Proposition> parts = new ArrayList<>();

        Class<? extends Conjunction> conjunctionType = getConjunctionType(connective);

        Proposition left = visitNode(connective.getLeft());
        if (conjunctionType.equals(left.getClass()))
            parts.addAll(((Conjunction) left).getParts());
        else
            parts.add(left);

        Proposition right = visitNode(connective.getRight());
        if (conjunctionType.equals(right.getClass()))
            parts.addAll(((Conjunction) right).getParts());
        else
            parts.add(right);

        try {
            return conjunctionType.getConstructor(List.class).newInstance(parts);
        } catch (NoSuchMethodException e) {
            throw new InternalCompilerException("Conjunction %s is missing a parameterized constructor with List.class.", conjunctionType);
        } catch (ReflectiveOperationException e) {
            throw new InternalCompilerException("Invalid implementation of conjunction %s.", conjunctionType);
        }
    }

    private Class<? extends Conjunction> getConjunctionType(Connective connective) {
        Set<Class<? extends Conjunction>> conjunctions = ReflectionHelper.getAllConjunctions();

        for (Class<? extends Conjunction> conjunction : conjunctions) {
            AstElement from = conjunction.getAnnotation(AstElement.class);

            if (from == null)
                throw new InternalCompilerException("Missing @AstElement annotation in %s.", conjunction);

            if (from.value() == connective.getClass()) {
                return conjunction;
            }
        }

        throw new InternalCompilerException("Missing conjunction implementation for connective: %s", connective.getClass());
    }

    private Proposition visitPredicate(Predicate predicate) {
        Set<Class<? extends Gadget>> gadgets = ReflectionHelper.getAllGadgets();

        for (Class<? extends Gadget> gadget : gadgets) {
            AstElement from = gadget.getAnnotation(AstElement.class);

            if (from == null)
                throw new InternalCompilerException("Missing @AstElement annotation in %s.", gadget);

            if (from.value() == predicate.getClass()) {
                Map<String, Object> sourceValues = getSourceValues(predicate);

                Gadget instance = ReflectionHelper.createInstance(gadget);
                instance.initFrom(sourceValues);
                return instance;
            }
        }

        throw new InternalCompilerException("Missing gadget implementation for predicate: %s", predicate.getClass());
    }

    private Map<String, Object> getSourceValues(Predicate predicate) {
        Map<String, Object> values = new HashMap<>();

        Field[] fields = predicate.getClass().getDeclaredFields();
        for (Field field : fields) {
            Object value = ReflectionHelper.invokeGetter(predicate, field);
            values.put(field.getName(), visitPredicateElement(value));
        }

        return values;
    }

    private Object visitPredicateElement(Object element) {
        if (element == null)
            return new Null();

        if (Collection.class.isAssignableFrom(element.getClass()))
            return visitCollection((Collection) element);

        if (element instanceof Value)
            return visitType((Value) element);

        if (element instanceof BinaryTree)
            return visitBinaryTree((BinaryTree) element);

        throw new InternalCompilerException("Unimplemented element %s in AST.", element.getClass());
    }

    private Variable visitType(Value type) {
        if (Literal.class.isAssignableFrom(type.getClass()))
            return visitLiteral((Literal) type);

        if (type.getClass().equals(Identifier.class))
            return visitIdentifier((Identifier) type);

        throw new InternalCompilerException("Unimplemented type %s in AST.", type.getClass());
    }

    /**
     * Returns an {@link InstanceVariable} of the visited literal.
     * As literals can only be public data (otherwise the witness would be leaked), return an {@link InstanceVariable}.
     */
    private Variable visitLiteral(Literal literal) {
        return new InstanceVariable(from(literal), null, pinPosition(literal));
    }

    private zkstrata.domain.data.types.Literal from(Literal literal) {
        Class<? extends Literal> type = literal.getClass();
        if (type == HexLiteral.class)
            return new zkstrata.domain.data.types.custom.HexLiteral(((HexLiteral) literal).getValue());

        return new zkstrata.domain.data.types.Literal(literal.getValue());
    }

    private Collection<Object> visitCollection(Collection collection) {
        Collection<Object> result = createCollection(collection.getClass());
        for (Object object : collection) {
            Object element = visitPredicateElement(object);
            if (!result.add(element) && element instanceof Traceable) {
                Traceable traceable = (Traceable) element;

                throw new CompileTimeException("Duplicate element.",
                        Set.of(pinPosition(getEqualTraceable(traceable, result)), pinPosition((Traceable) element)));
            }
        }

        return result;
    }

    private Traceable getEqualTraceable(Traceable traceable, Collection collection) {
        for (Object object : collection)
            if (traceable.equals(object))
                return (Traceable) object;

        throw new InternalCompilerException("The provided collection does not contain the requested element.");
    }

    private Collection<Object> createCollection(Class<? extends Collection> clazz) {
        @SuppressWarnings("unchecked")
        Collection<Object> instance = ReflectionHelper.createInstance(clazz);
        return instance;
    }

    /**
     * Binds an identifier to its referenced value.
     */
    private Variable visitIdentifier(Identifier identifier) {
        String subject = identifier.getSubject();
        if (subjects.containsKey(subject)) {
            StructuredData data = subjects.get(subject);
            return data.getVariable(new Selector(identifier.getSelectors()), pinPosition(identifier));
        } else {
            throw new CompileTimeException(String.format("Undeclared alias `%s` found.",
                    subject), pinPosition(identifier));
        }
    }

    private BinaryTree<Variable> visitBinaryTree(BinaryTree binaryTree) {
        return new BinaryTree<>(visitBinaryTreeNode(binaryTree.getRoot()));
    }

    private BinaryTree.Node<Variable> visitBinaryTreeNode(BinaryTree.Node node) {
        if (!node.isLeaf()) {
            return new BinaryTree.Node<>(visitBinaryTreeNode(node.getLeft()), visitBinaryTreeNode(node.getRight()));
        } else {
            if (node.getValue() instanceof Value)
                return new BinaryTree.Node<>(visitType((Value) node.getValue()));
            else
                throw new InternalCompilerException("Expected BinaryTree.Node of class Value, found %s.",
                        node.getValue().getClass().getSimpleName());
        }
    }

    private void checkUnusedSubjects() {
        for (String alias : subjects.getUnusedKeySet()) {
            if (!reservedAliases.contains(alias))
                LOGGER.warn("Removing unused subject '{}'", alias);
        }
    }

    private Position.Absolute pinPosition(Traceable traceable) {
        return new Position.Absolute(ast.getSource(), ast.getStatement(), traceable.getPosition());
    }
}
