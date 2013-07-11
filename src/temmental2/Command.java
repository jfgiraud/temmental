package temmental2;

class Command {

	private String command;
	Cursor cursor;
	private boolean opening;

	Command(String command, boolean opening, Cursor cursor) {
		this.command = command;
		this.opening = opening;
		this.cursor = cursor.clone();
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
	
}
