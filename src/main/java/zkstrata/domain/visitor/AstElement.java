package zkstrata.domain.visitor;

import zkstrata.parser.ast.Clause;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface AstElement {
    Class<? extends Clause> value();
}

