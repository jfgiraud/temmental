package temmentalr;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

abstract class Element {

	abstract Object writeObject(Map<String, Transform> functions, Map<String, Object> model, TemplateMessages messages) throws TemplateException;
	
	abstract String getIdentifier();

	abstract String getPosition();
	
	static boolean isRequired(String varname) {
		return varname != null && (varname.startsWith("'") || ! varname.endsWith("?"));
	}

	static Object asArray(Collection parameters, Class typeIn) {
		if (typeIn == null) {
			typeIn = determineType(parameters);
		}
		Object args = (Object) Array.newInstance(typeIn, parameters.size());
		int i = 0;
		for (Iterator it = parameters.iterator(); it.hasNext(); i++) {
			Object parameter = it.next();
			Array.set(args, i, parameter);
		}
		return args;
	}

	private static Class determineType(Collection parameters) {
		Class typeIn;
		typeIn = Object.class;
		int i = 0;
		for (Object p : parameters) {
			Class clazz = p.getClass();
			if (i == 0)
				typeIn = clazz;
			if (! clazz.equals(typeIn)) {
				typeIn = Object.class;
				break;
			}
		}
		return typeIn;
	}
	
	List create_parameters_after_process(List parameters, Map<String, Transform> functions, Map<String, Object> model, TemplateMessages messages) throws TemplateException {
		List args = new ArrayList();
        for (int i = 0; i < parameters.size(); i++) {
        	Object parameter = parameters.get(i);
        	Object afterProcess;
        	if (parameter == null) {
        		throw new TemplateException("Unable to apply function: null argument"); //FIXME
        	}
        	if (parameter instanceof Element) {
        		afterProcess = ((Element) parameter).writeObject(functions, model, messages);
        	} else {
        		System.out.println("parameter="+parameter);//FIXME
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
        	System.out.println(afterProcess.getClass().getName());//FIXME
        	args.add(afterProcess);
        }
		return args;
	}
	
	Object getInMap(Map map, boolean forModel) throws TemplateException {
		String varname = getIdentifier(); 
		varname = varname.substring(1);
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
				if (forModel)
					throw new TemplateException("Key '%s' is not present or has null value in the model map at position '%s'.", varname, getPosition());
				else
					return null;
			} else {
				return map.get(varname);
			}
		}
	}
	
	Object getInModel(Map<String, Object> model) throws TemplateException {
		return getInMap(model, true);
	}
	
}
