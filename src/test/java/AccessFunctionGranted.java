import org.junit.Before;
import org.junit.Test;
import temmental.Template;
import temmental.TemplateMessages;
import temmental.Transform;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static temmental.TemplateUtils.createModel;

public class AccessFunctionGranted {

    protected Map<String, Object> model;
    public Map<String, Object> transforms;
    protected Properties properties;
    protected TemplateMessages messages;

    @Before
    public void setUp() throws Exception {
        model = new HashMap<String, Object>();
        transforms = new HashMap<String, Object>();
        properties = new Properties();
        messages = new TemplateMessages(Locale.FRANCE, properties);
    }

    public void populateTransform(String key, Transform value) {
        transforms.put(key, value);
    }

    @Test
    public void testFoo() throws IOException {
        populateTransform("upper", new Transform<String, String>() {
            public String apply(String value) {
                return value.toUpperCase();
            }
        });
        Template template = new Template("src/test/resources/temmental/test-function.tpl", transforms, properties, Locale.ENGLISH);
        model = createModel("name", "jeff");

        assertEquals("JEFF", template.formatForTest("~$name:'upper~", model));
        StringWriter out = new StringWriter();
        template.printFile(out, model);
        out.flush();
        assertEquals("JEFF", out.toString());

    }
}
