package temmental2;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Command extends Element {

    private String tag;
    private Element element;
    private List<Object> betweenTags;

    public Command(String tag, Cursor cursor, Element element, List<Object> betweenTags) {
        super(cursor);
        this.tag = tag;
        this.element = element;
        this.betweenTags = betweenTags;
    }

    public Command(String tag, Cursor cursor, Element element) {
        this(tag, cursor, element, new ArrayList<Object>());
    }

    @Override
    public String toString() {
        //TODO
        return "@Command===>TODO" + tag;
    }

    @Override
    String getIdentifier() {
        throw new RuntimeException("No identifier for command");
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
            return oc.tag.equals(tag) && oc.element.equals(element) && oc.betweenTags.equals(betweenTags);
        }
        return false;
    }
}
