package temmentalr;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

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
	
	public void parse(String expression, String file, int line, int column) throws IOException {
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
							change_word(word, currentChar, outsideAnExpression);
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
				change_word(word, currentChar, outsideAnExpression);
			}
		} finally {
			sr.close();
		}
	}

	
	
	private void eval() {
		if (debug)
			System.err.println(toString());
		if (depth()>1) {
			Object last = value();
			if (last.equals("#func")) {
				rot(); // var 'func #func
				tolist(1); // 'func #func (var)
				rolld(3); // (var) 'func #func
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
		if (debug)
			System.err.println(toString());
	}

	private void assertFunction() {
		Object o = value();
		drop();
		System.out.println(o);
		if (o instanceof String) {
			String s = (String) o;
			if (! s.startsWith("'") && ! s.startsWith("$")) {
				throw new RuntimeException(String.format("Invalid function name '%s'", s));
			}
		}
		// TODO Auto-generated method stub
	}

	private void change_word(String word, int currentChar, boolean outsideAnExpression) {
		if (outsideAnExpression) {
			push(word);
			push("#text");
			tolist(2);
		} else {
			if (currentChar != '<' && currentChar != '>' && depth() > 0 && value().equals("#func")) {
				push(word);
				swap();
				eval();
			} else {
				push(word);
			}
			if (currentChar == '>') {
				eval();
			}
		}
	}

}
