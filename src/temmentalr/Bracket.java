package temmentalr;

public class Bracket {

	private char bracket;
	private String position;

	public Bracket(char bracket, String file, int line, int column) {
		this.bracket = bracket;
		this.position = String.format("%s:l%d:c%d", file, line, column);
	}
	
	public char other() {
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

	public String getPosition() {
		return position;
	}
	
	public boolean isClosed() {
		return bracket == ')' || bracket == ']' || bracket == '>';
	}

	public boolean isOpened() {
		return bracket == '(' || bracket == '[' || bracket == '<';
	}
	
}
