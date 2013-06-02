package temmental2.rpl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import temmental2.StringUtils;
import temmental2.Reader;
import temmental2.StackException;

public class Prog extends Reader {

	public String toString() {
		return "Prog" + operations;
	};
	
	Prog() {
		this(new ArrayList());
	}
	
	Prog(List operations) {
		super(operations);
	}

	public String read_until(List<String> tokens, List until) {
		 if (tokens.size() == 0) {
			 if (until != null) {
				 throw new StackException(String.format("Unable to reach token \"%s\".", StringUtils.join("\" or \"", until)));
			 }
		 }
		 String token = tokens.remove(0);
		 while (token != null) {
			 if (until != null && until.contains(token)) {
				 return token;
			 }
			 if (token.equals("{")) {
				 Prog pCmd = new Prog();
				 pCmd.read_until(tokens, Arrays.asList("}"));
				 operations.add(pCmd);
			 } else {
				 if (token.matches("^(true|false)$")) {
					 operations.add(new Boolean(token));
				 } else if (token.matches("^(-)?\\d+[lL]$")) {
					 operations.add(new Long(token));
				 } else if (token.matches("^(-)?\\d+$")) {
					 operations.add(new Integer(token));
				 } else if (token.matches("^(-)?((\\d*.)?\\d+?([eE][+-]?\\d+)?|nan|inf)$")) {
					 operations.add(new Float(token));
				 } else if (Arrays.asList( "depth", "drop", "drop2", "dropn", "dup", "dup2", "dupdup", "dupn", "ndupn", "nip", 
						 "over", "pick", "pick3", "roll", "rolld", "rot", "unrot", "keep", "pop", "push", "remove", "swap", 
						 "value", "insert", "empty", "clear", "unpick", "tolist", "get", 
						 "upper", "lower", "capitalize", "length", "startswith", "endswith", "reverse", "replace", "strip", 
						 "lstrip", "rstrip", "title", "split", "rsplit", 
						 "add", "sub", "mul", "div", 
						 "eq", "ne", "lt", "le", "gt", "ge", 
						 "ift", "ifte", 
						 "+", "-", "*", "/", "+", "==", "!=", "<", ">", "<=", ">=", 
						 "eval", 
						 "and", "or", "not", "xor").contains(token)) {
					 Map<String,String> associations = new HashMap<>();
                     associations.put("+", "add");
                     associations.put("-", "sub");
                     associations.put("*", "mul");
                     associations.put("/", "div");
                     associations.put("==", "eq");
                     associations.put("!=", "ne");
                     associations.put("<", "lt");
                     associations.put("<=", "le");
                     associations.put(">", "gt");
                     associations.put(">=", "ge");

                     String original = token;
                     
                     token = associations.containsKey(token) ? associations.get(token) : original;

                     boolean found = false;
                     for (Method m : new RplStack(new ArrayList<>()).getClass().getMethods()) {
                    	 if (m.getName().equals(token))
                    		 found = true;
                     }
                     if (!found) {
                    	 throw new StackException("Method '" + token + "' not present");
                     }
                     
                     
                     operations.add(new Function(token, original));
				 } else if (token.startsWith("\"") && token.endsWith("\"")) {
					 operations.add(token.substring(1, token.length()-1));
				 } else {
					 operations.add(new Variable(token));
				 }
			 }
			 if (tokens.size() > 0) {
				 token = tokens.remove(0);
			 } else {
				 if (until != null) {
					 throw new StackException(String.format("Unable to reach token \"%s\".", StringUtils.join("\" or \"", until)));
				 }
				 return null;
			 }
		 }
		 return null;
	}
	
	public void apply(RplStack stack, List operations, boolean execProg, boolean execFunc) {
		 if (operations == null) {
			 operations = getOperations();
		 }
		 RplStack.push_operations(stack, operations, execProg, execFunc);
	}
	
	public boolean equals(Object other) {
		return (other instanceof Prog) && ((Prog) other).operations.equals(operations);
	}
}
