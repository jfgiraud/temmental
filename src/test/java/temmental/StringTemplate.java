
package temmental;


import java.io.*;
import java.io.Reader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class StringTemplate extends Template {

    /**
     * Create a template with the given parameters.
     *
     * @param expression   the path to the template file to parse
     * @param transforms the map of transform functions
     * @param properties the messages
     * @param locale     locale to use to format messages (date, numbers...)
     * @throws java.io.IOException       if an I/O error occurs when reading the template file
     * @throws temmental.TemplateException if an other error occurs when reading the template file
     */
    public StringTemplate(String expression, Map<String, ? extends Object> transforms, Properties properties, Locale locale)
            throws IOException, TemplateException {
        this(expression, transforms, new TemplateMessages(properties, locale));
    }


    private StringTemplate(String expression, Map<String, ? extends Object> transforms, TemplateMessages messages)
            throws IOException, TemplateException {
        super(null, transforms, messages);
        if (expression != null) {
            parseString(expression, true);
        }
    }

    /**
     * Create a template with the given parameters. The default locale is used to retrieve localized messages and format messages (date, numbers...).
     *
     * @param expression   the path to the template file to parse
     * @param transforms the map of transform functions
     * @param properties the messages
     * @throws java.io.IOException       if an I/O error occurs when reading the template file
     * @throws temmental.TemplateException if an other error occurs when reading the template file
     */
    public StringTemplate(String expression, Map<String, ? extends Object> transforms, Properties properties)
            throws IOException, TemplateException {
        this(expression, transforms, properties, Locale.getDefault());
    }

    public StringTemplate(String expression, Map<String, ? extends Object> transforms, Locale locale, Object... resourcesContainers)
            throws IOException, TemplateException {
        this(expression, transforms, new TemplateMessages(locale, resourcesContainers));
    }

    /**
     * Create a template with the given parameters.
     *
     * @param expression   the path to the template file to parse
     * @param transforms the map of transform functions
     * @param bundle     the messages
     * @throws java.io.IOException       if an I/O error occurs when reading the template file
     * @throws temmental.TemplateException if an other error occurs when reading the template file
     */
    public StringTemplate(String expression, Map<String, ? extends Object> transforms, ResourceBundle bundle)
            throws IOException, TemplateException {
        this(expression, transforms, new TemplateMessages(bundle));
    }

    /**
     * Create a template with the given parameters. The default locale is used to retrieve localized messages and format messages (date, numbers...).
     *
     * @param expression     the path to the template file to parse
     * @param transforms   the map of transform functions
     * @param resourcePath the messages (<code>classpath:path.to.my.file</code> or <code>file:/path/to/my/file.properties</code>)
     * @throws java.io.IOException       if an I/O error occurs when reading the template file
     * @throws temmental.TemplateException if an other error occurs when reading the template file
     */
    public StringTemplate(String expression, Map<String, ? extends Object> transforms, String resourcePath)
            throws IOException, TemplateException {
        this(expression, transforms, resourcePath, Locale.getDefault());
    }

    /**
     * Create a template with the given parameters.
     *
     * @param expression     the path to the template file to parse
     * @param transforms   the map of transform functions
     * @param resourcePath the messages (<code>classpath:path.to.my.file</code> or <code>file:/path/to/my/file.properties</code>)
     * @param locale       locale to retrieve localized messages and format messages (date, numbers...)
     * @throws java.io.IOException       if an I/O error occurs when reading the template file
     * @throws temmental.TemplateException if an other error occurs when reading the template file
     */
    public StringTemplate(String expression, Map<String, ? extends Object> transforms, String resourcePath, Locale locale)
            throws IOException, TemplateException {
        this(expression, transforms, new TemplateMessages(resourcePath, locale));
    }

    String format(Map<String, Object> model) throws IOException, TemplateException {
        StringWriter out = new StringWriter();
        printFile(out, model);
        return out.toString();
    }

}
