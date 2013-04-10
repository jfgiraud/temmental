package temmentalr;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Calc extends Element {

	static final List<String> OPERATORS = Arrays.asList("+", "-", "*", "%", "neg", "odd", "even");
	 
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
			Stack stack = new Stack(); 
			for (Object e : tmp) {
				System.out.println(e);
				stack.push(e);
				if (OPERATORS.contains(e)) {
					String operation = (String) stack.pop();
					if ("+".equals(operation))  {
						Integer a = (Integer) stack.pop();
						Integer b = (Integer) stack.pop();
						stack.push(a.intValue()+b.intValue());
					} else if ("-".equals(operation))  {
						Integer a = (Integer) stack.pop();
						Integer b = (Integer) stack.pop();
						stack.push(b.intValue()-a.intValue());
					} else if ("*".equals(operation))  {
						Integer a = (Integer) stack.pop();
						Integer b = (Integer) stack.pop();
						stack.push(b.intValue()*a.intValue());
					} else if ("%".equals(operation))  {
						Integer a = (Integer) stack.pop();
						Integer b = (Integer) stack.pop();
						stack.push(b.intValue() % a.intValue());
					} else if ("neg".equals(operation))  {
						Integer a = (Integer) stack.pop();
						stack.push(-a);
					} else if ("odd".equals(operation))  {
						Integer a = (Integer) stack.pop();
						stack.push(a.intValue() % 2 == 1);
					} else if ("even".equals(operation))  {
						Integer a = (Integer) stack.pop();
						stack.push(a.intValue() % 2 == 0);
					} else {
						throw new TemplateException("Unsupported operation '%s'", operation);
					}
				} 
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
