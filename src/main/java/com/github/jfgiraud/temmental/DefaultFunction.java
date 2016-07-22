package com.github.jfgiraud.temmental;

import java.util.Map;

public class DefaultFunction extends Identifier {

    private final Object input;
    private final Object defaultValue;

    public DefaultFunction(Element input, Object defaultValue) throws TemplateException {
        this(input, defaultValue, input.cursor);
    }

    public DefaultFunction(Element input, Object defaultValue, Cursor defaultValueCursor) throws TemplateException {
        super(input.getIdentifier(), input.cursor);
        if (!input.isRequired()) {
            throw new TemplateException("Non compatible options (?/!) at positions '%s' and '%s'.", input.cursor.getPosition(), defaultValueCursor.move1r().getPosition());
        }
        this.input = input;
        this.defaultValue = defaultValue;
    }

    @Override
    String getIdentifier() {
        return super.getIdentifier();
    }

    @Override
    Object writeObject(Map<String, Object> functions, Map<String, Object> model, TemplateMessages messages) throws TemplateException {
        try {
            return ((Element) input).writeFinalObject(functions, model, messages);
        } catch (TemplateIgnoreRenderingException e) {
            throw e;
        } catch (TemplateException e) {
            if (defaultValue == null && input instanceof Function) {
                Object o = ((Function) input).input;
                if (o instanceof Element) {
                    return ((Element) o).writeObject(functions, model, messages);
                } else {
                    return o;
                }
            } else if (defaultValue == null) {
                return "";
            } else if (defaultValue instanceof Element) {
                return ((Element) defaultValue).writeObject(functions, model, messages);
            } else {
                return defaultValue;
            }
        }
    }

    @Override
    public String getIdentifierForErrorMessage() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String repr(int d, boolean displayPosition) {
        return super.repr(d, displayPosition) + " ! " + defaultValue;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !super.equals(o)) {
            return false;
        }
        if (o instanceof DefaultFunction) {
            DefaultFunction oc = (DefaultFunction) o;
            return oc.defaultValue.equals(defaultValue);
        }
        return false;
    }
}
