package com.github.jfgiraud.temmental;

import java.lang.reflect.Method;

// TODO add documentation
public class Transforms {

    static final ParamTransform<Object[], Boolean, Object> IF = new ParamTransform<Object[], Boolean, Object>() {
        public Object apply(Object[] values, Boolean value) {
            return value ? values[0] : values[1];
        }
    };

    static final ParamTransform<Object[], Boolean, Object> IF_NOT = new ParamTransform<Object[], Boolean, Object>() {
        public Object apply(Object[] values, Boolean value) {
            return value ? values[1] : values[0];
        }
    };

    static final ParamTransform<Object[], Object, Boolean> EQUALS = new ParamTransform<Object[], Object, Boolean>() {
        public Boolean apply(Object[] values, Object value) {
            return value != null ? value.equals(values[0]) : (value == values[0]);
        }
    };

    static final Transform<Boolean,Boolean> NOT = new Transform<Boolean, Boolean>() {
        public Boolean apply(Boolean value) {
            return !value;
        }
    };

    static final Method UPPER = TemplateUtils.getDeclaredMethod(String.class, "toUpperCase", null);

    static final Method LOWER = TemplateUtils.getDeclaredMethod(String.class, "toLowerCase", null);
}
