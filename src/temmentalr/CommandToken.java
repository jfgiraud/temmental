package temmentalr;

class CommandToken {

	private String position;
	private String command;

	public CommandToken(String command, String file, int line, int column) {
		this.command = command;
		this.position = String.format("%s:l%d:c%d", file, line, column);
	}

	public String getPosition() {
		return position;
	}

	public boolean isOpened() {
		return ! command.contains("/");
	}
	
	public boolean isClosed() {
		return ! isOpened();
	}

	public String get() {
		return command;
	}

	public String other() {
		return command.contains("/") ? command.replaceAll("/", "") : "/" + command;
	}

}
