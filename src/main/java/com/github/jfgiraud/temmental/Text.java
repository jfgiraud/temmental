package com.github.jfgiraud.temmental;

import java.util.Map;

import static com.github.jfgiraud.temmental.StringUtils.viewWhiteSpaces;

class Text extends Element {

    private String expr;

    Text(String expr, Cursor cursor) {
        super(cursor);
        this.expr = expr;
    }

    public static String cleanSpaces(String p, boolean left, boolean right) {
        if (left) {
            p = p.replaceFirst("[ \t]+$", "");
        }
        if (right) {
            p = p.replaceFirst("^[ \t]+", "");
            p = p.replaceFirst("^\n", "");
        }
        return p;
    }

    public void cleanSpaces(boolean left, boolean right) {
        expr = cleanSpaces(expr, left, right);
    }

    public boolean equals(Object o) {
        if (o == null || !(o instanceof Text))
            return false;
        Text oc = (Text) o;
        return oc.expr.equals(expr) && oc.cursor.equals(cursor);
    }

    @Override
    String getIdentifier() {
        throw new RuntimeException("Text have no identifier!");
    }

    @Override
    Object writeObject(Map<String, Object> functions, Map<String, Object> model, TemplateMessages messages) throws TemplateException {
        return expr;
    }

    @Override
    public String getIdentifierForErrorMessage() {
        return getIdentifier();
    }

    @Override
    public String repr(int d, boolean displayPosition) {
        /*d=0;
        displayPosition=true;*/
        return (displayPosition ? "@" + cursor.getPosition() + pref(d) : "") + "Text(" + viewWhiteSpaces(expr) + ")";
    }

}
