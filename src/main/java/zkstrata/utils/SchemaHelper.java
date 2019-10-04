package zkstrata.utils;

import org.reflections.Reflections;
import zkstrata.domain.data.schemas.predefined.Schema;
import zkstrata.exceptions.InternalCompilerErrorException;

import java.util.Set;

public class SchemaHelper {
    public static zkstrata.domain.data.schemas.Schema resolve(String name) {
        Reflections reflections = new Reflections("zkstrata.domain.data.schemas.predefined");
        Set<Class<?>> schemas = reflections.getTypesAnnotatedWith(Schema.class);

        for (Class<?> schema : schemas) {
            Schema annotation = schema.getAnnotation(Schema.class);
            if (annotation.name().equals(name)) {
                if (!zkstrata.domain.data.schemas.Schema.class.isAssignableFrom(schema)) {
                    // TODO: add test for this case
                    String msg = String.format("The predefined schema %s does not implement %s.", name, zkstrata.domain.data.schemas.Schema.class);
                    throw new InternalCompilerErrorException(msg);
                }

                try {
                    return (zkstrata.domain.data.schemas.Schema) schema.getConstructor().newInstance();
                } catch (Exception e) {
                    throw new InternalCompilerErrorException(String.format("Invalid implementation of schema: %s", name));
                }
            }
        }

        return null;
    }
}
