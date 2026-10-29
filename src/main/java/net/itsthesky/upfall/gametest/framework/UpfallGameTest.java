package net.itsthesky.upfall.gametest.framework;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface UpfallGameTest {
    String template();

    int timeoutTicks() default 100;

    String batch() default "defaultBatch";

    int setupTicks() default 0;

    boolean required() default true;

    boolean skyAccess() default false;
}
