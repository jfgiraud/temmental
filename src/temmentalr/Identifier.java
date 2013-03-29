package temmentalr;

import java.util.Map;

class Identifier extends Element {
	
	String identifier;
	String position;

	Identifier(String word, String file, int line, int column) throws TemplateException {
		this.identifier = word;
		this.position = String.format("%s:l%d:c%d", file, line, column);
		if (! isValid(word)) {
			throw new TemplateException("Invalid identifier syntax for '%s' at '%s'.", word, position); 
		}
	}
	
	@Override
	public String toString() {
		return "eval(" + identifier + ")"; 
	}

	static boolean isValid(String word) {
		return word.matches("'\\w+") || word.matches("\\$\\w+(\\?)?");  
	}

	private Object getInModel(Map<String, Object> model, String varname) throws TemplateException {
		varname = varname.substring(1);
		boolean optional = ! isRequired(varname);
		if (optional) {
			varname = varname.substring(0, varname.length()-1);
			if (model.containsKey(varname)) {
				return model.get(varname);
			} else {
				return null;
			}
		} else {
			if (! model.containsKey(varname)) {
				throw new TemplateException("Key '%s' is not present or has null value in the model map at position '%s'.", varname, position);
			} else {
				return model.get(varname);
			}
		}
	}
	
	@Override
	Object writeObject(Map<String, Transform> functions, Map<String, Object> model, TemplateMessages messages) throws TemplateException {
		if (identifier.startsWith("'")) {
			return identifier.substring(1);
		} else if (identifier.startsWith("$")) {
			return getInModel(model, identifier);
		} else {
			throw new TemplateException("Unsupported case #eval for '%s'", identifier);
		}
	}

	String getIdentifier() {
		return identifier;
	}

	String getPosition() {
		return position;
	}

}
