import com.github.jfgiraud.temmental.*;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.*;

import static com.github.jfgiraud.temmental.TemplateMessages.createFrom;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class BadCharsetTest {

    private Map<String, Object> transforms;
    private Template template;
    private Locale locale;
    private String propertiesPath;

    @Before
    public void setUp() throws IOException {
        locale = Locale.FRENCH;
        propertiesPath = "file:src/test/resources/example.properties";
        transforms = new HashMap<String, Object>();
    }

    @Test
    public void testTemplateBadCharset() throws IOException {
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("country", "France");

        try {
            template = new Template("src/test/resources/bad_charset.tpl", transforms, createFrom(locale, propertiesPath));
            StringWriter out = new StringWriter();
            template.printFile(out, model);
            fail("An exception must be raised.");
        } catch (TemplateException te) {
            assertEquals("Sentence not closed at position 'src/test/resources/bad_charset.tpl:l1:c20'.", te.getMessage());
        }
    }

    @Test
    public void testTemplateGoodCharset() throws IOException {
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("country", "France");

        try {
            template = new Template("src/test/resources/bad_charset.tpl", Charset.forName("ISO-8859-1"), transforms, createFrom(locale, propertiesPath));
            StringWriter out = new StringWriter();
            template.printFile(out, model);
        } catch (TemplateException te) {
            fail("No exception must be raised.");
        }
    }

}
