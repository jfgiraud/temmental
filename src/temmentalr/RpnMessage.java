package temmentalr;

import java.util.List;
import java.util.Map;

public class RpnMessage extends RpnElem {

	private RpnWord word;
	private List parameters;

	public RpnMessage(RpnWord func, List parameters) {
		this.word = func;
		this.parameters = parameters;
	}
	
	@Override
	Object writeObject(Map<String, Transform> functions, Map<String, Object> model, TemplateMessages messages) throws TemplateException {
		String key = (String) word.writeObject(functions, model, messages);

		Object args = create_parameters_after_process(parameters, functions, model, messages, Object.class);
		if (args == null) {
			return null;
		}
		return messages.format(key, (Object[]) args);
	}

	public String getWord() {
		return word.getWord();
	}

	public String getPos() {
		return word.getPos();
	}
	
	public String toString() {
		return "msg(" + word + "," + parameters.toString() + ")";
	}
}
