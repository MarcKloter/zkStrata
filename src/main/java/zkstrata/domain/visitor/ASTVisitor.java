package zkstrata.domain.visitor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import zkstrata.compiler.Arguments;
import zkstrata.domain.Proposition;
import zkstrata.domain.Statement;
import zkstrata.domain.conjunctions.Conjunction;
import zkstrata.domain.data.Selector;
import zkstrata.domain.data.accessors.ReferenceAccessor;
import zkstrata.domain.data.accessors.ValueAccessor;
import zkstrata.exceptions.*;
import zkstrata.parser.ast.Node;
import zkstrata.parser.ast.connectives.Connective;
import zkstrata.parser.ast.predicates.Predicate;
import zkstrata.parser.ast.types.*;
import zkstrata.utils.BinaryTree;
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
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static zkstrata.utils.ReflectionHelper.*;

public class ASTVisitor {
    private static final Logger LOGGER = LogManager.getRootLogger();
    private static final Set<Method> CONSTANTS = getMethodsAnnotatedWith(zkstrata.domain.data.types.Constant.class);
    private static final Set<Class<? extends Conjunction>> CONJUNCTION_TYPES = getAllConjunctions();
    private static final Set<Class<? extends Gadget>> GADGET_TYPES = prepareGadgetTypes();
    private static final List<String> RESERVED_ALIASES = List.of("private", "public");

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

    private static Set<Class<? extends Gadget>> prepareGadgetTypes() {
        return getAllGadgets().stream()
                .filter(gadget -> gadget.isAnnotationPresent(AstElement.class))
                .collect(Collectors.toSet());
    }

    public Statement visit(AbstractSyntaxTree ast) {
        this.ast = ast;

        LOGGER.debug("Starting visit of AST for {}", ast.getSource());

        for (Subject subject : ast.getSubjects()) {
            String alias = subject.getAlias().getName();
            if (subjects.containsKey(alias))
                throw new CompileTimeException(format("Alias `%s` is already defined.", alias),
                        pinPosition(subject.getAlias()));

            this.subjects.put(alias, visitSubject(subject));
        }

        Proposition predicate = visitNode(ast.getRoot());

        LOGGER.debug("Finishing visit of AST for {}", ast.getSource());

        return new Statement(getRelevantSubjects(), predicate);
    }

    private StructuredData visitSubject(Subject subject) {
        String alias = subject.getAlias().getName();

        if (RESERVED_ALIASES.contains(alias)) {
            if (parentAlias == null)
                throw new CompileTimeException(format("Reserved keyword `%s` used as alias.", alias),
                        pinPosition(subject.getAlias()));

            alias = parentAlias;
        }

        String schemaName = subject.getSchema().getName();
        Schema schema = schemas.getOrDefault(schemaName, SchemaHelper.resolve(schemaName));

        if (schema == null)
            throw new CompileTimeException(format("Undefined schema `%s`.", schemaName),
                    pinPosition(subject.getSchema()));

        if (subject.isWitness()) {
            if (!witnessData.isEmpty() && !witnessData.containsKey(alias))
                throw new CompileTimeException(format("Missing witness data for subject `%s`.", alias),
                        pinPosition(subject.getAlias()));

            ValueAccessor accessor = witnessData.getOrDefault(alias, new ReferenceAccessor(alias, schema));
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

        return Conjunction.createInstanceOf(conjunctionType, parts);
    }

    private Class<? extends Conjunction> getConjunctionType(Connective connective) {
        for (Class<? extends Conjunction> conjunctionType : CONJUNCTION_TYPES) {
            AstElement from = conjunctionType.getAnnotation(AstElement.class);

            if (from == null)
                throw new InternalCompilerException("Missing @AstElement annotation in %s.", conjunctionType);

            if (from.value() == connective.getClass()) {
                return conjunctionType;
            }
        }

        throw new InternalCompilerException("Missing conjunction implementation for connective: %s", connective.getClass());
    }

    private Proposition visitPredicate(Predicate predicate) {
        for (Class<? extends Gadget> gadgetType : GADGET_TYPES) {
            AstElement from = gadgetType.getAnnotation(AstElement.class);

            if (from.value() == predicate.getClass())
                return createInstance(gadgetType).initFrom(getSourceValues(predicate));
        }

        throw new InternalCompilerException("Missing gadget implementation for predicate: %s", predicate.getClass());
    }

    private Map<String, Object> getSourceValues(Predicate predicate) {
        Map<String, Object> values = new HashMap<>();

        Field[] fields = predicate.getClass().getDeclaredFields();
        for (Field field : fields) {
            Object value = invokeGetter(predicate, field);
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

        return element;
    }

    private Variable visitType(Value type) {
        if (Literal.class.isAssignableFrom(type.getClass()))
            return visitLiteral((Literal) type);

        if (type.getClass().equals(Identifier.class))
            return visitIdentifier((Identifier) type);

        if (type.getClass().equals(Constant.class))
            return visitConstant((Constant) type);

        throw new InternalCompilerException("Unimplemented type %s in AST.", type.getClass().getSimpleName());
    }

    /**
     * Returns an {@link InstanceVariable} of the visited literal.
     * As literals can only be public data (otherwise the witness would be leaked), return an {@link InstanceVariable}.
     */
    private InstanceVariable visitLiteral(Literal literal) {
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
                        Set.of(pinPosition(getEqualTraceable(traceable, result)), pinPosition(traceable)));
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
        Collection<Object> instance = createInstance(clazz);
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
            throw new CompileTimeException(format("Undeclared alias `%s` found.", subject), pinPosition(identifier));
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

    private InstanceVariable visitConstant(Constant constant) {
        for (Method method : CONSTANTS) {
            String constantIdentifier = method.getAnnotation(zkstrata.domain.data.types.Constant.class).value();

            if (constantIdentifier.equals(constant.getValue())) {
                return new InstanceVariable((zkstrata.domain.data.types.Literal) invokeStaticMethod(method),
                        null, pinPosition(constant));
            }
        }

        throw new CompileTimeException("Invalid constant.", pinPosition(constant));
    }

    private List<StructuredData> getRelevantSubjects() {
        List<StructuredData> relevantSubjects = new ArrayList<>(this.subjects.values());
        for (String subject : this.subjects.getUnusedKeySet()) {
            if (!isReservedAlias(subject) && (!hasValidationRule(subject) || isFlaggedAsPublic(subject))) {
                LOGGER.warn("Removing unused subject '{}'", subject);
                relevantSubjects.remove(this.subjects.get(subject));
            }
        }
        return relevantSubjects;
    }

    private boolean isReservedAlias(String alias) {
        return RESERVED_ALIASES.contains(alias);
    }

    private boolean hasValidationRule(String alias) {
        return subjects.get(alias).getSchema().hasValidationRule();
    }

    private boolean isFlaggedAsPublic(String alias) {
        return !subjects.get(alias).isWitness();
    }

    private Position.Absolute pinPosition(Traceable traceable) {
        return new Position.Absolute(ast.getSource(), ast.getStatement(), traceable.getPosition());
    }
}
