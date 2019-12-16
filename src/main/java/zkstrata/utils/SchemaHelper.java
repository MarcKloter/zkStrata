package zkstrata.utils;

import org.reflections.Reflections;
import zkstrata.domain.data.schemas.predefined.Schema;
import zkstrata.exceptions.InternalCompilerException;

import java.util.Set;

public class SchemaHelper {
    private SchemaHelper() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Resolves the given {@code name} to a {@link zkstrata.domain.data.schemas.Schema} object by creating a new
     * instance of the class annotated as {@link Schema} with name property equal to the given {@code name}.
     *
     * @param name name of the {@link Schema} to instantiate
     * @return instance of the class annotated as {@link Schema} with name property equal to {@code name}
     */
    public static zkstrata.domain.data.schemas.Schema resolve(String name) {
        Reflections reflections = new Reflections("zkstrata.domain.data.schemas.predefined");
        Set<Class<?>> schemas = reflections.getTypesAnnotatedWith(Schema.class);

        for (Class<?> schema : schemas) {
            Schema annotation = schema.getAnnotation(Schema.class);
            if (annotation.name().equals(name)) {
                if (!zkstrata.domain.data.schemas.Schema.class.isAssignableFrom(schema))
                    throw new InternalCompilerException("The predefined schema %s does not implement %s.",
                            name, zkstrata.domain.data.schemas.Schema.class);

                try {
                    return (zkstrata.domain.data.schemas.Schema) schema.getConstructor().newInstance();
                } catch (Exception e) {
                    throw new InternalCompilerException("Invalid implementation of schema: %s", name);
                }
            }
        }

        return null;
    }
}
