package temmental2;

import java.util.Map;

class Identifier extends Element {

	private String identifier;
	private Object defaultValue;

	Identifier(String expr, Cursor cursor) throws TemplateException {
		super(cursor);
        this.defaultValue = null;
        if (! expr.contains("!")) {
            this.identifier = expr;
            checkExprValid(expr);
        } else {
            String[] exprs = expr.split("!", 2);
            this.identifier = exprs[0]+"!";
            checkExprValid(this.identifier);
            if (! exprs[1].equals("")) {
                this.defaultValue = Expression.evalToken(exprs[1], cursor.clone().mover(expr.length()), false);
            } else {
                this.defaultValue = "";
            }
        }
	}

    public Identifier(String expr, Cursor cursor, Object defaultValue) throws TemplateException {
        this(expr, cursor);
        this.defaultValue = defaultValue;
    }

    private void checkExprValid(String expr) throws TemplateException {
        boolean valid = (expr.matches("'\\w+") || expr.matches("\\$\\w+(\\?|!)?"));
        if (! valid) {
            throw new TemplateException("Invalid identifier syntax for '%s' at position '%s'.", expr, cursor.getPosition());
        }
    }

	
	@Override
	public String repr(int d, boolean displayPostion) {
        return (displayPostion ? "@" + cursor.getPosition() + pref(d) : "") + "Identifier(" + identifier + ")"
                + (defaultValue != null ? "!" + defaultValue : "");
	}

	public boolean equals(Object o) {
		if (o == null || ! (o instanceof Identifier))
			return false;
		Identifier oc = (Identifier) o;
        return oc.identifier.equals(identifier) && oc.cursor.equals(cursor) &&
                ((oc.defaultValue != null && oc.defaultValue.equals(defaultValue))
                        || oc.defaultValue == defaultValue);
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

    @Override
    Object getInModel(Map<String, Object> map) throws TemplateException {
        Object value = super.getInModel(map);
        if (value != null) {
            return value;
        }
        if (defaultValue instanceof Identifier) {
            return ((Element) defaultValue).getInModel(map);
        } else if (defaultValue instanceof Char) {
            return ((Char) defaultValue).writeObject(null, map, null);
        } else if (defaultValue instanceof Text) {
            return ((Text) defaultValue).writeObject(null, map, null);
        } else if (defaultValue instanceof Element) {
            throw new TemplateException("Invalid default value at position '%s'. Only identifiers and java type are allowed!", cursor.getPosition());
        }
        return defaultValue;
    }
}
