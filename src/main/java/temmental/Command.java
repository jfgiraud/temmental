package temmental;

import java.io.IOException;
import java.io.Writer;
import java.util.*;

import static temmental.StringUtils.viewWhiteSpaces;

public class Command extends Element {

    private Keyword keyword;
    private Element element;
    private List<Object> betweenTags;
    private boolean opening;
    private Element varname;

    public Command(Keyword keyword, Cursor cursor, Element element) throws TemplateException {
        super(cursor);
        if (!Arrays.asList("for", "true", "false", "set").contains(keyword.getKeyword())) {
            throw new TemplateException("Invalid command name '%s' at position '%s'", keyword.getKeyword(), keyword.getCursor().getPosition());
        }
        this.keyword = keyword;
        this.element = element;
        this.betweenTags = new ArrayList<Object>();
        this.opening = (element != null);
        this.varname = null;
    }

    public Command(Keyword keyword, Cursor cursor) throws TemplateException {
        this(keyword, cursor, null);
    }

    @Override
    public String repr(int d, boolean displayPosition) {
        String buffer = "@" + keyword.getCursor().getPosition() + pref(d) + "Command(" + keyword.getKeyword() + ")\n";
        buffer += "   " + element.repr(d + 1, true);
        buffer += "\n";
        for (int i = 0; i < betweenTags.size(); i++) {
            Object obj = betweenTags.get(i);
            if (obj instanceof Element) {
                buffer += "   " + ((Element) obj).repr(d + 2, true);
            } else {
                buffer += "   " + viewWhiteSpaces(String.valueOf(obj));
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
        } else if (keyword.getKeyword().equals("true")) {
            writeObjectIf(out, functions, model, messages, false);
        } else if (keyword.getKeyword().equals("false")) {
            writeObjectIf(out, functions, model, messages, true);
        } else if (keyword.getKeyword().equals("set")) {
            writeObjectSet(out, functions, model, messages);
        } else {
            throw new TemplateException("writeObject not implemented for command '%s'", keyword.getKeyword());
        }
    }

    private void writeObjectIf(Writer out, Map<String, Object> functions, Map<String, Object> model, TemplateMessages messages, boolean invert) throws TemplateException, IOException {
        Object result = element.writeObject(functions, model, messages);
        if (!(result instanceof Boolean)) {
            throw new TemplateException("Command '%s' requires a boolean input at position '%s'", invert ? "false" : "true", keyword.getCursor().getPosition());
        }
        boolean b = (Boolean) result;
        if (invert) {
            b = !b;
        }
        if (b) {
            writeObjectBetweenTags(out, functions, messages, new HashMap<String, Object>(model));
        }
    }

    private void writeObjectSet(Writer out, Map<String, Object> functions, Map<String, Object> model, TemplateMessages messages) throws TemplateException, IOException {
        Object value = element.writeObject(functions, model, messages);
        String variable = (String) getVarname().writeObject(functions, model, messages);

        Map<String, Object> m = new HashMap<String, Object>(model);
        m.put(variable, value);

        writeObjectBetweenTags(out, functions, messages, m);
    }

    private void writeObjectFor(Writer out, Map<String, Object> functions, Map<String, Object> model, TemplateMessages messages) throws TemplateException, IOException {
        Object result = element.writeObject(functions, model, messages);
        if (!(result instanceof Iterable)) {
            throw new TemplateException("Command 'for' requires an iterable input at position '%s'", keyword.getCursor().getPosition());
        }
        for (Object c : ((Iterable) result)) {
            if (!(c instanceof Map)) {
                if (getVarname() != null) {
                    String variable = (String) getVarname().writeObject(functions, model, messages);
                    Map<String, Object> m = new HashMap<String, Object>(model);
                    m.put(variable, c);
                    writeObjectBetweenTags(out, functions, messages, m);
                } else {
                    throw new TemplateException("Command 'for' requires an iterable input of Map at position '%s'", keyword.getCursor().getPosition());
                }
            } else {
                Map<String, Object> m = new HashMap<String, Object>(model);
                m.putAll((Map) c);
                writeObjectBetweenTags(out, functions, messages, m);
            }
        }
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

    public void setVarname(Element varname) {
        this.varname = varname;
    }

    public Element getVarname() {
        return varname;
    }

    public boolean allowParameters(int number) {
        if ("for".equals(keyword.getKeyword()) && (number == 0 || number == 1)) {
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
        if ("set".equals(keyword.getKeyword()) && varname == null) {
            throw new TemplateException("Invalid syntax at position '%s'. " +
                    "Command 'set' expectes one parameter.",
                    cursor.getPosition(),
                    getKeyword().getKeyword());
        }
    }
}
