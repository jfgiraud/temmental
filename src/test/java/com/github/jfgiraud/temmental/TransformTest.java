package com.github.jfgiraud.temmental;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

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
        //TODO README
        transforms.put("if", Transforms.IF);
        transforms.put("ifn", Transforms.IF_NOT);
        transforms.put("not", Transforms.NOT);
        transforms.put("upper", Transforms.UPPER);
        transforms.put("lower", Transforms.LOWER);
        transforms.put("eq", Transforms.EQUALS);
        transforms.put("ne", Transforms.NOT_EQUALS);
        transforms.put("lt", Transforms.LESS_THAN);
        transforms.put("le", Transforms.LESS_EQUALS);
        transforms.put("gt", Transforms.GREATER_THAN);
        transforms.put("ge", Transforms.GREATER_EQUALS);
        transforms.put("and", Transforms.AND);
        transforms.put("or", Transforms.OR);
        transforms.put("empty", Transforms.EMPTY);
        transforms.put("size", Transforms.SIZE);
        transforms.put("all", Transforms.ALL);
        transforms.put("any", Transforms.ANY);
        transforms.put("none", Transforms.NONE);
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
    public void testAnd() throws IOException {
        StringTemplate template = new StringTemplate("before~$a:'and<$b>#true~OK~#true~after", transforms, properties, Locale.ENGLISH);
        model = createModel("a", true, "b", true);
        assertEquals("beforeOKafter", template.format(model));
        model = createModel("a", true, "b", false);
        assertEquals("beforeafter", template.format(model));
    }

    @Test
    public void testOr() throws IOException {
        StringTemplate template = new StringTemplate("before~$a:'or<$b>#true~OK~#true~after", transforms, properties, Locale.ENGLISH);
        model = createModel("a", true, "b", true);
        assertEquals("beforeOKafter", template.format(model));
        model = createModel("a", false, "b", true);
        assertEquals("beforeOKafter", template.format(model));
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

    @Test
    public void testLessThan() throws IOException {
        StringTemplate template = new StringTemplate("~$a:'lt<$b>#true~OK~#true~", transforms, properties, Locale.ENGLISH);
        model = createModel("a", 4, "b", 5);
        assertEquals("OK", template.format(model));
        model = createModel("a", 5, "b", 5);
        assertEquals("", template.format(model));
    }

    @Test
    public void testLessEquals() throws IOException {
        StringTemplate template = new StringTemplate("~$a:'le<$b>#true~OK~#true~", transforms, properties, Locale.ENGLISH);
        model = createModel("a", 4, "b", 5);
        assertEquals("OK", template.format(model));
        model = createModel("a", 5, "b", 5);
        assertEquals("OK", template.format(model));
    }

    @Test
    public void testGreaterThan() throws IOException {
        StringTemplate template = new StringTemplate("~$a:'gt<$b>#true~OK~#true~", transforms, properties, Locale.ENGLISH);
        model = createModel("a", 5, "b", 4);
        assertEquals("OK", template.format(model));
        model = createModel("a", 5, "b", 5);
        assertEquals("", template.format(model));
    }

    @Test
    public void testGreaterEquals() throws IOException {
        StringTemplate template = new StringTemplate("~$a:'ge<$b>#true~OK~#true~", transforms, properties, Locale.ENGLISH);
        model = createModel("a", 5, "b", 4);
        assertEquals("OK", template.format(model));
        model = createModel("a", 5, "b", 5);
        assertEquals("OK", template.format(model));
    }

    @Test
    public void testNotEquals() throws IOException {
        StringTemplate template = new StringTemplate("~$a:'ne<$b>#true~OK~#true~", transforms, properties, Locale.ENGLISH);
        model = createModel("a", 4, "b", 5);
        assertEquals("OK", template.format(model));
        model = createModel("a", 4, "b", 4);
        assertEquals("", template.format(model));
    }

    @Test
    public void testEmpty() throws IOException {
        StringTemplate template = new StringTemplate("~$a:'empty#true~OK~#true~", transforms, properties, Locale.ENGLISH);
        model = createModel("a", Arrays.asList());
        assertEquals("OK", template.format(model));
        model = createModel("a", Arrays.asList(1));
        assertEquals("", template.format(model));
    }

    @Test
    public void testAll() throws IOException {
        StringTemplate template = new StringTemplate("~($a,$b):'all#true~OK~#true~", transforms, properties, Locale.ENGLISH);
        model = createModel("a", true, "b", true);
        assertEquals("OK", template.format(model));
        model = createModel("a", true, "b", false);
        assertEquals("", template.format(model));
    }

    @Test
    public void testAll2() throws IOException {
        StringTemplate template = new StringTemplate("~$l:'all#true~OK~#true~", transforms, properties, Locale.ENGLISH);
        model = createModel("l", Arrays.asList(true, true));
        assertEquals("OK", template.format(model));
        model = createModel("l", Arrays.asList(true, false));
        assertEquals("", template.format(model));
    }

    @Test
    public void testAny() throws IOException {
        StringTemplate template = new StringTemplate("~($a,$b):'any#true~OK~#true~", transforms, properties, Locale.ENGLISH);
        model = createModel("a", false, "b", true);
        assertEquals("OK", template.format(model));
        model = createModel("a", false, "b", false);
        assertEquals("", template.format(model));
    }

    @Test
    public void testNone() throws IOException {
        StringTemplate template = new StringTemplate("~($a,$b):'none#true~OK~#true~", transforms, properties, Locale.ENGLISH);
        model = createModel("a", false, "b", true);
        assertEquals("", template.format(model));
        model = createModel("a", false, "b", false);
        assertEquals("OK", template.format(model));
    }

}
