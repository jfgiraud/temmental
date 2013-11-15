package temmental2;

class BracketTok extends Token {

    private final char bracket;

    BracketTok(char bracket, Cursor cursor) {
        super(bracket, cursor.clone());
        this.bracket = bracket;
	}
	
	public char neg() {
		if (bracket == '(') return ')';
		if (bracket == ')') return '(';
		if (bracket == '<') return '>';
		if (bracket == '>') return '<';
		if (bracket == '[') return ']';
		if (bracket == ']') return '[';
		if (bracket == '{') return '}';
		if (bracket == '}') return '{';
		throw new RuntimeException("Invalid bracket '" + bracket + "'");
	}

	public char getBracket() {
		return bracket;
	}

	public static boolean isBracket(int bracket) {
		return bracket == ')' || bracket == ']' || bracket == '>' || bracket == '}'
				|| bracket == '(' || bracket == '[' || bracket == '<' || bracket == '{';
	}
	
	public boolean isClosing() {
		return bracket == ')' || bracket == ']' || bracket == '>' || bracket == '}';
	}

	public boolean isOpening() {
		return bracket == '(' || bracket == '[' || bracket == '<' || bracket == '{';
	}
	
}
