import com.github.jfgiraud.temmental.Template;
import com.github.jfgiraud.temmental.Transform;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Example {

    private Map<String, Object> filters;
    private Template template;
    private Locale locale = Locale.FRENCH;

    @Before
    public void setUp() throws NoSuchMethodException, IOException {
        filters = new HashMap<String, Object>();
        filters.put("upper", String.class.getDeclaredMethod("toUpperCase"));
        filters.put("sdf", new Transform<String[], Transform<GregorianCalendar,String>>() {
            public Transform<GregorianCalendar, String> apply(final String[] objects) {
                return new Transform<GregorianCalendar, String>() {
                    public String apply(GregorianCalendar value) {
                        return new SimpleDateFormat(objects[0], locale).format(value.getTime());
                    }
                };
            }
        });

        template = new Template("src/test/resources/example.tpl", filters, "file:src/test/resources/example_fr.properties", locale);
    }

    @Test
    public void testTemplate() throws IOException {
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("firstName", "John");
        model.put("lastName", "Doe");
        model.put("street", "2 allée des Hirondelles");
        model.put("zip", "33320");
        model.put("city", "Le Taillan-Médoc");
        model.put("clientNumber", "12345678");
        model.put("lineNumber", "+33687654321");
        model.put("date", new GregorianCalendar(locale));
        StringWriter out = new StringWriter();
        template.printSection(out, "header", model);
        System.out.println(out.toString());

    }
}
