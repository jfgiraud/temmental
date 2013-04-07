package temmentalr;

import java.util.List;
import java.util.Map;

public class Calc extends Element {

	private List parameters;
	private Bracket bracket;

	Calc(Bracket b, List parameters) {
		this.parameters = parameters;
		this.bracket = b;
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
		return bracket.getPosition();
	}

	public String toString() {
		return "calc(" + parameters.toString() + ")";
	}
}
