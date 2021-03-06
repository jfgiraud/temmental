package com.github.jfgiraud.temmental;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

class Functionp extends Function {

    private List<Object> initParameters;

    public Functionp(Function f, List<Object> initParameters) {
        super(f.functionIdentifier, f.input);
        this.initParameters = initParameters;
    }

    public Functionp(Identifier func, List<Object> initParameters, Object input) {
        super(func, input);
        this.initParameters = initParameters;
    }

    @Override
    Object writeObject(Map<String, Object> functions, Map<String, Object> model, TemplateMessages messages) throws TemplateException {
        String o = (String) functionIdentifier.writeObject(functions, model, messages);

        Object fp = functions.get(o);

        if (fp == null && functionIdentifier.isRequired()) {
            throw new TemplateException("No transform function named '%s' is associated with the template for rendering '\u2026:%s' at position '%s'.", o, functionIdentifier.getIdentifier(), functionIdentifier.cursor.getPosition());
        } else if (fp == null && functionIdentifier.getIdentifier().endsWith("?")) {
            throw new TemplateIgnoreRenderingException("Ignore rendering because key '%s' is not present or has null value in the model map at position '%s'.", o, functionIdentifier.cursor.getPosition());
        }

        Object arg = ((input instanceof Element)
                ? ((Element) input).writeObject(functions, model, messages)
                : input);

        if (arg == null) {
            return null;
        }

        List<Object> initParametersProcessed = super.create_parameters_after_process(initParameters, functions, model, messages);
        if (initParametersProcessed == null) {
            return null;
        }

        if (fp instanceof Transform) {
            Method method = getApplyMethod((Transform) fp);

            List<Object> wrap = new ArrayList<Object>();
            wrap.add(asArray(initParametersProcessed, null));

            Object zz = callMethod(method, o, fp, wrap);

            method = getApplyMethod((Transform) zz);
            return super.callMethod(method, o, zz, arg);
        } else if (fp instanceof Method) {
            Method method = ((Method) fp);

            return callMethod(method, o, arg, initParametersProcessed);
        } else {
            throw new TemplateException("Invalid transform function type '%s'.", fp.getClass().getCanonicalName());
        }

    }

    private Object callMethod(Method method, String o, Object arg,
                              List<Object> initParametersProcessed) throws TemplateException {
        if (!((Class<?>) method.getDeclaringClass()).isAssignableFrom(((Class<?>) arg.getClass()))) {
            throw new TemplateException("Unable to render '\u2026:%s' at position '%s'. The function %s expects %s. It receives %s.",
                    getIdentifier(),
                    cursor.getPosition(),
                    o,
                    method.getDeclaringClass().getCanonicalName(),
                    arg.getClass().getCanonicalName());
        } else {
            if (initParametersProcessed.size() != method.getParameterTypes().length) {
                throw new TemplateException("Unable to render '\u2026:%s' at position '%s'. The function %s expects %d init-parameter(s) but receives %d init-parameter(s).",
                        getIdentifier(),
                        cursor.getPosition(),
                        o,
                        method.getParameterTypes().length,
                        initParametersProcessed.size());
            }
        }

        Exception occurred;
        try {
            return method.invoke(arg, initParametersProcessed.toArray());
        } catch (IllegalAccessException e) {
            occurred = e;
        } catch (IllegalArgumentException e) {
            occurred = e;
        } catch (InvocationTargetException e) {
            occurred = e;
        }
        for (int i = 0; i < initParametersProcessed.size(); i++) {
            Object tmpVlue = initParametersProcessed.get(i);
            if (!isAssignable(method.getParameterTypes()[i], tmpVlue.getClass())) {
                throw new TemplateException("Unable to render '\u2026:%s' at position '%s'. The function %s expects %s for parameter #%d. It receives %s.",
                        getIdentifier(),
                        cursor.getPosition(),
                        o,
                        method.getParameterTypes()[i].getCanonicalName(),
                        i + 1,
                        tmpVlue.getClass().getCanonicalName());
            }
        }

        throw new TemplateException(occurred, "Unable to determine reason.");
    }

    static final Map<Class, Class> builtInMap;

    static {
        builtInMap = new HashMap<Class, Class>();
        builtInMap.put(int.class, Integer.class);
        builtInMap.put(long.class, Long.class);
        builtInMap.put(double.class, Double.class);
        builtInMap.put(float.class, Float.class);
        builtInMap.put(boolean.class, Boolean.class);
        builtInMap.put(char.class, Character.class);
        builtInMap.put(byte.class, Byte.class);
        builtInMap.put(short.class, Short.class);
    }

    protected static boolean isAssignable(Class a, Class b) {
        if (a.isPrimitive() ^ b.isPrimitive()) {
            Class primitive = (a.isPrimitive() ? a : b);
            Class boxType = (a.isPrimitive() ? b : a);
            Class primitiveBoxed = builtInMap.get(primitive);
            return isAssignable(primitiveBoxed, boxType);
        } else {
            return a.isAssignableFrom(b);
        }
    }

    private static Object asArray(Collection<Object> parameters, Class<?> typeIn) {
        if (typeIn == null) {
            typeIn = determineType(parameters);
        }
        Object args = Array.newInstance(typeIn, parameters.size());
        int i = 0;
        for (Iterator<Object> it = parameters.iterator(); it.hasNext(); i++) {
            Object parameter = it.next();
            Array.set(args, i, parameter);
        }
        return args;
    }

    private static Class<?> determineType(Collection<Object> parameters) {
        Class<?> typeIn;
        typeIn = Object.class;
        int i = 0;
        for (Object p : parameters) {
            Class<?> clazz = p.getClass();
            if (i == 0)
                typeIn = clazz;
            if (!clazz.equals(typeIn)) {
                typeIn = Object.class;
                break;
            }
        }
        return typeIn;
    }


    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        if (o instanceof Functionp) {
            Functionp oc = (Functionp) o;
            return oc.input.equals(input) && oc.functionIdentifier.equals(functionIdentifier) && oc.initParameters.equals(initParameters);
        }
        return false;
    }

    @Override
    public String repr(int d, boolean displayPosition) {
        return (displayPosition ? "@" + cursor.getPosition() + pref(d) : "") + "Functionp(" + functionIdentifier + "," + initParameters + "," + input + ")";
    }

    @Override
    public String getIdentifierForErrorMessage() {
        return getIdentifier() + "[\u2026]";
    }

}
