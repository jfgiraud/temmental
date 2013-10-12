package temmental2;

public class ToDefaultTok {

    private String expr;
    private Cursor cursor;

    ToDefaultTok(Cursor cursor) {
        this.expr = "!";
        this.cursor = cursor.clone();
    }

    @Override
    public String toString() {
        return "@" + cursor.getPosition() + "\tToDefaultTok(" + expr + ")";
    }

    public boolean equals(Object o) {
        if (o == null || ! (o instanceof ToDefaultTok))
            return false;
        ToDefaultTok oc = (ToDefaultTok) o;
        return oc.expr.equals(expr) && oc.cursor.equals(cursor);
    }

}
