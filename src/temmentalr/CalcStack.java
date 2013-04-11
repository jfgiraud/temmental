package temmentalr;

import java.util.Arrays;
import java.util.List;

public class CalcStack extends Stack {

	static final List<String> OPERATORS = Arrays.asList("+", "-", "*", "/", "%", "neg", "odd", "even", "<", ">", "<=", ">=", "==", "!=", "sq", "pow", "ceil", "floor", "min", "max", "abs");

	public void enter(Object e) {
		push(e);
		if (OPERATORS.contains(e)) {
			String operation = (String) pop();
			if ("+".equals(operation))  {
				push(Operations.add((Number) pop(), (Number) pop()));
			} else if ("-".equals(operation))  {
				swap();
				push(Operations.sub((Number) pop(), (Number) pop()));
			} else if ("*".equals(operation))  {
				push(Operations.mul((Number) pop(), (Number) pop()));
			} else if ("min".equals(operation))  {
				push(Operations.min((Number) pop(), (Number) pop()));
			} else if ("max".equals(operation))  {
				push(Operations.max((Number) pop(), (Number) pop()));
			} else if ("/".equals(operation))  {
				swap();
				push(Operations.div((Number) pop(), (Number) pop()));
			} else if ("sq".equals(operation))  {
				push(Operations.sq((Number) pop()));
			} else if ("pow".equals(operation))  {
				swap();
				push(Operations.pow((Number) pop(), (Number) pop()));
			} else if ("%".equals(operation))  {
				swap();
				push(Operations.mod((Number) pop(), (Number) pop()));
			} else if ("neg".equals(operation))  {
				push(Operations.neg((Number) pop()));
			} else if ("abs".equals(operation))  {
				push(Operations.abs((Number) pop()));
			} else if ("odd".equals(operation))  {
				push(Operations.odd((Number) pop()));
			} else if ("even".equals(operation))  {
				push(Operations.even((Number) pop()));
			} else if ("ceil".equals(operation))  {
				push(Operations.ceil((Number) pop()));
			} else if ("floor".equals(operation))  {
				push(Operations.floor((Number) pop()));
			} else if ("<".equals(operation))  {
				swap();
				push(Operations.lt((Number) pop(), (Number) pop()));
			} else if (">".equals(operation))  {
				swap();
				push(Operations.gt((Number) pop(), (Number) pop()));
			} else if ("<=".equals(operation))  {
				swap();
				push(Operations.le((Number) pop(), (Number) pop()));
			} else if (">=".equals(operation))  {
				swap();
				push(Operations.ge((Number) pop(), (Number) pop()));
			} else if ("!=".equals(operation))  {
				swap();
				push(Operations.ne((Number) pop(), (Number) pop()));
			} else if ("==".equals(operation))  {
				swap();
				push(Operations.eq((Number) pop(), (Number) pop()));
			} else {
				throw new StackException("Unsupported operation '" + operation + "'");
			}
		} 

	}

}
