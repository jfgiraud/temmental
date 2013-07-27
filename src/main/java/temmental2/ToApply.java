package temmental2;


class ToApply {

	private String expr;
	private Cursor cursor;

	ToApply(Cursor cursor) {
		this.expr = ":";
		this.cursor = cursor.clone();
	}
	
	@Override
	public String toString() {
		return "@" + cursor.getPosition() + "\tToApply(" + expr + ")";
	}

	public boolean equals(Object o) {
		if (o == null || ! (o instanceof ToApply))
			return false;
		ToApply oc = (ToApply) o;
		return oc.expr.equals(expr) && oc.cursor.equals(cursor);
	}

}
