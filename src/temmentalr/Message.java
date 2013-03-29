package temmentalr;

import java.util.List;
import java.util.Map;

public class Message extends Element {

	private Identifier identifier;
	private List parameters;

	Message(Identifier identifier, List parameters) {
		this.identifier = identifier;
		this.parameters = parameters;
	}
	
	@Override
	Object writeObject(Map<String, Transform> functions, Map<String, Object> model, TemplateMessages messages) throws TemplateException {
		String key = (String) identifier.writeObject(functions, model, messages);

		Object args = create_parameters_after_process(parameters, functions, model, messages, Object.class);
		if (args == null) {
			return null;
		}
		return messages.format(key, (Object[]) args);
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
