package temmental2;

import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import temmental2.TemplateException;
import temmental2.TemplateMessages;
import temmental2.TemplateRecorder;
import temmental2.Node.BracketType;
import temmental2.Node.Type;

public class NewTemplate {

    private static final String DEFAULT_SECTION = "__DEFAULT_SECTION";
    
    String filepath;
    TemplateMessages messages;
    
    public void setMessages(TemplateMessages messages) {
		this.messages = messages;
	}

	private Map<String, Node> sections;
    private Map<String, Transform> functions;
    private ArrayList<String> sectionsOrder;
    
    public NewTemplate() {
    	functions = new HashMap<String, Transform>();
    }
    
    
    Node parse(String expression) throws IOException, TemplateException {
		Node parent = new Node(Node.Type.Section, "-", 1, 0, false);
		parent.setBuffer(DEFAULT_SECTION);
		StringReader sr = new StringReader(expression);
		try {
			parse(parent, "-", 1, 0, sr);
		} finally {
			sr.close();
		}
		sections = parent.sections();
		return parent;
	}

	private Node parse(Node root, String file, int line, int column, StringReader sr) throws IOException, TemplateException {
	    Node currentNode = createNodeDown(Node.Type.Text, file, line, column, root);
	    
		boolean outsideAnExpression = true;
		
		int currentChar = sr.read(); 
		int previousChar = -1;
		while (currentChar != -1) {
			column++;
			if (outsideAnExpression) {
				if (currentChar != '~') {
				    currentNode = currentNode.write(file, line, column, currentChar, this);
					if (currentChar == '\n') {
						line++;
						column = 0;
					} 
				} else {
					int nextChar = sr.read();
					if (nextChar == -1) {
					    currentNode = createSibling(Node.Type.Unknown, file, line, column, currentNode);
						outsideAnExpression = false;
						break;
					} else {
						if (nextChar == '~' && currentChar == '~') {
							currentNode = currentNode.write(file, line, column, currentChar, this);
							previousChar = currentChar;
							currentChar = sr.read();
							continue;
						} else {
						    currentNode = createSibling(Node.Type.Unknown, file, line, column, currentNode);
							outsideAnExpression = false;
							previousChar = currentChar;
							currentChar = nextChar;
							continue;
						}
					}

				}
			} else {
			    boolean nextLoop = false;
			    if (currentNode.getType() == Type.Unknown && ((currentChar >= '0' && currentChar <= '9') 
			    		/*|| (currentChar >= 'a' && currentChar <= 'z') || (currentChar >= 'A' && currentChar <= 'Z')*/)) {
			        currentNode = currentNode.startLitteral(file, line, column, currentChar);
			        nextLoop = true;
			    } else if (currentNode.getType() == Type.Unknown && currentChar == '"') {
			        currentNode = currentNode.startSentence(file, line, column, currentChar);
			        nextLoop = true;
			    } else if (currentNode.getType() == Type.Sentence && currentChar == '"') {
			        currentNode = currentNode.stopSentence(file, line, column, currentChar);
			        nextLoop = true;
			    } else if (currentNode.getType() == Type.Sentence && ! currentNode.isClosed() && currentChar != '~') {
			        currentNode = currentNode.write(file, line, column, currentChar, this);
			        nextLoop = true;
			    }
			    if (nextLoop) {
			        previousChar = currentChar;
			        currentChar = sr.read();
			        continue;
			    }
				if (currentChar == '~') {
					currentNode.validateAll(line, column, currentChar, true, this);
					outsideAnExpression = true;
					if (currentNode.getType() == Node.Type.CommandClose) {
					    currentNode = currentNode.parentNode();
					}
					currentNode = createSibling(Node.Type.Text, file, line, column, currentNode);
				} else if (currentChar == ' ') {
				    currentNode = currentNode.startCondition(file, line, column, currentChar);
				} else if (currentChar == ':') {
					currentNode = currentNode.startTransform(file, line, column, currentChar, this);
				} else if (currentChar == '[') {
					currentNode = currentNode.openBracket(BracketType.Square, file, line, column, currentChar);
				} else if (currentChar == ']') {
					currentNode = currentNode.closeBracket(BracketType.Square, file, line, column, currentChar);
				} else if (currentChar == '(') {
                    currentNode = currentNode.openBracket(BracketType.Round, file, line, column, currentChar);
                } else if (currentChar == ')') {
                    currentNode = currentNode.closeBracket(BracketType.Round, file, line, column, currentChar);
                } else if (currentChar == '<') {
                    currentNode = currentNode.openBracket(BracketType.Angle, file, line, column, currentChar);
                } else if (currentChar == '>') {
                    currentNode = currentNode.closeBracket(BracketType.Angle, file, line, column, currentChar);
                } else if (currentChar == ',') {
                    currentNode = currentNode.newSibling(file, line, column, currentChar, this);
                } else if (currentChar == '#') {
                    currentNode = currentNode.openCommand(file, line, column, currentChar);
                } else if (currentChar == '/') {
                    currentNode = currentNode.closeCommand(file, line, column, currentChar);
                } else if (currentNode.allow(currentChar)) {
                    currentNode = currentNode.write(file, line, column, currentChar, this);
				} else {
					throw new TemplateException("Invalid syntax at position '%s' - reach character '%c'", currentNode.positionInformation(file, line, column), currentChar);
				}
			}
			previousChar = currentChar;
			currentChar = sr.read();
		}
		return currentNode;
	}

	private Node createNodeDown(Type type, String file, int line, int column, Node parent) {
        Node newNode = new Node(type, file, line, column, false);
        newNode.setParent(parent);
        parent.add(newNode);
        return newNode;
    }

    private Node createSibling(Type type, String file, int line, int column, Node currentNode) {
        Node parent = currentNode.parentNode();
        Node newNode = new Node(type, file, line, column, false);
        newNode.setParent(parent);
        parent.add(newNode);
        return newNode;
    }

//    private Node createNodeUpper(Type type, String file, int line, int column, Node currentNode) {
//	    Node newNode = new Node(type, file, line, column, false);
//	    Node parent = currentNode.parentNode();
//	    newNode.setParent(parent);
//	    parent.removeChild(currentNode);
//	    parent.addChild(newNode);
//	    return newNode;
//	}

	static String representation(Node node) {
		return node.representation();
	}
	
    /**
     * Tests if the given section exists in the template
     * @param sectionName the possible section name
     * @return <code>true</code> if the section exists, <code>false</code> otherwise.
     */
    public boolean hasSection(String sectionName) {
        return sections.containsKey(sectionName);
    }
    
    /**
     * Prints the whole file on the stream.
     * @param out the stream
     * @throws TemplateException if an error is detected by the template engine 
     * @throws java.io.IOException if an I/O error occurs
     */
    public void printFile(Writer out) throws TemplateException, java.io.IOException {
        printSection(out, DEFAULT_SECTION, new HashMap<String, Object>());
    }

    /**
     * Prints the whole file on the stream.
     * @param out the stream
     * @param model the model  
     * @throws TemplateException if an error is detected by the template engine 
     * @throws java.io.IOException if an I/O error occurs
     */
    public void printFile(Writer out, Map<String, ? extends Object> model) throws TemplateException,
    java.io.IOException {
        printSection(out, DEFAULT_SECTION, model);
    }

    /**
     * Prints a section of the file on the stream. The tags are replaced by the corresponding values in the model. 
     * @param out the stream
     * @param sectionName the section to display
     * @param model the model  
     * @throws TemplateException if an error is detected by the template engine 
     * @throws java.io.IOException if an I/O error occurs
     */
    public void printSection(Writer out, String sectionName, Map<String, ? extends Object> model)
    throws TemplateException, java.io.IOException {
        if (sectionName == null || !hasSection(sectionName)) {
            throw new TemplateException("Section '" + sectionName + "' not found.");
        }
        writeSection(out, sections.get(sectionName), model);
        TemplateRecorder.log(this, sectionName, model);
    }

    /**
     * Prints a section of the file on the stream.  
     * @param out the stream
     * @param sectionName the section to display
     * @throws TemplateException if an error is detected by the template engine 
     * @throws java.io.IOException if an I/O error occurs
     */
    public void printSection(Writer out, String sectionName) throws TemplateException, java.io.IOException {
        printSection(out, sectionName, new HashMap<String, Object>());
    }

    public TemplateMessages getMessages() {
        return messages;
    }

    private void writeSection(Writer out, Node section, Map<String, ? extends Object> model) throws IOException,
    TemplateException {
    	section.value(out, model, this);
    }

	public void addTransform(String name, Transform function) {
		functions.put(name, function);
	}

	public Transform getTransform(String varname) {
		return functions.get(varname);
	}

}
