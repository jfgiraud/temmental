package temmentalr;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;

public abstract class RpnElem {

	abstract Object writeObject(Map<String, Transform> functions, Map<String, Object> model, TemplateMessages messages) throws TemplateException;
	
	abstract String getWord();

	abstract String getPos();
	
	static boolean isRequired(String varname) {
		return varname != null && varname.startsWith("'") || ! varname.endsWith("?");
	}
	
	Object create_parameters_after_process(List parameters, Map<String, Transform> functions, Map<String, Object> model,
			TemplateMessages messages, Class typeIn) throws TemplateException {
		Object args;
		args = Array.newInstance(typeIn, parameters.size());
        for (int i = 0; i < parameters.size(); i++) {
        	Object parameter = parameters.get(i);
        	Object afterProcess;
        	if (parameter == null) {
        		throw new TemplateException("Unable to apply function: null argument"); //FIXME
        	}
        	if (parameter instanceof RpnElem) {
        		afterProcess = ((RpnElem) parameter).writeObject(functions, model, messages);
        	} else {
        		afterProcess = parameter;
        	}
        	if (afterProcess == null) {
        		if (((RpnElem) parameter).isRequired(((RpnElem) parameter).getWord())) {
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
}
