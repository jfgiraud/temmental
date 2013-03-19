package temmentalr;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class RpnStack extends Stack {
	
	private static final boolean debug = true;
	
	private List<Integer> chars(int ... chars) {
		ArrayList<Integer> result = new ArrayList<Integer>();
		for (int c : chars) {
			result.add(c);
		}
		return result;
	}
	
	public void parse(String expression) throws IOException {
		StringReader sr = new StringReader(expression);
		StringWriter buffer = new StringWriter();
		try {
			int currentChar = sr.read(); 
			while (currentChar != -1) {
				if (chars('<', '>', '[', ']', ',', ':', '~').contains(currentChar)) {
					String word = buffer.toString();
					if (! "".equals(word)) {
						change_word(word, currentChar);
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
				currentChar = sr.read(); 
			}
			String word = buffer.toString();
			if (! "".equals(word)) {
				change_word(word, currentChar);
			}
		} finally {
			sr.close();
		}
	}

	
	
	private void eval() {
		if (debug)
			System.err.println(toString());
		if (depth()>1) {
			Object last = last();
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
				while (i<=depth() && ! pick(i).equals("#<")) {
					i++;
				}
				tolist(i-1);
				remove(2);
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
		Object o = drop();
		System.out.println(o);
		if (o instanceof String) {
			String s = (String) o;
			if (! s.startsWith("'") && ! s.startsWith("$")) {
				throw new RuntimeException(String.format("Invalid function name '%s'", s));
			}
		}
		// TODO Auto-generated method stub
	}

	private void change_word(String word, int currentChar) {
		if (currentChar != '<' && currentChar != '>' && depth() > 0 && last().equals("#func")) {
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
