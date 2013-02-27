package temmental;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Read and manipulate a template file to write data on an output stream.<br/>
 * <br/>
 * Many constructors are available:<br/>
 * <ul>
 *   <li>to read a template</li> 
 *   <li>to associate a properties file or a bundle to the template for internationalization</li>
 * </ul>
 * <br/>
 * When the template is created, you can call public methods to write a part of the file or the whole file on the<br/> 
 * output stream. Tags are replaced by their value defined in the model Map.<br/>
 * <br/>
 * For a better documentation: <a href="http://temmental.sourceforge.net/">http://temmental.sourceforge.net/</a>
 */
public class Template {
/*
	~$var~
	~$var?~
	~$var[]~
	~$var?[]~
	~'var[]~
	~$var[$parametre1?,$parametre2,@parametres1,@parametres2?,'parametre3[]]~
	~'var[$parametre1?,$parametre2,@parametres1,@parametres2?,'parametre3[]]~
	:'filtre
	:$filtre
	:$filtre?
	:@filtres
	:@filtres?
	~$var#true~
	~$var#false~
	~$var#iterate~
*/	
    private HashMap<String, List<Chunk>> sections;
    private ArrayList<String> sectionsOrder;

    Map<String, ? extends ObjectFilter> filters;
    String filepath;
    TemplateMessages messages;
    private static final String DEFAULT_SECTION = "__default_section";

    /**
     * Create a template with the given parameters. The default locale is used to retrieve localized messages and format messages (date, numbers...).
     * @param filepath the path to the template file to parse
     * @param filters the map of filters
     * @param properties the messages
     * @throws IOException if an I/O error occurs when reading the template file
     * @throws TemplateException if an other error occurs when reading the template file
     */
    public Template(String filepath, Map<String, ? extends ObjectFilter> filters, Properties properties) 
    throws IOException, TemplateException {
        this(filepath, filters, properties, Locale.getDefault());
    }
    
    public Template(String filepath, Map<String, ? extends ObjectFilter> filters, Locale locale, Object ... resourcesContainers) 
    throws IOException, TemplateException {
        this.filters = filters;
        this.messages = new TemplateMessages(locale, resourcesContainers);
        this.filepath = filepath;
        if (filepath != null) {
            readFile(filepath);
        }
    }
    
    
    /**
     * Create a template with the given parameters.
     * @param filepath the path to the template file to parse
     * @param filters the map of filters
     * @param properties the messages
     * @param locale locale to use to format messages (date, numbers...)
     * @throws IOException if an I/O error occurs when reading the template file
     * @throws TemplateException if an other error occurs when reading the template file
     */
    public Template(String filepath, Map<String, ? extends ObjectFilter> filters, Properties properties, Locale locale)
    throws IOException, TemplateException {
        this.filters = filters;
        this.messages = new TemplateMessages(properties, locale);
        this.filepath = filepath;
        if (filepath != null) {
            readFile(filepath);
        }
    }

    /**
     * Create a template with the given parameters.
     * @param filepath the path to the template file to parse
     * @param filters the map of filters
     * @param bundle the messages
     * @throws IOException if an I/O error occurs when reading the template file
     * @throws TemplateException if an other error occurs when reading the template file
     */
    public Template(String filepath, Map<String, ? extends ObjectFilter> filters, ResourceBundle bundle) 
    throws IOException, TemplateException {
        this.filters = filters;
        this.messages = new TemplateMessages(bundle);
        this.filepath = filepath;
        if (filepath != null) {
            readFile(filepath);
        }
    }
    
    /**
     * Create a template with the given parameters. The default locale is used to retrieve localized messages and format messages (date, numbers...).
     * @param filepath the path to the template file to parse
     * @param filters the map of filters
     * @param resourcePath the messages (<code>classpath:path.to.my.file</code> or <code>file:/path/to/my/file.properties</code>)
     * @throws IOException if an I/O error occurs when reading the template file
     * @throws TemplateException if an other error occurs when reading the template file
     */
    public Template(String filepath, Map<String, ? extends ObjectFilter> filters, String resourcePath) 
    throws IOException, TemplateException {
        this(filepath, filters, resourcePath, Locale.getDefault());
    }
    
    /**
     * Create a template with the given parameters. 
     * @param filepath the path to the template file to parse
     * @param filters the map of filters
     * @param resourcePath the messages (<code>classpath:path.to.my.file</code> or <code>file:/path/to/my/file.properties</code>)
     * @param locale locale to retrieve localized messages and format messages (date, numbers...)
     * @throws IOException if an I/O error occurs when reading the template file
     * @throws TemplateException if an other error occurs when reading the template file
     */
    public Template(String filepath, Map<String, ? extends ObjectFilter> filters, String resourcePath, Locale locale) 
    throws IOException, TemplateException {
        this.filters = filters;
        this.filepath = filepath;
        this.messages = new TemplateMessages(resourcePath, locale);
        if (filepath != null) {
            readFile(filepath);
        }
    }

    private void readFile(String filepath) throws IOException, TemplateException {
        BufferedReader br = new BufferedReader(new FileReader(filepath));
        try {
            sections = new HashMap<String, List<Chunk>>();
            sectionsOrder = new ArrayList<String>();
            List<Chunk> currentSectionChunks = new ArrayList<Chunk>();
            LinkedList<List<Chunk>> lifoListChunks = new LinkedList<List<Chunk>>();
            String sectionName = DEFAULT_SECTION;

            sectionsOrder.add(sectionName);
            sections.put(sectionName, currentSectionChunks);

            String line = br.readLine();
            int lineNumber = 1;
            while (line != null) {
                String positionInformation = filepath + ":" + lineNumber;
                String tmp = line.trim();
                if (tmp.startsWith("<!-- #section ") && tmp.endsWith("-->")) {
                    tmp = tmp.replace("<!-- #section ", "");
                    tmp = tmp.replace("-->", "");
                    tmp = tmp.trim();
                    if (!tmp.matches("\\w+")) {
                        throw new TemplateException("Invalid section name '%s'", tmp);
                    }
                    sectionName = tmp;
                    sectionsOrder.add(sectionName);
                    currentSectionChunks = new ArrayList<Chunk>();
                    lifoListChunks = new LinkedList<List<Chunk>>();
                    sections.put(sectionName, currentSectionChunks);
                } else {
                    currentSectionChunks = _parse(positionInformation, currentSectionChunks, lifoListChunks, line
                            + "\n");
                }
                line = br.readLine();
                lineNumber++;
            }
        } finally {
            br.close();
        }
    }

    abstract class Chunk {
        public abstract boolean render(Writer out, Map<String, ? extends Object> model) throws IOException,
        TemplateException;
    }

    class BlockChunk extends Chunk {
        private List<Chunk> chunks;
        private ExprVal value;
        protected String positionInformation;
        protected String allMatchStr;
        private String type;

        BlockChunk(String positionInformation, String allMatchStr, ExprVal value, List<Chunk> chunks, String type) {
            this.value = value;
            this.chunks = chunks;
            this.positionInformation = positionInformation;
            this.allMatchStr = allMatchStr;
            this.type = type;
        }

        @Override
        public boolean render(Writer out, Map<String, ? extends Object> model) throws IOException, TemplateException {
            if (type.startsWith("set")) {
                String var = (type.indexOf("<") >= 0) ? type.substring(type.indexOf("<") + 1, type.indexOf(">")) : null;
                if (var == null || var.trim().equals("")) {
                    throw new TemplateException("Type '%s' not supported with null or value to render '%s' at position '%s'.", type,
                            allMatchStr, positionInformation);
                }
                return renderSet(out, model, var);
            } else if (type.startsWith("list")) {
                String var = (type.indexOf("<") >= 0) ? type.substring(type.indexOf("<") + 1, type.indexOf(">")) : null;
                return renderList(out, model, var);
            } else if (type.equals("true")) {
                return renderBoolean(out, model, true);
            } else if (type.equals("false")) {
                return renderBoolean(out, model, false);
            } else {
                throw new TemplateException("Type '%s' not supported to render '%s' at position '%s'.", type,
                        allMatchStr, positionInformation);
            }
        }

        private boolean renderSet(Writer out, Map<String, ? extends Object> model, String var) throws IOException,
        TemplateException {
            Map<String, Object> modelToRender = new HashMap<String, Object>();
            modelToRender.putAll(model);
            
            Object o = this.value.compute(model);
            modelToRender.put(var, o);
            
            
            for (Chunk chunk : chunks) {
                chunk.render(out, modelToRender);
            }
            return true;
        }
        
        private boolean renderBoolean(Writer out, Map<String, ? extends Object> model, boolean condition)
        throws TemplateException, IOException {
            Object o = this.value.compute(model);
            if (o != null && !(o instanceof Boolean)) {
                throw new TemplateException("Key '%s' is not a boolean [%s]. Unable to render '%s' at position '%s'.",
                        this.value.getTagOrKey(), /* o.toString() */o.getClass().getName(), allMatchStr,
                        positionInformation);
            }
            if (o != null && ((Boolean) o).booleanValue() == condition) {
                Map<String, Object> modelToRender = new HashMap<String, Object>();
                modelToRender.putAll(model);
                for (Chunk chunk : chunks) {
                    chunk.render(out, modelToRender);
                }
                return true;
            }
            return false;
        }

        private boolean renderList(Writer out, Map<String, ? extends Object> model, String var) throws IOException,
        TemplateException {
            Object o = this.value.compute(model);
            if (o != null) {
                List list = (List) o;
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i) instanceof Map) {
                        Map objectToRender = (Map) list.get(i);
                        Map<String, Object> modelToRender = new HashMap<String, Object>();
                        modelToRender.putAll(model);
                        modelToRender.putAll(objectToRender);
                        for (Chunk chunk : chunks) {
                            chunk.render(out, modelToRender);
                        }
                    } else {
                        Object objectToRender = list.get(i);
                        Map<String, Object> modelToRender = new HashMap<String, Object>();
                        modelToRender.putAll(model);
                        modelToRender.put(var, objectToRender);
                        for (Chunk chunk : chunks) {
                            chunk.render(out, modelToRender);
                        }
                    }
                }
                return true;
            }
            return false;
        }

    }

    class AliasChunk extends Chunk {
        private String alias;
        private ExprChunk chunk;

        AliasChunk(String alias, ExprChunk chunk) {
            this.alias = alias;
            this.chunk = chunk;
        }

        @Override
        public boolean render(Writer out, Map<String, ? extends Object> model) throws IOException, TemplateException {
            try {
                Map<String, Object> modelToRender = (Map<String, Object>) model;
                Object o = chunk.value.compute(modelToRender);
                boolean rendered = o != null;
                if (rendered) {
                    modelToRender.put(alias, o);
                }
                return rendered;
            } catch (TemplateException te) {
                throw new TemplateException("Unable to set '%s'. Cause: %s", alias, te.getMessage());
            }
        }

    }

    class TextChunk extends Chunk {
        String value;

        TextChunk(String value) {
            this.value = value;
        }

        @Override
        public boolean render(Writer out, Map<String, ? extends Object> model) throws IOException, TemplateException {
            out.write(value);
            return true;
        }
    }

    enum Type {
        simple, property, dynamic_property, multiple
    };

    class ExprChunk extends Chunk {
        ExprVal value;

        ExprChunk(ExprVal value) throws TemplateException {
            this.value = value;
        }

        @Override
        public boolean render(Writer out, Map<String, ? extends Object> model) throws IOException, TemplateException {
        	return value.render(out, model) != null;
        }
    }

    String formatForTest(String format, HashMap<String, Object> model) throws IOException, TemplateException {
        List<Chunk> lineChunks = parse(format);
        StringWriter out = new StringWriter();
        writeSection(out, lineChunks, model);
        TemplateRecorder.log(this, "__default_section", model);
        return out.toString();
    }

    private void writeSection(Writer out, List<Chunk> result, Map<String, ? extends Object> model) throws IOException,
    TemplateException {
        Map<String, Object> modelToRender = new HashMap<String, Object>();
        modelToRender.putAll(model);
        for (Chunk chunk : result) {
            chunk.render(out, modelToRender);
        }
    }

    List<Chunk> parse(String reference) throws TemplateException {
        LinkedList<List<Chunk>> lifoChunks = new LinkedList<List<Chunk>>();
        return parse(lifoChunks, reference);
    }

    private List<Chunk> parse(LinkedList<List<Chunk>> lifoChunks, String reference) throws TemplateException {
        List<Chunk> result = new ArrayList<Chunk>();
        _parse("", result, lifoChunks, reference);
        return result;
    }
    
    private static String filters_regex = "((:\\w+[<\\w\\.>]*)*)?";
    private static int filters_regex_paren_count = 2;

    private static String tag_or_key_regex = "@?[\\w\\.]+\\??";
    private static int tag_or_key_regex_paren_count = 0;
    
    private static String parameter_and_filters_regex = "(" + tag_or_key_regex + ")" + filters_regex;
    private static int parameter_and_filters_regex_paren_count = 1 + tag_or_key_regex_paren_count + filters_regex_paren_count;
    
    private static String parameter_and_filters_list_regex = "(" + parameter_and_filters_regex + "(," + parameter_and_filters_regex + ")*)?";
    private static int  parameter_and_filters_list_regex_paren_count = 1 + parameter_and_filters_regex_paren_count + 1 + parameter_and_filters_regex_paren_count;
    
    //--------------------------------------------------------------------------------------------------------------11--------------------------1//////1--------11--------------------
    //---------------------------------1----------------2---345--------------67--------------------8-9--------------01--------------------------2//////3--------45--------------------
    static final String SIMPLE_EXPR = "('?" + tag_or_key_regex + ")(\\[(" + parameter_and_filters_list_regex + ")\\])?" +filters_regex;
    static final int se_tokg = 1;
    static final int se_pg = se_tokg + tag_or_key_regex_paren_count + 1;
    static final int se_fg = se_pg + 1 + parameter_and_filters_list_regex_paren_count + 1;
    private static final int se_paren_count = (se_fg - 1) + filters_regex_paren_count;

    private static final int gse_tokg = se_tokg;
    private static final int gse_pg = se_pg;
    private static final int gse_fg = se_fg;
    private static final int gse_paren_count = se_paren_count;
    
    //---------------------------------------------------/////////1----1-----1/////////2-----------22-------------------------------------------------------------------
    //-----------------------------------------1---2----3/////////4----5-----6/////////7-----------89-------------------------------------------------------------------
    private static final String MULTIP_EXPR = "(\\((" + SIMPLE_EXPR + "(," + SIMPLE_EXPR + ")*)\\))" + filters_regex; 
    private static final int me_pg = 1;
    private static final int me_fg = 1 + 1 + se_paren_count + 1 + se_paren_count + 1;
    private static final int me_paren_count = (me_fg - 1) + filters_regex_paren_count;

    private static final int gme_pg = gse_paren_count + me_pg; 
    private static final int gme_fg = gse_paren_count + me_fg;
    private static final int gme_paren_count = gse_paren_count + me_paren_count;
    //------------------------------------------------/////////1---------1/////////4------4---------------4------4------------------------------------------------------
    //------------------------------------------1----2/////////3---------4/////////2------3---------------4------5------------------------------------------------------
    private static final String BLOCK_EXPR_B = "(" + SIMPLE_EXPR + "|" + MULTIP_EXPR + ")#(true|false|list(<\\w+>(,\\w+)?)?|set<\\w+>)";
    private static final int teb_se_tokg = 1 + se_tokg;
    private static final int teb_se_pg = 1 + se_pg;
    private static final int teb_se_fg = 1 + se_fg;
    
    private static final int teb_me_pg = 1 + se_paren_count + me_pg;
    private static final int teb_me_fg = 1 + se_paren_count + me_fg;
    
    private static final int teb_tg = 1 + se_paren_count + me_paren_count + 1;
    private static final int teb_lastparen = teb_tg + 1 + 1;
    
    private static final int gteb_se_tokg = gme_paren_count + teb_se_tokg;
    private static final int gteb_se_pg = gme_paren_count + teb_se_pg;
    private static final int gteb_se_fg = gme_paren_count + teb_se_fg;
    private static final int gteb_me_pg = gme_paren_count + teb_me_pg;
    private static final int gteb_me_fg = gme_paren_count + teb_me_fg;
    private static final int gteb_tg = gme_paren_count + teb_tg;
    private static final int gteb_lastparen = gme_paren_count + teb_lastparen;

    //-------------------------------------------8----------------------------------------------------------------------------------------------------------------------
    //-------------------------------------------7----------------------------------------------------------------------------------------------------------------------
    private static final String BLOCK_EXPR_E = "#(true|false|list|set)";
    static final int tee_tg = 1;
    static final int gtee_tg = gteb_lastparen + tee_tg;
    //------------------------------------------------------------------------------------------------------------------------------------------------------------------
    //private static final String BLOCK_EXPR_E = "#(true|false|list|set)";
    private static final String TILDE_EXPR = "~~";

    static final String FINAL_EXPR = "~" + SIMPLE_EXPR + "~" + "|" + "~" + MULTIP_EXPR + "~" + "|" + "~" + BLOCK_EXPR_B + "~" + "|" + "~" + BLOCK_EXPR_E + "~" + "|" + TILDE_EXPR;

    List<Chunk> _parse(String positionInformation, List<Chunk> result, LinkedList<List<Chunk>> lifoChunks,
            String reference) throws TemplateException {
        Pattern p = Pattern.compile(FINAL_EXPR);
        Matcher m = p.matcher(reference);
        int from = 0;
        int to;
        do {
            if (m.find()) {
                to = m.start();
                result.add(new TextChunk(reference.substring(from, to)));
                String allMatchString = m.group(0);
                ExprVal valToAdd;
                if (allMatchString.contains("#")) {
                    if (!allMatchString.startsWith("~#")) {
                        String type = m.group(gteb_tg);
                        if (allMatchString.contains("(")) {
                            valToAdd = new ExprValMultiple(this, positionInformation, allMatchString);
                            ((ExprValMultiple) valToAdd).initialize(m, gteb_me_pg, gteb_me_fg);
                            List<Chunk> tmp = new ArrayList<Chunk>();
                            result.add(new BlockChunk(positionInformation, allMatchString, valToAdd, tmp, type));
                            lifoChunks.addLast(result);
                            result = tmp;
                        } else {
                            String parameters = m.group(gteb_se_pg);
                            if (parameters == null) {
                                valToAdd = new ExprValTag(this, positionInformation, allMatchString);
                                ((ExprValTag) valToAdd).initialize(m, gteb_se_tokg, gteb_se_fg);
                            } else {
                                valToAdd = new ExprValMessage(this, positionInformation, allMatchString);
                                ((ExprValMessage) valToAdd).initialize(m, gteb_se_tokg, gteb_se_pg, gteb_se_fg);
                            }
                            List<Chunk> tmp = new ArrayList<Chunk>();
                            result.add(new BlockChunk(positionInformation, allMatchString, valToAdd, tmp, type));
                            lifoChunks.addLast(result);
                            result = tmp;
                        }
                        from = m.end();
                    } else {
                        result = lifoChunks.removeLast();
                        int lg = result.size();
                        String closeType = m.group(gtee_tg);
                        BlockChunk openChunk = ((BlockChunk) result.get(lg - 1));
                        String openType = openChunk.type.replaceAll("<\\w+>", "");
                        if (!openType.equals(closeType)) {
                            throw new TemplateException(
                                    "Invalid syntax. The open tag '%s' doesn't match the close tag '%s'. Unable to render '%s' at position '%s'.",
                                    openType, closeType, openChunk.allMatchStr, openChunk.positionInformation);
                        }
                        from = m.end();
                    }
                    continue;
                }

                if (allMatchString.equals("~~")) {
                    result.add(new TextChunk("~"));
                    from = m.end();
                    continue;
                }

                if (allMatchString.contains("(")) {
                    valToAdd = new ExprValMultiple(this, positionInformation, allMatchString);
                    ((ExprValMultiple) valToAdd).initialize(m, gme_pg, gme_fg);
                } else {
                    String parameters = m.group(gse_pg);
                    if (parameters == null) {
                        valToAdd = new ExprValTag(this, positionInformation, allMatchString);
                        ((ExprValTag) valToAdd).initialize(m, gse_tokg, gse_fg);
                    } else {
                        valToAdd = new ExprValMessage(this, positionInformation, allMatchString);
                        ((ExprValMessage) valToAdd).initialize(m, gse_tokg, gse_pg, gse_fg);
                    }
                }

                ExprChunk chunk = new ExprChunk(valToAdd);
                result.add(chunk);
                
                from = m.end();
            } else {
                to = reference.length();
                result.add(new TextChunk(reference.substring(from, to)));
            }
        } while (to != reference.length());
        return result;
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

}
