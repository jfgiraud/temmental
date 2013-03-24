package temmentalr;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RpnStack extends Stack {
	
	private static final boolean debug = true;

	private Map<String, Transform> functions;

	public RpnStack() {
		this(new ArrayList<>());
	}

	public RpnStack(List<Object> tocopy) {
		super(tocopy);
		functions = new HashMap<String, Transform>();
	}

	private List<Integer> chars(int ... chars) {
		ArrayList<Integer> result = new ArrayList<Integer>();
		for (int c : chars) {
			result.add(c);
		}
		return result;
	}
	
	private void debug(String format, Object ... parameters) {
		if (debug)
			System.err.println(String.format(format, parameters));
	}
	
	public void parse(String expression, String file, int line, int column) throws IOException, TemplateException {
		StringReader sr = new StringReader(expression);
		StringWriter buffer = new StringWriter();
		boolean outsideAnExpression = true;
		try {
			int currentChar = sr.read(); 
			while (currentChar != -1) {
				column++;
				if (outsideAnExpression) {
					if (currentChar != '~') {
						buffer.write(currentChar);
						if (currentChar == '\n') {
							line++;
							column = 0;
						} 
//						debug("%c %c => %s", currentChar, '#', buffer.toString());
					} else {
						int nextChar = sr.read();
						if (nextChar == -1) {
//							debug("%c %c => %s", currentChar, nextChar, buffer.toString());
							outsideAnExpression = false;
							String word = buffer.toString();
							if (! "".equals(word)) {
								change_word(word, file, line, column, currentChar, true);
							}
							buffer = new StringWriter();
							break;
						} else {
							if (nextChar == '~' && currentChar == '~') {
								buffer.write(currentChar);
//								debug("%c %c => %s", currentChar, nextChar, buffer.toString());
								currentChar = sr.read();
								continue;
							} else {
//								debug("%c %c => %s", currentChar, nextChar, buffer.toString());
								outsideAnExpression = false;
								String word = buffer.toString();
								if (! "".equals(word)) {
									change_word(word, file, line, column, currentChar, true);
								}
								buffer = new StringWriter();
								currentChar = nextChar;
								continue;
							}
						}
					}
				} else {
					if (chars('<', '>', '[', ']', ',', ':', '~').contains(currentChar)) {
//						debug("# %c => %s", currentChar, buffer.toString());
						String word = buffer.toString();
						if (! "".equals(word)) {
							change_word(word, file, line, column, currentChar, outsideAnExpression);
						}
						buffer = new StringWriter();
						if (currentChar == ':') {
							push("#func");
						} else if (currentChar == '<') {
							push("#<");
						} else if (currentChar == '>') {
							push("#>");
							eval();
						}
					} else {
						buffer.write(currentChar);
					}
					if (currentChar == '~') {
						outsideAnExpression = true;
					}
				}
				currentChar = sr.read(); 
			}
			String word = buffer.toString();
			if (! "".equals(word)) {
				change_word(word, file, line, column, currentChar, outsideAnExpression);
				buffer = new StringWriter();
			}
		} finally {
			sr.close();
		}
	}

	private void eval() throws TemplateException {
		if (depth()>1) {
			Object last = value();
			if (last.equals("#func")) {
				// var 'func #func
				rot(); // 'func #func var
				tolist(1); // 'func #func (var)
				unrot(); // (var) 'func #func
				tolist(3); // ( (var) 'func #func )
			} else if (last.equals("#>")) {
				drop();
				int i=1;
				while (i<=depth() && ! value(i).equals("#<")) {
					i++;
				}
				tolist(i-1);
				nip(); 
				swap();
				push("#func");
				tolist(3);
				swap();
				eval();
			}
		}
	}

	private void change_word(String word, String file, int line, int column, int currentChar, boolean outsideAnExpression) throws TemplateException {
		if (outsideAnExpression) {
			push(word);
		} else {
			if (currentChar != '<' && currentChar != '>' && depth() > 0 && value().equals("#func")) {
				push_word(word, file, line, column);
				swap(); // [ word pos #eval ] #func 
			} else {
				push_word(word, file, line, column);
			}
			eval();
		}
	}

	private void push_word(String word, String file, int line, int column) throws TemplateException {
		push(new RpnWord(word, file, line, column-word.length()-1));
	}
	
	private static boolean isRequired(String varname) {
		return varname != null && varname.startsWith("'");
	}
	
	private static Object writeObject(Writer out, Map<String, Transform> functions, Map<String, Object> model, Object value) throws IOException, TemplateException {
		
		if (value instanceof String)
			return value;

		if (value instanceof RpnWord) {
			return ((RpnWord) value).writeObject(model);
		}
		
		Stack stk = new Stack((List) value);
		String operation = (String) stk.pop();
		System.out.println("writeObject>> " + operation);
		if ("#func".equals(operation)) {
			
			RpnWord word = (RpnWord) stk.pop();

			Transform fp = functions.get(word.writeObject(model));
			if (fp == null && isRequired(word.word)) {
				throw new TemplateException("No transform function named '%s' is associated with the template for rendering at position '%s'.", word.word, word.pos);
			} else if (fp == null) {
				return null;
			}
			List parameters = new ArrayList();
			for (Object parameter : (List) stk.pop()) {
				parameters.add(writeObject(out, functions, model, parameter));
			}
//			if (key.startsWith("'")) {
//				Object o = getInModel(model, key);
//				if (o != null) {
//					out.write(o.toString());
//				}
//			} else {
//				throw new TemplateException("Unsupported case #eval for '%s'", key);
//			}
			return fp.apply(parameters.get(0));
		} else {
			throw new TemplateException("Unsupported operation '%s'", operation);
		}
	}
	
	public void write(Writer out, Map<String, Object> model) throws IOException, TemplateException {
		printStack(System.out);
		
		for (int i=depth(); i>0; i--) {
			Object o = writeObject(out, functions, model, value(i));
			if (o != null) {
				out.write(o.toString());
				System.out.println(i+">> " + o.toString());
			}
		}
	}

	public void addFunction(String name, Transform function) {
		functions.put(name, function);		
	}

}
