package temmental2.rpl;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import temmental2.Stack;
import temmental2.StackException;
import temmental2.StringUtils;



public class RplStack extends Stack {

	private Map g_variables;
	private List l_variables;

	@SuppressWarnings("rawtypes")
	public RplStack(List<Object> expected) {
		super(expected);
		g_variables = new HashMap<>();
		l_variables = new ArrayList<>();
	}

	void setGlobalVariables(Map m) {
		g_variables = m;
	}
	
	void setLocalVariables(List l) {
		l_variables = l;
	}

	public Map getGlobalVariables() {
		return g_variables;
	}

	public List getLocalVariables() {
		return l_variables;
	}
	
	private List list(Object ... objects) {
		return Arrays.asList(objects);
	}
	
	public void add() {
		_assert_number("ADD", 1, 2);
        push(Operations.add((Number) pop(), (Number) pop()));
	}
	
	public void mul() {
		_assert_number("MUL", 1, 2);
        push(Operations.mul((Number) pop(), (Number) pop()));
	}

	public void sub() {
		_assert_number("SUB", 1, 2);
		swap();
        push(Operations.sub((Number) pop(), (Number) pop()));
	}

	public void div() {
		_assert_number("DIV", 1, 2);
		swap();
        push(Operations.div((Number) pop(), (Number) pop()));
	}

	public void eq() {
		_assert_enough_elements("==", 2);
		Object a=pop();
		Object b=pop();
		if (a == null || b == null) {
			push_bool(a == b);
		} else {
			push_bool(a.equals(b));
		}
	}
	
	private void push_bool(boolean b) {
		push(b /*? 1 : 0*/);
	}

	public void ne() {
		_assert_enough_elements("!=", 2);
		Object a=pop();
		Object b=pop();
		if (a == null || b == null) {
			push_bool(a != b);
		} else {
			push_bool(! a.equals(b));
		}
	}
	
	public void lt() {
		_assert_enough_elements("<", 2);
		Object a=pop();
		Object b=pop();
		if (a instanceof Comparable && b instanceof Comparable) {
			push_bool( ((Comparable) a).compareTo((Comparable) b) > 0);
		} else {
			throw new AssertionError("< Error : Bad Argument Types");
		}
	}
	
	public void gt() {
		_assert_enough_elements(">", 2);
		Object a=pop();
		Object b=pop();
		if (a instanceof Comparable && b instanceof Comparable) {
			push_bool( ((Comparable) a).compareTo((Comparable) b) < 0);
		} else {
			throw new AssertionError("> Error : Bad Argument Types");
		}
	}

	public void le() {
		_assert_enough_elements("<=", 2);
		Object a=pop();
		Object b=pop();
		if (a instanceof Comparable && b instanceof Comparable) {
			push_bool( ((Comparable) a).compareTo((Comparable) b) >= 0);
		} else {
			throw new AssertionError("<= Error : Bad Argument Types");
		}
	}

	public void ge() {
		_assert_enough_elements(">=", 2);
		Object a=pop();
		Object b=pop();
		if (a instanceof Comparable && b instanceof Comparable) {
			push_bool( ((Comparable) a).compareTo((Comparable) b) <= 0);
		} else {
			throw new AssertionError(">= Error : Bad Argument Types");
		}
	}
	
	private boolean istrue(Number n) {
		return (new Operations.NumberComparator()).compare(n, 0) != 0;
	}
	
	public void and() {
		_assert_boolean("AND", 1, 2);
		Boolean a = (Boolean) pop();
		Boolean b = (Boolean) pop();
		push_bool(a && b);
	}
	
	public void or() {
		_assert_boolean("OR", 1, 2);
		Boolean a = (Boolean) pop();
		Boolean b = (Boolean) pop();
		push_bool(a || b);
	}
	
	public void xor() {
		_assert_boolean("XOR", 1, 2);
		Boolean a = (Boolean) pop();
		Boolean b = (Boolean) pop();
		push_bool(a ^ b);
	}
	
	public void not() {
		_assert_boolean("NOT", 1);
		Boolean a = (Boolean) pop();
		System.out.println("=+>" + a);
		push_bool(! a);
	}
	
	
	public void ift() {
		_assert_enough_elements("IFT", 2);
		_assert_boolean("IFT", 2);
		Boolean b = (Boolean) remove(2);
		if (b) 
			eval();
		else
			drop();
	}

	public void ifte() {
		_assert_enough_elements("IFTE", 3);
		_assert_boolean("IFTE", 3);
		rot();
		Boolean b = (Boolean) pop();
		if (b) {
			drop();
			eval();
		} else {
			nip();
			eval();
		}
	}
	
	public void eval() { 
		Object x = pop();
		if (x instanceof String) {
			push(x);
		} else if (x instanceof Function) {
			((Function) x).apply(this);
		} else if (x instanceof Prog) {
			((Prog) x).apply(this, null, false, true);
		} else {
			throw new RuntimeException("zzz");
		}
		// TODO Auto-generated method stub
	}
	
	private void _assert_enough_elements(String caller, int n) {
		assertTrue(String.format("%s Error: Too Few Arguments", caller), depth() >= n);
	}
	
	private void _assert_number(String caller, int ... levels) {
		assertTrue(String.format("%s Error: Too Few Arguments", caller), levels.length >= 1);
		for (int level : levels) {
			assertTrue(String.format("%s Error : Bad Argument Type", caller), value(level) instanceof Number);
		}
	}
	
	private void _assert_string(String caller, int ... levels) {
		assertTrue(String.format("%s Error: Too Few Arguments", caller), levels.length >= 1);
		for (int level : levels) {
			assertTrue(String.format("%s Error : Bad Argument Type", caller), value(level) instanceof String);
		}
	}
	
	private void _assert_boolean(String caller, int ... levels) {
		assertTrue(String.format("%s Error: Too Few Arguments", caller), levels.length >= 1);
		for (int level : levels) {
			assertTrue(String.format("%s Error: Bad Argument Type", caller), value(level) instanceof Boolean);
		}
	}

	private void assertTrue(String message, boolean b) {
		if (! b) {
			throw new StackException(message);
		}
	}

	public static List<String> tokenize(String t) {
		List<String> s = new ArrayList<>();
		StringWriter w = new StringWriter();
		Map<Integer,Boolean> ins = new HashMap<>();
		ins.put((int) '\'', false);
		ins.put((int) '"', false);
		for (int i=0; i<t.length(); i++) {
			int c = t.charAt(i);
			if (c == '"' || c == '\'') {
				if (! ins.get((int) '"') && ! ins.get((int) '\'')) {
					ins.put(c, true);
					w.write(c);
				} else if (ins.get(c)) {
					w.write(c);
					ins.put(c, false);
				} else {
					w.write(c);
				}
			} else if (c == ' ' && ! ins.get((int) '"') && ! ins.get((int) '\'')) {
				if (w.toString().length() > 0) {
					s.add(w.toString());
					w = new StringWriter();
				}
			} else {
				w.write(c);
			}
		}
		if (w.toString().length() > 0) {
			s.add(w.toString());
		}
		return s;
	}

	List<Object> getElements() {
		return elements;
	}
	
	
	public static void push_operations(RplStack stack, List operations, boolean executeProg, boolean executeFunction) {
		for (Object e : operations) {
			if (e instanceof Integer || e instanceof Long || e instanceof Float || e instanceof Boolean || e instanceof String) {
				stack.push(e);
			} else if (e instanceof Variable) {
				if (! executeFunction)
					stack.push(e);
				else
					((Variable) e).apply(stack);
			} else if (e instanceof Prog) {
				if (! executeProg)
					stack.push(e);
				else
	                push_operations(stack, ((Prog) e).getOperations(), false, true);
			} else if (e instanceof Command) {
			} else if (e instanceof Function) {
				if (! executeFunction) {
					stack.push(e);
				} else {
					if (((Function) e).original.equals("eval")) {
						if (! stack.empty()) {
							Object r = stack.pop();
							push_operations(stack, Arrays.asList(r), true, true);
						}
					} else {
						((Function) e).apply(stack);
					}
				}
			} else {
				throw new StackException("Unsupported type " + e.getClass().getCanonicalName());
			}
		}
	}
	
	public void length() {
		push(((String) pop()).length());		
	}
	
	public void upper() {
		_assert_string("UPPER", 1);
		push(((String) pop()).toUpperCase());		
	}
	
	public void lower() {
		_assert_string("LOWER", 1);
		push(((String) pop()).toLowerCase());		
	}
	
	public void capitalize() {
		_assert_string("CAPITALIZE", 1);
		push(StringUtils.capitalize(((String) pop())));
	}
	
	public void title() {
		_assert_string("TITLE", 1);
		push(StringUtils.titlelize(((String) pop())));
	}
	
	public void startswith() {
		_assert_string("STARTSWITH", 1);
		String pref = ((String) pop());
		String text = ((String) pop());
		push( text.startsWith(pref) );
	}
	
	public void endswith() {
		_assert_string("ENDSWITH", 1);
		String suffix = ((String) pop());
		String text = ((String) pop());
		push( text.endsWith(suffix) );
	}
	
	public void lstrip() {
		_assert_string("LSTRIP", 1);
		push(StringUtils.lstrip(((String) pop())));
	}
	
	public void rstrip() {
		_assert_string("RSTRIP", 1);
		push(StringUtils.rstrip(((String) pop())));
	}
	
	public void strip() {
		_assert_string("STRIP", 1);
		push(StringUtils.strip(((String) pop())));
	}
	
	public void reverse() {
		_assert_string("REVERSE", 1);
		push(StringUtils.reverse(((String) pop())));
	}
	
	public void replace() {
        _assert_string("REPLACE", 1, 2, 3);
        String by = (String) pop();
        String what = (String) pop();
        String text = (String) pop();
        push(text.replace(what, by));
	}
	
	public void split() {
		if (depth()>=3 && value() instanceof Integer) {
			Integer max = (Integer) pop();
			_assert_string("SPLIT", 1, 2);
			String regex = (String) pop();
			String text = (String) pop();
			int n=0;
			for (String s : text.split(" ", max+1)) {
				push(s);
				n++;
			}
			push(n);
			tolist();
		} else if (depth() >= 1 && value() instanceof String) {
			System.out.println("*************===");
			String text = (String) pop();
			int n=0;
			for (String s : text.split("\\s+")) {
				push(s);
				n++;
			}
			push(n);
			tolist();
		} else {
			System.out.println("*************!!!");
		}
	}
	
	/*public static String toTitleCase(String input) {
	    StringBuilder titleCase = new StringBuilder();
	    boolean nextTitleCase = true;

	    for (char c : input.toCharArray()) {
	        if (Character.isSpaceChar(c)) {
	            nextTitleCase = true;
	        } else if (nextTitleCase) {
	            c = Character.toTitleCase(c);
	            nextTitleCase = false;
	        }

	        titleCase.append(c);
	    }

	    return titleCase.toString();
	}*/
	
	
	
	/*def push_operations(stack, operations, executeProg=False, executeFunction=False):
	    
	        elif isinstance(e, Command):
	            e.apply(stack)
	        elif isinstance(e, Function):
	            if not executeFunction:
	                stack.push(e)
	            else:
	                if e.original == 'eval':
	                    if not stack.empty():
	                        r = stack.pop()
	                        push_operations(stack, [ r ], True, True)
	                else:
	                    e.apply(stack)
	        else:
	            raise Exception('Unsupported type' + str(e))*/

}
