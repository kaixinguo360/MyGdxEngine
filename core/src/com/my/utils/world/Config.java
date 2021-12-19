package com.my.utils.world;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface Config {
    String name() default "";
    Type type() default Type.Loadable;

    enum Type {
        Primitive, Asset, Loadable
    }
}
