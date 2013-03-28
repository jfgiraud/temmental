package temmentalr;

import java.util.Map;

public abstract class RpnElem {

	abstract Object writeObject(Map<String, Transform> functions, Map<String, Object> model, TemplateMessages messages) throws TemplateException;
	
	abstract String getWord();

	abstract String getPos();
	
	static boolean isRequired(String varname) {
		return varname != null && varname.startsWith("'") || ! varname.endsWith("?");
	}
}
