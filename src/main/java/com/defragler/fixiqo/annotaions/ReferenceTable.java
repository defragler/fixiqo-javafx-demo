package com.defragler.fixiqo.annotaions;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ReferenceTable {
    Class<? extends Enum<?>> values();
}
