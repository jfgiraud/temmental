package temmentalr;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;

abstract class Element {

	abstract Object writeObject(Map<String, Transform> functions, Map<String, Object> model, TemplateMessages messages) throws TemplateException;
	
	abstract String getIdentifier();

	abstract String getPosition();
	
	static boolean isRequired(String varname) {
		return varname != null && varname.startsWith("'") || ! varname.endsWith("?");
	}
	
	Object create_parameters_after_process(List parameters, Map<String, Transform> functions, Map<String, Object> model, TemplateMessages messages, Class typeIn) throws TemplateException {
		Object args;
		args = Array.newInstance(typeIn, parameters.size());
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
        	Array.set(args, i, afterProcess);
        }
		return args;
	}
	
	Object getInModel(Map<String, Object> model) throws TemplateException {
		String varname = getIdentifier(); 
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
				throw new TemplateException("Key '%s' is not present or has null value in the model map at position '%s'.", varname, getPosition());
			} else {
				return model.get(varname);
			}
		}
	}
	
}
