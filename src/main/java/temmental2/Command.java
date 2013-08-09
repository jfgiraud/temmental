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

    @Override
    public String toString() {
        //TODO
        return "@Command===>TODO" + keyword;
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
            return oc.keyword.equals(keyword) && ((oc.element != null && oc.element.equals(element)) || (oc.element == element)) && oc.betweenTags.equals(betweenTags) && (oc.opening == opening);
        }
        return false;
    }

}
