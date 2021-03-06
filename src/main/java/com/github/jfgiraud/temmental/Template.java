
package com.github.jfgiraud.temmental;


import java.io.*;
import java.io.Reader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Template {

    private TemplateMessages messages;
    private String filepath;
    private Map<String, ? extends Object> transforms;
    private static final String DEFAULT_SECTION = "__default_section";

    private HashMap<String, Stack> sections;
    private HashMap<String, String> sectionAliases;


    /**
     * Create a template with the given parameters.
     *
     * @param filePath   the path to the template file to parse
     * @param transforms the map of transform functions
     * @param properties the messages
     * @param locale     locale to use to format messages (date, numbers...)
     * @throws IOException       if an I/O error occurs when reading the template file
     * @throws TemplateException if an other error occurs when reading the template file
     */
    public Template(String filePath, Map<String, ? extends Object> transforms, Properties properties, Locale locale)
            throws IOException, TemplateException {
        this(filePath, transforms, new TemplateMessages(properties, locale));
    }


    private Template(String filePath, Map<String, ? extends Object> transforms, TemplateMessages messages)
            throws IOException, TemplateException {
        this.transforms = transforms;
        this.messages = messages;
        this.filepath = filePath;
        if (filePath != null) {
            readFile(filePath);
        }
    }

    /**
     * Create a template with the given parameters. The default locale is used to retrieve localized messages and format messages (date, numbers...).
     *
     * @param filePath   the path to the template file to parse
     * @param transforms the map of transform functions
     * @param properties the messages
     * @throws IOException       if an I/O error occurs when reading the template file
     * @throws TemplateException if an other error occurs when reading the template file
     */
    public Template(String filePath, Map<String, ? extends Object> transforms, Properties properties)
            throws IOException, TemplateException {
        this(filePath, transforms, properties, Locale.getDefault());
    }

    public Template(String filePath, Map<String, ? extends Object> transforms, Locale locale, Object... resourcesContainers)
            throws IOException, TemplateException {
        this(filePath, transforms, new TemplateMessages(locale, resourcesContainers));
    }

    /**
     * Create a template with the given parameters.
     *
     * @param filePath   the path to the template file to parse
     * @param transforms the map of transform functions
     * @param bundle     the messages
     * @throws IOException       if an I/O error occurs when reading the template file
     * @throws TemplateException if an other error occurs when reading the template file
     */
    public Template(String filePath, Map<String, ? extends Object> transforms, ResourceBundle bundle)
            throws IOException, TemplateException {
        this(filePath, transforms, new TemplateMessages(bundle));
    }

    /**
     * Create a template with the given parameters. The default locale is used to retrieve localized messages and format messages (date, numbers...).
     *
     * @param filePath     the path to the template file to parse
     * @param transforms   the map of transform functions
     * @param resourcePath the messages (<code>classpath:path.to.my.file</code> or <code>file:/path/to/my/file.properties</code>)
     * @throws IOException       if an I/O error occurs when reading the template file
     * @throws TemplateException if an other error occurs when reading the template file
     */
    public Template(String filePath, Map<String, ? extends Object> transforms, String resourcePath)
            throws IOException, TemplateException {
        this(filePath, transforms, resourcePath, Locale.getDefault());
    }

    /**
     * Create a template with the given parameters.
     *
     * @param filePath     the path to the template file to parse
     * @param transforms   the map of transform functions
     * @param resourcePath the messages (<code>classpath:path.to.my.file</code> or <code>file:/path/to/my/file.properties</code>)
     * @param locale       locale to retrieve localized messages and format messages (date, numbers...)
     * @throws IOException       if an I/O error occurs when reading the template file
     * @throws TemplateException if an other error occurs when reading the template file
     */
    public Template(String filePath, Map<String, ? extends Object> transforms, String resourcePath, Locale locale)
            throws IOException, TemplateException {
        this(filePath, transforms, new TemplateMessages(resourcePath, locale));
    }

    private void readFile(String filePath) throws IOException, TemplateException {
        FileReader fr = new FileReader(new File(filePath));
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

        {
            sections = new HashMap<String, Stack>();
            sectionAliases = new HashMap<String, String>();
            sections.put(DEFAULT_SECTION, new Stack());
            Stack stack = sections.get(DEFAULT_SECTION);

            Stack taeStack = parseToTextAndExpressions(sr, new Cursor(filepath, line, column));
            taeStack.reverse();
            while (!taeStack.empty()) {
                Object o = taeStack.pop();
                if (o instanceof Text) {
                    stack = parseToSections(stack, (Text) o);
                } else {
                    stack.push(o);
                }
            }
        }

        {
            Stack stack;
            for (String sectionName : sections.keySet()) {
                Stack taeStack = sections.get(sectionName);
                taeStack.reverse();
                stack = new Stack();
                while (!taeStack.empty()) {
                    Object o = taeStack.pop();
                    if (parseExpression && (o instanceof Expression)) {
                        stack.push(((Expression) o).parse());
                    } else {
                        stack.push(o);
                    }
                }
                cleanSpaces(stack);
                sections.put(sectionName, createCommands(stack));
            }
        }
    }

    private void cleanSpaces(Stack stack) {
        for (int i = 1, d = stack.depth(); i <= d; i++) {
            Object c = stack.value(i);
            if (c instanceof Element) {
                Element e = (Element) c;
                if (e.cursor.eatLeft && i < d) {
                    cleanAt(stack, i + 1, true, false);
                }
                if (e.cursor.eatRight && i > 1) {
                    cleanAt(stack, i - 1, false, true);
                }
            }
        }
    }

    private void cleanAt(Stack stack, int i, boolean left, boolean right) {
        Object p = stack.value(i);
        if (p instanceof String) {
            stack.setvalue(i, Text.cleanSpaces((String) p, left, right));
        } else if (p instanceof Text) {
            ((Text) p).cleanSpaces(left, right);
        }
    }

    private Stack createCommands(Stack stack) throws TemplateException, IOException {
        Stack oldOut = new Stack();
        Stack out = new Stack();
        stack.reverse();
        while (!stack.empty()) {
            Object obj = stack.pop();
            if (obj instanceof Command) {
                Command cmd = (Command) obj;
                cmd.readUntilClosing(stack);
                cmd.check();
                out.push(cmd);
            } else {
                out.push(obj);
            }
        }

        if (!oldOut.empty()) {
            throw new TemplateException("A command is not closed!");
        }

        return out;
    }


    private Stack parseToSections(Stack stack, Text o) throws TemplateException {
        String s = (String) o.writeObject(null, null, null);
        Pattern p = Pattern.compile("<!--\\s*#section\\s+(?<name>[a-zA-Z0-9_]+)(?<aliases>(\\s+\\|\\|\\s+([a-zA-Z0-9_]+))+)?\\s*-->\\s*");
        Matcher m = p.matcher(s);
        if (m.find()) {
            stack.push(s.substring(0, m.start())); // before
            List<String> aliases = computeAliases(m.group("aliases"));
            String name = m.group("name");
            int b = m.end();
            while (m.find()) {
                int e = m.start();
                stack = new Stack();
                setSectionAndAliases(name, stack, aliases);
                stack.push(s.substring(b, e)); // after
                aliases = computeAliases(m.group("aliases"));
                name = m.group("name");
                b = m.end();
            }
            stack = new Stack();
            setSectionAndAliases(name, stack, aliases);
            stack.push(s.substring(b)); // after
        } else {
            stack.push(o);
        }

        return stack;
    }

    private void setSectionAndAliases(String name, Stack stack, List<String> aliases) {
        sections.put(name, stack);
        if (!aliases.isEmpty()) {
            for (String alias : aliases) {
                sectionAliases.put(alias, name);
            }
        }
    }

    static List<String> computeAliases(String aliases) {
        List<String> result = new ArrayList<String>();
        if (aliases != null) {
            aliases = aliases.replaceFirst("^\\s*\\|\\|", "");
            for (String section : Arrays.asList(StringUtils.split(aliases, "||", -1))) {
                result.add(StringUtils.strip(section));
            }
        }
        return result;
    }


    Stack getStack() {
        return sections.get(DEFAULT_SECTION);
    }

    private static Stack parseToTextAndExpressions(Reader sr, Cursor cursor) throws IOException, TemplateException {
        Stack stack = new Stack();
        StringWriter buffer = new StringWriter();
        boolean opened = false;
        try {
            int currentChar = sr.read();
            boolean escape = false;
            while (currentChar != -1) {
                cursor.next(currentChar);
                if (escape) {
                    if (currentChar != '~') {
                        buffer.write('\\');
                    }
                    buffer.write(currentChar);
                    escape = false;
                } else if (currentChar == '\\') {
                    escape = true;
                    cursor.move1l();
                } else if (!opened && currentChar == '~') {
                    String expr = buffer.toString();
                    if (!expr.equals("")) {
                        stack.push(new Text(expr, cursor.clone().movel(expr, 0)));
                        buffer = new StringWriter();
                    }
                    buffer.write(currentChar);
                    opened = true;
                } else if (opened && currentChar == '~') {
                    buffer.write(currentChar);
                    String expr = buffer.toString();
                    if (!expr.equals("")) {
                        stack.push(new Expression(expr, cursor.clone().movel(expr, 1)));
                        buffer = new StringWriter();
                    }
                    opened = false;
                } else {
                    buffer.write(currentChar);
                }
                currentChar = sr.read();
            }
        } finally {
            sr.close();
        }
        if (opened) {
            throw new TemplateException("End of parsing. Character '~' is not escaped at position '%s'.", cursor.getPosition());
        }
        String expr = buffer.toString();
        if (!expr.equals("")) {
            stack.push(new Text(expr, cursor.clone().movel(expr, 1)));
            buffer = new StringWriter();
        }
        return stack;
    }

    private static HashMap<String, Object> cloneModel(Map<String, Object> model) {
        return new HashMap<String, Object>(model);
    }

    static void writeObject(Writer out, Map<String, Object> functions, Map<String, Object> model, TemplateMessages messages, Object value) throws TemplateException, IOException {

        Map<String, Object> newModel = cloneModel(model);

        if (value instanceof String || value instanceof Number) {
            out.write(value.toString());
            return;
        }

        if (value instanceof Identifier) {
            Object o = ((Identifier) value).writeFinalObject(functions, newModel, messages);
            out.write(o.toString());
            return;
        }

        if (value instanceof Text) {
            Object o = ((Text) value).writeFinalObject(functions, newModel, messages);
            out.write(o.toString());
            return;
        }

        if (value instanceof Function) {
            Function function = ((Function) value);
            Object result = function.writeFinalObject(functions, newModel, messages);
            out.write(result.toString());
            return;
        }

        if (value instanceof Message) {
            Object result = ((Message) value).writeFinalObject(functions, newModel, messages);
            out.write(result.toString());
            return;
        }

        if (value instanceof Command) {
            ((Command) value).writeObject(out, functions, newModel, messages);
            return;
        }

        throw new TemplateException("Unsupported operation for class '%s'", value.getClass().getName());
    }

    private void writeSection(Writer out, String sectionName, Map<String, Object> functions, Map<String, Object> model) throws IOException, TemplateException {
        Stack stack = sections.containsKey(sectionName) ? sections.get(sectionName) : sections.get(sectionAliases.get(sectionName));
        for (int i = stack.depth(); i > 0; i--) {
            try {
                writeObject(out, functions, model, messages, stack.value(i));
            } catch (TemplateIgnoreRenderingException e) {
            }
        }
    }

    public void printStructure(PrintStream out) throws IOException {
        printStructure(new PrintWriter(out));
    }

    public void printStructure(PrintWriter out) throws IOException {
        for (Map.Entry<String, Stack> entry : sections.entrySet()) {
            out.println("--- section '" + entry.getKey() + "'");
            if (sectionAliases.containsValue(entry.getKey())) {
                for (String alias : sectionAliases.keySet()) {
                    if (sectionAliases.get(alias).equals(entry.getKey())) {
                        out.println("--- declared alias '" + alias + "'");
                    }
                }
            }
            entry.getValue().printStack(out);
        }
    }

    public TemplateMessages getMessages() {
        return messages;
    }

    public String getFilepath() {
        return filepath;
    }

//    String formatForTest(String format, Map<String, Object> model) throws IOException, TemplateException {
//        parseString(format, true);
//        StringWriter out = new StringWriter();
//        writeSection(out, DEFAULT_SECTION, (Map<String, Object>) transforms, model);
//        TemplateRecorder.log(this, DEFAULT_SECTION, model);
//        return out.toString();
//    }

    /**
     * Prints the whole file on the stream.
     *
     * @param out the stream
     * @throws TemplateException   if an error is detected by the template engine
     * @throws java.io.IOException if an I/O error occurs
     */
    public void printFile(Writer out) throws TemplateException, java.io.IOException {
        printSection(out, DEFAULT_SECTION, newEmptyModel());
    }

    /**
     * Prints the whole file on the stream.
     *
     * @param out   the stream
     * @param model the model
     * @throws TemplateException   if an error is detected by the template engine
     * @throws java.io.IOException if an I/O error occurs
     */
    public void printFile(Writer out, Map<String, ? extends Object> model) throws TemplateException,
            java.io.IOException {
        printSection(out, DEFAULT_SECTION, model);
    }

    /**
     * Prints a section of the file on the stream. The tags are replaced by the corresponding values in the model.
     *
     * @param out         the stream
     * @param sectionName the section to display
     * @param model       the model
     * @throws TemplateException   if an error is detected by the template engine
     * @throws java.io.IOException if an I/O error occurs
     */
    public void printSection(Writer out, String sectionName, Map<String, ? extends Object> model)
            throws TemplateException, java.io.IOException {
        if (sectionName == null || !hasSection(sectionName)) {
            throw new TemplateException("Section '" + sectionName + "' not found.");
        }
        writeSection(out, sectionName, (Map<String, Object>) transforms, (Map<String, Object>) model);
        TemplateRecorder.log(this, sectionName, model);
    }

    /**
     * Prints a section of the file on the stream.
     *
     * @param out         the stream
     * @param sectionName the section to display
     * @throws TemplateException   if an error is detected by the template engine
     * @throws java.io.IOException if an I/O error occurs
     */
    public void printSection(Writer out, String sectionName) throws TemplateException, java.io.IOException {
        printSection(out, sectionName, newEmptyModel());
    }

    private HashMap<String, Object> newEmptyModel() {
        return new HashMap<String, Object>();
    }

    /**
     * Tests if the given section exists in the template
     *
     * @param sectionName the possible section name
     * @return <code>true</code> if the section exists, <code>false</code> otherwise.
     */
    public boolean hasSection(String sectionName) {
        return sections.containsKey(sectionName) || sectionAliases.containsKey(sectionName);
    }
}
