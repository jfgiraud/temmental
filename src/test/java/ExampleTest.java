import com.github.jfgiraud.temmental.StringUtils;
import com.github.jfgiraud.temmental.Template;
import com.github.jfgiraud.temmental.Transform;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ExampleTest {

    private Map<String, Object> filters;
    private Template template;
    private Locale locale = Locale.FRENCH;

    @Before
    public void setUp() throws NoSuchMethodException, IOException {
        filters = new HashMap<String, Object>();
        filters.put("upper", String.class.getDeclaredMethod("toUpperCase"));
        filters.put("titleize", StringUtils.class.getDeclaredMethod("titleize", String.class));
        filters.put("sdf", new Transform<String[], Transform<Date,String>>() {
            public Transform<Date, String> apply(final String[] objects) {
                return new Transform<Date, String>() {
                    public String apply(Date value) {
                        return new SimpleDateFormat(objects[0], locale).format(value);
                    }
                };
            }
        });

        template = new Template("src/test/resources/example.tpl", filters, "file:src/test/resources/example_fr.properties", locale);
    }

    @Test
    public void testTemplate() throws IOException, ParseException {
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("firstName", "John");
        model.put("lastName", "Doe");
        model.put("streetLines", Arrays.asList("Appartement 26", "2 allée des Hirondelles"));
        model.put("zip", "33320");
        model.put("city", "Le Taillan-Médoc");
        model.put("clientNumber", "12345678");
        model.put("lineNumber", "+33687654321");
        model.put("date", new GregorianCalendar(locale).getTime());
        model.put("inscription", new SimpleDateFormat("yyyy/MM/dd hh:mm:ss").parse("2010/11/05 15:26:34"));

        StringWriter out = new StringWriter();
        template.printSection(out, "header", model);
        System.out.println(out.toString());

    }

    @Test
    public void testFoo() throws IOException, ParseException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method m = StringUtils.class.getDeclaredMethod("capitalize", String.class);
        Object r = m.invoke(StringUtils.class, "toto");
        System.out.println(r);
    }
}
