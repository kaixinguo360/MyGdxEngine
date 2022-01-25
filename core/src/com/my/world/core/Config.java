package com.my.world.core;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface Config {

    String name() default "";

    Type type() default Type.Serializable;

    Class<?> elementType() default Object.class;

    String[] fields() default {};

    enum Type {
        Primitive, Asset, Entity, Serializable
    }
}
