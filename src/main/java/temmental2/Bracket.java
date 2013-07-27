package temmental2;

class Bracket {

	private char bracket;
	Cursor cursor;

	Bracket(char bracket, Cursor cursor) {
		this.bracket = bracket;
		this.cursor = cursor.clone();
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
	
	@Override
	public String toString() {
		return "@" + cursor.getPosition() + "\tBracket(" + bracket +  ")" ;
	}

	public String getPosition() {
		return cursor.getPosition();
	}
	
	public boolean equals(Object o) {
		if (o == null || ! (o instanceof Bracket))
			return false;
		Bracket oc = (Bracket) o;
		return oc.bracket == bracket && oc.cursor.equals(cursor);
	}
	
}
