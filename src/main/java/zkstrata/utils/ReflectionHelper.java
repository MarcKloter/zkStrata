package zkstrata.utils;

import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import zkstrata.domain.gadgets.Gadget;
import zkstrata.domain.conjunctions.Conjunction;
import zkstrata.exceptions.CompileTimeException;
import zkstrata.exceptions.InternalCompilerException;
import zkstrata.exceptions.TypeCheckException;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
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
     * Returns all non-abstract classes implementing the {@link Conjunction} interface.
     *
     * @return {@link Set} of classes implementing {@link Conjunction}
     */
    public static Set<Class<? extends Conjunction>> getAllConjunctions() {
        Reflections reflections = new Reflections("zkstrata.domain.conjunctions");

        Set<Class<? extends Conjunction>> conjunctions = reflections.getSubTypesOf(Conjunction.class);

        return conjunctions
                .stream()
                .filter(conjunction -> !Modifier.isAbstract(conjunction.getModifiers()))
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
     * Returns all constructors annotated with the given annotation.
     *
     * @param clazz {@link Annotation} to look for
     * @return {@link Set} of {@link Constructor} annotated with {@code clazz}
     */
    public static Set<Constructor> getConstructorsAnnotatedWith(Class<? extends Annotation> clazz) {
        Reflections reflections = new Reflections(
                new ConfigurationBuilder()
                        .setUrls(ClasspathHelper.forPackage("zkstrata"))
                        .setScanners(new MethodAnnotationsScanner())
        );

        return reflections.getConstructorsAnnotatedWith(clazz);
    }

    /**
     * Asserts whether the return type of the given {@link Method} matches the {@code parameterizedType} and {@code argumentType}.
     * Throws an {@link InternalCompilerException} if this does not match.
     *
     * @param method            {@link Method} object to check the return type on
     * @param parameterizedType expected class of the enclosing parameterized type
     * @param argumentType      expected class of the enclosed argument type
     */
    public static void assertParameterizedReturnType(Method method, Class<?> parameterizedType, Class<?> argumentType) {
        ParameterizedType parent = (ParameterizedType) method.getGenericReturnType();
        Class<?> child = (Class<?>) parent.getActualTypeArguments()[0];
        if (method.getReturnType() != parameterizedType)
            assertIsAssignableFrom(child, argumentType);
    }

    /**
     * Creates an instance of the given {@code clazz}.
     *
     * @param clazz class to instantiate
     * @return instance of the provided {@code clazz}
     */
    public static <T> T createInstance(Class<T> clazz) {
        try {
            return clazz.getConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new InternalCompilerException("Unable to create a new instance of %s using the default constructor.", clazz);
        }
    }

    /**
     * Asserts whether the given {@code object} is assignable from {@code clazz}.
     * Throws a {@link InternalCompilerException} if the check fails.
     *
     * @param clazz  class to check assign-ability
     * @param object object to check
     */
    public static void assertIsAssignableFrom(Class<?> clazz, Class<?> object) {
        if (!clazz.isAssignableFrom(object))
            throw new InternalCompilerException("The object %s does not implement %s.", object, clazz);
    }

    /**
     * Invokes the getter method for the provided {@link Field} on the given {@link Object}.
     *
     * @param object {@link Object} to invoke getter on
     * @param field  {@link Field} to invoke getter for
     * @return {@link Object} returned by the getter invocation
     */
    public static Object invokeGetter(Object object, Field field) {
        try {
            String fieldName = field.getName();
            String readMethod = "get" + StringUtils.capitalize(fieldName);
            PropertyDescriptor property = new PropertyDescriptor(fieldName, object.getClass(), readMethod, null);
            return property.getReadMethod().invoke(object);
        } catch (Exception e) {
            throw new InternalCompilerException("Unable to call getter method for field %s in object of class %s. "
                    + "Ensure the class defines public getter methods for all its fields.",
                    field.getName(), object.getClass().getSimpleName());
        }
    }

    /**
     * Invokes the given static {@link Method} using the provided {@code args}.
     *
     * @param method {@link Method} to invoke
     * @param args   array of objects to use as arguments
     * @return {@link Object} returned by the method invocation
     */
    public static Object invokeStaticMethod(Method method, Object... args) {
        try {
            return method.invoke(null, args);
        } catch (IllegalAccessException e) {
            throw new InternalCompilerException("Error during invocation of method %s in %s.",
                    method.getName(), method.getDeclaringClass().getSimpleName());
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof CompileTimeException)
                throw (CompileTimeException) cause;
            if (cause instanceof TypeCheckException)
                throw (TypeCheckException) cause;
            else
                throw new InternalCompilerException(cause, "Invalid exception %s thrown by %s in %s.",
                        cause.getClass().getSimpleName(), method.getName(), method.getDeclaringClass());
        }
    }
}
