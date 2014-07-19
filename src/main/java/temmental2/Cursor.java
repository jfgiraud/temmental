package temmental2;

class Cursor {

    private String file;
    private int line;
    private int column;
    private Stack linesLength;

    Cursor(String file, int line, int column) {
        this.file = (file == null ? "-" : file);
        this.line = line;
        this.column = column;
        linesLength = new Stack();
    }

    Cursor(String file, int line, int column, Stack linesLength) {
        this.file = file;
        this.line = line;
        this.column = column;
        this.linesLength = (linesLength == null ? new Stack() : linesLength);
    }

    Cursor(String position) {
        String t[] = position.split(":");
        this.file = t[0];
        if (t[1].charAt(0) != 'l')
            throw new RuntimeException(String.format("Invalid line position '%s'", position));
        this.line = Integer.parseInt(t[1].substring(1));
        if (t[2].charAt(0) != 'c')
            throw new RuntimeException(String.format("Invalid column position '%s'", position));
        this.column = Integer.parseInt(t[2].substring(1));
    }

    String getPosition() {
        return getPosition(0);
    }

    protected Cursor clone() {
        return new Cursor(file, line, column, linesLength);
    }

    public void next(int currentChar) {
        if (currentChar != '\n') {
            column++;
        } else {
            linesLength.push(column);
            column = 0;
            line++;
        }
    }

    String getPosition(int delta) {
        return String.format("%s:l%d:c%d", file, line, column + delta);
    }

    public Cursor move1l() {
        column -= 1;
        if (column == 0) {
            if (!linesLength.empty()) {
                line--;
                column = (Integer) linesLength.pop() + 1;
            }
        }
        return this;
    }

    public Cursor move1r() {
        column++;
        return this;
    }

    public Cursor movel(String expr, int i) {
        return move(-expr.length() + i);
    }

    public Cursor mover(int delta) {
        return move(delta);
    }

    private Cursor move(int delta) {
        boolean neg = (delta < 0);
        if (neg) delta = -delta;
        for (int i = 1; i <= delta; i++) {
            if (neg) {
                move1l();
            } else {
                move1r();
            }
        }
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof Cursor))
            return false;
        Cursor oc = (Cursor) o;
        return oc.file.equals(file) && oc.line == line && oc.column == column;
    }


}
