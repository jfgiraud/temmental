package temmentalr;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RpnStack extends Stack {
	
	private static final boolean debug = true;
	
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
								currentChar = nextChar;
								continue;
							}
						}
					}
				} else {
					if (chars('<', '>', '[', ']', ',', ':', '~').contains(currentChar)) {
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
				nip(); //remove(2);
				swap();
				push("#func");
				tolist(3);
				swap();
				eval();
			}
		}
	}

	private void assertIdentifier(String pos, String word) throws TemplateException {
		if (! word.startsWith("'") && ! word.startsWith("$")) {
			throw new TemplateException("Invalid identifier syntax for '%s' at '%s'.", word, pos); 
		}
	}

	private void change_word(String word, String file, int line, int column, int currentChar, boolean outsideAnExpression) throws TemplateException {
		if (outsideAnExpression) {
			push(word);
			push("#text");
			tolist(2);
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
		String pos = String.format("%s:l%d:c%d", file, line, column-word.length()-1);
		assertIdentifier(pos, word);
		push(word); // #func newword
		push(pos);
		push("#pos");
		tolist(2); // #func newword pos
		push("#eval"); // #func newword pos #eval
		tolist(3); // #func [ newword pos #eval ]
	}

	private static void writeObject(Writer out, Map<String, Object> model, Object value) throws IOException, TemplateException {
		Stack stk = new Stack((List) value);
		String operation = (String) stk.pop();
		if ("#text".equals(operation)) {
			out.write((String) stk.pop());
		} else if ("#eval".equals(operation)) {
			stk.drop();
			String key = (String) stk.pop();
			if (key.startsWith("$")) {
				key = key.substring(1);
				boolean optional = (key.charAt(key.length()-1) == '?');
				if (optional)
					key = key.substring(0, key.length()-1);
				System.out.println(optional);
				System.out.println(key);
				Object o = getInModel(model, key, optional);
				if (o != null) {
					out.write(o.toString());
				}
			} else {
				throw new TemplateException("Unsupported case #eval for '%s'", key);
			}
		}
	}
	
	
	private static Object getInModel(Map<String, Object> model, String varname, boolean optional) throws TemplateException {
		if (optional) {
			if (model.containsKey(varname)) {
				return model.get(varname);
			} else {
				return model.get(varname);
			}
		} else {
			if (! model.containsKey(varname)) {
				throw new TemplateException("Key '%s' is not present or has null value in the model map.", varname);
			} else {
				return model.get(varname);
			}
		}
	}
	
	public void write(Writer out, Map<String, Object> model) throws IOException, TemplateException {
		for (int i=depth(); i>0; i--) {
			writeObject(out, model, value(i));
		}
	}

}
