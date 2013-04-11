package temmentalr;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Calc extends Element {

	static final List<String> OPERATORS = Arrays.asList("+", "-", "*", "%", "neg", "odd", "even", "<", ">", "<=", ">=", "==", "!=");
	 
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
						stack.push(Operations.add((Number) stack.pop(), (Number) stack.pop()));
					} else if ("-".equals(operation))  {
						stack.swap();
						stack.push(Operations.sub((Number) stack.pop(), (Number) stack.pop()));
					} else if ("*".equals(operation))  {
						stack.push(Operations.mul((Number) stack.pop(), (Number) stack.pop()));
					} else if ("%".equals(operation))  {
						stack.swap();
						stack.push(Operations.mod((Number) stack.pop(), (Number) stack.pop()));
					} else if ("neg".equals(operation))  {
						stack.push(Operations.neg((Number) stack.pop()));
					} else if ("odd".equals(operation))  {
						stack.push(Operations.odd((Number) stack.pop()));
					} else if ("even".equals(operation))  {
						stack.push(Operations.even((Number) stack.pop()));
					} else if ("<".equals(operation))  {
						stack.swap();
						stack.push(Operations.lt((Number) stack.pop(), (Number) stack.pop()));
					} else if (">".equals(operation))  {
						stack.swap();
						stack.push(Operations.gt((Number) stack.pop(), (Number) stack.pop()));
					} else if ("<=".equals(operation))  {
						stack.swap();
						stack.push(Operations.le((Number) stack.pop(), (Number) stack.pop()));
					} else if (">=".equals(operation))  {
						stack.swap();
						stack.push(Operations.ge((Number) stack.pop(), (Number) stack.pop()));
					} else if ("!=".equals(operation))  {
						stack.swap();
						stack.push(Operations.ne((Number) stack.pop(), (Number) stack.pop()));
					} else if ("==".equals(operation))  {
						stack.swap();
						stack.push(Operations.eq((Number) stack.pop(), (Number) stack.pop()));
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
