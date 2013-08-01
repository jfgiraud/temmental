package temmental2;

class CommaTok {

	private String expr;
	private Cursor cursor;

	CommaTok(Cursor cursor) {
		this.expr = ",";
		this.cursor = cursor.clone();
	}
	
	Cursor cursor() {
		return cursor.clone();
	}
	
	@Override
	public String toString() {
		return "@" + cursor.getPosition() + "\tCommaTok(" + expr + ")";
	}

	public boolean equals(Object o) {
		if (o == null || ! (o instanceof CommaTok))
			return false;
		CommaTok oc = (CommaTok) o;
		return oc.expr.equals(expr) && oc.cursor.equals(cursor);
	}

}
