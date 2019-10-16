package zkstrata.domain.visitor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import zkstrata.domain.Statement;
import zkstrata.domain.data.Selector;
import zkstrata.domain.data.accessors.SchemaAccessor;
import zkstrata.domain.data.accessors.ValueAccessor;
import zkstrata.exceptions.CompileTimeException;
import zkstrata.exceptions.InternalCompilerException;
import zkstrata.exceptions.Position;
import zkstrata.exceptions.Traceable;
import zkstrata.parser.ast.types.*;
import zkstrata.utils.ReflectionHelper;
import zkstrata.utils.SchemaHelper;
import zkstrata.domain.data.types.wrapper.Nullable;
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

import java.lang.reflect.Field;
import java.util.*;

public class ASTVisitor {
    private static final Logger LOGGER = LogManager.getLogger(ASTVisitor.class);

    private Map<String, ValueAccessor> witnessData;
    private Map<String, ValueAccessor> instanceData;
    private Map<String, Schema> schemas;

    private MapListener<String, StructuredData> subjects;

    private AbstractSyntaxTree ast;

    public ASTVisitor(
            AbstractSyntaxTree ast,
            Map<String, ValueAccessor> witnessData,
            Map<String, ValueAccessor> instanceData,
            Map<String, Schema> schemas
    ) {
        this.ast = ast;
        this.witnessData = witnessData;
        this.instanceData = instanceData;
        this.schemas = schemas;
        this.subjects = new MapListener<>(new HashMap<>());
    }

    public Statement visitStatement() {
        for (Subject subject : ast.getSubjects()) {
            String alias = subject.getAlias().getName();
            if (subjects.containsKey(alias))
                throw new CompileTimeException(String.format("Alias `%s` is already defined.", alias), pinPosition(subject.getAlias()));

            this.subjects.put(alias, visitSubject(subject));
        }

        List<Gadget> gadgets = new ArrayList<>();
        for (Predicate predicate : ast.getPredicates())
            gadgets.add(visitPredicate(predicate));

        this.checkUnusedSubjects();

        return new Statement(subjects.getUsedMap(), gadgets);
    }

    private StructuredData visitSubject(Subject subject) {
        String alias = subject.getAlias().getName();
        String schemaName = subject.getSchema().getName();
        Schema schema = schemas.getOrDefault(schemaName, SchemaHelper.resolve(schemaName));

        if (schema == null)
            throw new CompileTimeException(String.format("Undefined schema %s.", schemaName), pinPosition(subject.getSchema()));

        if (subject.isWitness()) {
            if (!witnessData.isEmpty() && !witnessData.containsKey(alias))
                throw new CompileTimeException(String.format("Missing witness data for subject %s.", alias), pinPosition(subject.getAlias()));

            ValueAccessor accessor = witnessData.getOrDefault(alias, new SchemaAccessor(alias, schema));
            return new Witness(alias, schema, accessor);
        } else {
            ValueAccessor accessor = instanceData.get(alias);

            if (accessor == null)
                throw new CompileTimeException(String.format("Missing instance data for subject %s.", alias), pinPosition(subject.getAlias()));

            return new Instance(alias, schema, accessor);
        }
    }

    // TODO: refactor?
    private Gadget visitPredicate(Predicate predicate) {
        Set<Class<? extends Gadget>> gadgets = ReflectionHelper.getAllGadgets();

        for (Class<? extends Gadget> gadget : gadgets) {
            AstElement from = gadget.getAnnotation(AstElement.class);

            if (from == null)
                throw new InternalCompilerException("Missing @AstElement annotation in %s.", gadget);

            if (from.value() == predicate.getClass()) {
                try {
                    Gadget instance = gadget.getConstructor().newInstance();

                    Field[] sourceFields = predicate.getClass().getDeclaredFields();
                    Map<String, Variable> sourceValues = new HashMap<>();

                    for (Field source : sourceFields) {
                        source.setAccessible(true);
                        Value sourceValue = (Value) source.get(predicate);
                        sourceValues.put(source.getName(), visitType(sourceValue));
                    }

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

    private Variable visitType(Value type) {
        if (type == null) {
            return new Nullable();
        } else {
            if (Literal.class.isAssignableFrom(type.getClass()))
                return visitLiteral((Literal) type);
            if (type.getClass().equals(Identifier.class))
                return visitIdentifier((Identifier) type);
        }

        throw new InternalCompilerException("Unimplemented type %s in AST.", type.getClass());
    }

    /**
     * Returns an {@link InstanceVariable} of the visited literal.
     * As literals can only be public data (otherwise the witness would be leaked), return an {@link InstanceVariable}.
     */
    private Variable visitLiteral(Literal literal) {
        return new InstanceVariable(from(literal), pinPosition(literal));
    }

    private zkstrata.domain.data.types.Literal from(Literal literal) {
        Class<? extends Literal> type = literal.getClass();
        if (type == HexLiteral.class)
            return new zkstrata.domain.data.types.custom.HexLiteral(((HexLiteral) literal).getValue());

        return new zkstrata.domain.data.types.Literal(literal.getValue());
    }

    /**
     * Binds an identifier to its referenced value.
     */
    private Variable visitIdentifier(Identifier identifier) {
        String subject = identifier.getSubject();
        if (subjects.containsKey(subject)) {
            StructuredData data = subjects.get(subject);
            return data.getVariable(Selector.from(identifier.getSelectors()), pinPosition(identifier));
        } else {
            throw new CompileTimeException(String.format("Missing declaration for subject alias `%s`.", subject), pinPosition(identifier));
        }
    }

    private void checkUnusedSubjects() {
        for (String alias : subjects.getUnusedKeySet()) {
            LOGGER.warn("Unused subject '{}'", alias);
        }
    }

    private Position.Absolute pinPosition(Traceable traceable) {
        return new Position.Absolute(ast.getSource(), ast.getStatement(), traceable.getPosition());
    }
}
