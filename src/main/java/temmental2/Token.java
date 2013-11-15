package temmental2;

public class Token {

    private char expr;
    private Cursor cursor;

    Token(char token, Cursor cursor) {
        this.expr = token;
        this.cursor = cursor.clone();
    }

    @Override
    public String toString() {
        return String.format("@%s\ttoken(%c)", cursor.getPosition(), expr);
    }

    public boolean equals(Object o) {

        if ((o == null) || !(o instanceof Token)) {
            return false;
        }
        Token oc = (Token) o;
        return oc.expr == expr && oc.cursor.equals(cursor);
    }

    Cursor getCursor() {
        return cursor;
    }

}
