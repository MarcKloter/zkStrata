package domain;

import org.junit.jupiter.api.Test;
import zkstrata.domain.data.Selector;
import zkstrata.domain.data.types.Literal;
import zkstrata.domain.data.types.Reference;
import zkstrata.domain.data.types.wrapper.InstanceVariable;
import zkstrata.domain.data.types.wrapper.Null;
import zkstrata.domain.data.types.wrapper.WitnessVariable;
import zkstrata.exceptions.InternalCompilerException;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;
import static zkstrata.utils.TestHelper.getAbsPosition;

public class DataTypesTest {
    @Test
    void Selector_Equals_1() {
        Selector selector = new Selector("selector");
        assertFalse(selector.equals(null));
    }

    @Test
    void Selector_Equals_2() {
        Selector selector = new Selector("selector");
        assertFalse(selector.equals(new Object()));
    }

    @Test
    void Selector_Equals_3() {
        Selector selector1 = new Selector("selector1");
        Selector selector2 = new Selector("selector2");
        assertNotEquals(selector1, selector2);
    }

    @Test
    void Selector_Equals_4() {
        Selector selector1 = new Selector("selector");
        Selector selector2 = new Selector("selector");
        assertEquals(selector1, selector2);
    }

    @Test
    void Reference_ToHex_Should_Throw() {
        Reference reference = new Reference(Object.class, "subject", new Selector("selector"));
        assertThrows(InternalCompilerException.class, () -> reference.toHex());
    }

    @Test
    void Reference_Equals_1() {
        Reference reference = new Reference(Object.class, "subject", new Selector("selector"));
        assertFalse(reference.equals(null));
    }

    @Test
    void Reference_Equals_2() {
        Reference reference = new Reference(Object.class, "subject", new Selector("selector"));
        assertFalse(reference.equals(new Object()));
    }

    @Test
    void Reference_Equals_3() {
        Reference reference1 = new Reference(String.class, "subject", new Selector("selector"));
        Reference reference2 = new Reference(BigInteger.class, "subject", new Selector("selector"));
        assertNotEquals(reference1, reference2);
    }

    @Test
    void Reference_Equals_4() {
        Reference reference1 = new Reference(Object.class, "subject1", new Selector("selector"));
        Reference reference2 = new Reference(Object.class, "subject2", new Selector("selector"));
        assertNotEquals(reference1, reference2);
    }

    @Test
    void Reference_Equals_5() {
        Reference reference1 = new Reference(Object.class, "subject", new Selector("selector1"));
        Reference reference2 = new Reference(Object.class, "subject", new Selector("selector2"));
        assertNotEquals(reference1, reference2);
    }

    @Test
    void Reference_Equals_6() {
        Reference reference1 = new Reference(Object.class, "subject", new Selector("selector"));
        Reference reference2 = new Reference(Object.class, "subject", new Selector("selector"));
        assertEquals(reference1, reference2);
    }

    @Test
    void WitnessVariable_Equals_1() {
        Reference reference = new Reference(Object.class, "subject", new Selector("selector"));
        WitnessVariable witnessVariable = new WitnessVariable(reference, reference, getAbsPosition());
        assertFalse(witnessVariable.equals(null));
    }

    @Test
    void WitnessVariable_Equals_2() {
        Reference reference = new Reference(Object.class, "subject", new Selector("selector"));
        WitnessVariable witnessVariable = new WitnessVariable(reference, reference, getAbsPosition());
        assertFalse(witnessVariable.equals(new Object()));
    }

    @Test
    void WitnessVariable_Equals_3() {
        Reference reference = new Reference(Object.class, "subject", new Selector("selector"));
        WitnessVariable witnessVariable1 = new WitnessVariable(reference, reference, getAbsPosition());
        WitnessVariable witnessVariable2 = new WitnessVariable(reference, reference, getAbsPosition());
        assertEquals(witnessVariable1, witnessVariable2);
    }

    @Test
    void Literal_Equals_1() {
        Literal literal = new Literal("string");
        assertFalse(literal.equals(null));
    }

    @Test
    void Literal_Equals_2() {
        Literal literal = new Literal(new Object());
        assertFalse(literal.equals(new Object()));
    }

    @Test
    void Literal_Equals_3() {
        Literal literal1 = new Literal("string");
        Literal literal2 = new Literal("string");
        assertEquals(literal1, literal2);
    }

    @Test
    void InstanceVariable_Equals_1() {
        Literal literal = new Literal("string");
        InstanceVariable instanceVariable = new InstanceVariable(literal, null, getAbsPosition());
        assertFalse(instanceVariable.equals(null));
    }

    @Test
    void InstanceVariable_Equals_2() {
        Literal literal = new Literal(new Object());
        InstanceVariable instanceVariable = new InstanceVariable(literal, null, getAbsPosition());
        assertFalse(instanceVariable.equals(new Object()));
    }

    @Test
    void InstanceVariable_Equals_3() {
        Literal literal = new Literal(new Object());
        InstanceVariable instanceVariable1 = new InstanceVariable(literal, null, getAbsPosition());
        InstanceVariable instanceVariable2 = new InstanceVariable(literal, null, getAbsPosition());
        assertEquals(instanceVariable1, instanceVariable2);
    }

    @Test
    void Null_GetType() {
        Null nullVariable = new Null();
        assertEquals(Null.class, nullVariable.getType());
    }

    @Test
    void Null_GetValue() {
        Null nullVariable = new Null();
        assertNull(nullVariable.getValue());
    }

    @Test
    void Null_GetReference() {
        Null nullVariable = new Null();
        assertNull(nullVariable.getReference());
    }

    @Test
    void Null_GetPosition() {
        Null nullVariable = new Null();
        assertNull(nullVariable.getPosition());
    }
}
