package zkstrata.utils;

import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import zkstrata.domain.gadgets.Gadget;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.Set;
import java.util.stream.Collectors;

public class ReflectionHelper {
    private ReflectionHelper() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Returns all non-abstract classes implementing the {@link Gadget} interface.
     *
     * @return {@link Set} of classes implementing {@link Gadget}
     */
    public static Set<Class<? extends Gadget>> getAllGadgets() {
        Reflections reflections = new Reflections("zkstrata.domain.gadgets.impl");

        Set<Class<? extends Gadget>> gadgets = reflections.getSubTypesOf(Gadget.class);

        return gadgets
                .stream()
                .filter(gadget -> !Modifier.isAbstract(gadget.getModifiers()))
                .collect(Collectors.toSet());
    }

    /**
     * Returns all methods annotated with the given annotation.
     *
     * @param clazz {@link Annotation} to look for
     * @return {@link Set} of {@link Method} annotated with {@code clazz}
     */
    public static Set<Method> getMethodsAnnotatedWith(Class<? extends Annotation> clazz) {
        Reflections reflections = new Reflections(
                new ConfigurationBuilder()
                        .setUrls(ClasspathHelper.forPackage("zkstrata"))
                        .setScanners(new MethodAnnotationsScanner())
        );

        return reflections.getMethodsAnnotatedWith(clazz);
    }

    /**
     * Checks whether the return type of the given {@link Method} matches the {@code parameterizedType} and {@code argumentType}.
     *
     * @param method            {@link Method} object to check the return type on
     * @param parameterizedType expected class of the enclosing parameterized type
     * @param argumentType      expected class of the enclosed argument type
     * @return {@code true} if the parameterized and argument types match the return type of the given method, {@code false} otherwise
     */
    public static boolean checkReturnType(Method method, Class<?> parameterizedType, Class<?> argumentType) {
        ParameterizedType parent = (ParameterizedType) method.getGenericReturnType();
        Class<?> child = (Class<?>) parent.getActualTypeArguments()[0];
        return method.getReturnType() == parameterizedType && child.isAssignableFrom(argumentType);
    }
}
