package temmentalr;

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
		return "[" + parameters + ", " + func + ", #func]";
	}

	private static boolean isRequired(String varname) {
		return varname != null && varname.startsWith("'");
	}
	
	public Object writeObject(Map<String, Transform> functions, Map<String, Object> model) throws TemplateException {

		Transform fp = functions.get(func.writeObject(functions, model));
		if (fp == null && isRequired(func.getWord())) {
			throw new TemplateException("No transform function named '%s' is associated with the template for rendering at position '%s'.", func.getWord(), func.getPos());
		} else if (fp == null) {
			return null;
		}

		List parameters2 = new ArrayList<>();
		for (Object parameter : parameters) {
			if (parameter instanceof RpnElem) {
				parameters2.add(((RpnElem) parameter).writeObject(functions, model));
			} else {
				parameters2.add(parameter);
			}
		}
//		if (key.startsWith("'")) {
//			Object o = getInModel(model, key);
//			if (o != null) {
//				out.write(o.toString());
//			}
//		} else {
//			throw new TemplateException("Unsupported case #eval for '%s'", key);
//		}
		return fp.apply(parameters2.get(0));
	}
	
	public String getWord() {
		return func.getWord();
	}

	public String getPos() {
		return func.getPos();
	}

}
