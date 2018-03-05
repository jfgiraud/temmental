package com.github.jfgiraud.temmental;

import java.io.IOException;
import java.io.Writer;
import java.util.*;

public class Command extends Element {

    private Keyword keyword;
    private Object element;
    private List<Object> betweenTags;
    private boolean opening;
    private List initParameters;

    public Command(Keyword keyword, Cursor cursor, Object element) throws TemplateException {
        super(cursor);
        if (element != null && !Expression.isLeafToken(element) && !(element instanceof Element)) {
            throw new TemplateException("Parsing exception at position %s.", ((Token) element).getCursor().getPosition());
        }
        if (!Arrays.asList("for", "true", "false", "set", "enum", "override", "put").contains(keyword.getKeyword())) {
            throw new TemplateException("Invalid command name '%s' at position '%s'", keyword.getKeyword(), keyword.getCursor().getPosition());
        }
        this.keyword = keyword;
        this.element = element;
        this.betweenTags = new ArrayList<Object>();
        this.opening = (element != null);
        this.initParameters = null;
    }

    public Command(Keyword keyword, Cursor cursor) throws TemplateException {
        this(keyword, cursor, null);
    }

    @Override
    public String repr(int d, boolean displayPosition) {
        String buffer = "@" + keyword.getCursor().getPosition() + pref(d) + "Command(" + keyword.getKeyword() + ")\n";
        buffer += "   " + ((element instanceof Element) ? ((Element) element).repr(d + 1, true) : element);
        buffer += "\n";
        for (int i = 0; i < betweenTags.size(); i++) {
            Object obj = betweenTags.get(i);
            if (obj instanceof Element) {
                buffer += "   " + ((Element) obj).repr(d + 2, true);
            } else {
                buffer += "   " + StringUtils.viewWhiteSpaces(String.valueOf(obj));
            }
            buffer += ((i < betweenTags.size() - 1) ? "\n" : "");
        }
        return buffer;
    }


    @Override
    String getIdentifier() {
        return keyword.toString();
    }

    @Override
    public String getIdentifierForErrorMessage() {
        return getIdentifier();
    }

    void writeObject(Writer out, Map<String, Object> functions, Map<String, Object> model, TemplateMessages messages) throws TemplateException, IOException {
        if (keyword.getKeyword().equals("for")) {
            writeObjectFor(out, functions, model, messages);
        } else if (keyword.getKeyword().equals("enum")) {
            writeObjectEnum(out, functions, model, messages);
        } else if (keyword.getKeyword().equals("true")) {
            writeObjectIf(out, functions, model, messages, false);
        } else if (keyword.getKeyword().equals("false")) {
            writeObjectIf(out, functions, model, messages, true);
        } else if (keyword.getKeyword().equals("set")) {
            writeObjectSet(out, functions, model, messages);
        } else if (keyword.getKeyword().equals("override")) {
            writeObjectOverride(out, functions, model, messages);
        } else {
            throw new TemplateException("writeObject not implemented for command '%s'", keyword.getKeyword());
        }
    }

    private void writeObjectIf(Writer out, Map<String, Object> functions, Map<String, Object> model, TemplateMessages messages, boolean invert) throws TemplateException, IOException {
        Object result = writeElement(functions, model, messages);
        if (!(result instanceof Boolean)) {
            throw new TemplateException("Command '%s' requires a boolean input at position '%s'", invert ? "false" : "true", keyword.getCursor().getPosition());
        }
        boolean b = (Boolean) result;
        if (invert) {
            b = !b;
        }
        if (b) {
            writeObjectBetweenTags(out, functions, messages, cloneModel(model));
        }
    }

    private HashMap<String, Object> cloneModel(Map<String, Object> model) {
        return new HashMap<String, Object>(model);
    }

    private void writeObjectOverride(Writer out, Map<String, Object> functions, Map<String, Object> model, TemplateMessages messages) throws TemplateException, IOException {
        Object result = writeElement(functions, model, messages);

        if (!(result instanceof Map)) {
            throw new TemplateException("Command '%s' requires a map at position '%s'", keyword.getKeyword(), keyword.getCursor().getPosition());
        }

        Map<String, Object> m = cloneModel(model);
        m.putAll((Map) result);

        writeObjectBetweenTags(out, functions, messages, m);
    }

    private void writeObjectSet(Writer out, Map<String, Object> functions, Map<String, Object> model, TemplateMessages messages) throws TemplateException, IOException {
        Object value = writeElement(functions, model, messages);
        String variable = getInitParameterValue(functions, model, messages, 0);

        Map<String, Object> m = cloneModel(model);
        m.put(variable, value);

        writeObjectBetweenTags(out, functions, messages, m);
    }

    private String getInitParameterValue(Map<String, Object> functions, Map<String, Object> model, TemplateMessages messages, int index) {
        if (getInitParameter(index) instanceof Element) {
            return (String) ((Element) getInitParameter(index)).writeObject(functions, model, messages);
        } else {
            return getInitParameter(index).toString();
        }
    }

    private void writeObjectFor(Writer out, Map<String, Object> functions, Map<String, Object> model, TemplateMessages messages) throws TemplateException, IOException {
        Object result = writeElement(functions, model, messages);
        if (!(result instanceof Iterable)) {
            throw new TemplateException("Command 'for' requires an iterable input at position '%s'", keyword.getCursor().getPosition());
        }
        for (Object c : ((Iterable) result)) {
            if (!(c instanceof Map)) {
                if (getInitParameter(0) != null) {
                    String variable = getInitParameterValue(functions, model, messages, 0);
                    Map<String, Object> m = cloneModel(model);
                    m.put(variable, c);
                    writeObjectBetweenTags(out, functions, messages, m);
                } else {
                    throw new TemplateException("Command 'for' requires an iterable input of Map at position '%s'", keyword.getCursor().getPosition());
                }
            } else {
                Map<String, Object> m = cloneModel(model);
                m.putAll((Map) c);
                writeObjectBetweenTags(out, functions, messages, m);
            }
        }
    }

    private void writeObjectEnum(Writer out, Map<String, Object> functions, Map<String, Object> model, TemplateMessages messages) throws TemplateException, IOException {
        Object result = writeElement(functions, model, messages);
        if (!(result instanceof Iterable)) {
            throw new TemplateException("Command 'enum' requires an iterable input at position '%s'", keyword.getCursor().getPosition());
        }
        int i = 0;
        String variable = getInitParameterValue(functions, model, messages, 0);
        for (Object c : ((Iterable) result)) {
            Map<String, Object> m = cloneModel(model);
            m.put(variable, i);
            if (!(c instanceof Map)) {
                if (getInitParameter(1) != null) {
                    String variable2 = getInitParameterValue(functions, model, messages, 1);
                    m.put(variable2, c);
                    writeObjectBetweenTags(out, functions, messages, m);
                } else {
                    throw new TemplateException("Command 'enum' requires an iterable input of Map at position '%s'", keyword.getCursor().getPosition());
                }
            } else {
                m.putAll((Map) c);
                writeObjectBetweenTags(out, functions, messages, m);
            }
            i++;
        }
    }

    private Object writeElement(Map<String, Object> functions, Map<String, Object> model, TemplateMessages messages) {
        if (element instanceof Element)
            return ((Element) element).writeObject(functions, model, messages);
        else
            return element;
    }

    private void writeObjectBetweenTags(Writer out, Map<String, Object> functions, TemplateMessages messages, Map<String, Object> m) throws TemplateException, IOException {
        for (Object item : betweenTags) {
            try {
                if (item instanceof Command) {
                    ((Command) item).writeObject(out, functions, m, messages);
                } else if (item instanceof Element) {
                    Object o = ((Element) item).writeObject(functions, m, messages);
                    if (o != null) {
                        out.write(o.toString());
                    }
                } else {
                    out.write(String.valueOf(item));
                }
            } catch (TemplateIgnoreRenderingException e) {
                // pass
            }
        }
    }


    @Override
    Object writeObject(Map<String, Object> functions, Map<String, Object> model, TemplateMessages messages) throws TemplateException {
        /*StringWriter sw = new StringWriter();
        writeObject(sw, functions, model, messages);
        return sw.toString();*/
        throw new TemplateException("writeObject without out parameter should not be called for Command");
    }

    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        if (o instanceof Command) {
            Command oc = (Command) o;
            return oc.keyword.equals(keyword) && ((oc.element != null && oc.element.equals(element)) || (oc.element == element)) && oc.betweenTags.equals(betweenTags) && (oc.opening == opening);
        }
        return false;
    }

    public void readUntilClosing(Stack stack) throws TemplateException {
        if (stack.empty()) {
            throw new TemplateException("Reach end of stack. No closing tag for command '%s' at position '%s'.", keyword.getKeyword(), cursor.getPosition());
        }
        while (!stack.empty()) {
            Object line = stack.pop();
            if (line instanceof Command) {
                Command cmd = (Command) line;
                if (cmd.opening) {
                    cmd.readUntilClosing(stack);
                    betweenTags.add(cmd);
                } else {
                    if (!cmd.keyword.getKeyword().equals(keyword.getKeyword())) {
                        throw new TemplateException("Mismatch closing tag for command '%s' at position '%s' (reach '%s' at position '%s').", keyword.getKeyword(), cursor.getPosition(), cmd.keyword.getKeyword(), cmd.cursor.getPosition());
                    } else {
                        return;
                    }
                }
            } else {
                betweenTags.add(line);
            }
        }
    }

    public void setInitParameters(List initParameters) {
        this.initParameters = initParameters;
    }

    public Object getInitParameter(int index) {
        return initParameters == null ? null : initParameters.get(index);
    }

    public boolean allowParameters(int number) {
        if ("for".equals(keyword.getKeyword()) && (number == 0 || number == 1)) {
            return true;
        } else if ("enum".equals(keyword.getKeyword()) && (number == 1 || number == 2 || number == 3)) {
            return true;
        } else if ("set".equals(keyword.getKeyword()) && number == 1) {
            return true;
        }
        return false;
    }

    public Keyword getKeyword() {
        return keyword;
    }

    public void check() {
        if ("set".equals(keyword.getKeyword()) && (initParameters == null || initParameters.get(0) == null)) {
            throw new TemplateException("Invalid syntax at position '%s'. " +
                    "Command 'set' expects one parameter.",
                    cursor.getPosition(),
                    getKeyword().getKeyword());
        }
        if ("enum".equals(keyword.getKeyword()) && (initParameters == null || (initParameters.size() < 1 || initParameters.size() > 3))) {
            throw new TemplateException("Invalid syntax at position '%s'. " +
                    "Command 'enum' expects one, two or three parameters.",
                    cursor.getPosition(),
                    getKeyword().getKeyword());
        }
    }
}
