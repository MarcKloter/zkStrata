package zkstrata.domain.data.schemas.predefined;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Schema {
    String name();
}
