package com.my.utils.world;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface Config {
    boolean isPrimitive() default false;
}
