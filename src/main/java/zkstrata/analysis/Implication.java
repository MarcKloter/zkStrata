package zkstrata.analysis;

import zkstrata.domain.gadgets.Gadget;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as implication rule.
 *
 * An implication rule takes instances of {@link Gadget} as defined in the property {@code assumption} as arguments and
 * returns an {@link java.util.Optional} of {@link Gadget} which can be implied from the {@code assumption}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Implication {
}