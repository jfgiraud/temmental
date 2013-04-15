package temmentalr;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class CalcStack extends Stack {

	static final List<String> OPERATORS = Arrays.asList("+", "-", "*", "/", "%", "neg", "odd", "even", "<", ">", 
			"<=", ">=", "==", "!=", "sq", "pow", "ceil", "floor", "min", "max", "abs", "round", "trunc",
			"true", "false", "and", "or", "xor", "not", "/%");

	public void enter(Object e) {
		push(e);
		if (OPERATORS.contains(e)) {
			String operation = (String) pop();
			if ("+".equals(operation))  {
				push(Operations.add((Number) pop(), (Number) pop()));
			} else if ("true".equals(operation))  {
				push(true);
			} else if ("false".equals(operation))  {
				push(false);
			} else if ("and".equals(operation))  {
				push(Operations.and((Boolean) pop(), (Boolean) pop()));
			} else if ("or".equals(operation))  {
				push(Operations.or((Boolean) pop(), (Boolean) pop()));
			} else if ("xor".equals(operation))  {
				push(Operations.xor((Boolean) pop(), (Boolean) pop()));
			} else if ("not".equals(operation))  {
				push(Operations.not((Boolean) pop()));
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
			} else if ("round".equals(operation))  {
				swap();
				push(Operations.round((Number) pop(), (Number) pop()));
			} else if ("trunc".equals(operation))  {
				swap();
				push(Operations.trunc((Number) pop(), (Number) pop()));
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
			} else if ("/%".equals(operation))  {
				dup2();
				swap();
				push(Operations.div((Number) pop(), (Number) pop()));
				unrot();
				swap();
				push(Operations.mod((Number) pop(), (Number) pop()));
				try {
					printStack(System.out);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}  else {
				throw new StackException("Unsupported operation '" + operation + "'");
			}
		} 

	}

}
