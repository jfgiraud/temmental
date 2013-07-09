package temmental2;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import junit.framework.TestCase;
import static temmental2.TemplateUtils.createModel;
import static temmental2.TemplateUtils.createList;

public class TestTemplateTest extends TestCase {

    protected HashMap<String, Object> filters;
    private HashMap<String, Object> model;
    protected Properties properties;
    protected StringWriter out;
    protected Template template;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        template = new Template("test/temmental2/test.tpl", filters, properties, Locale.ENGLISH);
        TemplateRecorder.setRecording(true);
        out = new StringWriter();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        TemplateRecorder.setRecording(false);
    }

    public void testPrintFile() throws IOException, TemplateException {

        model = new HashMap<String, Object>();
        model.put("firstname", "John");
        model.put("lastname", "Doe");
        template.printFile(out, model);

        HashMap<String, Object> expectedModel = new HashMap<String, Object>();
        expectedModel.put("firstname", "John");
        expectedModel.put("lastname", "Doe");

        TemplateRecord record = TemplateRecorder.getTemplateRecordFor("test/temmental2/test.tpl");
        
        Map<String, ? extends Object> model = record.getModelForFile();
        assertEquals(expectedModel, model);
    }

    public void testPrintSection() throws IOException, TemplateException {

        List<Map<String, Object>> list = createList(
                createModel("index", 0, "fruit", "orange"),
                createModel("index", 1, "fruit", "apple"),
                createModel("index", 2));

        model = new HashMap<String, Object>();
        model.put("fruits", list);
        model.put("firstname", "John");
        model.put("lastname", "Doe");
        template.printSection(out, "test", model);

        model = new HashMap<String, Object>();
        model.put("fruits", list);
        model.put("firstname", "Jane");
        model.put("lastname", "Doe");
        template.printSection(out, "test", model);

        TemplateRecord record = TemplateRecorder.getTemplateRecordFor("test/temmental/test2.tpl");
        List<Map<String, ? extends Object>> models = record.getModelsForSection("test");

        HashMap<String, Object> expectedModel = new HashMap<String, Object>();
        expectedModel.put("firstname", "John");
        expectedModel.put("lastname", "Doe");
        expectedModel.put("fruits", list);
        assertEquals(expectedModel, models.get(0));

        expectedModel = new HashMap<String, Object>();
        expectedModel.put("firstname", "Jane");
        expectedModel.put("lastname", "Doe");
        expectedModel.put("fruits", list);
        assertEquals(expectedModel, models.get(1));
    }

}
