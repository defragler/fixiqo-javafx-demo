package com.defragler.fixiqo.annotaions;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Column {
    String name();
    int length() default -1;
    boolean unique() default false;
    boolean nullable() default true;
    String defaultValue() default "";
}
