package com.github.jfgiraud.temmental;

import java.lang.reflect.Method;

import static com.github.jfgiraud.temmental.TemplateUtils.getDeclaredMethod;

// TODO add documentation
public class Transforms {

    static final ParamTransform<Object[], Boolean, Object> IF = new ParamTransform<Object[], Boolean, Object>() {
        public Object apply(Object[] values, Boolean value) {
            if (value)
                return values[0];
            if (values.length == 2)
                return values[1];
            throw new TemplateIgnoreRenderingException("Invalid number of parameters!");
        }
    };

    static final ParamTransform<Object[], Boolean, Object> IF_NOT = new ParamTransform<Object[], Boolean, Object>() {
        public Object apply(Object[] values, Boolean value) {
            if (!value)
                return values[0];
            if (values.length == 2)
                return values[1];
            throw new TemplateIgnoreRenderingException("Invalid number of parameters!");
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

    static final Method UPPER = getDeclaredMethod(String.class, "toUpperCase", null);

    static final Method LOWER = getDeclaredMethod(String.class, "toLowerCase", null);
}
