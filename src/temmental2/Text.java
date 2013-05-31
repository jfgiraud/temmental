package temmental2;

import java.util.Map;

class Text extends Element {
	
	private String expr;
	
	Text(String expr, Cursor cursor) {
		super(cursor);
		this.expr = expr;
	}
	
	@Override
	public String toString() {
		return "@" + cursor.getPosition() + "\tText(" + expr + ")";
	}

	public boolean equals(Object o) {
		if (o == null || ! (o instanceof Text))
			return false;
		Text oc = (Text) o;
		return oc.expr.equals(expr) && oc.cursor.equals(cursor);
	}

	@Override
	String getIdentifier() {
		throw new RuntimeException("Text have no identifier!");
	}

	@Override
	Object writeObject(Map<String, Object> functions, Map<String, Object> model, TemplateMessages messages) throws TemplateException {
		return expr;
	}

}
