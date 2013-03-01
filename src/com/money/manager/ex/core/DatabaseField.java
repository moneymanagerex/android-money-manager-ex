package com.money.manager.ex.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.LOCAL_VARIABLE})
public @interface DatabaseField {
	String columnName();
}
