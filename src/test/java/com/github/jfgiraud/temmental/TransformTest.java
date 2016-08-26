package com.github.jfgiraud.temmental;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import static com.github.jfgiraud.temmental.TemplateUtils.createModel;
import static org.junit.Assert.assertEquals;

public class TransformTest {

    protected Map<String, Object> transforms;
    private Map<String, Object> model;
    protected Properties properties;
    protected StringWriter out;

    @Before
    public void setUp() throws Exception {
        transforms = new HashMap();
        transforms.put("if", Transforms.IF);
        transforms.put("ifn", Transforms.IF_NOT);
        transforms.put("not", Transforms.NOT);
        transforms.put("upper", Transforms.UPPER);
        transforms.put("lower", Transforms.LOWER);
        transforms.put("eq", Transforms.EQUALS);
        properties = new Properties();
        TemplateRecorder.setRecording(true);
        out = new StringWriter();
    }

    @After
    public void tearDown() throws Exception {
        TemplateRecorder.setRecording(false);
    }


    @Test
    public void testIfTwoArgs() throws IOException {
        StringTemplate template = new StringTemplate("before~$bool:'if<\"##true-block##\",\"##false-block##\">:'upper~after", transforms, properties, Locale.ENGLISH);
        model = createModel("bool", true);
        assertEquals("before##TRUE-BLOCK##after", template.format(model));
        model = createModel("bool", false);
        assertEquals("before##FALSE-BLOCK##after", template.format(model));
    }

    @Test
    public void testIfOneArg() throws IOException {
        StringTemplate template = new StringTemplate("before~$bool:'if<\"##true-block##\">:'upper~after", transforms, properties, Locale.ENGLISH);
        model = createModel("bool", true);
        assertEquals("before##TRUE-BLOCK##after", template.format(model));
        model = createModel("bool", false);
        assertEquals("beforeafter", template.format(model));
    }

    @Test
    public void testNot() throws IOException {
        StringTemplate template = new StringTemplate("before~$bool:'not:'if<\"##FALSE-BLOCK##\",\"##TRUE-BLOCK##\">:'lower~after", transforms, properties, Locale.ENGLISH);
        model = createModel("bool", true);
        assertEquals("before##true-block##after", template.format(model));
        model = createModel("bool", false);
        assertEquals("before##false-block##after", template.format(model));
    }

    @Test
    public void testIfNotTwoArgs() throws IOException {
        StringTemplate template = new StringTemplate("before~$bool:'ifn<\"##false-block##\",\"##true-block##\">:'upper~after", transforms, properties, Locale.ENGLISH);
        model = createModel("bool", true);
        assertEquals("before##TRUE-BLOCK##after", template.format(model));
        model = createModel("bool", false);
        assertEquals("before##FALSE-BLOCK##after", template.format(model));
    }

    @Test
    public void testIfNotOneArg() throws IOException {
        StringTemplate template = new StringTemplate("before~$bool:'ifn<\"##false-block##\">:'upper~after", transforms, properties, Locale.ENGLISH);
        model = createModel("bool", true);
        assertEquals("beforeafter", template.format(model));
        model = createModel("bool", false);
        assertEquals("before##FALSE-BLOCK##after", template.format(model));
    }

    @Test
    public void testEquals() throws IOException {
        StringTemplate template = new StringTemplate("~$a:'eq<$b>#true~OK~#true~", transforms, properties, Locale.ENGLISH);
        model = createModel("a", true, "b", true);
        assertEquals("OK", template.format(model));
        model = createModel("a", 1, "b", "1");
        assertEquals("", template.format(model));
    }

}
