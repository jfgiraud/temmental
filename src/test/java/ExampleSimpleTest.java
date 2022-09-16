import com.github.jfgiraud.temmental.*;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.github.jfgiraud.temmental.TemplateMessages.createFrom;
import static org.junit.Assert.assertEquals;

public class ExampleSimpleTest {

    @Test
    public void testTemplate() throws IOException, NoSuchMethodException {
        String propertiesPath = "file:src/test/resources/example_simple.properties";
        Locale locale = Locale.FRENCH;

        Map<String, Object> transforms = new HashMap<String, Object>();
        transforms.put("upper", String.class.getDeclaredMethod("toUpperCase"));
        transforms.put("quote", new Transform<String, String>() {
            public String apply(String value) {
                return "'" + value + "'";
            }
        });
        transforms.put("replace", new ParamTransform<String[],String,String>() {
            public String apply(String[] values, String value) {
                return value.replace(values[0], values[1]);
            }
        });

        Template template = new Template("src/test/resources/example_simple.tpl", transforms, createFrom(locale, propertiesPath));

        Map<String, Object> model = new HashMap<String, Object>();
        model.put("firstname", "John");
        model.put("lastname", "Doe");

        StringWriter out = new StringWriter();
        template.printFile(out, model);

        String expected = "Hello John DOE!\n" +
                "Hello John DOE!\n" +
                "'Hell* J*hn DOE!'";

        assertEquals(expected, out.toString());
    }

}
