package temmentalr;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
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
	
	public Object writeObject(Map<String, Transform> functions, Map<String, Object> model) throws TemplateException {
		Object o = func.writeObject(functions, model);
		Transform fp = (Transform) ((o instanceof String) ? functions.get((String) o) : o);

		System.out.println(">>>f>>>>" + func.toString());
		System.out.println(">>>o>>>>" + o);
		System.out.println(">>>fp>>>>" + fp);

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
        
        
//        System.out.println(">>>>>>>" + typeIn + " " + isArray);
        
        Object args;
//        typeIn = Object.class;

//        System.out.println("typeIn="+typeIn + "   " + func.getWord());
        args = Array.newInstance(typeIn, parameters.size());
        for (int i = 0; i < parameters.size(); i++) {
        	Object parameter = parameters.get(i);
        	Object afterProcess;
        	if (parameter == null) {
        		throw new TemplateException("Unable to apply function: null argument"); //FIXME
        	}
        	if (parameter instanceof RpnElem) {
        		afterProcess = ((RpnElem) parameter).writeObject(functions, model);
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
//        	if (parameter instanceof RpnWord) {
//        		System.out.println("apres " + ((RpnWord) parameter).getWord());
//        	}
        	Array.set(args, i, afterProcess);
        }
        if (parameters.size() == 1) {
//        	System.out.println("get1 " + fp.getClass().getTypeParameters());
        	args = ((Object[]) args)[0];
        }

		try {
			return apply.invoke(fp, args);
		} catch (Exception e) {
//			System.out.println("func="+ func.getWord());
//			System.out.println("apply="+apply);
//			System.out.println("fp="+fp);
//			System.out.println("size="+((Object[]) args).length);
//			System.out.println("class="+args.getClass());
//			for (int i=0; i<((Object[]) args).length; i++) {
//				System.out.println("p["+i+"]="+((Object[]) args)[i] + " %% " + ((Object[]) args)[i].getClass().getName());
//			}
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
