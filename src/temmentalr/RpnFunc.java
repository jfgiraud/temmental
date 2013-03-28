package temmentalr;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public class RpnFunc extends RpnElem {

	private RpnElem func;
	private List parameters;

	public RpnFunc(RpnElem func, List parameters) {
		this.func = func;
		this.parameters = parameters;
	}
	
	public String toString() {
		return func + parameters.toString();
	}
	
	public Object writeObject(Map<String, Transform> functions, Map<String, Object> model, TemplateMessages messages) throws TemplateException {
		Object o = func.writeObject(functions, model, messages);
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
        
        Object args = create_parameters_after_process(parameters, functions, model, messages, typeIn);
        if (args == null) {
        	return null;
        }
        if (parameters.size() == 1) {
        	args = ((Object[]) args)[0];
        }

		try {
			return apply.invoke(fp, args);
		} catch (Exception e) {
			throw new TemplateException(e, "Unable to apply function '" + func.getWord() + "'"); //FIXME 
		}
	}

	
	
	private Method getApplyMethod(Transform t) {
		Method[] methods = t.getClass().getMethods();
        for (Method method : methods) {
//        	System.out.println("method=" + method);
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
