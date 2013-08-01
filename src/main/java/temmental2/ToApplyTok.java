package temmental2;


class ToApplyTok {

	private String expr;
	private Cursor cursor;

	ToApplyTok(Cursor cursor) {
		this.expr = ":";
		this.cursor = cursor.clone();
	}
	
	@Override
	public String toString() {
		return "@" + cursor.getPosition() + "\tToApplyTok(" + expr + ")";
	}

	public boolean equals(Object o) {
		if (o == null || ! (o instanceof ToApplyTok))
			return false;
		ToApplyTok oc = (ToApplyTok) o;
		return oc.expr.equals(expr) && oc.cursor.equals(cursor);
	}

}
