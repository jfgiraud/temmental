package com.github.jfgiraud.temmental;

import java.lang.reflect.Method;
import java.util.Collection;

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

    static final ParamTransform<Object[], Object, Boolean> NOT_EQUALS = new ParamTransform<Object[], Object, Boolean>() {
        public Boolean apply(Object[] values, Object value) {
            return value != null ? !value.equals(values[0]) : (value != values[0]);
        }
    };

    static final ParamTransform<Comparable[], Comparable, Boolean> LESS_THAN = new ParamTransform<Comparable[], Comparable, Boolean>() {
        public Boolean apply(Comparable[] values, Comparable value) {
            return value.compareTo(values[0]) < 0;
        }
    };

    static final ParamTransform<Comparable[], Comparable, Boolean> LESS_EQUALS = new ParamTransform<Comparable[], Comparable, Boolean>() {
        public Boolean apply(Comparable[] values, Comparable value) {
            return value.compareTo(values[0]) <= 0;
        }
    };

    static final ParamTransform<Comparable[], Comparable, Boolean> GREATER_THAN = new ParamTransform<Comparable[], Comparable, Boolean>() {
        public Boolean apply(Comparable[] values, Comparable value) {
            return value.compareTo(values[0]) > 0;
        }
    };

    static final ParamTransform<Comparable[], Comparable, Boolean> GREATER_EQUALS = new ParamTransform<Comparable[], Comparable, Boolean>() {
        public Boolean apply(Comparable[] values, Comparable value) {
            return value.compareTo(values[0]) >= 0;
        }
    };

    static final Transform<Boolean, Boolean> NOT = new Transform<Boolean, Boolean>() {
        public Boolean apply(Boolean value) {
            return !value;
        }
    };

    static final ParamTransform<Boolean[], Boolean, Boolean> AND = new ParamTransform<Boolean[], Boolean, Boolean>() {
        public Boolean apply(Boolean[] values, Boolean value) {
            if (!value) return false;
            for (Boolean b : values) {
                if (!b) return false;
            }
            return true;
        }
    };

    static final ParamTransform<Boolean[], Boolean, Boolean> OR = new ParamTransform<Boolean[], Boolean, Boolean>() {
        public Boolean apply(Boolean[] values, Boolean value) {
            if (value) return true;
            for (Boolean b : values) {
                if (b) return true;
            }
            return false;
        }
    };


    static final Transform<Collection, Boolean> EMPTY = new Transform<Collection, Boolean>() {
        public Boolean apply(Collection value) {
            return value.isEmpty();
        }
    };

    static final Transform<Collection, Integer> SIZE = new Transform<Collection, Integer>() {
        public Integer apply(Collection value) {
            return value.size();
        }
    };

    static final Transform<Collection<Boolean>, Boolean> ALL = new Transform<Collection<Boolean>, Boolean>() {
        public Boolean apply(Collection<Boolean> values) {
            for (Boolean b : values) {
                if (!b) return false;
            }
            return true;
        }
    };

    static final Transform<Collection<Boolean>, Boolean> ANY = new Transform<Collection<Boolean>, Boolean>() {
        public Boolean apply(Collection<Boolean> values) {
            for (Boolean b : values) {
                if (b) return true;
            }
            return false;
        }
    };

    static final Transform<Collection<Boolean>, Boolean> NONE = new Transform<Collection<Boolean>, Boolean>() {
        public Boolean apply(Collection<Boolean> values) {
            for (Boolean b : values) {
                if (b) return false;
            }
            return true;
        }
    };

    static final Method UPPER = getDeclaredMethod(String.class, "toUpperCase", null);

    static final Method LOWER = getDeclaredMethod(String.class, "toLowerCase", null);
}
