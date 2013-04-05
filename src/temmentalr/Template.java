package temmentalr;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO mettre un stack.empty() ??

public class Template extends Stack {
	
	private static final boolean debug = true;

	private Map<String, Transform> functions;
	private TemplateMessages messages;
	
	public Template(TemplateMessages messages) {
		this(new ArrayList());
		this.messages = messages;
	}

	public Template(List<Object> tocopy) {
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
					} else {
						int nextChar = sr.read();
						if (nextChar == -1) {
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
								previousChar = currentChar;
								currentChar = sr.read();
								continue;
							} else {
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
						if (currentChar == '"' && previousChar != '\\') {
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
					} else if (chars('<', '>', '[', ']', '(', ')', ',', ':', '~').contains(currentChar)) {
						String word = buffer.toString();
						if (! "".equals(word)) {
							change_word(word, file, line, column, currentChar, outsideAnExpression);
						}
						buffer = new StringWriter();
						if (currentChar == ':') {
							push("#func");
						} else if (currentChar == '<') {
							push(new Bracket('<', file, line, column));
						} else if (currentChar == '>') {
							push(new Bracket('>', file, line, column));
							eval();
						} else if (currentChar == '[') {
							push(new Bracket('[', file, line, column));
						} else if (currentChar == ']') {
							push(new Bracket(']', file, line, column));
							eval();
						}  else if (currentChar == '(') {
							push(new Bracket('(', file, line, column));
						} else if (currentChar == ')') {
							push(new Bracket(')', file, line, column));
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
		
		check_stack();
	}

	private void check_stack() throws TemplateException {
		if (depth()>1) {
			for (int i=1; i<=depth(); i++) {
				if (value(i) instanceof Bracket) {
					Bracket b = (Bracket) value(i);
					if (b.isClosed())
						throw new TemplateException("Bracket not opened ('%c' at position '%s')", b.getBracket(), b.getPosition());
					else 
						throw new TemplateException("Bracket not closed ('%c' at position '%s')", b.getBracket(), b.getPosition());
				}
			}
		}
	}

	private void eval() throws TemplateException {
		if (depth()>1) {
			Object last = value();
			if (last.equals("#func")) {
				// var 'func #func
				drop(); // var 'func 
				Element func = (Element) pop(); // var 
				tolist(1); // [ var ]
				List parameters = (List) pop();
				push(new Function(func, parameters));
			} else if (last instanceof Bracket) {
				Bracket bracket = (Bracket) value();
				if (bracket.getBracket() == '>') {
					// $text #func $funcname #< $p1 $p2 #>
					create_list('<', '>'); // $text #func $funcname [$p1, $p2]
					List parameters = (List) pop(); // $text #func $funcname 
					Element func = (Element) pop(); // $text #func 
					push(new Function(func, parameters)); // $text #func RpnFunc
					swap(); // $text RpnFunc #func 
					eval();
				} else if (bracket.getBracket() == ']') {
					create_list('[', ']');
					List parameters = (List) pop();  
					if (! (value() instanceof Identifier)) {
						throw new TemplateException("Bad bracket type. Should be <> but is [] at position '%s'", bracket.getPosition());
					}
					Identifier word = (Identifier) pop();  
					push(new Message(word, parameters)); // $text #func RpnFunc
				} else if (bracket.getBracket() == ')') {
					create_list('(', ')');
					List parameters = (List) pop();
					push(new Array(parameters));
				}
			}
		}
	}

	/*
	 * http://www.donghuna.com/247
	 */
	
	private void create_list(char start, char end) throws TemplateException {
		Bracket closeBracket = (Bracket) pop();
		int i;
		for (i=1; i<=depth(); i++) {
			if (value(i) instanceof Bracket) {
				Bracket bracket = (Bracket)value(i);
				if (bracket.isOpened() && bracket.getBracket() != closeBracket.other()) {
					throw new TemplateException("Bracket mismatch ('%c' at position '%s' vs '%c' at position '%s')", 
							bracket.getBracket(), bracket.getPosition(), closeBracket.getBracket(), closeBracket.getPosition());
				}
				break;
			}
		}
		try {
			tolist(i-1);
			nip();
		} catch (StackException e) {
			throw new TemplateException(e, "Bracket '%c' not opened for the closing bracket '%c' '%s')", 
					closeBracket.other(), closeBracket.getBracket(), closeBracket.getPosition());
		}
			
//		try {
//			int i=1;
//			while (i<=depth() && ! (value(i) instanceof Bracket && ((Bracket)value(i)).getBracket() == start)) {
//				i++;
//			}
//			Bracket openBracket = (Bracket)value(i);
//			tolist(i-1);
//			nip();
//		} catch (StackException e) {
//			throw new TemplateException(e, "Bracket '%c' not opened at position '%s'.", closeBracket.other(), closeBracket.getPosition());
//		}
//		Bracket closeBracket = (Bracket) pop();  
//		try {
//			int i=1;
//			while (i<=depth() && ! (value(i) instanceof Bracket && ((Bracket)value(i)).getBracket() == start)) {
//				i++;
//			}
//			Bracket openBracket = (Bracket)value(i);
//			tolist(i-1);
//			nip();
//		} catch (StackException e) {
//			throw new TemplateException(e, "Bracket '%c' not opened at position '%s'.", closeBracket.other(), closeBracket.getPosition());
//		}
	}
	
	private void change_word(String word, String file, int line, int column, int currentChar, boolean outsideAnExpression) throws TemplateException {
		
		if (outsideAnExpression) {
			push(word);
		} else {
			if (currentChar != '<' /*&& currentChar != '>'*/ && depth() > 0 && value().equals("#func")) {
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
			push(new Identifier(word, file, line, column-word.length()-1));
		}
	}
	
	private static Object writeObject(Writer out, Map<String, Transform> functions, Map<String, Object> model, TemplateMessages messages, Object value) throws IOException, TemplateException {
		
		if (value instanceof String)
			return value;

		if (value instanceof Identifier) {
			return ((Identifier) value).writeObject(functions, model, messages);
		}
		
		if (value instanceof Function) {
			Function function = ((Function) value); 
			Object result = function.writeObject(functions, model, messages);
			if (result != null && result instanceof Transform) {
				throw new TemplateException("Unable to apply function '%s' at position '-:l1:c8'. This function expects one or more parameters. It receives no parameter.",	function.getIdentifier(), function.getPosition());
			} 
			return result;
		}
		
		if (value instanceof Message) {
			return ((Message) value).writeObject(functions, model, messages);
		}
		
		throw new TemplateException("Unsupported operation for class '%s'", value.getClass().getName());
	}
	
	public void write(Writer out, Map<String, Object> model) throws IOException, TemplateException {
		printStack(System.out);
		
		for (int i=depth(); i>0; i--) {
			Object o = writeObject(out, functions, model, messages, value(i));
			if (o != null) {
				out.write(o.toString());
			}
		}
	}

	public void addFunction(String name, Transform function) {
		functions.put(name, function);		
	}

	public void addFunction(final String name, final Method method) {
		if (method.getParameterTypes().length>1) {
			addFunction(name, new Transform<Object[], Transform>() {
				@Override
				public Transform apply(final Object[] values) {
					return new Transform<Object, Object>() {
						@Override
						public Object apply(Object text) throws TemplateException {
							try {
								return method.invoke(text, values);
							} catch (Exception e) {
								if (! ((Class<?>)method.getDeclaringClass()).isAssignableFrom(((Class<?>)text.getClass()))) {
									throw new TemplateException(e, "This function expects %s. It receives %s.", 
											method.getDeclaringClass().getCanonicalName(),  
											text.getClass().getCanonicalName()); 
								} else {
									if (values.length != method.getParameterTypes().length) {
										throw new TemplateException(e, "This function expects %d parameters. It receives %d parameters.", 
												method.getParameterTypes().length,  
												values.length);
									} else {
										for (int i=0; i<values.length; i++) {
											if (! ((Class<?>)method.getParameterTypes()[i]).isAssignableFrom(((Class<?>)values[i].getClass()))) {
												throw new TemplateException(e, "This function expects %s for parameter #%d. It receives %s.", 
														method.getParameterTypes()[i].getCanonicalName(),  
														i+1, 
														values[i].getClass().getCanonicalName()); 
											}			
										}
										throw new TemplateException("Unable to determine reason.");
									}
								}
							}
						}
					};
				}
			});
		} else if (method.getParameterTypes().length == 0) {
			addFunction(name, new Transform<Object, Object>() {
				@Override
				public Object apply(Object value) throws TemplateException {
					try {
						return method.invoke(value);
					} catch (Exception e) {
						throw new TemplateException(e, "This function expects %s. It receives %s.", 
								method.getDeclaringClass().getCanonicalName(),  
								value.getClass().getCanonicalName());  
					}
				}
			});
		} else { // = 1
			addFunction(name, new Transform<Object, Transform>() {
				@Override
				public Transform apply(final Object value) {
					return new Transform<Object, Object>() {
						@Override
						public Object apply(Object text) throws TemplateException {
							try {
								return method.invoke(text, value);
							} catch (Exception e) {
								if (! ((Class<?>)method.getDeclaringClass()).isAssignableFrom(((Class<?>)text.getClass()))) {
									throw new TemplateException(e, "This function expects %s. It receives %s.", 
											method.getDeclaringClass().getCanonicalName(),  
											text.getClass().getCanonicalName()); 
								} else {
									if (  value.getClass().isArray() ) {
										int n = ((Object[])value).length;
										if (n > 0)
											throw new TemplateException(e, "This function expects only one parameter. It receives %d parameters.", n);
										else 
											throw new TemplateException(e, "This function expects one parameter. It receives no parameter.");
									} else {
										throw new TemplateException(e, "This function expects %s for parameter #1. It receives %s.", 
												method.getParameterTypes()[0].getCanonicalName(),  
												value.getClass().getCanonicalName());
									}
								}
							}
						}
					};
				}
			});
		}
	}

}
