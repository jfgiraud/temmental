package temmental2;

import junit.framework.TestCase;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

import static temmental2.TemplateUtils.createList;
import static temmental2.TemplateUtils.createModel;

public class TestTemplateTest extends TestCase {

    protected HashMap<String, Object> filters;
    private Map<String, Object> model;
    protected Properties properties;
    protected StringWriter out;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        TemplateRecorder.setRecording(true);
        out = new StringWriter();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        TemplateRecorder.setRecording(false);
    }

    public void testPrintFile() throws IOException, TemplateException {

    	Template template = new Template("src/test/resources/temmental2/test-file.tpl", filters, properties, Locale.ENGLISH);

        model = new HashMap<String, Object>();
        model.put("firstname", "John");
        model.put("lastname", "Doe");
        template.printFile(out, model);

        HashMap<String, Object> expectedModel = new HashMap<String, Object>();
        expectedModel.put("firstname", "John");
        expectedModel.put("lastname", "Doe");

        TemplateRecord record = TemplateRecorder.getTemplateRecordFor("src/test/resources/temmental2/test-file.tpl");
        
        Map<String, ? extends Object> model = record.getModelForFile();
        assertEquals(expectedModel, model);
    }

    public void testPrintSection() throws IOException, TemplateException {

    	Template template = new Template("src/test/resources/temmental2/test-sections.tpl", filters, properties, Locale.ENGLISH);

        template.printStructure(System.out);

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

        
        TemplateRecord record = TemplateRecorder.getTemplateRecordFor("src/test/resources/temmental2/test-sections.tpl");
        
        List<Map<String, ? extends Object>> models = record.getModelsForSection("first");

        HashMap<String, Object> expectedModel = new HashMap<String, Object>();
        expectedModel.put("firstname", "John");
        expectedModel.put("lastname", "Doe");
        assertEquals(expectedModel, models.get(0));

        expectedModel = new HashMap<String, Object>();
        expectedModel.put("firstname", "Jane");
        expectedModel.put("lastname", "Doe");
        assertEquals(expectedModel, models.get(1));
        
        models = record.getModelsForSection("second");
        expectedModel = new HashMap<String, Object>();
        expectedModel.put("fruits", list);
        assertEquals(expectedModel, models.get(0));
    }

    public void testCommand() throws IOException, TemplateException {
        Template template = new Template("src/test/resources/temmental2/test-sections.tpl", filters, properties, Locale.ENGLISH);
        List<Map<String, Object>> elements = createList(
                createModel("elem", 1),
                createModel("elem", 2),
                createModel("elem", 3)
        );
        model = createModel("l", elements, "elem", "before");
        assertEquals("before<1><2><3>before", template.formatForTest("~$elem~~$l#for~<~$elem~>~#for~~$elem~", model));
    }

}
