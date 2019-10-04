package zkstrata.domain.visitor;

import zkstrata.domain.data.Selector;
import zkstrata.domain.data.accessors.SchemaAccessor;
import zkstrata.domain.data.accessors.ValueAccessor;
import zkstrata.exceptions.CompileException;
import zkstrata.exceptions.InternalCompilerErrorException;
import zkstrata.parser.ast.Position;
import zkstrata.parser.ast.types.*;
import zkstrata.utils.ErrorUtils;
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
import zkstrata.parser.ast.Statement;
import zkstrata.parser.ast.Subject;
import zkstrata.parser.ast.predicates.Predicate;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ASTVisitorImpl implements ASTVisitor {
    private String statement;
    private Map<String, Schema> schemas;
    private Map<String, StructuredData> subjects;
    private Map<String, ValueAccessor> witnessData;
    private Map<String, ValueAccessor> instanceData;

    public ASTVisitorImpl(
            Map<String, Schema> schemas,
            Map<String, ValueAccessor> witnessData,
            Map<String, ValueAccessor> instanceData
    ) {
        this.schemas = schemas;
        this.subjects = new HashMap<>();
        this.witnessData = witnessData;
        this.instanceData = instanceData;
    }

    @Override
    public List<Gadget> visitStatement(Statement statement) {
        this.statement = statement.getStatement();

        for (Subject subject : statement.getSubjects()) {
            String alias = subject.getAlias().getName();
            if (subjects.containsKey(alias)) {
                String message = String.format("Alias `%s` is already defined.", alias);
                throw new CompileException(message, this.statement, alias.length(), subject.getAlias().getPosition());
            }

            this.subjects.put(alias, visitSubject(subject));
        }

        return statement.getPredicates().stream()
                .map(this::visitPredicate)
                .collect(Collectors.toList());
    }

    private StructuredData visitSubject(Subject subject) {
        String alias = subject.getAlias().getName();
        String schemaName = subject.getSchema().getName();
        Schema schema = schemas.getOrDefault(schemaName, SchemaHelper.resolve(schemaName));

        if (schema == null) {
            Position position = subject.getSchema().getPosition();
            String message = String.format("Undefined schema %s.", schemaName);
            throw new CompileException(message, statement, schemaName.length(), position);
        }

        if (subject.isWitness()) {
            if (!witnessData.isEmpty() && !witnessData.containsKey(alias)) {
                String message = String.format("Missing witness data for subject %s.", alias);
                throw new CompileException(message, statement, alias.length(), subject.getAlias().getPosition());
            }

            ValueAccessor accessor = witnessData.getOrDefault(alias, new SchemaAccessor(alias, schema));
            return new Witness(alias, schema, accessor);
        } else {
            ValueAccessor accessor = instanceData.get(alias);

            if (accessor == null) {
                String message = String.format("Missing instance data for subject %s.", alias);
                throw new CompileException(message, statement, alias.length(), subject.getAlias().getPosition());
            }

            return new Instance(alias, schema, accessor);
        }
    }

    // TODO: refactor?
    private Gadget visitPredicate(Predicate predicate) {
        Set<Class<? extends Gadget>> gadgets = ReflectionHelper.getAllGadgets();

        for (Class<? extends Gadget> gadget : gadgets) {
            AstElement from = gadget.getAnnotation(AstElement.class);

            if (from == null)
                throw new InternalCompilerErrorException(String.format("Missing @AstElement annotation in %s.", gadget));

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
                    String msg = String.format("Gadget %s is missing a default constructor.", gadget);
                    throw new InternalCompilerErrorException(msg);
                } catch (ReflectiveOperationException e) {
                    String msg = String.format("Invalid implementation of gadget %s.", gadget);
                    throw new InternalCompilerErrorException(msg);
                }
            }
        }

        String msg = String.format("Missing gadget implementation for predicate: %s", predicate.getClass());
        throw new InternalCompilerErrorException(msg);
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

        throw new InternalCompilerErrorException(String.format("Unimplemented type %s in AST.", type.getClass()));
    }

    /**
     * Returns an {@link InstanceVariable} of the visited literal.
     * As literals can only be public data (otherwise the witness would be leaked), return an {@link InstanceVariable}.
     */
    private Variable visitLiteral(Literal literal) {
        return new InstanceVariable(from(literal));
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
            return data.getVariable(Selector.from(identifier.getSelectors()));
        } else {
            String message = String.format("Missing declaration for subject alias `%s`.", subject);
            throw new CompileException(message, statement, subject.length(), identifier.getPosition());
        }
    }

    private String underlineError(int length, Position position) {
        int start = position.getCharPositionInLine();
        return ErrorUtils.underlineError(statement, start, start + length - 1, position.getLine(), start);
    }
}
