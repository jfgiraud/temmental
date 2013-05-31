
package temmental2;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;


public class Template {

	private Stack stack;
	private TemplateMessages messages;

	public Template(TemplateMessages messages) {
		this.stack = new Stack();
		this.messages = messages;
	}
	
	void parse(String expression, boolean parseExpression) throws IOException, TemplateException {
		parseString(expression, "-", 1, 0, parseExpression);
	}
	
	private void parseString(String expression, String file, int line, int column, boolean parseExpression) throws IOException, TemplateException {
		StringReader sr = new StringReader(expression);
		Stack taeStack = parseToTextAndExpressions(sr, new Cursor(file, line, column));
		stack.clear();
		while (! taeStack.empty()) {
			Object o = taeStack.pop();
			if (parseExpression && (o instanceof Expression)) {
				stack.push(((Expression) o).parse());
			} else {
				stack.push(o);
			}
		}
	}

	Stack getStack() {
		return stack;
	}

	private static Stack parseToTextAndExpressions(StringReader sr, Cursor cursor) throws IOException, TemplateException {
		Stack stack = new Stack();
		StringWriter buffer = new StringWriter();
		boolean betweenTildes = false;
		try {
			int currentChar = sr.read();
			int previousChar = 0;
			while (currentChar != -1) {
				cursor.next(currentChar);
				if (currentChar == '~') {
					previousChar = currentChar;
					currentChar = sr.read();
					cursor.next(currentChar);
					if (currentChar == -1) {
						if (! betweenTildes) {
							throw new TemplateException("End of parsing. Character '~' is not escaped at position '%s'.", cursor.getPosition(-1));
						} else {
							buffer.write('~');
							String expr = buffer.toString();
							if (! expr.equals("")) {
								stack.push(new Expression(expr, cursor.clone().movel(expr, 0)));
								buffer = new StringWriter();
								betweenTildes = false;
							}
							betweenTildes = false;
						}
					} else if (currentChar == '~') {
						betweenTildes = false;
						// ~~ escape
						cursor.move1l();
						buffer.write('~'); 
					} else {
						String expr = buffer.toString();
						if (! expr.startsWith("~")) {
							if (! expr.equals("")) {
								// text~$| cursor-len(text)-len(~)
								stack.push(new Text(expr, cursor.clone().movel(expr, -1)));
								buffer = new StringWriter();
							}
							betweenTildes = true;
							buffer.write(previousChar); 
							buffer.write(currentChar); 
						} else {
							betweenTildes = true;
							buffer.write('~'); 
							expr = buffer.toString();
							if (! expr.equals("")) {
								stack.push(new Expression(expr, cursor.clone().movel(expr, 0)));
								buffer = new StringWriter();
								betweenTildes = false;
							}
							buffer.write(currentChar);
						}
					}
				} else {
					buffer.write(currentChar); 
				}

				previousChar = currentChar;
				currentChar = sr.read(); 
			}
		} finally {
			sr.close();
		}
		if (betweenTildes) {
			throw new TemplateException("Reach end of line. A character '~' is not escaped at position '%s'.", cursor.getPosition());
		}
		String expr = buffer.toString();
		if (! expr.equals("")) {
			stack.push(new Text(expr, cursor.clone().movel(expr, 1)));
			buffer = new StringWriter();
		}
		return stack;
	}

	static Object writeObject(Map<String, Object> functions, Map<String, Object> model, TemplateMessages messages, Object value) throws TemplateException {
		
		if (value instanceof String || value instanceof Number)
			return value;

		if (value instanceof Identifier) {
			return ((Identifier) value).writeObject(functions, model, messages);
		}
		
		if (value instanceof Function) {
			Function function = ((Function) value); 
			Object result = function.writeObject(functions, model, messages);
			if (result != null && result instanceof Transform) {
				throw new TemplateException("Unable to apply function '%s' at position '%s'. This function expects one or more parameters. It receives no parameter.",	function.getIdentifier(), function.cursor.getPosition());
			} 
			return result;
		}
		
		if (value instanceof Message) {
			return ((Message) value).writeObject(functions, model, messages);
		}
		
		throw new TemplateException("Unsupported operation for class '%s'", value.getClass().getName());
	}
	
	public void write(Writer out, Map<String, Object> functions, Map<String, Object> model) throws IOException, TemplateException {
		for (int i=stack.depth(); i>0; i--) {
			Object o = writeObject(functions, model, messages, stack.value(i));
			if (o != null) {
				out.write(o.toString());
			}
		}
	}
}
