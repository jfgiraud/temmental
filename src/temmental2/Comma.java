package temmental2;

class Comma {

	private String expr;
	private Cursor cursor;

	Comma(Cursor cursor) {
		this.expr = ",";
		this.cursor = cursor.clone();
	}
	
	Cursor cursor() {
		return cursor.clone();
	}
	
	@Override
	public String toString() {
		return "@" + cursor.getPosition() + "\tComma(" + expr + ")";
	}

	public boolean equals(Object o) {
		if (o == null || ! (o instanceof Comma))
			return false;
		Comma oc = (Comma) o;
		return oc.expr.equals(expr) && oc.cursor.equals(cursor);
	}

}
