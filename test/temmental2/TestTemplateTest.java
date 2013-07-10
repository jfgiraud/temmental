package temmental2;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.TestCase;
import static temmental2.TemplateUtils.createModel;
import static temmental2.TemplateUtils.createList;

public class TestTemplateTest extends TestCase {

    protected HashMap<String, Object> filters;
    private HashMap<String, Object> model;
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

    private String extractSection(String s) {
    	Pattern p = Pattern.compile("<!--\\s*#section\\s+([a-zA-Z0-9_]+)\\s*-->");
        Matcher m = p.matcher(s);
        if (m.find()) {
        	String before = s.substring(0, m.start());
        	System.out.println("BEFORE " + before);
        	String name = m.group(1);
        	int b = m.end();
        	while (m.find()) {
        		int e = m.start();
        		String after = s.substring(b, e);
        		System.out.println("AFTER " + after + " << " + name);
        		name = m.group(1);
//        		System.out.println(name);
        		b = m.end();
        	}
        	String after = s.substring(b);
        	System.out.println("AFTER " + after + " << " + name);
        	return null;
        } else {
        	return s;
        }
    }
    
    
    public void testSectionRe() {
    	assertEquals("", extractSection(""));
    	assertEquals("Something...", extractSection("Something..."));
//    	assertEquals("foo", extractSection("abc<!-- #section foo  -->def<!-- #section bar  -->ghi<!-- #section shi  -->klm"));
    	assertEquals("foo", extractSection("abc<!-- #section foo  -->def<!-- #section bar  -->ghi<!-- #section shi  -->"));
    }

    
    public void testPrintFile() throws IOException, TemplateException {

    	Template template = new Template("test/temmental2/test-file.tpl", filters, properties, Locale.ENGLISH);

        model = new HashMap<String, Object>();
        model.put("firstname", "John");
        model.put("lastname", "Doe");
        template.printFile(out, model);

        HashMap<String, Object> expectedModel = new HashMap<String, Object>();
        expectedModel.put("firstname", "John");
        expectedModel.put("lastname", "Doe");

        TemplateRecord record = TemplateRecorder.getTemplateRecordFor("test/temmental2/test-file.tpl");
        
        Map<String, ? extends Object> model = record.getModelForFile();
        assertEquals(expectedModel, model);
    }

    public void testPrintSection() throws IOException, TemplateException {

    	Template template = new Template("test/temmental2/test-sections.tpl", filters, properties, Locale.ENGLISH);
    	
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
