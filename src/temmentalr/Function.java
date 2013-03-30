package temmentalr;

import java.lang.reflect.Method;
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
		System.out.println("ici:"+element);
		
		Transform fp = null;
		
//		if (element instanceof Identifier) {
//			System.out.println("#Identifier##" + element);
//			if (getIdentifier().startsWith("$")) {
//				fp = functions.get(getInMap(model, false));
//			} else {
//				fp = (Transform) getInMap(functions, false);
//			}
//			System.out.println("#Identifier## rc=" + element + " ## " + fp);
//		} else if (element instanceof Function){
//			System.out.println("#Function##" + element);
//			Object o = element.writeObject(functions, model, messages);
//			fp = (Transform) o;
//			System.out.println("#Function## rc=" + element + " ## " + fp);
//		} 
		
		Object o = element.writeObject(functions, model, messages);
		System.out.println("#2## " + o);
		 fp = (Transform) ((o instanceof String) ? functions.get((String) o) : o);

		if (fp == null && isRequired(element.getIdentifier())) {
			throw new TemplateException("No transform function named '%s' is associated with the template for rendering at position '%s'.", element.getIdentifier(), element.getPosition());
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
			System.out.println(">"+element.getIdentifier() +">" + args);
			return apply.invoke(fp, args);
		} catch (Exception e) {
			throw new TemplateException(e, "Unable to apply function '" + element.getIdentifier() + "'"); //FIXME 
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
