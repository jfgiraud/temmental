package temmental2;

import java.util.List;
import java.util.Map;

class Message extends Element {

	private List<Object> parameters;
	private Identifier messageIdentifier;

	public Message(Identifier messageIdentifier, List<Object> parameters) {
		super(messageIdentifier.cursor);
		this.messageIdentifier = messageIdentifier;
		this.parameters = parameters;
	}
	
	@Override
	public String repr(int d) {
		return "@" + cursor.getPosition() + pref(d) + "Message(" + messageIdentifier.getIdentifier() + "," + parameters + ")";
	}

	@Override
	Object writeObject(Map<String, Object> functions, Map<String, Object> model, TemplateMessages messages) throws TemplateException {
		String key = (String) messageIdentifier.writeObject(functions, model, messages);

		List args = create_parameters_after_process(parameters, functions, model, messages);
		if (args == null) {
			return null;
		}

		if (messageIdentifier.isRequired() && (key == null || ! messages.containsKey(key))) {
			throw new TemplateException("Key '%s' is not present in the property map to render message '%s[%s]' at position '%s'.", 
					key, messageIdentifier.getIdentifier(), (args.size() > 1 ? "\u2026" : ""), cursor.getPosition());
		} else if (! messageIdentifier.isRequired() && key == null) {
			return null;
		} else if (! messageIdentifier.isRequired() && ! messages.containsKey(key)) {
			throw new TemplateException("Key '%s' is not present in the property map to render message '%s[%s]' at position '%s'.", 
					key, messageIdentifier.getIdentifier(), (args.size() > 1 ? "\u2026" : ""), cursor.getPosition());
		}
		
		return messages.format(key, args.toArray());
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null)
			return false;
		if (o instanceof Message) {
			Message oc = (Message) o;
			return oc.messageIdentifier.equals(messageIdentifier) && oc.parameters.equals(parameters);
		} 
		return false;
	}

	@Override
	String getIdentifier() {
		return messageIdentifier.getIdentifier();
	}

}
