package temmental2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

abstract class Element {

	protected Cursor cursor;
	
	Element(Cursor cursor) {
		this.cursor = cursor.clone();
	}
	
	abstract String getIdentifier();
	
	abstract Object writeObject(Map<String, Object> functions, Map<String, Object> model, TemplateMessages messages) throws TemplateException, IOException;
	
	Object getInModel(Map<String, Object> map) throws TemplateException {
		String varName = getIdentifier();
		varName = varName.substring(1);
		boolean optional = ! isRequired(varName);
		if (optional) {
            boolean forceDefault = varName.endsWith("!");
			varName = varName.substring(0, varName.length()-1);
			if (map.containsKey(varName)) {
				return map.get(varName);
			} else if (forceDefault) {
                return null;
            } else {
				throw new TemplateIgnoreRenderingException("Ignore rendering because key '%s' is not present or has null value in the model map at position '%s'.", varName, cursor.getPosition());
			}
		} else {
			if (! map.containsKey(varName) || map.get(varName) == null) {
				throw new TemplateException("Key '%s' is not present or has null value in the model map at position '%s'.", varName, cursor.getPosition());
			} else {
				return map.get(varName);
			}
		}
	}

	static boolean isRequired(String varname) {
		return varname != null && (varname.startsWith("'") || (! varname.endsWith("?") && ! varname.endsWith("!")));
	}

    public abstract String getIdentifierForErrorMessage();

	List<Object> create_parameters_after_process(List<Object> parameters, Map<String, Object> functions, Map<String, Object> model, TemplateMessages messages) throws TemplateException, IOException {
		List<Object> args = new ArrayList<Object>();
        for (int i = 0; i < parameters.size(); i++) {
        	Object parameter = parameters.get(i);
        	Object afterProcess;
        	/*if (parameter == null) {
        		throw new TemplateException("Unable to apply function: parameter #%d is null", i);     //FIXME
        	} */
        	if (parameter instanceof Element) {
        		afterProcess = ((Element) parameter).writeObject(functions, model, messages);
        	} else {
        		afterProcess = parameter;
        	}
        	if (afterProcess == null) {
                if (this instanceof Array) {
                    throw new TemplateException("Unable to render array at position '%s'. Required parameter #%d is null.",
                            cursor.getPosition(), i+1);
                } else {
                    throw new TemplateException("Unable to render '%s' at position '%s'. Required parameter #%d is null.",
                        getIdentifierForErrorMessage(), cursor.getPosition(), i+1);
                }
        	}
        	args.add(afterProcess);
        }
		return args;
	}

    protected final String pref(int d) {
        String p = "\t";
        for (int i=0; i<d; i++) {
            p += "\t";
        }
        return p;
    }

    public final String toString() {
        return repr(0, false);
    }

    public abstract String repr(int d, boolean displayPosition);

}
