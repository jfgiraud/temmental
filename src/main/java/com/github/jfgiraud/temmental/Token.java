package com.github.jfgiraud.temmental;

public class Token {

    private int expr;
    private Cursor cursor;

    Token(int token, Cursor cursor) {
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
