package temmental2;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Command extends Element {

    private Keyword keyword;
    private Element element;
    private List<Object> betweenTags;
    private boolean opening;

    public Command(Keyword keyword, Cursor cursor, Element element) {
        super(cursor);
        this.keyword = keyword;
        this.element = element;
        this.betweenTags = new ArrayList<Object>();
        this.opening = (element != null);
    }

    public Command(Keyword keyword, Cursor cursor) {
        this(keyword, cursor, null);
    }

    public String repr(int d, boolean displayPosition) {
        String buffer = "@" + keyword.getCursor().getPosition() + pref(d) + "Command(" + keyword.getKeyword() + ")\n";
        if (element instanceof Element) {
            buffer += "   " + ((Element) element).repr(d + 1, true);
        } else {
            buffer += "   " + repr(String.valueOf(element));
        }
        buffer += "\n";
        for (int i=0; i<betweenTags.size(); i++) {
            Object obj = betweenTags.get(i);
            if (obj instanceof Element) {
                buffer += "   " + ((Element) obj).repr(d + 2, true);
            } else {
                buffer += "   " + repr(String.valueOf(obj));
            }
            buffer += ((i < betweenTags.size()-1) ? "\n" : "");
        }
        return buffer;
    }

    @Override
    String getIdentifier() {
        return keyword.toString();
    }

    @Override
    Object writeObject(Map<String, Object> functions, Map<String, Object> model, TemplateMessages messages) throws TemplateException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
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
