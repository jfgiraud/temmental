package com.github.jfgiraud.temmental;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

import static com.github.jfgiraud.temmental.TemplateMessages.createFrom;
import static com.github.jfgiraud.temmental.TemplateUtils.createList;
import static com.github.jfgiraud.temmental.TemplateUtils.createModel;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestTemplateTest {

    protected HashMap<String, Object> transforms;
    private Map<String, Object> model;
    protected Properties properties;
    protected StringWriter out;

    @Before
    public void setUp() throws Exception {
        transforms = new HashMap<String, Object>();
        transforms.put("concat", String.class.getDeclaredMethod("concat", String.class));
        properties = new Properties();
        properties.put("hello", "Bonjour");
        TemplateRecorder.setRecording(true);
        out = new StringWriter();
    }

    @After
    public void tearDown() throws Exception {
        TemplateRecorder.setRecording(false);
    }

    @Test
    public void testPrintFile() throws IOException, TemplateException {

        Template template = new Template("src/test/resources/temmental/test-file.tpl", transforms, createFrom(properties));

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

    @Test
    public void testPrintSection() throws IOException, TemplateException {

        Template template = new Template("src/test/resources/temmental/test-sections.tpl", transforms, createFrom(properties));

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

    @Test
    public void testBetweenSections() throws IOException, TemplateException {
        Template template = new Template("src/test/resources/temmental/test-sections.tpl", transforms, createFrom(properties));
        StringWriter out = new StringWriter();
        template.printSection(out, "third");
        template.printSection(out, "third");
        template.printSection(out, "fourth");
        assertEquals("3e  \n3e  \n4e", out.toString());
    }

    @Test
    public void testCommandFor() throws IOException, TemplateException {
        StringTemplate template = new StringTemplate("~$elem~~$l#for~<~$elem~>~#for~~$elem~", transforms, properties);
        List<Map<String, Object>> elements = createList(
                createModel("elem", 1),
                createModel("elem", 2),
                createModel("elem", 3)
        );
        model = createModel("l", elements, "elem", "before");
        assertEquals("before<1><2><3>before", template.format(model));
    }

    @Test
    public void testCommandEnum() throws IOException, TemplateException {
        StringTemplate template = new StringTemplate("~$elem~~$l#enum<'index>~~$index~<~$elem~>~#enum~~$elem~", transforms, properties);
        List<Map<String, Object>> elements = createList(
                createModel("elem", 1),
                createModel("elem", 2),
                createModel("elem", 3)
        );
        model = createModel("l", elements, "elem", "before");
        assertEquals("before0<1>1<2>2<3>before", template.format(model));
    }

    @Test
    public void testCommandEnumForQuote() throws IOException, TemplateException {
        StringTemplate template = new StringTemplate("~$elem~~$l#enum<'index,'elem>~~$index~<~$elem~>~#enum~~$elem~", transforms, properties);
        List<Integer> elements = Arrays.asList(10, 20, 30);
        model = createModel("l", elements, "elem", "before");
        assertEquals("before0<10>1<20>2<30>before", template.format(model));
    }

    @Test
    public void testCommandForQuote() throws IOException, TemplateException {
        StringTemplate template = new StringTemplate("~$elem~~$l#for<'elem>~<~$elem~>~#for~~$elem~", transforms, properties);
        List<Integer> elements = Arrays.asList(1, 2, 3);
        model = createModel("l", elements, "elem", "before");
        assertEquals("before<1><2><3>before", template.format(model));
    }

    @Test
    public void testBackSlashInTextContext() throws IOException, TemplateException {
        StringTemplate template = new StringTemplate("^[\\w]*$", transforms, properties);
        model = createModel();
        assertEquals("^[\\w]*$", template.format(model));
    }

    @Test
    public void testCommandForIndirection() throws IOException, TemplateException {
        StringTemplate template = new StringTemplate("~$it#for<'branch>~#~$branch~#~\"elem_\":'concat<$branch>#set<'l>~~$l~#~$$l~~#set~\n~#for~", transforms, properties);
        model = createModel("it", Arrays.asList("b", "b1", "b2"), "elem_b", "VALUE_B", "elem_b1", "VALUE_B1", "elem_b2", "VALUE_B2");
        assertEquals("#b#elem_b#VALUE_B\n#b1#elem_b1#VALUE_B1\n#b2#elem_b2#VALUE_B2\n", template.format(model));
    }

    @Test
    public void testCommandForVariable() throws IOException, TemplateException {
        StringTemplate template = new StringTemplate("~$elem~~$l#for<$elem>~<~$before~>~#for~~$elem~", transforms, properties);
        List<Integer> elements = Arrays.asList(1, 2, 3);
        model = createModel("l", elements, "elem", "before");
        assertEquals("before<1><2><3>before", template.format(model));
    }

    @Test
    public void testCommandSet() throws IOException, TemplateException {
        StringTemplate template = new StringTemplate("~$elem~~$newvalue#set<'elem>~<~$elem~>~$l#for~<~$elem~>~#for~<~$elem~>~#set~~$elem~", transforms, properties);
        model = createModel("elem", "before",
                "newvalue", "after",
                "l", createList(
                        createModel("elem", 1),
                        createModel("elem", 2),
                        createModel("elem", 3)
                ));
        assertEquals("before<after><1><2><3><after>before", template.format(model));
    }

    @Test
    public void testCommandSetWithIndirection() throws IOException, TemplateException {
        StringTemplate template = new StringTemplate("~$elem~~$newvalue#set<$elem>~<~$elem~><~$before?~>~#set~~$elem~", transforms, properties);
        model = createModel("elem", "before", "newvalue", "after");
        assertEquals("before<before><after>before", template.format(model));
    }

    @Test
    public void testCommandTrueCondIsTrue() throws IOException, TemplateException {
        StringTemplate template = new StringTemplate("~$cond#true~hello ~$name~~#true~", transforms, properties);
        model = createModel("cond", true, "name", "jeff");
        assertEquals("hello jeff", template.format(model));
    }

    @Test
    public void testCommandTrueCondIsFalse() throws IOException, TemplateException {
        StringTemplate template = new StringTemplate("~$cond#true~hello ~$name~~#true~", transforms, properties);
        model = createModel("cond", false, "name", "jeff");
        assertEquals("", template.format(model));
    }

    @Test
    public void testCommandFalseCondIsTrue() throws IOException, TemplateException {
        StringTemplate template = new StringTemplate("~$cond#false~hello ~$name~~#false~", transforms, properties);
        model = createModel("cond", true, "name", "jeff");
        assertEquals("", template.format(model));
    }

    @Test
    public void testCommandFalseCondIsFalse() throws IOException, TemplateException {
        StringTemplate template = new StringTemplate("~$cond#false~hello ~$name~~#false~", transforms, properties);
        model = createModel("cond", false, "name", "jeff");
        assertEquals("hello jeff", template.format(model));
    }

    @Test
    public void testMessageDefault() throws IOException, TemplateException {
        properties.put("hello", "Bonjour {0}");
        StringTemplate template = new StringTemplate("~$message[$name]!\"text\"¡~", transforms, properties);
        model = createModel("message", false, "name", "jeff");
        assertEquals("text", template.format(model));
    }

    @Test
    public void testMessageOptional() throws IOException, TemplateException {
        properties.put("hello", "Bonjour {0}");
        StringTemplate template = new StringTemplate("~$message?[$name]~", transforms, properties);
        model = createModel("name", "jeff");
        assertEquals("", template.format(model));
    }

    @Test
    public void testDefaultAcceptedWhenAFunctionIsUsedAndReturnsNull() throws IOException, TemplateException {
        properties.put("hello", "Bonjour {0}");
        transforms.put("null", new Transform<String, String>() {
            public String apply(String value) {
                return null;
            }
        });
        StringTemplate template = new StringTemplate("~$message[$name]:'null!123¡~", transforms, properties);
        model = createModel("message", "hello", "name", "jeff");
        assertEquals("123", template.format(model));
    }

    @Test
    public void testCommandDefaultValueTrue() throws IOException, TemplateException {
        transforms.put("id", new Transform<Object, Object>() {
            public Object apply(Object value) {
                return value;
            }
        });
        StringTemplate template = new StringTemplate("~$cond!true¡#false~hello ~$name~~#false~", transforms, properties);
        model = createModel("name", "jeff");
        assertEquals("", template.format(model));
    }

    @Test
    public void testCommandDefaultValueFalse() throws IOException, TemplateException {
        transforms.put("id", new Transform<Object, Object>() {
            public Object apply(Object value) {
                return value;
            }
        });
        StringTemplate template = new StringTemplate("~$cond!false¡#false~hello ~$name~~#false~", transforms, properties);
        model = createModel("name", "jeff");
        assertEquals("hello jeff", template.format(model));
    }

    @Test
    public void testCharAt() throws IOException, TemplateException, NoSuchMethodException {
        StringTemplate template = new StringTemplate("Some text...~$data:'indexOf<'o'>~And after", transforms, properties);
        transforms.put("indexOf", String.class.getDeclaredMethod("indexOf", int.class));
        model = createModel("data", "the key is 'open'");
        assertEquals("Some text...12And after", template.format(model));
    }

    @Test
    public void testCharAtSQ() throws IOException, TemplateException, NoSuchMethodException {
        StringTemplate template = new StringTemplate("Some text...~$data:'indexOf<'\\''>~And after", transforms, properties);
        transforms.put("indexOf", String.class.getDeclaredMethod("indexOf", int.class));
        model = createModel("data", "the key is 'open'");
        assertEquals("Some text...11And after", template.format(model));
    }

    @Test
    public void testOptionalWithSettedAndNull() throws IOException, TemplateException {
        StringTemplate template = new StringTemplate("Some text...~$data?:'id~And after", transforms, properties);
        transforms.put("id", new Transform<Object, Object>() {
            public Object apply(Object value) {
                return value;
            }
        });
        model = createModel("data", null);
        assertEquals("Some text...And after", template.format(model));
    }

    @Test
    public void testDefaultWithSettedAndNull() throws IOException, TemplateException {
        StringTemplate template = new StringTemplate("Some text...~$data:'id!\"xxx\"¡~And after", transforms, properties);
        transforms.put("id", new Transform<Object, Object>() {
            public Object apply(Object value) {
                return value;
            }
        });
        model = createModel("data", null);
        assertEquals("Some text...xxxAnd after", template.format(model));
    }

    @Test
    public void testMessage() throws IOException, TemplateException {
        properties.put("key", "0:{0} 1:{1} 2:{2} 3:{3}");
        StringTemplate template = new StringTemplate("<<<~'key[$before,@$parameters,$after]~>>>", transforms, properties);
        model = createModel("before", "BEFORE", "parameters", Arrays.asList("zero", "one"), "after", "AFTER");
        assertEquals("<<<0:BEFORE 1:zero 2:one 3:AFTER>>>", template.format(model));
    }

    @Test
    public void testMessageEmpty() throws IOException, TemplateException {
        properties.put("key", "0:{0} 1:{1} 2:{2} 3:{3}");
        StringTemplate template = new StringTemplate("<<<~'key[$before,@$parameters,$after]~>>>", transforms, properties);
        model = createModel("before", "BEFORE", "parameters", Collections.emptyList(), "after", "AFTER");
        assertEquals("<<<0:BEFORE 1:AFTER 2:{2} 3:{3}>>>", template.format(model));
    }

    @Test
    public void testMessageEmpty2() throws IOException, TemplateException {
        properties.put("key", "0:{0} 1:{1}");
        StringTemplate template = new StringTemplate("<<<~'key[@$parameters]~>>>", transforms, properties);
        model = createModel("parameters", Collections.emptyList());
        assertEquals("<<<0:{0} 1:{1}>>>", template.format(model));
    }

    @Test
    public void testMessageWithArray() throws IOException, TemplateException {
        properties.put("key", "0:{0} 1:{1} 2:{2} 3:{3}");
        StringTemplate template = new StringTemplate("~($a,$b)#set<'parameters>~<<<~'key[$before,@$parameters,$after]~>>>~#set~", transforms, properties);
        model = createModel("before", "BEFORE", "a", "zero", "b", "one", "after", "AFTER");
        assertEquals("<<<0:BEFORE 1:zero 2:one 3:AFTER>>>", template.format(model));
    }

    @Test
    public void testStrip() throws IOException, TemplateException {
        {
            StringTemplate template = new StringTemplate("before   \n    ~|lt|$var?~  \n   after", transforms, properties);
            model = createModel("var", "azerty");
            assertEquals("before   \nazerty  \n   after", template.format(model));
        }
        {
            StringTemplate template = new StringTemplate("before   \n    ~|rt|$var?~  \n   after", transforms, properties);
            model = createModel("var", "azerty");
            assertEquals("before   \n    azerty   after", template.format(model));
        }
        {
            StringTemplate template = new StringTemplate("before   \n    ~|t|$var?~  \n   after", transforms, properties);
            model = createModel("var", "azerty");
            assertEquals("before   \nazerty   after", template.format(model));
        }
        {
            StringTemplate template = new StringTemplate("before   \n    ~|lt|$var?~  \n   after", transforms, properties);
            model = createModel();
            assertEquals("before   \n  \n   after", template.format(model));
        }
        {
            StringTemplate template = new StringTemplate("before   \n    ~|rt|$var?~  \n   after", transforms, properties);
            model = createModel();
            assertEquals("before   \n       after", template.format(model));
        }
        {
            StringTemplate template = new StringTemplate("before   \n    ~|t|$var?~  \n   after", transforms, properties);
            model = createModel();
            assertEquals("before   \n   after", template.format(model));
        }
        {
            StringTemplate template = new StringTemplate("before   \n    ~|lt|$var?~\n\nafter", transforms, properties);
            model = createModel("var", "azerty");
            assertEquals("before   \nazerty\n\nafter", template.format(model));
        }
    }
}