package temmental2;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

abstract class Element {

	protected Cursor cursor;
	
	Element(Cursor cursor) {
		this.cursor = cursor.clone();
	}
	
	abstract String getIdentifier();
	
	abstract Object writeObject(Map<String, Object> functions, Map<String, Object> model, TemplateMessages messages) throws TemplateException;
	
	Object getInModel(Map<String, Object> map) throws TemplateException {
		String varname = getIdentifier();
		varname = varname.substring(1);
		boolean force = varname.endsWith("!");
		varname = varname.replace("!", "");
		boolean optional = ! isRequired(varname);
		if (optional) {
			varname = varname.substring(0, varname.length()-1);
			if (map.containsKey(varname)) {
				return map.get(varname);
			} else {
				return null;
			}
		} else {
			if (! map.containsKey(varname)) {
				if (force)
					return null;
				throw new TemplateException("Key '%s' is not present or has null value in the model map at position '%s'.", varname, cursor.getPosition());
			} else {
				return map.get(varname);
			}
		}
	}
	
	static boolean isRequired(String varname) {
		return varname != null && (varname.startsWith("'") || ! varname.endsWith("?"));
	}
	
	List<Object> create_parameters_after_process(List<Object> parameters, Map<String, Object> functions, Map<String, Object> model, TemplateMessages messages) throws TemplateException {
		List<Object> args = new ArrayList<Object>();
        for (int i = 0; i < parameters.size(); i++) {
        	Object parameter = parameters.get(i);
        	Object afterProcess;
        	if (parameter == null) {
        		throw new TemplateException("Unable to apply function: null argument"); //FIXME
        	}
        	if (parameter instanceof Element) {
        		afterProcess = ((Element) parameter).writeObject(functions, model, messages);
        	} else {
        		afterProcess = parameter;
        	}
        	if (afterProcess == null) {
        		if (((Element) parameter).isRequired(((Element) parameter).getIdentifier())) {
        			// FIXME pas top le test
        			throw new TemplateException("Unable to apply function: null argument"+parameter.getClass().getName()); //FIXME
        		} else {
        			return null;
        		}
        	}
        	args.add(afterProcess);
        }
		return args;
	}
}
