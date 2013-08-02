package temmental2;

import java.util.Map;

class Keyword extends Element {

	private String keyword;

	Keyword(String expr, Cursor cursor) throws TemplateException {
		super(cursor);
		this.keyword = expr;
		
		boolean valid = expr.matches("#\\w+");
		if (! valid) {
			throw new TemplateException("Invalid keyword syntax for '%s' at position '%s'.", expr, cursor.getPosition());
		} 
	}
	
	@Override
	public String toString() {
		return "@" + cursor.getPosition() + "\tIdentifier(" + keyword + ")";
	}

	public boolean equals(Object o) {
		if (o == null || ! (o instanceof Keyword))
			return false;
		Keyword oc = (Keyword) o;
		return oc.keyword.equals(keyword) && oc.cursor.equals(cursor);
	}

	@Override
	Object writeObject(Map<String, Object> functions, Map<String, Object> model, TemplateMessages messages) throws TemplateException {
		if (keyword.startsWith("'")) {
			return keyword.substring(1);
		} else if (keyword.startsWith("$")) {
			return getInModel(model);
		} else {
			throw new TemplateException("Unsupported case #eval for '%s'", keyword);
		}
	}

	boolean isRequired() {
		return keyword != null && (keyword.startsWith("'") || ! keyword.endsWith("?"));
	}
	
	@Override
	String getIdentifier() {
		return keyword;
	}
	
}
