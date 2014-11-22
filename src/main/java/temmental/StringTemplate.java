package temmental;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

public class StringTemplate extends Template {

    public StringTemplate(String text, Map<String, ? extends Object> transforms, Properties properties, Locale locale) throws IOException, TemplateException {
        super(null, transforms, properties, locale);
        parseString(text, true);
    }

    public StringTemplate(String text, HashMap<String, Transform> filters, String resourcePath, Locale locale) throws IOException {
        super(null, filters, resourcePath, locale);
        parseString(text, true);
    }

    public String format(Map<String, ? extends Object> model) throws IOException {
        Writer out = new StringWriter();
        printFile(out, model);
        return out.toString();
    }

}
