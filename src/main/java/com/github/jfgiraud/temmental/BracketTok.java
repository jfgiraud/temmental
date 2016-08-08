package com.github.jfgiraud.temmental;

class BracketTok extends Token {

    static final int[] OPENING_BRACKETS = {'(', '<', '[', '!'};
    private static final int[] CLOSING_BRACKETS = {')', '>', ']', '¡'};

    private final int bracket;

    BracketTok(int bracket, Cursor cursor) {
        super(bracket, cursor.clone());
        this.bracket = bracket;
    }

    public int neg() {
        int index = indexOf(OPENING_BRACKETS, bracket);
        if (index >= 0) {
            return CLOSING_BRACKETS[index];
        }
        index = indexOf(CLOSING_BRACKETS, bracket);
        if (index >= 0) {
            return OPENING_BRACKETS[index];
        }
        throw new RuntimeException("Invalid bracket '" + bracket + "'");
    }

    public int getBracket() {
        return bracket;
    }

    public static boolean isBracket(int bracket) {
        return indexOf(CLOSING_BRACKETS, bracket) >= 0 || indexOf(OPENING_BRACKETS, bracket) >= 0;
    }

    private static int indexOf(int[] brackets, int c) {
        for (int i = 0; i < brackets.length; i++) {
            if (brackets[i] == c)
                return i;
        }
        return -1;
    }

    public boolean isClosing() {
        return indexOf(CLOSING_BRACKETS, bracket) >= 0;
    }

    public boolean isOpening() {
        return indexOf(OPENING_BRACKETS, bracket) >= 0;
    }

}
