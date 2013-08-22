package temmental2;

import java.io.IOException;
import java.io.Writer;
import java.util.*;

import static temmental2.StringUtils.viewWhiteSpaces;

public class Command extends Element {

    private Keyword keyword;
    private Element element;
    private List<Object> betweenTags;
    private boolean opening;

    public Command(Keyword keyword, Cursor cursor, Element element) throws TemplateException {
        super(cursor);
        if (! Arrays.asList("for").contains(keyword.getKeyword())) {
            throw new TemplateException("Invalid command name '%s' at position '%s'", keyword.getKeyword(), keyword.getCursor().getPosition());
        }
        this.keyword = keyword;
        this.element = element;
        this.betweenTags = new ArrayList<Object>();
        this.opening = (element != null);
    }

    public Command(Keyword keyword, Cursor cursor) throws TemplateException {
        this(keyword, cursor, null);
    }

    @Override
    public String repr(int d, boolean displayPosition) {
        String buffer = "@" + keyword.getCursor().getPosition() + pref(d) + "Command(" + keyword.getKeyword() + ")\n";
        if (element instanceof Element) {
            buffer += "   " + ((Element) element).repr(d + 1, true);
        } else {
            buffer += "   " + viewWhiteSpaces(String.valueOf(element));
        }
        buffer += "\n";
        for (int i=0; i<betweenTags.size(); i++) {
            Object obj = betweenTags.get(i);
            if (obj instanceof Element) {
                buffer += "   " + ((Element) obj).repr(d + 2, true);
            } else {
                buffer += "   " + viewWhiteSpaces(String.valueOf(obj));
            }
            buffer += ((i < betweenTags.size()-1) ? "\n" : "");
        }
        return buffer;
    }


    @Override
    String getIdentifier() {
        return keyword.toString();
    }

    void writeObject(Writer out, Map<String, Object> functions, Map<String, Object> model, TemplateMessages messages) throws TemplateException, IOException {
        if (keyword.getKeyword().equals("for")) {
            Object result = element.writeObject(functions, model, messages);
            if (! (result instanceof Iterable)) {
                throw new TemplateException("Command 'for' requires an iterable input at position '%s'", keyword.getCursor().getPosition());
            }
            Iterator it = ((Iterable) result).iterator();
            while (it.hasNext()) {
                Object c = it.next();
                if (! (c instanceof Map)) {
                    throw new TemplateException("Command 'for' requires an iterable input of Map at position '%s'", keyword.getCursor().getPosition());
                }
                Map m = new HashMap();
                m.putAll(model);
                m.putAll((Map) c);
                for (Object item : betweenTags) {
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
                }
            }
        } else {
            throw new TemplateException("writeObject not implemented for command '%s'", keyword.getKeyword());
        }
    }


    @Override
    Object writeObject(Map<String, Object> functions, Map<String, Object> model, TemplateMessages messages) throws TemplateException, IOException {
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
        while (! stack.empty()) {
            Object line = stack.pop();
            if (line instanceof Command) {
                Command cmd = (Command) line;
                if (cmd.opening) {
                    cmd.readUntilClosing(stack);
                    betweenTags.add(cmd);
                } else {
                    if (! cmd.keyword.getKeyword().equals(keyword.getKeyword())) {
                        throw new TemplateException("Mismatch closing tag for command '%s' at position '%s' (reach '%s' at position '%s').", keyword.getKeyword(), cursor.getPosition(), cmd.keyword.getKeyword(), cmd.cursor.getPosition());
                    } else {
                        return;
                    }
                }
            } else {
                betweenTags.add(line);
            }
        }
        return;
    }
}
