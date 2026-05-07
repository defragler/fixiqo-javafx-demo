package com.defragler.fixiqo.annotaions;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Index {
    String name() default "";
    String[] columns();
    boolean unique() default false;
}
