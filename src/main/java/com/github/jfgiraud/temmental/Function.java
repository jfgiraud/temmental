package com.github.jfgiraud.temmental;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;

class Function extends Element {

    protected Object input;
    protected Identifier functionIdentifier;

    Function(Identifier func, Object input) {
        super(func.cursor);
        this.input = input;
        this.functionIdentifier = func;
    }

    @Override
    String getIdentifier() {
        return functionIdentifier.getIdentifier();
    }

    @Override
    public String getIdentifierForErrorMessage() {
        return getIdentifier();
    }

    @Override
    public String repr(int d, boolean displayPosition) {
        return (displayPosition ? "@" + cursor.getPosition() + pref(d) : "") + "Function(" + functionIdentifier + "," + input + ")";
    }

    @Override
    Object writeObject(Map<String, Object> functions, Map<String, Object> model, TemplateMessages messages) throws TemplateException {
        String o = (String) functionIdentifier.writeObject(functions, model, messages);

        Object fp = functions.get(o);

        if (fp == null && functionIdentifier.isRequired()) {
            throw new TemplateException("No transform function named '%s' is associated with the template for rendering '\u2026:%s' at position '%s'.", o, functionIdentifier.getIdentifier(), functionIdentifier.cursor.getPosition());
        } else if (fp == null && functionIdentifier.getIdentifier().endsWith("?")) {
            throw new TemplateIgnoreRenderingException("Ignore rendering because key '%s' is not present or has null value in the model map at position '%s'.", o, functionIdentifier.cursor.getPosition());
        } else if (fp == null && !functionIdentifier.isRequired()) {
            // fp = IDT;
        }

        Object arg = ((input instanceof Element)
                ? ((Element) input).writeObject(functions, model, messages)
                : input);

        if (arg == null) {
            return null;
        }

        if (fp instanceof Method) {
            Method method = ((Method) fp);
            return callMethod(method, o, arg, null);
        } else if (fp instanceof Transform) {
            Method method = getApplyMethod((Transform) fp);
            return callMethod(method, o, fp, arg);
        } else {
            throw new TemplateException("Invalid transform function type '%s'.", fp.getClass().getCanonicalName());
        }

    }

    protected Object callMethod(Method method, String o, Object obj, Object params)
            throws TemplateException {


        if (params != null && !method.getParameterTypes()[0].isAssignableFrom(params.getClass())) {
            throw new TemplateException("Unable to render '\u2026:%s' at position '%s'. The function %s expects %s. It receives %s.",
                    getIdentifier(),
                    cursor.getPosition(),
                    o,
                    method.getParameterTypes()[0].getCanonicalName(),
                    params.getClass().getCanonicalName());
        }

        Exception occurred;
        try {
            if (!Modifier.isStatic(method.getModifiers())) {
                if (params == null)
                    return method.invoke(obj);
                else
                    return method.invoke(obj, params);
            } else {
                return method.invoke(null, obj);
            }
        } catch (IllegalAccessException e) {
            occurred = e;
        } catch (IllegalArgumentException e) {
            occurred = e;
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof TemplateIgnoreRenderingException)
                throw (TemplateIgnoreRenderingException) e.getCause();
            throw new TemplateException(e, "Unable to render '\u2026:%s' at position '%s'. The function %s throws an exception!",
                    getIdentifier(),
                    cursor.getPosition(),
                    o);
        }

        if (!method.getDeclaringClass().isAssignableFrom(obj.getClass())) {
            throw new TemplateException(occurred, "Unable to render '\u2026:%s' at position '%s'. The function %s expects %s. It receives %s.",
                    getIdentifier(),
                    cursor.getPosition(),
                    o,
                    method.getDeclaringClass().getCanonicalName(),
                    obj.getClass().getCanonicalName());
        } else {
            int length = method.getParameterTypes().length;
            if (length != 0) {
                throw new TemplateException(occurred, "Unable to render '\u2026:%s' at position '%s'. The function %s expects %s parameter%s but is called without parameter!",
                        getIdentifier(),
                        cursor.getPosition(),
                        o,
                        (length == 1 ? "one" : Integer.toString(length)),
                        (length > 1 ? "s" : ""));
            } else {
                throw new TemplateException(occurred, "Unable to determine reason.");
            }
        }
    }

    protected Method getApplyMethod(Transform t) {
        Method[] methods = t.getClass().getMethods();
        for (Method method : methods) {
            if (method.getName().equals("apply") && method.getParameterTypes().length == 1 && !method.isSynthetic()) {
                method.setAccessible(true);
                return method;
            }
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        if (o instanceof Function) {
            Function oc = (Function) o;
            return oc.input.equals(input) && oc.functionIdentifier.equals(functionIdentifier);
        }
        return false;
    }

}
