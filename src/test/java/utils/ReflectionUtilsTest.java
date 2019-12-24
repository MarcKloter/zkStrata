package utils;

import org.junit.jupiter.api.Test;
import zkstrata.exceptions.InternalCompilerException;

import java.io.IOException;
import java.util.Collection;

import static zkstrata.utils.ReflectionHelper.*;
import static org.junit.jupiter.api.Assertions.*;

public class ReflectionUtilsTest {
    @Test
    void Invoke_Wrong_Arguments_Should_Throw() {
        InternalCompilerException exception = assertThrows(InternalCompilerException.class, () ->
                invokeStaticMethod(ReflectionMock.class.getMethod("wrongArguments", Object.class))
        );

        assertTrue(exception.getMessage().toLowerCase().contains("error during invocation"));
    }

    @Test
    void Invalid_Exception_Should_Throw() {
        InternalCompilerException exception = assertThrows(InternalCompilerException.class, () ->
                invokeStaticMethod(ReflectionMock.class.getMethod("invalidException"))
        );

        assertTrue(exception.getMessage().toLowerCase().contains("invalid exception"));
    }

    @Test
    void Missing_Getter_Should_Throw() {
        InternalCompilerException exception = assertThrows(InternalCompilerException.class, () ->
                invokeGetter(new ReflectionMock(null), ReflectionMock.class.getField("fieldWithoutGetter"))
        );

        assertTrue(exception.getMessage().toLowerCase().contains("unable to call getter method"));
    }

    @Test
    void Not_Assignable_Should_Throw() {
        InternalCompilerException exception = assertThrows(InternalCompilerException.class, () ->
                assertIsAssignableFrom(String.class, Integer.class)
        );

        assertTrue(exception.getMessage().toLowerCase().contains("does not implement"));
    }

    @Test
    void Missing_Default_Constructor_Should_Throw() {
        InternalCompilerException exception = assertThrows(InternalCompilerException.class, () ->
                createInstance(ReflectionMock.class)
        );

        assertTrue(exception.getMessage().toLowerCase().contains("unable to create a new instance"));
    }

    @Test
    void Incorrect_Return_Type_Should_Throw_1() {
        InternalCompilerException exception = assertThrows(InternalCompilerException.class, () ->
                assertParameterizedReturnType(ReflectionMock.class.getMethod("invalidReturnType"), Object.class, Object.class)
        );

        assertTrue(exception.getMessage().toLowerCase().contains("return type of method"));
    }

    @Test
    void Incorrect_Return_Type_Should_Throw_2() {
        InternalCompilerException exception = assertThrows(InternalCompilerException.class, () ->
                assertParameterizedReturnType(ReflectionMock.class.getMethod("invalidReturnType2"), Object.class, Object.class)
        );

        assertTrue(exception.getMessage().toLowerCase().contains("return type of method"));
    }

    @Test
    void Incorrect_Return_Type_Should_Throw_3() {
        InternalCompilerException exception = assertThrows(InternalCompilerException.class, () ->
                assertParameterizedReturnType(ReflectionMock.class.getMethod("invalidReturnType2"), Collection.class, Object.class)
        );

        assertTrue(exception.getMessage().toLowerCase().contains("return type of method"));
    }

    public static class ReflectionMock {
        public String fieldWithoutGetter;

        public ReflectionMock(Object object) {

        }

        public static void wrongArguments(Object argument) {

        }

        public static void invalidException() throws IOException {
            throw new IOException();
        }

        public static void invalidReturnType() {

        }

        public static Collection<String> invalidReturnType2() {
            return null;
        }
    }
}
