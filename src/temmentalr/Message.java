package temmentalr;

import java.util.List;
import java.util.Map;

class Message extends Element {

	private Identifier identifier;
	private List parameters;

	Message(Identifier identifier, List parameters) {
		this.identifier = identifier;
		this.parameters = parameters;
	}
	
	@Override
	Object writeObject(Map<String, Transform> functions, Map<String, Object> model, TemplateMessages messages) throws TemplateException {
		String key = (String) identifier.writeObject(functions, model, messages);

		List args = create_parameters_after_process(parameters, functions, model, messages);
		if (args == null) {
			return null;
		}
		
		if (isRequired(key) && ! messages.containsKey(key)) {
			throw new TemplateException("Key '%s' is not present in the property map to render message (%s)", key, getPosition());
		}
		
		return messages.format(key, args.toArray());
	}

	String getIdentifier() {
		return identifier.getIdentifier();
	}

	String getPosition() {
		return identifier.getPosition();
	}
	
	public String toString() {
		return "msg(" + identifier + "," + parameters.toString() + ")";
	}
}
