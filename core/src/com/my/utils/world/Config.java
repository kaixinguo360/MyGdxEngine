package com.my.utils.world;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface Config {
    String name() default "";
    boolean isPrimitive() default false;
}
