package temmentalr;

public class Bracket {

	char bracket;
	String position;

	public Bracket(char bracket, String file, int line, int column) {
		this.bracket = bracket;
		this.position = String.format("%s:l%d:c%d", file, line, column);
	}

}
