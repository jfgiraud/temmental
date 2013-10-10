package temmental2;

import java.util.Map;

class Identifier extends Element {

	private String identifier;
	
	Identifier(String expr, Cursor cursor) throws TemplateException {
		super(cursor);

        if (! expr.contains("!")) {
            this.identifier = expr;
            checkExprValid(expr);
        } else {
            String[] exprs = expr.split("!", 2);
            this.identifier = exprs[0]+"!";
            checkExprValid(exprs[0]+"!");
            if (! exprs[1].equals("")) {
                Expression.evalToken(exprs[1], cursor.clone(), false);
            }
        }
	}

    private void checkExprValid(String expr) throws TemplateException {
        boolean valid = (expr.matches("'\\w+") || expr.matches("\\$\\w+(\\?|!)?"));
        if (! valid) {
            throw new TemplateException("Invalid identifier syntax for '%s' at position '%s'.", expr, cursor.getPosition());
        }
    }

	
	@Override
	public String repr(int d, boolean displayPostion) {
        return (displayPostion ? "@" + cursor.getPosition() + pref(d) : "") + "Identifier(" + identifier + ")";
	}

	public boolean equals(Object o) {
		if (o == null || ! (o instanceof Identifier))
			return false;
		Identifier oc = (Identifier) o;
		return oc.identifier.equals(identifier) && oc.cursor.equals(cursor);
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

    boolean isRequired() {
		return identifier != null && (identifier.startsWith("'") || (! identifier.endsWith("?") && ! identifier.endsWith("!")));
	}

	@Override
	String getIdentifier() {
		return identifier;
	}

}
