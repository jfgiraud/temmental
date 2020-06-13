import com.github.jfgiraud.temmental.*;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ExampleTest {

    private Map<String, Object> transforms;
    private Template template;
    private Locale locale;
    private String propertiesPath;

    @Before
    public void setUp() throws NoSuchMethodException, IOException {
        setLanguage(Locale.FRENCH);
        transforms = new HashMap<String, Object>();
        transforms.put("upper", String.class.getDeclaredMethod("toUpperCase"));
        transforms.put("size", Transforms.SIZE);
        transforms.put("gender", new ParamTransform<String[], Character, String>() {
            @Override
            public String apply(String[] values, Character c) {
                if (c == 'f')
                    return values[0];
                if (c == 'm')
                    return values[1];
                return StringUtils.join(", ", Arrays.asList(values[0], values[1]));
            }
        });
        transforms.put("titleize", StringUtils.class.getDeclaredMethod("titleize", String.class));
        transforms.put("date_formatter", new Transform<String[], Transform<Date, String>>() {
            public Transform<Date, String> apply(final String[] objects) {
                return new Transform<Date, String>() {
                    public String apply(Date value) {
                        return new SimpleDateFormat(objects[0], locale).format(value);
                    }
                };
            }
        });
        transforms.put("toModel", TemplateUtils.getDeclaredMethod(ConvertToModel.class, "toModel", null));
        transforms.put("add", Transforms.ADD);

        template = new Template("src/test/resources/example.tpl", transforms, propertiesPath, locale);
    }

    private void setLanguage(Locale locale) {
        this.locale = locale;
        propertiesPath = "file:src/test/resources/example.properties";
    }

    class Option implements ConvertToModel {

        private final String type;
        private String label;
        public int number;
        public double price;

        public Option(String label, double value) {
            this(label, value, 0, "");
        }

        public Option(String label, double value, int number, String type) {
            this.label = label;
            this.price = value;
            this.number = number;
            this.type = type;
        }

        public Map<String, Object> toModel() {
            return TemplateUtils.createModel("label", label, "price", price, "quantity", number,
                    "unit", type);
        }
    }

    @Test
    public void testTemplate() throws IOException, ParseException {
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("firstName", "John");
        model.put("lastName", "Doe");
        model.put("streetLines", Arrays.asList("Appartement 26", "2 allée des Hirondelles"));
        model.put("zip", "33320");
        model.put("email", "john.doe@example.com");
        model.put("city", "Le Taillan-Médoc");
        model.put("clientNumber", "12345678");
        model.put("lineNumber", "+33687654321");
        model.put("date", new GregorianCalendar(locale).getTime());
        model.put("inscription", new SimpleDateFormat("yyyy/MM/dd hh:mm:ss").parse("2010/11/05 15:26:34"));

        StringWriter out = new StringWriter();
        template.printSection(out, "header", model);

        model.clear();
        model.put("genre", 'm');
        List<Option> options = Arrays.asList(new Option("Appels", 25.2, 7200, "duration"), new Option("Envoi SMS", 3.5, 25, "sms"));
        model.put("options", options);
        double totaux = 0;
        for (Option o : options) {
            totaux += o.price;
        }
        model.put("totaux", new Option("Totaux", totaux));
        template.printSection(out, "body", model);
        System.out.println(out.toString());

    }

}
