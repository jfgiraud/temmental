package temmentalr;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RpnStack extends Stack {
	
	private static final boolean debug = true;

	private Map<String, Transform> functions;

	public RpnStack() {
		this(new ArrayList());
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
		boolean sentence = false;
		try {
			int previousChar = 0;
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
						debug("%c %c => %s", currentChar, '#', buffer.toString());
					} else {
						int nextChar = sr.read();
						if (nextChar == -1) {
							debug("%c %c => %s", currentChar, nextChar, buffer.toString());
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
								debug("%c %c => %s", currentChar, nextChar, buffer.toString());
								previousChar = currentChar;
								currentChar = sr.read();
								continue;
							} else {
								debug("%c %c => %s", currentChar, nextChar, buffer.toString());
								outsideAnExpression = false;
								String word = buffer.toString();
								if (! "".equals(word)) {
									change_word(word, file, line, column, currentChar, true);
								}
								buffer = new StringWriter();
								previousChar = currentChar;
								currentChar = nextChar;
								continue;
							}
						}
					}
				} else {
					if (chars('"').contains(currentChar) || sentence) {
						buffer.write(currentChar);
						debug("# %c => %s 3", currentChar, buffer.toString());
						if (currentChar == '"' && previousChar != '\\') {
//							sentence = ! sentence;
							if (sentence) {
								sentence = false;
								String word = buffer.toString();
								if (! "".equals(word)) {
									change_word(word, file, line, column, currentChar, outsideAnExpression);
								}
								buffer = new StringWriter();
							} else {
								sentence = true;
							}
						}
						previousChar = currentChar;
						currentChar = sr.read(); 
						continue;
					} else if (chars('<', '>', '[', ']', ',', ':', '~').contains(currentChar)) {
						debug("# %c => %s", currentChar, buffer.toString());
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
				previousChar = currentChar;
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
				drop(); // var 'func 
				RpnElem func = (RpnElem) pop(); // var 
				tolist(1); // [ var ]
				List parameters = (List) pop();
				push(new RpnFunc(func, parameters));
			} else if (last.equals("#>")) {
				drop();
				int i=1;
				while (i<=depth() && ! value(i).equals("#<")) {
					i++;
				}
				tolist(i-1);
				List parameters = (List) pop();
				drop();
				RpnElem func = (RpnElem) pop();
				
				push(new RpnFunc(func, parameters));
				
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
		if (word.startsWith("\"") && word.endsWith("\"")) {
			push(word.substring(1, word.length()-1));
		} else {
			push(new RpnWord(word, file, line, column-word.length()-1));
		}
	}
	
	private static Object writeObject(Writer out, Map<String, Transform> functions, Map<String, Object> model, Object value) throws IOException, TemplateException {
		
		if (value instanceof String)
			return value;

		if (value instanceof RpnWord) {
			return ((RpnWord) value).writeObject(functions, model);
		}
		
		if (value instanceof RpnFunc) {
			return ((RpnFunc) value).writeObject(functions, model);
		}
		
		throw new TemplateException("Unsupported operation for class '%s'", value.getClass().getName());
	}
	
	public void write(Writer out, Map<String, Object> model) throws IOException, TemplateException {
		printStack(System.out);
		
		for (int i=depth(); i>0; i--) {
			Object o = writeObject(out, functions, model, value(i));
			if (o != null) {
				out.write(o.toString());
			}
		}
	}

	public void addFunction(String name, Transform function) {
		functions.put(name, function);		
	}
	
//	public void addFunction(final String name, final Method method) {
//		functions.put(name, new Transform<Object,Object>() {
//			@Override
//			public Object apply(Object value) {
//				try {
//					return method.invoke(value);
//				} catch (Exception e) {
//					throw new RuntimeException("Unable to apply '" + name + "' function. ", e);
//				}
//			}
//		});		
//	}

}
