package zkstrata.optimizer;

import zkstrata.domain.gadgets.Gadget;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Substitution {
    Class<? extends Gadget>[] target();
    Class<? extends Gadget>[] context() default {};
}
