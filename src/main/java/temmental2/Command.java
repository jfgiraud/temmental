package temmental2;

class Command {

	private String command;
    Cursor cursor;
	private boolean opening;
    private Expression expression;

	Command(String command, Expression expression, Cursor cursor) {
	    this(command, true, cursor, expression);
	}

	Command(String command, Cursor cursor) {
	    this(command, false, cursor, null);
	}

    private Command(String command, boolean opening, Cursor cursor, Expression expression) {
		this.command = command;
		this.opening = opening;
		this.cursor = cursor.clone();
		this.expression = expression;
		if (command.startsWith("#/"))
			throw new RuntimeException(expression.toString());
	}
	
	@Override
	public String toString() {
		return "@" + cursor.getPosition() + "\tCommand(" + (opening?"":"/") + command +  ")" ;
	}

	public String getPosition() {
		return cursor.getPosition();
	}
	
	public boolean equals(Object o) {
		if (o == null || ! (o instanceof Command))
			return false;
		Command oc = (Command) o;
		return oc.command == command && oc.cursor.equals(cursor);
	}

	public String getCommand() {
	    return command;
	}

	public boolean isOpening() {
	    return opening;
	}

}
