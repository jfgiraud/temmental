package temmentalr;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import temmental2.Node;

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
							break;
						} else {
							if (nextChar == '~' && currentChar == '~') {
								buffer.write(currentChar);
								debug("%c %c => %s", currentChar, nextChar, buffer.toString());
								currentChar = sr.read();
								continue;
							} else {
								outsideAnExpression = false;
								debug("%c %c => %s", currentChar, nextChar, buffer.toString());
								currentChar = nextChar;
								continue;
							}
						}
					}
				} else {
					if (chars('<', '>', '[', ']', ',', ':', '~').contains(currentChar)) {
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
				}
				currentChar = sr.read(); 
			}
			String word = buffer.toString();
			if (! "".equals(word)) {
				change_word(word, file, line, column, currentChar, outsideAnExpression);
			}
		} finally {
			sr.close();
		}
	}

	private void eval() throws TemplateException {
		debug(toString());
		if (depth()>1) {
			Object last = value();
			if (last.equals("#func")) {
				debug("#### func beg");
				debug(stackToString());
				// var 'func #func
				rot(); // 'func #func var
				tolist(1); // 'func #func (var)
				unrot(); // (var) 'func #func
				
//				debug(">>> value => " + value(2));
//				debug(">>> seemsToBeEval => " + seemsToBeEval(value(2)));
//				if (seemsToBeEval(value(2))) {
//					swap(); // (var) #func 'func
//					push("#eval"); // (var) #func 'func #eval
//					tolist(2); // (var) #func ('func #eval)
//					swap(); // (var) ('func #eval)  #func
//				}
//				debug(toString());
				over(); // (var) 'func #func 'func
				assertFunction();
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
		debug(toString());
	}

	private void assertFunction() throws TemplateException {
		debug("#### assertFunction");
		dup();
		get(1);
		Object o = pop();
		debug("# assert " + o.toString());
		get(2);
		Object pos = pop();
		debug(stackToString());
		if (o instanceof String) {
			String s = (String) o;
			if (! s.startsWith("'") && ! s.startsWith("$")) {
				throw new TemplateException("zzz" + pos); 
			}
		} 
	}

	private void push_pos(String file, int line, int column) {
		push(String.format("%s:l%d:c%d", file, line, column));
		push("#pos");
		tolist(2);
	}
	
	private void change_word(String word, String file, int line, int column, int currentChar, boolean outsideAnExpression) throws TemplateException {
		debug("Word is %s", word);
		if (outsideAnExpression) {
			push(word);
			push("#text");
			tolist(2);
		} else {
			if (currentChar != '<' && currentChar != '>' && depth() > 0 && value().equals("#func")) {
				push(word);
				push_pos(file, line, column-word.length()-1);
				push("#eval");
				tolist(3);
				swap();
				eval();
			} else {
				push(word);
				push_pos(file, line, column-word.length()-1);
				push("#eval");
				tolist(3);
			}
			if (currentChar == '>') {
				eval();
			}
		}
	}

	private static void writeObject(Writer out, Map<String, Object> model, Object value) throws IOException, TemplateException {
		Stack stk = new Stack((List) value);
		String operation = (String) stk.pop();
		if ("#text".equals(operation)) {
			out.write((String) stk.pop());
		} else if ("#eval".equals(operation)) {
			stk.drop();
			String v = (String) stk.pop();
			if (v.startsWith("$")) {
				v = v.substring(1);
				if (! model.containsKey(v)) {
					throw new TemplateException("Key '%s' is not present or has null value in the model map.", v);
				} else {
					out.write((String) model.get(v));
				}
			} else {
				throw new TemplateException("Unsupported case #eval for '%s'", v);
			}
		}
	}
	
	public void write(Writer out, Map<String, Object> model) throws IOException, TemplateException {
		int n = depth();
		try {
			dupn(n);
			for (int i=0; i<n; i++) {
				writeObject(out, model, pop());
			}
		} finally {
			int np = depth();
			while (np > n) {
				drop();
			}
		}
	}

}
