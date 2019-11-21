package zkstrata.domain.visitor;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import zkstrata.compiler.Arguments;
import zkstrata.domain.Statement;
import zkstrata.domain.data.Selector;
import zkstrata.domain.data.accessors.SchemaAccessor;
import zkstrata.domain.data.accessors.ValueAccessor;
import zkstrata.exceptions.*;
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
import zkstrata.domain.gadgets.AstElement;
import zkstrata.domain.gadgets.Gadget;
import zkstrata.domain.data.schemas.Schema;
import zkstrata.parser.ast.AbstractSyntaxTree;
import zkstrata.parser.ast.Subject;
import zkstrata.parser.ast.predicates.Predicate;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.*;

public class ASTVisitor {
    private static final Logger LOGGER = LogManager.getLogger(ASTVisitor.class);
    private static final List<String> reservedAliases = List.of("private", "public");

    private final String parentAlias;
    private final Map<String, ValueAccessor> witnessData;
    private final Map<String, ValueAccessor> instanceData;
    private final Map<String, Schema> schemas;
    private final MapListener<String, StructuredData> subjects;

    private AbstractSyntaxTree ast;

    public ASTVisitor(Arguments args, String parentAlias) {
        this.witnessData = args.getWitnessData();
        this.instanceData = args.getInstanceData();
        this.schemas = args.getSchemas();
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

        List<Gadget> gadgets = new ArrayList<>();
        for (Predicate predicate : ast.getPredicates())
            gadgets.add(visitPredicate(predicate));

        this.checkUnusedSubjects();

        LOGGER.debug("Finishing visit of AST for {}: Found {} gadgets over {} subjects",
                ast.getSource(), gadgets.size(), subjects.getUsedMap().size());
        return new Statement(subjects.getUsedMap(), gadgets);
    }

    private StructuredData visitSubject(Subject subject) {
        String alias = subject.getAlias().getName();

        if (reservedAliases.contains(alias)) {
            if (parentAlias == null)
                throw new CompileTimeException(String.format("Reserved word `%s` used as alias.", alias),
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

    private Gadget visitPredicate(Predicate predicate) {
        Set<Class<? extends Gadget>> gadgets = ReflectionHelper.getAllGadgets();

        for (Class<? extends Gadget> gadget : gadgets) {
            AstElement from = gadget.getAnnotation(AstElement.class);

            if (from == null)
                throw new InternalCompilerException("Missing @AstElement annotation in %s.", gadget);

            if (from.value() == predicate.getClass()) {
                Map<String, Object> sourceValues = getSourceValues(predicate);
                try {
                    @SuppressWarnings("unchecked")
                    Gadget<? extends Gadget> instance = gadget.getConstructor().newInstance();
                    instance.initFrom(sourceValues);
                    return instance;
                } catch (NoSuchMethodException e) {
                    throw new InternalCompilerException("Gadget %s is missing a default constructor.", gadget);
                } catch (ReflectiveOperationException e) {
                    throw new InternalCompilerException("Invalid implementation of gadget %s.", gadget);
                }
            }
        }

        throw new InternalCompilerException("Missing gadget implementation for predicate: %s", predicate.getClass());
    }

    private Map<String, Object> getSourceValues(Predicate predicate) {
        Map<String, Object> values = new HashMap<>();

        Field[] fields = predicate.getClass().getDeclaredFields();
        for (Field field : fields) {
            try {
                String fieldName = field.getName();
                Object value = new PropertyDescriptor(fieldName, predicate.getClass(), "get" +
                        StringUtils.capitalize(fieldName), null).getReadMethod().invoke(predicate);
                values.put(fieldName, visitPredicateElement(value));
            } catch (IntrospectionException e) {
                throw new InternalCompilerException("Unable to call getter method for field %s in predicate %s. "
                        + "Ensure the predicate class defines public getter methods for all its fields.",
                        field.getName(), predicate.getClass().getSimpleName());
            } catch (ReflectiveOperationException e) {
                throw new InternalCompilerException("Invalid getter method for field %s in predicate %s.",
                        field.getName(), predicate.getClass().getSimpleName());
            }
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
        Collection<Object> result = createNewInstanceOf(collection.getClass());
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

    private Collection<Object> createNewInstanceOf(Class<? extends Collection> clazz) {
        try {
            @SuppressWarnings("unchecked")
            Collection<Object> instance = clazz.getConstructor().newInstance();
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new InternalCompilerException("Unable to create a new instance of collection %s using the default "
                    + "constructor.", clazz);
        }
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
            throw new CompileTimeException(String.format("Missing declaration for subject alias `%s`.",
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
