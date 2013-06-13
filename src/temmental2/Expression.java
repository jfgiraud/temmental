package temmental2;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

class Expression {

	private String expr;
	private Cursor cursor;
	
	Expression(String expr, Cursor cursor) {
		this.expr = expr;
		this.cursor = cursor.clone();
	}
	
	public Object parse() throws IOException, TemplateException {
		Stack tokens = parseToTokens();
		tokens.reverse();
		return interpretTokens(tokens);
	}
	
	@Override
	public String toString() {
		return "@" + cursor.getPosition() + "\tExpression(" + expr + ")";
	}

	public boolean equals(Object o) {
		if (o == null || ! (o instanceof Expression))
			return false;
		Expression oc = (Expression) o;
		return oc.expr.equals(expr) && oc.cursor.equals(cursor);
	}

	// ====== static methods ======================================================================================
	
	Object interpretTokens(Stack tokens) throws TemplateException {
		Stack oldOut = new Stack();
		Stack oldCommas = new Stack();
		int commas = 0;
		Stack out = new Stack();
		while (tokens.depth()>=1) {
			Object token = tokens.pop();
			if (token instanceof Char || token instanceof Text || token instanceof Number || token instanceof Identifier) {
				out.push(token);
			} else if (token instanceof Bracket) {
				Bracket b = (Bracket) token;
				if (b.isOpening()) {
					oldOut.push(out);
					out = new Stack();
					out.push(token);
					oldCommas.push(commas);
					commas = 0;
				} else {
					Bracket other = (Bracket) out.value(out.depth());
					if (other.getBracket() != b.neg()) {
						throw new TemplateException("Corresponding bracket for '%c' at position '%s' is invalid (found '%c' at position '%s') .", b.getBracket(), b.getPosition(),
								other.getBracket(), other.getPosition());
					}
					out.remove(out.depth());
					if (commas != out.depth() -1) {
						if (b.getBracket() == '>') {
							throw new TemplateException("Empty init list parameter before '%c' at position '%s'.", b.getBracket(), b.getPosition());
						} else if (b.getBracket() != ']'){
							throw new TemplateException("Empty list parameter before '%c' at position '%s'.", b.getBracket(), b.getPosition());
						}
					}
					
					if (b.getBracket() == '>') {
						if (commas != out.depth() - 1) {
							throw new TemplateException("Too much commas!"); //TODO
						}
						out.tolist(out.depth());
						List initParameters = (List) out.pop();
						out = (Stack) oldOut.pop();
						Function func = (Function) out.pop();
						out.push(new Functionp(func, initParameters));
						commas = (Integer) oldCommas.pop();
					} else if (b.getBracket() == ']') { 
						if ((out.depth() != 0) && (commas != out.depth() - 1)) {
							throw new TemplateException("Too much commas!"); //TODO
						}
						out.tolist(out.depth());
						List msgParameters = (List) out.pop();
						out = (Stack) oldOut.pop();
						Identifier messageIdentifier = (Identifier) out.pop();
						out.push(new Message(messageIdentifier, msgParameters));
						commas = (Integer) oldCommas.pop();
					} else if (b.getBracket() == ')') {
						if (commas != out.depth() - 1) {
							throw new TemplateException("Too much commas!"); //TODO
						}
						out.tolist(out.depth());
						out.push(new Array((List<Object>) out.pop(), other.cursor));
						commas = (Integer) oldCommas.pop();
					} else {
						throw new TemplateException("Bracket %c not supported!", b.getBracket()); //TODO
					}
				}
			} else if (token instanceof ToApply) {
				Identifier filter = (Identifier) tokens.pop();
				Object input = out.pop();
				out.push(new Function(filter, input));
			} else if (token instanceof Comma) {
				commas += 1;
			} else {
				throw new TemplateException("Case " + token.getClass().getCanonicalName() + " not supported");
			}
		}
		
		
		if (out.depth() != 1) {
			throw new TemplateException("Too much objects in the stack!");
		}
		return out.pop();
	}	
	
	Stack parseToTokens() throws IOException, TemplateException {
		Stack stack = new Stack(); 
		
		String expression = expr;
		Cursor cursor = this.cursor.clone();
		
		boolean inSentence = false;
		if (! expression.startsWith("~")) {
			throw new TemplateException("Expression '%s' doesn't start with '~' character at position '%s'", expression, cursor.getPosition());
		}
		if (! expression.endsWith("~")) {
			throw new TemplateException("Expression '%s' doesn't end with '~' character at position '%s'", expression, cursor.getPosition());
		}

		expression = expression.substring(1);
		expression = expression.substring(0, expression.length()-1);
		cursor.move1r();

		//'~$hello[$firstname,$lastname:'upper:'quote<\"'\">]~'
		StringReader sr = new StringReader(expression);
		StringWriter buffer = new StringWriter();
		try {
			int currentChar = sr.read();
			while (currentChar != -1) {
				cursor.next(currentChar);
				if (! inSentence && currentChar == ',') {
					String expr = buffer.toString();
					if (! expr.equals("")) {
						stack.push(evalToken(expr, cursor.clone().move1l()));
					} else {
						behaviourOnEmptyToken(currentChar, stack, cursor);
					}
					buffer = new StringWriter();
					stack.push(new Comma(cursor.clone().move1l()));
				} else if (! inSentence && currentChar == ':') {
					String expr = buffer.toString();
					if (! expr.equals("")) {
						stack.push(evalToken(expr, cursor.clone().move1l()));
					} else {
						behaviourOnEmptyToken(currentChar, stack, cursor);
					}
					buffer = new StringWriter();
					stack.push(new ToApply(cursor.clone().move1l()));
				} else if (! inSentence && Bracket.isBracket(currentChar)) {
					String expr = buffer.toString();
					if (! expr.equals("")) {
						stack.push(evalToken(expr, cursor.clone().move1l()));
					} else {
						behaviourOnEmptyToken(currentChar, stack, cursor);
					}
					buffer = new StringWriter();

					stack.push(new Bracket((char) currentChar, cursor.clone().move1l()));
				} else {
					if (! buffer.toString().endsWith("\\") && currentChar == '"') {
						inSentence = ! inSentence;
					} else if (buffer.toString().endsWith("\\") && currentChar == '"') {
						String expr = buffer.toString();
						buffer = new StringWriter();
						buffer.append(expr.substring(0, expr.length()-1));
						cursor.move1l();
					}
					buffer.write(currentChar);
				}
				currentChar = sr.read(); 
			}
		} finally {
			sr.close();
		}

		String expr = buffer.toString();
		if (! expr.equals("")) {
			stack.push(evalToken(expr, cursor.clone()));
		} else {
			behaviourOnEmptyToken(0, stack, cursor);
		}
		
		return stack;
	}
	
	private static void behaviourOnEmptyToken(int currentChar, Stack stack, Cursor cursor) throws TemplateException {
		if (currentChar == ',') {
			throw new TemplateException("No parameter before ',' at position '%s'.", cursor.getPosition(-1));
		} else if (currentChar == ':') {
			if (stack.empty())
				throw new TemplateException("No identifier before ':' at position '%s'.", cursor.getPosition(-1));
			if (stack.value() instanceof Bracket) {
				Bracket b = (Bracket) stack.value();
				if (b.isOpening()) {
					throw new TemplateException("No parameter before ':' at position '%s'.", cursor.getPosition(-1));
				} else {
					// ] } > )
					if (b.getBracket() != ']' && b.getBracket() != '>' && b.getBracket() != ')') {
						throw new TemplateException("No token at position '%s'.", cursor.getPosition(-1));
					}
				}
			} else if (stack.value() instanceof Comma) {
				throw new TemplateException("No parameter before ':' at position '%s'.", cursor.getPosition(-1));
			}
		} else if (currentChar == '[' || currentChar == '<') {
			throw new TemplateException("No function before '%c' at position '%s'.", currentChar, cursor.getPosition(-1));
		}
	}

	private static Object evalToken(String expr, Cursor cursor) throws TemplateException {
		if (expr.startsWith("\"")) {
			if (! expr.endsWith("\"")) {
				throw new TemplateException("Sentence not closed at position '%s').", cursor.getPosition());
			}
			Cursor c = cursor.clone().movel(expr, 0);
			String t = expr.substring(1); //c.move1r();
			t = t.substring(0, t.length()-1);
			return new Text(t, c);
		} else if (expr.startsWith("'") && expr.endsWith("'")) {
			Cursor c = cursor.clone().movel(expr, 0);
			String t = expr.substring(1); //c.move1r();
			t = t.substring(0, t.length()-1);
			if (t.length() == 0) {
				throw new TemplateException("Empty char at position '%s').", cursor.getPosition());
			} else if (t.length() > 1) {
				throw new TemplateException("Invalid length for char at position '%s').", cursor.getPosition());
			} 
			return new Char(t.charAt(0), c);
		} else if (expr.matches("(-)?\\d+[lL]")) {
			return Long.parseLong(expr.substring(0, expr.length()-1));
		} else if (expr.matches("(-)?\\d+")) {
			return Integer.parseInt(expr);
		} else if (expr.matches("(-)?(\\d*.)?\\d+?([eE][+-]?\\d+)?[dD]?")) {
			return Double.parseDouble(expr);
		} else if (expr.matches("(-)?(\\d*.)?\\d+?([eE][+-]?\\d+)?[fF]")) {
			return Float.parseFloat(expr);
		}
		return new Identifier(expr, cursor.clone().movel(expr, 0));
	}
	
}
