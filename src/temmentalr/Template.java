package temmentalr;

import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO mettre un stack.empty() ??

public class Template {
	
	private static final boolean debug = true;

	private Map<String, Transform> functions;
	private TemplateMessages messages;
	private Stack stack;
	
	public Template(TemplateMessages messages) {
		this.stack = new Stack();
		this.messages = messages;
		this.functions = new HashMap<String, Transform>();
	}

	private static final List<Integer> chars(int ... chars) {
		ArrayList<Integer> result = new ArrayList<Integer>();
		for (int c : chars) {
			result.add(c);
		}
		return result;
	}
	
	void parse(String expression, String file, int line, int column) throws IOException, TemplateException {
		parse(stack, expression, file, line, column);
	}
	
	private static void parse(Stack stack, String expression, String file, int line, int column) throws IOException, TemplateException {
		StringReader sr = new StringReader(expression);
		StringWriter buffer = new StringWriter();
		boolean outsideAnExpression = true;
		boolean sentence = false;
		boolean calc = false;
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
							column = 1;
						} 
					} else {
						int nextChar = sr.read();
						if (nextChar == -1) {
							outsideAnExpression = false;
							String word = buffer.toString();
							if (! "".equals(word)) {
								change_word(stack, word, file, line, column, currentChar, true);
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
									change_word(stack, word, file, line, column, currentChar, true);
								}
								buffer = new StringWriter();
								previousChar = currentChar;
								currentChar = nextChar;
								continue;
							}
						}
					}
				} else {
					if (currentChar == '"' || sentence) {
						if (currentChar == '\\') {
							previousChar = currentChar;
							currentChar = sr.read();
							if (currentChar == '"') {
								buffer.write(currentChar);
								previousChar = currentChar;
								currentChar = sr.read();
								continue;
							} else {
								buffer.write(previousChar);
							}
						}
						buffer.write(currentChar);
						if (currentChar == '"') {
							if (sentence) {
								sentence = false;
								String word = buffer.toString();
								if (! "".equals(word)) {
									push_word(stack, word, file, line, column);
								}
								buffer = new StringWriter();
							} else {
								sentence = true;
							}
						}
						previousChar = currentChar;
						currentChar = sr.read(); 
						continue;
					} else if (chars('{', '}').contains(currentChar) || calc) {
						if (currentChar == '{') {
							calc = true;
							stack.push(new Bracket('{', file, line, column));
						} else if (currentChar == '}') {
							calc = false;
							String word = buffer.toString();
							if (! "".equals(word)) {
								if (Calc.OPERATORS.contains(word)) {
									stack.push(word);
								} else {
									Stack subStack = new Stack();
									Template.parse(subStack , "~" + word + "~", file, line, column-word.length()-2);
									stack.push(subStack.pop());
								}
							}
							buffer = new StringWriter();
							stack.push(new Bracket('}', file, line, column));
							eval(stack);
						} else if (currentChar == ' ') {
							String word = buffer.toString();
							if (! "".equals(word)) {
								if (Calc.OPERATORS.contains(word)) {
									stack.push(word);
								} else {
									Stack subStack = new Stack();
									Template.parse(subStack , "~" + word + "~", file, line, column-word.length()-2);
									stack.push(subStack.pop());
								}
							}
							buffer = new StringWriter();
						} else {
							buffer.write(currentChar);
						}
					} else if (chars('<', '>', '[', ']', '(', ')', ',', ':', '~').contains(currentChar)) {
						String word = buffer.toString();
						if (! "".equals(word)) {
							change_word(stack, word, file, line, column, currentChar, outsideAnExpression);
						}
						buffer = new StringWriter();
						if (currentChar == ':') {
							stack.push("#func");
						} else if (currentChar == '<') {
							stack.push(new Bracket('<', file, line, column));
						} else if (currentChar == '>') {
							stack.push(new Bracket('>', file, line, column));
							eval(stack);
						} else if (currentChar == '[') {
							if (! stack.empty() && ! (stack.value() instanceof Identifier)) {
								throw new TemplateException("Invalid bracket type '[' at position '%s'.", String.format("%s:l%d:c%d", file, line, column));
							}
							stack.push(new Bracket('[', file, line, column));
						} else if (currentChar == ']') {
							stack.push(new Bracket(']', file, line, column));
							eval(stack);
						}  else if (currentChar == '(') {
							if (! stack.empty() && ! (stack.value() instanceof String)) {
								throw new TemplateException("Invalid bracket type '(' at position '%s'.", String.format("%s:l%d:c%d", file, line, column));
							}
							stack.push(new Bracket('(', file, line, column));
						} else if (currentChar == ')') {
							stack.push(new Bracket(')', file, line, column));
							eval(stack);
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
				change_word(stack, word, file, line, column, currentChar, outsideAnExpression);
				buffer = new StringWriter();
			}
		} finally {
			sr.close();
		}
		
		check_stack(stack);
	}

	@Override
	public String toString() {
		return stack.toString();
	}
	
	private static void check_stack(Stack stack) throws TemplateException {
		if (stack.depth()>1) {
			for (int i=1; i<=stack.depth(); i++) {
				Object o = stack.value(i);
				if (o instanceof Bracket) {
					Bracket b = (Bracket) o;
					if (b.isClosed())
						throw new TemplateException("Bracket not opened ('%c' at position '%s').", b.getBracket(), b.getPosition());
					else 
						throw new TemplateException("Bracket not closed ('%c' at position '%s').", b.getBracket(), b.getPosition());
				} 
			}
		}
	}

	private static void eval(Stack stack) throws TemplateException {
		if (stack.depth()>1) {
			Object last = stack.value();
			if (last.equals("#func")) {
				// var 'func #func
				stack.drop(); // var 'func 
				Element func = (Element) stack.pop(); // var 
				stack.tolist(1); // [ var ]
				List parameters = (List) stack.pop();
				stack.push(new Function(func, parameters));
			} else if (last instanceof Bracket) {
				Bracket bracket = (Bracket) stack.value();
				if (bracket.getBracket() == '>') {
					// $text #func $funcname #< $p1 $p2 #>
					create_list(stack, '<', '>'); // $text #func $funcname [$p1, $p2]
					List parameters = (List) stack.pop(); // $text #func $funcname 
					Element func = (Element) stack.pop(); // $text #func 
					stack.push(new Function(func, parameters)); // $text #func RpnFunc
					stack.swap(); // $text RpnFunc #func 
					eval(stack);
				} else if (bracket.getBracket() == ']') {
					// $text #[ $p1 $p2 #]					
					create_list(stack, '[', ']'); // $text [$p1, $p2]
					List parameters = (List) stack.pop();  // $text 
//					if (! (value() instanceof Identifier)) {
//						throw new TemplateException("Invalid bracket type '[' at position '%s'.", bracket.getPosition());
//					}
					Identifier word = (Identifier) stack.pop();  
					stack.push(new Message(word, parameters)); // Message($text, [$p1, $p2])
				} else if (bracket.getBracket() == ')') {
					Bracket startAt = (Bracket) create_list(stack, '(', ')');
					List parameters = (List) stack.pop();
					stack.push(new Array(startAt, parameters));
				} else if (bracket.getBracket() == '}') {
					Bracket startAt = (Bracket) create_list(stack, '{', '}');
					List parameters = (List) stack.pop();
					stack.push(new Calc(startAt, parameters));
				}
			}
		}
	}

	

	

	/*
	 * http://www.donghuna.com/247
	 */
	
	
	private static Object create_list(Stack stack, char start, char end) throws TemplateException {
		Bracket closeBracket = (Bracket) stack.pop();
		int i;
		for (i=1; i<=stack.depth(); i++) {
			if (stack.value(i) instanceof Bracket) {
				Bracket bracket = (Bracket)stack.value(i);
				if (bracket.isOpened() && bracket.getBracket() != closeBracket.other()) {
					throw new TemplateException("Bracket mismatch ('%c' at position '%s' vs '%c' at position '%s').", 
							bracket.getBracket(), bracket.getPosition(), closeBracket.getBracket(), closeBracket.getPosition());
				}
				break;
			}
		}
		try {
			stack.tolist(i-1);
			stack.over();
			Object o = stack.pop();
			stack.nip();
			return o;
		} catch (StackException e) {
			throw new TemplateException(e, "Bracket '%c' not opened for the closing bracket '%c' '%s').", 
					closeBracket.other(), closeBracket.getBracket(), closeBracket.getPosition());
		}
	}
	
	private static void change_word(Stack stack, String word, String file, int line, int column, int currentChar, boolean outsideAnExpression) throws TemplateException {
		
		if (outsideAnExpression) {
			stack.push(word);
		} else {
			if (currentChar != '<' /*&& currentChar != '>'*/ && stack.depth() > 0 && stack.value().equals("#func")) {
				push_word(stack, word, file, line, column);
				stack.swap(); // [ word pos #eval ] #func 
			} else {
				push_word(stack, word, file, line, column);
			}
			eval(stack);
		}
	}

	private static void push_word(Stack stack, String word, String file, int line, int column) throws TemplateException {
		if (word.startsWith("\"") && word.endsWith("\"")) {
			stack.push(word.substring(1, word.length()-1));
		} else if (word.matches("(-)?\\d+")) {
			stack.push(Integer.parseInt(word));
		} else {
			stack.push(new Identifier(word, file, line, column-word.length()));
		}
	}
	
	private static Object writeObject(Writer out, Map<String, Transform> functions, Map<String, Object> model, TemplateMessages messages, Object value) throws IOException, TemplateException {
		
		if (value instanceof String || value instanceof Number)
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
		
		if (value instanceof Calc) {
			return ((Calc) value).writeObject(functions, model, messages);
		}
		
		throw new TemplateException("Unsupported operation for class '%s'", value.getClass().getName());
	}
	
	public void write(Writer out, Map<String, Object> model) throws IOException, TemplateException {
		for (int i=stack.depth(); i>0; i--) {
			Object o = writeObject(out, functions, model, messages, stack.value(i));
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

	void clear() {
		stack.clear();
	}

	void printStack(PrintStream out) throws IOException {
		stack.printStack(out);
	}

}
