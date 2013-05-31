
package temmental2;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;

public class Template {

	private Stack stack;
	private TemplateMessages messages;
	private String filepath;
	private Map<String, ? extends Object> transforms;
	
	public Template(String filepath, TemplateMessages messages) {
		this.stack = new Stack();
		this.messages = messages;
		this.filepath = filepath;
	}
	
    /**
     * Create a template with the given parameters. The default locale is used to retrieve localized messages and format messages (date, numbers...).
     * @param filepath the path to the template file to parse
     * @param transforms the map of transform functions
     * @param properties the messages
     * @throws IOException if an I/O error occurs when reading the template file
     * @throws TemplateException if an other error occurs when reading the template file
     */
    public Template(String filepath, Map<String, ? extends Object> transforms, Properties properties) 
    throws IOException, TemplateException {
        this(filepath, transforms, properties, Locale.getDefault());
    }
    
    public Template(String filepath, Map<String, ? extends Object> transforms, Locale locale, Object ... resourcesContainers) 
    throws IOException, TemplateException {
    	this.stack = new Stack();
        this.transforms = transforms;
        this.messages = new TemplateMessages(locale, resourcesContainers);
        this.filepath = filepath;
        if (filepath != null) {
            readFile(filepath);
        }
    }
    
    
    /**
     * Create a template with the given parameters.
     * @param filepath the path to the template file to parse
     * @param transforms the map of transform functions
     * @param properties the messages
     * @param locale locale to use to format messages (date, numbers...)
     * @throws IOException if an I/O error occurs when reading the template file
     * @throws TemplateException if an other error occurs when reading the template file
     */
    public Template(String filepath, Map<String, ? extends Object> transforms, Properties properties, Locale locale)
    throws IOException, TemplateException {
    	this.stack = new Stack();
        this.transforms = transforms;
        this.messages = new TemplateMessages(properties, locale);
        this.filepath = filepath;
        if (filepath != null) {
            readFile(filepath);
        }
    }

    /**
     * Create a template with the given parameters.
     * @param filepath the path to the template file to parse
     * @param transforms the map of transform functions
     * @param bundle the messages
     * @throws IOException if an I/O error occurs when reading the template file
     * @throws TemplateException if an other error occurs when reading the template file
     */
    public Template(String filepath, Map<String, ? extends Object> transforms, ResourceBundle bundle) 
    throws IOException, TemplateException {
    	this.stack = new Stack();
        this.transforms = transforms;
        this.messages = new TemplateMessages(bundle);
        this.filepath = filepath;
        if (filepath != null) {
            readFile(filepath);
        }
    }
    
    /**
     * Create a template with the given parameters. The default locale is used to retrieve localized messages and format messages (date, numbers...).
     * @param filepath the path to the template file to parse
     * @param transforms the map of transform functions
     * @param resourcePath the messages (<code>classpath:path.to.my.file</code> or <code>file:/path/to/my/file.properties</code>)
     * @throws IOException if an I/O error occurs when reading the template file
     * @throws TemplateException if an other error occurs when reading the template file
     */
    public Template(String filepath, Map<String, ? extends Object> transforms, String resourcePath) 
    throws IOException, TemplateException {
        this(filepath, transforms, resourcePath, Locale.getDefault());
    }
    
    /**
     * Create a template with the given parameters. 
     * @param filepath the path to the template file to parse
     * @param transforms the map of transform functions
     * @param resourcePath the messages (<code>classpath:path.to.my.file</code> or <code>file:/path/to/my/file.properties</code>)
     * @param locale locale to retrieve localized messages and format messages (date, numbers...)
     * @throws IOException if an I/O error occurs when reading the template file
     * @throws TemplateException if an other error occurs when reading the template file
     */
    public Template(String filepath, Map<String, ? extends Object> transforms, String resourcePath, Locale locale) 
    throws IOException, TemplateException {
    	this.stack = new Stack();
        this.transforms = transforms;
        this.filepath = filepath;
        this.messages = new TemplateMessages(resourcePath, locale);
        if (filepath != null) {
            readFile(filepath);
        }
    }

    
    
    
    
    
    
    
    
    
    
    
    
    
	private void readFile(String filepath) throws IOException, TemplateException {
		FileReader fr = new FileReader(new File(filepath));
		try {
			readReader(fr, 1, 0, true);
		} finally {
			fr.close();
		}
	}

	void parseString(String expression, boolean parseExpression) throws IOException, TemplateException {
		StringReader sr = new StringReader(expression);
		readReader(sr, 1, 0, parseExpression);
	}
	
	private void readReader(Reader sr, int line, int column, boolean parseExpression) throws IOException, TemplateException {
		Stack taeStack = parseToTextAndExpressions(sr, new Cursor(filepath, line, column));
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

	private static Stack parseToTextAndExpressions(Reader sr, Cursor cursor) throws IOException, TemplateException {
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
	
    public TemplateMessages getMessages() {
        return messages;
    }

	public String getFilepath() {
		return filepath;
	}

    String formatForTest(String format, HashMap<String, Object> model) throws IOException, TemplateException {
    	parseString(format, true);
        StringWriter out = new StringWriter();
        write(out, (Map<String, Object>) transforms, model);
        TemplateRecorder.log(this, "__default_section", model);
        return out.toString();
    }

}
