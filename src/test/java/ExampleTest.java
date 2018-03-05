import com.github.jfgiraud.temmental.*;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Stream;

public class ExampleTest {

    private Map<String, Object> filters;
    private Template template;
    private Locale locale = Locale.FRENCH;

    @Before
    public void setUp() throws NoSuchMethodException, IOException {
        filters = new HashMap<String, Object>();
        filters.put("upper", String.class.getDeclaredMethod("toUpperCase"));
        filters.put("genre", new ParamTransform<String[], Character, String>() {
            @Override
            public String apply(String[] values, Character c) {
                if (c == 'f')
                    return values[0];
                if (c == 'm')
                    return values[1];
                return StringUtils.join(", ", Arrays.asList(values[0], values[1]));
            }
        });
        filters.put("titleize", StringUtils.class.getDeclaredMethod("titleize", String.class));
        filters.put("date_formatter", new Transform<String[], Transform<Date,String>>() {
            public Transform<Date, String> apply(final String[] objects) {
                return new Transform<Date, String>() {
                    public String apply(Date value) {
                        return new SimpleDateFormat(objects[0], locale).format(value);
                    }
                };
            }
        });
        filters.put("toModel", TemplateUtils.getDeclaredMethod(ConvertToModel.class, "toModel", null));
        filters.put("add", Transforms.ADD);

        template = new Template("src/test/resources/example.tpl", filters, "file:src/test/resources/example_fr.properties", locale);
    }

    class Option implements ConvertToModel {

        private String label;
        public double value;

        public Option(String label, double value) {
            this.label = label;
            this.value = value;
        }

        public Map<String, Object> toModel() {
            return TemplateUtils.createModel("label", label, "value", value);
        }
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

        model.clear();
        model.put("genre", 'm');
        List<Option> options = Arrays.asList(new Option("Appels", 25.2), new Option("Envoi SMS", 3.5));
        model.put("options", options);
//        model.put("totaux", new Option("Totaux", options.stream().map(o -> o.value).mapToDouble(Double::doubleValue).sum()));
        double totaux = 0;
        for (Option o : options) {
            totaux += o.value;
        }
        model.put("totaux", new Option("Totaux", totaux));
        template.printSection(out, "body", model);
        System.out.println(out.toString());

    }

}
