package com.github.jfgiraud.temmental;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

abstract class Element {

    protected Cursor cursor;

    Element(Cursor cursor) {
        this.cursor = cursor.clone();
    }

    abstract String getIdentifier();

    abstract Object writeObject(Map<String, Object> functions, Map<String, Object> model, TemplateMessages messages) throws TemplateException;

    Object writeFinalObject(Map<String, Object> functions, Map<String, Object> model, TemplateMessages messages) throws TemplateException {
        Object result = writeObject(functions, model, messages);
        if (result == null) {
            throw new TemplateException("Final returned value is null at position '%s'", cursor.getPosition());
        }
        return result;
    }

     /*
        if (defaultValue instanceof Identifier) {
            return ((Element) defaultValue).getInModel(map);
        } else if (defaultValue instanceof Char) {
            return ((Char) defaultValue).writeObject(null, map, null);
        } else if (defaultValue instanceof Text) {
            return ((Text) defaultValue).writeObject(null, map, null);
        } else if (defaultValue instanceof Element) {
            throw new TemplateException("Invalid default value at position '%s'. Only identifiers and java type are allowed!", cursor.getPosition());
        }
        return defaultValue;
    }  */

    Object getInModel(Map<String, Object> map, String prefix) throws TemplateException {
        String varName = getIdentifier();
        varName = varName.substring(prefix.length());
        boolean optional = !isRequired();
        if (optional) {
            if (varName.endsWith("?")) {
                varName = varName.substring(0, varName.length() - 1);
            }
            if (map.containsKey(varName) && map.get(varName) != null) {
                return isIndirection(prefix) ? getIndirectionValue(map, map.get(varName), varName, true) : map.get(varName);
            } else {
                throw new TemplateIgnoreRenderingException("Ignore rendering because key '%s' is not present or has null value in the model map at position '%s'.", varName, cursor.getPosition());
            }
        } else {
            if (!map.containsKey(varName) || map.get(varName) == null) {
                throw new TemplateException("Key '%s' is not present or has null value in the model map at position '%s'.", varName, cursor.getPosition());
            } else {
                return isIndirection(prefix) ? getIndirectionValue(map, map.get(varName), varName, false) : map.get(varName);
            }
        }
    }

    private Object getIndirectionValue(Map<String, Object> map, Object o, String varName, boolean optional) {
        if (! (o instanceof String)) {
            throw new TemplateException("Value for '$%s' is not a String", varName, cursor);
        }
        if (!map.containsKey(o) || map.get(o) == null) {
            if (!optional) {
                throw new TemplateException("Key '%s' is not present or has null value in the model map at position '%s'.", o, cursor.getPosition());
            } else {
                throw new TemplateIgnoreRenderingException("Ignore rendering because key '%s' is not present or has null value in the model map at position '%s'.", o, cursor.getPosition());
            }
        }
        return map.get(o);
    }

    private boolean isIndirection(String prefix) {
        return "$$".equals(prefix);
    }

    boolean isRequired() {
        String identifier = getIdentifier();
        return identifier != null && (identifier.startsWith("'") || !identifier.endsWith("?"));
    }

    public abstract String getIdentifierForErrorMessage();

    List<Object> create_parameters_after_process(List<Object> parameters, Map<String, Object> functions, Map<String, Object> model, TemplateMessages messages) throws TemplateException {
        List<Object> args = new ArrayList<Object>();
        for (int i = 0; i < parameters.size(); i++) {
            Object parameter = parameters.get(i);
            Object afterProcess;
            /*if (parameter == null) {
        		throw new TemplateException("Unable to apply function: parameter #%d is null", i);     //FIXME
        	} */
            if (parameter instanceof Element) {
                afterProcess = ((Element) parameter).writeObject(functions, model, messages);
            } else {
                afterProcess = parameter;
            }
            if (afterProcess == null) {
                if (this instanceof Array) {
                    throw new TemplateException("Unable to render array at position '%s'. Required parameter #%d is null.",
                            cursor.getPosition(), i + 1);
                } else {
                    throw new TemplateException("Unable to render '%s' at position '%s'. Required parameter #%d is null.",
                            getIdentifierForErrorMessage(), cursor.getPosition(), i + 1);
                }
            }
            if (parameter != null && parameter instanceof Identifier && ((Identifier) parameter).getIdentifier().startsWith("@$")) {
                if (afterProcess instanceof List) {
                    args.addAll((List) afterProcess);
                } else {
                    throw new TemplateException("Unable to render '%s' at position '%s'. Parameter %s does not implement List interface.",
                            getIdentifierForErrorMessage(), cursor.getPosition(), getIdentifier());
                }
            } else {
                args.add(afterProcess);
            }
        }
        return args;
    }

    protected final String pref(int d) {
        String p = "\t";
        for (int i = 0; i < d; i++) {
            p += "\t";
        }
        return p;
    }

    public final String toString() {
        return repr(0, true);
    }

    public abstract String repr(int d, boolean displayPosition);

    public boolean equals(Object o) {
        if (o == null)
            return false;
        if (o instanceof Element) {
            Element oc = (Element) o;
            return oc.cursor.equals(cursor);
        }
        return false;
    }
}
