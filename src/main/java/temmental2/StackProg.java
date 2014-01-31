package temmental2;

import java.util.Map;

public class StackProg extends Element {

    public StackProg(Cursor cursor) {
        super(cursor);
    }

    @Override
    String getIdentifier() {
        throw new RuntimeException("Should not be called!");
    }

    @Override
    Object writeObject(Map<String, Object> functions, Map<String, Object> model, TemplateMessages messages) throws TemplateException {
        return "zzz StackProg yyy";
    }

    @Override
    public String getIdentifierForErrorMessage() {
        return getIdentifier();
    }

    @Override
    public String repr(int d, boolean displayPosition) {
        return (displayPosition ? "@" + cursor.getPosition() + pref(d) : "") + "StackProg";
    }
}
