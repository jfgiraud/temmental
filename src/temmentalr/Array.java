package temmentalr;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class Array extends Element {

	private List parameters;

	Array(List parameters) {
		this.parameters = parameters;
	}
	
	@Override
	Object writeObject(Map<String, Transform> functions, Map<String, Object> model, TemplateMessages messages) throws TemplateException {
		List tmp = create_parameters_after_process(parameters, functions, model, messages);
		return asArray(tmp, null);
	}

	@Override
	String getIdentifier() {
		return null;
	}

	@Override
	String getPosition() {
		return null;
	}

	public String toString() {
		return parameters.toString();
	}
}
