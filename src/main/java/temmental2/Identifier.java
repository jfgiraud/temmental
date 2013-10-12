package temmental2;

import java.util.Map;

class Identifier extends Element {

	private String identifier;

	Identifier(String expr, Cursor cursor) throws TemplateException {
		super(cursor);
        this.identifier = expr;
        checkExprValid(expr);
	}

    private void checkExprValid(String expr) throws TemplateException {
        boolean valid = (expr.matches("'\\w+") || expr.matches("\\$\\w+(\\?)?"));
        if (! valid) {
            throw new TemplateException("Invalid identifier syntax for '%s' at position '%s'.", expr, cursor.getPosition());
        }
    }

	
	@Override
	public String repr(int d, boolean displayPostion) {
        return (displayPostion ? "@" + cursor.getPosition() + pref(d) : "") + "Identifier(" + identifier + ")";
	}

	public boolean equals(Object o) {
		if (o == null || ! super.equals(o)) {
			return false;
        }
        if (o instanceof Identifier) {
		    Identifier oc = (Identifier) o;
            return oc.identifier.equals(identifier) && oc.cursor.equals(cursor);
        }
        return false;
	}

    @Override
    Object writeObject(Map<String, Object> functions, Map<String, Object> model, TemplateMessages messages) throws TemplateException {
        if (identifier.startsWith("'")) {
            return identifier.substring(1);
        } else if (identifier.startsWith("$")) {
            return getInModel(model);
        } else {
            throw new TemplateException("Unsupported case #eval for '%s'", identifier);
        }
    }

    @Override
    public String getIdentifierForErrorMessage() {
        return getIdentifier();
    }

	@Override
	String getIdentifier() {
		return identifier;
	}

}
