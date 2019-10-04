package zkstrata.domain.gadgets;

import zkstrata.parser.ast.predicates.Predicate;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface AstElement {
    Class<? extends Predicate> value();
}

