package temmental2;

class CommandTok {

    private String expr;
    private Cursor cursor;

    CommandTok(Cursor cursor) {
        this.expr = "#";
        this.cursor = cursor.clone();
    }

    @Override
    public String toString() {
        return "@" + cursor.getPosition() + "\tCommandTok(" + expr + ")";
    }

    public boolean equals(Object o) {
        if (o == null || ! (o instanceof CommandTok))
            return false;
        CommandTok oc = (CommandTok) o;
        return oc.expr.equals(expr) && oc.cursor.equals(cursor);
    }
}
