package temmental;

import junit.framework.TestCase;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

import static temmental.TemplateUtils.createList;
import static temmental.TemplateUtils.createModel;

public class TestTemplateTest extends TestCase {

    protected HashMap<String, Object> filters;
    private Map<String, Object> model;
    protected Properties properties;
    protected StringWriter out;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        filters = new HashMap<String, Object>();
        properties = new Properties();
        properties.put("hello", "Bonjour");
        TemplateRecorder.setRecording(true);
        out = new StringWriter();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        TemplateRecorder.setRecording(false);
    }

    public void testPrintFile() throws IOException, TemplateException {

        Template template = new Template("src/test/resources/temmental/test-file.tpl", filters, properties, Locale.ENGLISH);

        model = new HashMap<String, Object>();
        model.put("firstname", "John");
        model.put("lastname", "Doe");
        template.printFile(out, model);

        HashMap<String, Object> expectedModel = new HashMap<String, Object>();
        expectedModel.put("firstname", "John");
        expectedModel.put("lastname", "Doe");

        TemplateRecord record = TemplateRecorder.getTemplateRecordFor("src/test/resources/temmental/test-file.tpl");

        PrintCall call = record.getPrintCallForFile();
        assertEquals(expectedModel, call.getModel());
    }

    public void testPrintSection() throws IOException, TemplateException {

        Template template = new Template("src/test/resources/temmental/test-sections.tpl", filters, properties, Locale.ENGLISH);

        List<Map<String, Object>> list = createList(
                createModel("index", 0, "fruit", "orange"),
                createModel("index", 1, "fruit", "apple"),
                createModel("index", 2));

        model = new HashMap<String, Object>();
        model.put("firstname", "John");
        model.put("lastname", "Doe");
        template.printSection(out, "first", model);

        model = new HashMap<String, Object>();
        model.put("firstname", "Jane");
        model.put("lastname", "Doe");
        template.printSection(out, "first", model);

        model = new HashMap<String, Object>();
        model.put("fruits", list);
        template.printSection(out, "second", model);


        TemplateRecord record = TemplateRecorder.getTemplateRecordFor("src/test/resources/temmental/test-sections.tpl");

        List<PrintCall> calls = record.getPrintCallsForSection("first");

        HashMap<String, Object> expectedModel = new HashMap<String, Object>();
        expectedModel.put("firstname", "John");
        expectedModel.put("lastname", "Doe");
        assertEquals(expectedModel, calls.get(0).getModel());

        expectedModel = new HashMap<String, Object>();
        expectedModel.put("firstname", "Jane");
        expectedModel.put("lastname", "Doe");
        assertEquals(expectedModel, calls.get(1).getModel());

        calls = record.getPrintCallsForSection("second");
        expectedModel = new HashMap<String, Object>();
        expectedModel.put("fruits", list);
        assertEquals(expectedModel, calls.get(0).getModel());
    }

    public void testCommandFor() throws IOException, TemplateException {
        StringTemplate template = new StringTemplate("~$elem~~$l#for~<~$elem~>~#for~~$elem~", filters, properties, Locale.ENGLISH);
        List<Map<String, Object>> elements = createList(
                createModel("elem", 1),
                createModel("elem", 2),
                createModel("elem", 3)
        );
        model = createModel("l", elements, "elem", "before");
        assertEquals("before<1><2><3>before", template.format(model));
    }

    public void testCommandTrue() throws IOException, TemplateException {
        StringTemplate template = new StringTemplate("~$cond#true~hello ~$name~~#true~", filters, properties, Locale.ENGLISH);
        model = createModel("cond", true, "name", "jeff");
        assertEquals("hello jeff", template.format(model));
        model = createModel("cond", false, "name", "jeff");
        assertEquals("", template.format(model));
    }

    public void testCommandFalse() throws IOException, TemplateException {
        StringTemplate template = new StringTemplate("~$cond#false~hello ~$name~~#false~", filters, properties, Locale.ENGLISH);
        model = createModel("cond", true, "name", "jeff");
        assertEquals("", template.format(model));
        model = createModel("cond", false, "name", "jeff");
        assertEquals("hello jeff", template.format(model));
    }

    public void testCommandDefaultValue() throws IOException, TemplateException {
        filters.put("id", new Transform<Object, Object>() {
            public Object apply(Object value) {
                return value;
            }
        });
        StringTemplate template = new StringTemplate("~$cond!true#false~hello ~$name~~#false~", filters, properties, Locale.ENGLISH);
        model = createModel("name", "jeff");
        assertEquals("", template.format(model));

        template = new StringTemplate("~$cond!false#false~hello ~$name~~#false~", filters, properties, Locale.ENGLISH);
        model = createModel("name", "jeff");
        assertEquals("hello jeff", template.format(model));
    }
}
