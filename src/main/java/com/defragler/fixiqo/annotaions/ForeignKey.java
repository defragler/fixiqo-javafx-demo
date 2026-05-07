package com.defragler.fixiqo.annotaions;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ForeignKey {
    Class<?> target();
    String column() default "id";
    boolean onDeleteCascade() default false;
}
