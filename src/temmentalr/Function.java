package temmentalr;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

class Function extends Element {

	private Element element; // can reference an identifier or a function
	private List parameters;

	Function(Element element, List parameters) {
		this.element = element;
		this.parameters = parameters;
	}
	
	public String toString() {
		return element + parameters.toString();
	}
	
	Object writeObject(Map<String, Transform> functions, Map<String, Object> model, TemplateMessages messages) throws TemplateException {
		Object o = element.writeObject(functions, model, messages);
		Transform fp = (Transform) ((o instanceof String) ? functions.get((String) o) : o);

		if (fp == null && isRequired(element.getIdentifier())) {
			throw new TemplateException("No transform function named '%s' is associated with the template for rendering at position '%s'.", element.getIdentifier(), element.getPosition());
		} else if (fp == null) {
			return null;
		}
		
		Method apply = getApplyMethod(fp);
        

        List args = create_parameters_after_process(parameters, functions, model, messages);
        if (args == null) {
        	return null;
        }
        
        System.out.println("aaa " + args.size());
        try {
			if (args.size() == 1) {
				o = args.get(0);
			} else {
				Class nextIn = apply.getParameterTypes()[0]; 
				o = asArray(args, nextIn.isArray() ? nextIn.getComponentType() : nextIn);
			}
			System.out.println("aaa " + o);
			
			return apply.invoke(fp, o);
		} catch (Exception e) {
//			e.printStackTrace();
			String message = String.format("Unable to apply function '%s' at position '%s'. ", element.getIdentifier(), element.getPosition());
			Throwable z = e;
			while (z != null && ! (z instanceof TemplateException)) {
				z = e.getCause();
			}
			if (z != null) {
				message += z.getMessage();
			} else {
				message += String.format("This function expects %s. It receives %s.", 
						apply.getParameterTypes()[0].getCanonicalName(),  
						o.getClass().getCanonicalName());
			}
			throw new TemplateException(e, message);  
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
	
	String getIdentifier() {
		return element.getIdentifier();
	}

	String getPosition() {
		return element.getPosition();
	}

}
