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
	
	public Prog() {
		this(new ArrayList());
	}
	
	Prog(List operations) {
		super(operations);
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
