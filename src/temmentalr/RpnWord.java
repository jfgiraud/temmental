package temmentalr;

import java.util.Map;

public class RpnWord implements RpnElem {
	
	String word;
	String pos;

	public RpnWord(String word, String file, int line, int column) throws TemplateException {
		this.word = word;
		this.pos = String.format("%s:l%d:c%d", file, line, column);
		if (! isValidIdentifier(word)) {
			throw new TemplateException("Invalid identifier syntax for '%s' at '%s'.", word, pos); 
		}
	}
	
	@Override
	public String toString() {
		// [ word [ pos #pos ] #eval ]
		return "eval(" + word + ")"; 
	}

	static boolean isValidIdentifier(String word) {
		return word.matches("'\\w+") || word.matches("\\$\\w+(\\?)?");  
	}

	private static Object getInModel(Map<String, Object> model, String varname) throws TemplateException {
		varname = varname.substring(1);
		boolean optional = (varname.charAt(varname.length()-1) == '?');
		if (optional)
			varname = varname.substring(0, varname.length()-1);
		if (optional) {
			if (model.containsKey(varname)) {
				return model.get(varname);
			} else {
				return model.get(varname);
			}
		} else {
			if (! model.containsKey(varname)) {
				throw new TemplateException("Key '%s' is not present or has null value in the model map.", varname);
			} else {
				return model.get(varname);
			}
		}
	}
	
	@Override
	public Object writeObject(Map<String, Transform> functions, Map<String, Object> model) throws TemplateException {
		if (word.startsWith("'")) {
			return word.substring(1);
		} else if (word.startsWith("$")) {
			return getInModel(model, word);
		} else {
			throw new TemplateException("Unsupported case #eval for '%s'", word);
		}
	}

	public String getWord() {
		return word;
	}

	public String getPos() {
		return pos;
	}

}
