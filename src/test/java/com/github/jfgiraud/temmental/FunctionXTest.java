package com.github.jfgiraud.temmental;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.Assert.*;

public class FunctionXTest extends AbstractTestElement {

    private Template template;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        template = new Template(null, transforms, properties);
    }

    @Test
    public void testParameterizedQuoteFunctionChain() throws IOException, TemplateException, NoSuchMethodException, SecurityException {
        parse("~$text:'concat<$suffix>~");
        populateTransform("concat", String.class.getDeclaredMethod("concat", String.class));
        populateModel("text", "hello world");
        populateModel("suffix", "!");
        assertWriteEquals("hello world!");
    }

    @Test
    public void testTestMessageWithGlobalDefaultValue() throws IOException, TemplateException {
        parse("~'msg[$p1]!\"text\"¡~");
        assertWriteEquals("text");
    }

    @Test
    public void testTestMessageWithDefaultValue() throws IOException, TemplateException {
        parse("~'msg[$p1!\"unknown\"¡]~");
        populateProperty("msg", "Hello {0}");
        assertWriteEquals("Hello unknown");
    }

    @Test
    public void testTestMessageWithDefaultValue2() throws IOException, TemplateException {
        parse("~'msg[$p1!¡]~");
        populateProperty("msg", "Hello {0}");
        assertWriteEquals("Hello ");
    }

    @Test
    public void testTestMessageWithDefaultValue3() throws IOException, TemplateException {
        parse("~'msg[$p1]!\"text\"¡~");
        populateProperty("msg", "Hello {0}");
        assertWriteEquals("text");
    }

    @Test
    public void testTestMessageWithDefaultValue4() throws IOException, TemplateException {
        parse("~'msg[$p1]!\"text\"¡~");
        populateModel("p1", "something");
        assertWriteEquals("text");
    }

    @Test
    public void testTestMessageWithDefaultValue5() throws IOException, TemplateException {
        parse("~$msg!'default[$p1]¡~");
        populateProperty("default", "Bonjour {0}");
        populateModel("p1", "bidule");
        assertWriteEquals("Bonjour bidule");
    }

    private void assertWriteEquals(String expected) throws IOException, TemplateException {
        StringWriter out = new StringWriter();
        template.printFile(out, model);
        assertEquals(expected, out.toString());
    }

    private void assertWriteThrowsException(String expected) throws IOException, TemplateException {
        StringWriter out = new StringWriter();
        try {
            template.printFile(out, model);
            fail("An exception must be raised.");
        } catch (TemplateIgnoreRenderingException e) {
            fail("Catch bad exception type: TemplateIgnoreRenderingException");
        } catch (TemplateException e) {
            assertEquals(expected, e.getMessage());
        }
    }

    private void assertWriteThrowsIgnoreRenderingException(String expected) throws IOException, TemplateException {
        StringWriter out = new StringWriter();
        try {
            template.printFile(out, model);
            fail("An exception must be raised.");
        } catch (TemplateIgnoreRenderingException e) {
            assertEquals(expected, e.getMessage());
        } catch (TemplateException e) {
            fail("Catch bad exception type: TemplateException");
        }
    }


    private void parse(String string) throws IOException, TemplateException {
        template.parseString(string, true);
    }

    @Test
    public void testParameterizedQuoteFunctionAcceptsFunctionApplicationOnItsInitializers() throws IOException, TemplateException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        parse("~$text:'replace<$what,$with:'upper>~");
        populateTransform("replace", String.class.getDeclaredMethod("replaceAll", String.class, String.class));
        populateTransform("upper", String.class.getDeclaredMethod("toUpperCase"));
        populateModel("text", "this is string example....wow!!! this is really string");
        populateModel("what", "is");
        populateModel("with", "was");
        assertWriteEquals("thWAS WAS string example....wow!!! thWAS WAS really string");
    }

    @Test
    public void testExceptionMessageOnTransformWith1Param_CaseOk() throws IOException, TemplateException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        parse("~$text:'charat<$index>~");
        populateTransform("charat", String.class.getDeclaredMethod("charAt", int.class));
        populateModel("text", "lorem ipsum");
        populateModel("index", 2);
        assertWriteEquals("r");
    }

    @Test
    public void testExceptionMessageOnTransformWith1Param_CaseKo1() throws IOException, TemplateException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        parse("~$text:'charat<$index,$index>~");
        populateTransform("charat", String.class.getDeclaredMethod("charAt", int.class));
        populateModel("text", "lorem ipsum");
        populateModel("index", 2);
        assertWriteThrowsException("Unable to render '…:'charat' at position '-:l1:c8'. The function charat expects 1 init-parameter(s) but receives 2 init-parameter(s).");
    }

    @Test
    public void testExceptionMessageOnTransformWith1Param_CaseKo4() throws IOException, TemplateException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        assertParseThrowsException("~$text:'charat<>~", "Empty init list parameter before '>' at position '-:l1:c16'.");
    }

    private void assertParseThrowsException(String pattern, String expected) throws IOException {
        try {
            parse(pattern);
            fail("An exception must be raised.");
        } catch (TemplateException e) {
            assertEquals(expected, e.getMessage());
        }

    }

    @Test
    public void testExceptionMessageOnTransformWithoutParam() throws IOException, TemplateException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        parse("~$text:'charat~");
        populateTransform("charat", String.class.getDeclaredMethod("charAt", int.class));
        populateModel("text", "lorem ipsum");
        populateModel("index", 2);
        assertWriteThrowsException("Unable to render '…:'charat' at position '-:l1:c8'. The function charat expects one parameter but is called without parameter!");
    }

    @Test
    public void testExceptionMessageInsideNoParameter() throws IOException, TemplateException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        parse("~$text:'replace<$what,$with:'upper:'charat>~");
        populateTransform("replace", String.class.getDeclaredMethod("replaceAll", String.class, String.class));
        populateTransform("upper", String.class.getDeclaredMethod("toUpperCase"));
        populateTransform("charat", String.class.getDeclaredMethod("charAt", int.class));
        populateModel("text", "lorem ipsum dolor sit amet");
        populateModel("what", "ipsum");
        populateModel("with", "elit");
        assertWriteThrowsException("Unable to render '…:'charat' at position '-:l1:c36'. The function charat expects one parameter but is called without parameter!");
    }

    @Test
    public void testExceptionMessageInsideBadParameter() throws IOException, TemplateException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        parse("~$text:'replace<$what,$with:'upper:'charat<$index>>~");
        populateTransform("replace", String.class.getDeclaredMethod("replaceAll", String.class, String.class));
        populateTransform("upper", String.class.getDeclaredMethod("toUpperCase"));
        populateTransform("charat", String.class.getDeclaredMethod("charAt", int.class));
        populateModel("text", "lorem ipsum dolor sit amet");
        populateModel("what", "ipsum");
        populateModel("with", "elit");
        populateModel("index", "2");
        assertWriteThrowsException("Unable to render '…:'charat' at position '-:l1:c36'. The function charat expects int for parameter #1. It receives java.lang.String.");
    }

    @Test
    public void testExceptionMessageInsideEmptyParameterList() throws IOException, TemplateException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        assertParseThrowsException("~$text:'replace<$what,$with:'upper:'charat<>>~", "Empty init list parameter before '>' at position '-:l1:c44'.");
    }

    @Test
    public void testExceptionMessageTooMuchCommas() throws IOException, TemplateException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        assertParseThrowsException("~$text:'replace<$what,,$with>~", "No parameter before ',' at position '-:l1:c23'.");
    }

    @Test
    public void testExceptionMessageInsideTooMuchCommas() throws IOException, TemplateException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        assertParseThrowsException("~$text:'replace<$what,$with:'upper:'replace<$a,,$b>>~", "No parameter before ',' at position '-:l1:c48'.");
    }

    @Test
    public void testExceptionMessageInsideWrongNumberOfParameters() throws IOException, TemplateException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        parse("~$text:'replace<$what,$with:'upper:'charat<$index,$index>>~");
        populateTransform("replace", String.class.getDeclaredMethod("replaceAll", String.class, String.class));
        populateTransform("upper", String.class.getDeclaredMethod("toUpperCase"));
        populateTransform("charat", String.class.getDeclaredMethod("charAt", int.class));
        populateModel("text", "lorem ipsum dolor sit amet");
        populateModel("what", "ipsum");
        populateModel("with", "elit");
        populateModel("index", 2);
        assertWriteThrowsException("Unable to render '…:'charat' at position '-:l1:c36'. The function charat expects 1 init-parameter(s) but receives 2 init-parameter(s).");
    }

    @Test
    public void testExceptionMessageOnTransformWith1Param_CaseKo2() throws IOException, TemplateException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        parse("~$text:'charat<$index>~");
        Method toto = String.class.getDeclaredMethod("charAt", int.class);
        populateTransform("charat", toto);
        populateModel("text", "lorem ipsum");
        populateModel("index", "2");
        assertWriteThrowsException("Unable to render '…:'charat' at position '-:l1:c8'. The function charat expects int for parameter #1. It receives java.lang.String.");
    }

    @Test
    public void testExceptionMessageOnTransformWith1Param_CaseKo3() throws IOException, TemplateException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        parse("~$text:'charat<$index>~");
        Method toto = String.class.getDeclaredMethod("charAt", int.class);
        populateTransform("charat", toto);
        populateModel("text", 12345);
        populateModel("index", 2);
        assertWriteThrowsException("Unable to render '…:'charat' at position '-:l1:c8'. The function charat expects java.lang.String. It receives java.lang.Integer.");
    }

    @Test
    public void testExceptionMessageOnTransformWith2Params_CaseOk() throws IOException, TemplateException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        parse("~$text:'replace<$what,$with>~");
        populateTransform("replace", String.class.getDeclaredMethod("replaceAll", String.class, String.class));
        populateModel("text", "lorem ipsum dolor sit amet");
        populateModel("what", "ipsum");
        populateModel("with", "elit");
        assertWriteEquals("lorem elit dolor sit amet");
    }

    @Test
    public void testExceptionMessageOnTransformWith2Params_CaseKo1() throws IOException, TemplateException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        parse("~$text:'replace<$what,$with,$with>~");
        populateTransform("replace", String.class.getDeclaredMethod("replaceAll", String.class, String.class));
        populateModel("text", "lorem ipsum dolor sit amet");
        populateModel("what", "ipsum");
        populateModel("with", "elit");
        assertWriteThrowsException("Unable to render '…:'replace' at position '-:l1:c8'. The function replace expects 2 init-parameter(s) but receives 3 init-parameter(s).");
    }

    @Test
    public void testExceptionMessageOnTransformWith2Params_CaseKo2() throws IOException, TemplateException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        parse("~$text:'replace<$what,$with>~");
        populateTransform("replace", String.class.getDeclaredMethod("replaceAll", String.class, String.class));
        populateModel("text", "lorem ipsum dolor sit amet");
        populateModel("what", 5);
        populateModel("with", "elit");
        assertWriteThrowsException("Unable to render '…:'replace' at position '-:l1:c8'. The function replace expects java.lang.String for parameter #1. It receives java.lang.Integer.");
    }

    @Test
    public void testExceptionMessageOnTransformWith2Params_CaseKo3() throws IOException, TemplateException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        parse("~$text:'replace<$what,$with>~");
        populateTransform("replace", String.class.getDeclaredMethod("replaceAll", String.class, String.class));
        populateModel("text", "lorem ipsum dolor sit amet");
        populateModel("what", "ipsum");
        populateModel("with", 5);
        assertWriteThrowsException("Unable to render '…:'replace' at position '-:l1:c8'. The function replace expects java.lang.String for parameter #2. It receives java.lang.Integer.");
    }


    @Test
    public void testParameterizedQuoteFunctionAcceptsFunctionApplicationOnTheResult() throws IOException, TemplateException, NoSuchMethodException, SecurityException {
        parse("~$text:'replace<$what:'lower,$with>:'upper~");
        populateTransform("replace", String.class.getDeclaredMethod("replaceAll", String.class, String.class));
        populateTransform("upper", String.class.getDeclaredMethod("toUpperCase"));
        populateTransform("lower", String.class.getDeclaredMethod("toLowerCase"));
        populateModel("text", "this is string example....wow!!! this is really string");
        populateModel("what", "Is");
        populateModel("with", "was");
        assertWriteEquals("THWAS WAS STRING EXAMPLE....WOW!!! THWAS WAS REALLY STRING");
    }

    @Test
    public void testADynamicFunctionCanBeAppliedOnTheResultOfTheReplacement() throws IOException, TemplateException, NoSuchMethodException, SecurityException {
        parse("~$text:$f~");
        populateModel("text", "It is an example!");
        populateModel("f", "upper");
        populateTransform("upper", String.class.getDeclaredMethod("toUpperCase"));
        assertWriteEquals("IT IS AN EXAMPLE!");
    }

    @Test
    public void testADynamicFunctionCanBeOptional_CaseKnown() throws IOException, TemplateException, NoSuchMethodException, SecurityException {
        parse("~$text:$f?~");
        populateModel("text", "It is an example!");
        populateModel("f", "upper");
        populateTransform("upper", String.class.getDeclaredMethod("toUpperCase"));
        assertWriteEquals("IT IS AN EXAMPLE!");
    }

    @Test
    public void testADynamicFunctionCanBeOptional_CaseUnknown() throws IOException, TemplateException, NoSuchMethodException, SecurityException {
        parse("abc~$text:$f?~def");
        populateModel("text", "It is an example!");
        populateTransform("upper", String.class.getDeclaredMethod("toUpperCase"));
        assertWriteEquals("abcdef");
    }

    @Test
    public void testADynamicFunctionCanBeOptional_CaseUnknown2() throws IOException, TemplateException, NoSuchMethodException, SecurityException {
        parse("~$text:$f!¡:'upper~");
        populateModel("text", "It is an example!");
        populateTransform("upper", String.class.getDeclaredMethod("toUpperCase"));
        assertWriteEquals("IT IS AN EXAMPLE!");
    }

    @Test
    public void testWhenARequiredDynamicFunctionIsNotFoundAnExceptionIsRaised() throws IOException, TemplateException, NoSuchMethodException, SecurityException {
        parse("~$text:$f~");
        populateModel("text", "It is an example!");
        populateTransform("upper", String.class.getDeclaredMethod("toUpperCase"));
        assertWriteThrowsException("Key 'f' is not present or has null value in the model map at position '-:l1:c8'.");
    }

    @Test
    public void testSubstr() throws IOException, TemplateException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        parse("~$text:'substr<$b1,$b2>~");
        Method func = String.class.getDeclaredMethod("substring", int.class, int.class);
        populateTransform("substr", func);
        populateModel("text", "lorem ipsum");
        populateModel("b1", 0);
        populateModel("b2", 5);
        assertWriteEquals("lorem");
    }

    @Test
    public void testSubstr_ParamUnknown() throws IOException, TemplateException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        parse("~$text:'substr<$b1,$b2>~");
        Method func = String.class.getDeclaredMethod("substring", int.class, int.class);
        populateTransform("substr", func);
        populateModel("text", "lorem ipsum");
        populateModel("b1", 5);
        assertWriteThrowsException("Key 'b2' is not present or has null value in the model map at position '-:l1:c20'.");
    }

    @Test
    public void testSubstr_ParamOptional() throws IOException, TemplateException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        parse("abc~$text:'substr<$b1,$b2?>~def");
        Method func = String.class.getDeclaredMethod("substring", int.class, int.class);
        populateTransform("substr", func);
        populateModel("text", "lorem ipsum");
        populateModel("b1", 5);
//        assertWriteThrowsIgnoreRenderingException("Ignore rendering because key 'b2' is not present or has null value in the model map at position '-:l1:c20'.");
        assertWriteEquals("abcdef");
    }

    @Test
    public void testSubstr_ParamOptional2() throws IOException, TemplateException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        parse("~$text:'substr<$b1,$b2!8¡>~");
        Method func = String.class.getDeclaredMethod("substring", int.class, int.class);
        populateTransform("substr", func);
        populateModel("text", "lorem ipsum");
        populateModel("b1", 5);
        assertWriteEquals(" ip");
    }

    @Test
    public void testSubstr_ParamOptional3() throws IOException, TemplateException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        parse("~$text:'substr<$b1,$b2>!88¡~");
        Method func = String.class.getDeclaredMethod("substring", int.class, int.class);
        populateTransform("substr", func);
        populateModel("text", "lorem ipsum");
        populateModel("b1", 5);
        assertWriteEquals("88");
    }

    @Test
    public void testIsAssignable() throws NoSuchMethodException, NoSuchFieldException {
        assertTrue(Functionp.isAssignable(int.class, int.class));
        assertTrue(Functionp.isAssignable(Integer.class, Integer.class));
        assertTrue(Functionp.isAssignable(int.class, Integer.class));
        assertTrue(Functionp.isAssignable(Integer.class, int.class));
        assertTrue(Functionp.isAssignable(String.class, String.class));
        assertFalse(Functionp.isAssignable(Integer.class, String.class));
        assertFalse(Functionp.isAssignable(Integer.class, Long.class));
    }


}
