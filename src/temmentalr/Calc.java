package temmentalr;

import java.util.Arrays;
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
		if (tmp != null) {
			CalcStack stack = new CalcStack(); 
			for (Object e : tmp) {
				stack.enter(e);
			}
			if (stack.empty()) {
				throw new TemplateException("Stack evaluation returns an empty result at position '%s'", getPosition());
			} else if (stack.depth()>1) {
				throw new TemplateException("Stack evaluation returns more than one value at position '%s'", getPosition());
			}  
			return stack.pop();
		}
		return null;
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
