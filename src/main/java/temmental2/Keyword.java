package temmental2;

import java.util.Map;

class Keyword extends Element {

	private String keyword;

	Keyword(String expr, Cursor cursor) throws TemplateException {
		super(cursor);
		this.keyword = expr;
		
		boolean valid = expr.matches("\\w+") || expr.matches("/\\w+");
		if (! valid) {
			throw new TemplateException("Invalid keyword syntax for '%s' at position '%s'.", expr, cursor.getPosition());
		} 
	}
	
	@Override
	public String toString() {
		return "@" + cursor.getPosition() + "\tKeyword(" + keyword + ")";
	}

	public boolean equals(Object o) {
		if (o == null || ! (o instanceof Keyword))
			return false;
		Keyword oc = (Keyword) o;
		return oc.keyword.equals(keyword) && oc.cursor.equals(cursor);
	}

    public boolean isClosing() {
        return keyword.startsWith("/");
    }

    public boolean isOpening() {
        return ! isClosing();
    }

	@Override
	Object writeObject(Map<String, Object> functions, Map<String, Object> model, TemplateMessages messages) throws TemplateException {
        throw new TemplateException("No sense to call writeObject for keyword '%s'", keyword);
	}

	@Override
	String getIdentifier() {
		return keyword;
	}
	
}
