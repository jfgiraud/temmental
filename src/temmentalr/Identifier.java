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

	@Override
	Object writeObject(Map<String, Transform> functions, Map<String, Object> model, TemplateMessages messages) throws TemplateException {
		if (identifier.startsWith("'")) {
			return identifier.substring(1);
		} else if (identifier.startsWith("$")) {
			return getInModel(model);
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
