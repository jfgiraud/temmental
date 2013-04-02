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
        
		Class nextIn = apply.getParameterTypes()[0]; 

        List args = create_parameters_after_process(parameters, functions, model, messages);
        if (args == null) {
        	return null;
        }
        
        try {
			if (args.size() == 1) {
				o = args.get(0);
			} else {
				o = asArray(args, nextIn.isArray() ? nextIn.getComponentType() : nextIn);
			}
			
			Class firstOut = o.getClass();
			boolean firstOutArray = firstOut.isArray();
            boolean nextInArray = nextIn.isArray();
            System.out.println("firstOutArray="+firstOutArray);
            System.out.println("nextInArray="+nextInArray);
            System.out.println("!^="+!(firstOutArray ^ nextInArray));
            System.out.println("isAssignableFrom="+((Class<?>)nextIn).isAssignableFrom(((Class<?>)firstOut)));
        	System.out.println("outClass="+((Class<?>)firstOut).getCanonicalName());
        	System.out.println("inClass="+((Class<?>)nextIn).getCanonicalName());
            boolean ok = !(firstOutArray ^ nextInArray) && ((Class<?>)nextIn).isAssignableFrom(((Class<?>)firstOut));
            System.out.println("ok=" + ok);
            if (! ok/* && nextIn != String.class && nextIn != Object.class*/) {
            	String outClass = ((Class<?>)firstOut).getCanonicalName();
            	String inClass = ((Class<?>)nextIn).getCanonicalName();
            	System.out.println("throw exception");
            	throw new TemplateException("aaa");
            }

			return apply.invoke(fp, o);
		} catch (Exception e) {
			e.printStackTrace();
			throw new TemplateException(e, "Unable to apply function '%s' at position '%s'. This function expects %s. It receives %s.", 
					element.getIdentifier(), 
					element.getPosition(), 
					apply.getParameterTypes()[0].getCanonicalName(),  
					o.getClass().getCanonicalName());  
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
