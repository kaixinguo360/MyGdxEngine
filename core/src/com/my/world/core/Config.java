package com.my.world.core;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface Config {

    String name() default "";

    Type type() default Type.Loadable;

    Class<?> elementType() default Object.class;

    enum Type {
        Primitive, Asset, Entity, Loadable
    }
}
