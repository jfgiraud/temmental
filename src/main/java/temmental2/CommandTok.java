package temmental2;

import java.io.IOException;

class CommandTok {

	private String command;
    Cursor cursor;
	private boolean opening;
    private Expression expression;

	CommandTok(String command, Expression expression, Cursor cursor) {
	    this(command, true, cursor, expression);
	}

	CommandTok(String command, Cursor cursor) {
	    this(command, false, cursor, null);
	}

    private CommandTok(String command, boolean opening, Cursor cursor, Expression expression) {
		this.command = command;
		this.opening = opening;
		this.cursor = cursor.clone();
		this.expression = expression;
		if (command.startsWith("#/"))
			throw new RuntimeException(expression.toString());
	}
	
	@Override
	public String toString() {
		return "@" + cursor.getPosition() + "\tCommandTok(" + (opening ? "" : "/") + command +  ")" ;
	}

	public String getPosition() {
		return cursor.getPosition();
	}
	
	public boolean equals(Object o) {
		if (o == null || ! (o instanceof CommandTok))
			return false;
		CommandTok oc = (CommandTok) o;
		return oc.command == command && oc.cursor.equals(cursor);
	}

	public String getCommand() {
	    return command;
	}

	public boolean isOpening() {
	    return opening;
	}

    public Object parseExpression() throws IOException, TemplateException {
        return expression.parse();
    }

}
