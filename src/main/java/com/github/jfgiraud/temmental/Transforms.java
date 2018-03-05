package com.github.jfgiraud.temmental;

import java.lang.reflect.Method;
import java.math.BigInteger;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static com.github.jfgiraud.temmental.TemplateUtils.getDeclaredMethod;

// TODO add documentation
public class Transforms {

    public static final ParamTransform<Object[], Boolean, Object> IF = new ParamTransform<Object[], Boolean, Object>() {
        public Object apply(Object[] values, Boolean value) {
            if (value)
                return values[0];
            if (values.length == 2)
                return values[1];
            throw new TemplateIgnoreRenderingException("Invalid number of parameters!");
        }
    };

    public static final ParamTransform<Object[], Boolean, Object> IF_NOT = new ParamTransform<Object[], Boolean, Object>() {
        public Object apply(Object[] values, Boolean value) {
            if (!value)
                return values[0];
            if (values.length == 2)
                return values[1];
            throw new TemplateIgnoreRenderingException("Invalid number of parameters!");
        }
    };

    public static final ParamTransform<Object[], Object, Boolean> EQUALS = new ParamTransform<Object[], Object, Boolean>() {
        public Boolean apply(Object[] values, Object value) {
            return value != null ? value.equals(values[0]) : (value == values[0]);
        }
    };

    public static final ParamTransform<Object[], Object, Boolean> NOT_EQUALS = new ParamTransform<Object[], Object, Boolean>() {
        public Boolean apply(Object[] values, Object value) {
            return value != null ? !value.equals(values[0]) : (value != values[0]);
        }
    };

    public static final ParamTransform<Comparable[], Comparable, Boolean> LESS_THAN = new ParamTransform<Comparable[], Comparable, Boolean>() {
        public Boolean apply(Comparable[] values, Comparable value) {
            return value.compareTo(values[0]) < 0;
        }
    };

    public static final ParamTransform<Comparable[], Comparable, Boolean> LESS_EQUALS = new ParamTransform<Comparable[], Comparable, Boolean>() {
        public Boolean apply(Comparable[] values, Comparable value) {
            return value.compareTo(values[0]) <= 0;
        }
    };

    public static final ParamTransform<Comparable[], Comparable, Boolean> GREATER_THAN = new ParamTransform<Comparable[], Comparable, Boolean>() {
        public Boolean apply(Comparable[] values, Comparable value) {
            return value.compareTo(values[0]) > 0;
        }
    };

    public static final ParamTransform<Comparable[], Comparable, Boolean> GREATER_EQUALS = new ParamTransform<Comparable[], Comparable, Boolean>() {
        public Boolean apply(Comparable[] values, Comparable value) {
            return value.compareTo(values[0]) >= 0;
        }
    };

    public static final Transform<Boolean, Boolean> NOT = new Transform<Boolean, Boolean>() {
        public Boolean apply(Boolean value) {
            return !value;
        }
    };

    public static final ParamTransform<Boolean[], Boolean, Boolean> AND = new ParamTransform<Boolean[], Boolean, Boolean>() {
        public Boolean apply(Boolean[] values, Boolean value) {
            if (!value) return false;
            for (Boolean b : values) {
                if (!b) return false;
            }
            return true;
        }
    };

    public static final ParamTransform<Boolean[], Boolean, Boolean> OR = new ParamTransform<Boolean[], Boolean, Boolean>() {
        public Boolean apply(Boolean[] values, Boolean value) {
            if (value) return true;
            for (Boolean b : values) {
                if (b) return true;
            }
            return false;
        }
    };

    private static boolean isInt(Number number) {
        return number instanceof Long || number instanceof Integer ||
                number instanceof Short || number instanceof Byte ||
                number instanceof AtomicInteger || number instanceof AtomicLong ||
                (number instanceof BigInteger && ((BigInteger) number).bitLength() < 64);
    }

    public static final ParamTransform<Number[], Number, Number> ADD = new ParamTransform<Number[], Number, Number>() {
        @Override
        public Number apply(Number[] values, Number value) {
            if (isInt(value) && isInt(values[0])) {
                return value.longValue() + values[0].longValue();
            } else if (value instanceof Number && values[0] instanceof Number) {
                return value.doubleValue() + values[0].doubleValue();
            } else {
                throw new IllegalArgumentException("Not a Number!");
            }
        }
    };

    public static final Transform<Collection, Boolean> EMPTY = new Transform<Collection, Boolean>() {
        public Boolean apply(Collection value) {
            return value.isEmpty();
        }
    };

    public static final Transform<Collection, Integer> SIZE = new Transform<Collection, Integer>() {
        public Integer apply(Collection value) {
            return value.size();
        }
    };

    public static final Transform<Collection<Boolean>, Boolean> ALL = new Transform<Collection<Boolean>, Boolean>() {
        public Boolean apply(Collection<Boolean> values) {
            for (Boolean b : values) {
                if (!b) return false;
            }
            return true;
        }
    };

    public static final Transform<Collection<Boolean>, Boolean> ANY = new Transform<Collection<Boolean>, Boolean>() {
        public Boolean apply(Collection<Boolean> values) {
            for (Boolean b : values) {
                if (b) return true;
            }
            return false;
        }
    };

    public static final Transform<Collection<Boolean>, Boolean> NONE = new Transform<Collection<Boolean>, Boolean>() {
        public Boolean apply(Collection<Boolean> values) {
            for (Boolean b : values) {
                if (b) return false;
            }
            return true;
        }
    };

    public static final Method UPPER = getDeclaredMethod(String.class, "toUpperCase", null);

    public static final Method LOWER = getDeclaredMethod(String.class, "toLowerCase", null);
}
