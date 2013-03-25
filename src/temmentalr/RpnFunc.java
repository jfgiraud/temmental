package temmentalr;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RpnFunc implements RpnElem {

	private RpnElem func;
	private List parameters;

	public RpnFunc(RpnElem func, List parameters) {
		this.func = func;
		this.parameters = parameters;
	}
	
	public String toString() {
		return func + parameters.toString();
	}

	private static boolean isRequired(String varname) {
		return varname != null && varname.startsWith("'");
	}
	
	public Object writeObject(Map<String, Transform> functions, Map<String, Object> model) throws TemplateException {
		Object o = func.writeObject(functions, model);
		Transform fp = (Transform) ((o instanceof String) ? functions.get((String) o) : o);
		
		if (fp == null && isRequired(func.getWord())) {
			throw new TemplateException("No transform function named '%s' is associated with the template for rendering at position '%s'.", func.getWord(), func.getPos());
		} else if (fp == null) {
			return null;
		}

		Method apply = getApplyMethod(fp);
		
		Class typeIn = Object.class; 
        boolean isArray = false;
        typeIn = apply.getParameterTypes()[0]; 
        isArray = typeIn.isArray();
        if (isArray)
            typeIn = typeIn.getComponentType();
        boolean convertToString = typeIn == String.class;
        
        Object args;
        if (isArray) {
        	args = Array.newInstance(typeIn, parameters.size());
        	for (int i = 0; i < parameters.size(); i++) {
        		Object val = parameters.get(i);
        		if (val instanceof RpnElem) {
        			Array.set(args, i, ((RpnElem) val).writeObject(functions, model));
        		} else {
        			Array.set(args, i, val);
        		}
        	}
        } else {
        	if (parameters.size() > 1) {
        		throw new TemplateException("Unable to apply function: Too much arguments"); //FIXME
        	}
        	Object val = parameters.get(0);
    		if (val instanceof RpnElem) {
    			args = ((RpnElem) val).writeObject(functions, model);
    		} else {
    			args = val;
    		}
        }
        
		try {
			return apply.invoke(fp, args);
		} catch (Exception e) {
			throw new TemplateException(e, "Unable to apply function " + args); //FIXME 
		}
	}
	
	private Method getApplyMethod(Transform t) {
		Method[] methods = t.getClass().getMethods();
        for (Method method : methods) {
            if (method.getName().equals("apply"))
                return method;
        }
        return null;
	}
	
	public String getWord() {
		return func.getWord();
	}

	public String getPos() {
		return func.getPos();
	}

}
