package temmental2;

import java.util.Map;

class Keyword {

    private final Cursor cursor;
    private String keyword;

	Keyword(String expr, Cursor cursor) throws TemplateException {
		this.cursor = cursor.clone();
		this.keyword = expr;
		
		boolean valid = expr.matches("\\w+");
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

}
