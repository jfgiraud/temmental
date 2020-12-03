package com.github.jfgiraud.temmental;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static com.github.jfgiraud.temmental.TemplateMessages.createFrom;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class TemplateParseExpressionErrorsTest extends AbstractTestTemplate {

    protected Map<String, Object> model;
    protected Properties properties;
    private Template template;
    private boolean displayRule = true;

    @Before
    public void setUp() throws TemplateException, IOException {
        model = new HashMap<String, Object>();
        properties = new Properties();
        template = new Template(null, null, createFrom(properties));
    }

    @Test
    public void testErrorTwoComma() throws IOException, TemplateException {
        assertParseThrowsException("No parameter before ',' at position '-:l1:c22'.",
                "~$message[$firstname,,$lastname\"]~");
    }

    @Test
    public void testErrorNoVariableIdentifier() throws IOException, TemplateException {
        assertParseThrowsException("No identifier before ':' at position '-:l1:c2'.",
                "~:$upper~");
    }

    @Test
    public void testErrorNoIdentifierInArgument() throws IOException, TemplateException {
        assertParseThrowsException("No parameter before ':' at position '-:l1:c22'.",
                "~$variable:$function<:$arg>~");
    }

    @Test
    public void testErrorNoIdentifierInArgument2() throws IOException, TemplateException {
        assertParseThrowsException("No parameter before ':' at position '-:l1:c28'.",
                "~$variable:$function<$arg1,:$arg2>~");
    }

    @Test
    public void testErrorNoFunctionIdentifier() throws IOException, TemplateException {
        assertParseThrowsException("No identifier before '<' at position '-:l1:c9'.",
                "~$msg[]:<$arg>~");
    }

    @Test
    public void testErrorNoParameterIdentifier() throws IOException, TemplateException {
        assertParseThrowsException("No parameter before ',' at position '-:l1:c19'.",
                "~$msg[]:$function<,$arg>~");
    }

    @Test
    public void testErrorNoParameterAfterCommaAndBeforeAngleBracket() throws IOException, TemplateException {
        assertParseThrowsException("No parameter before '>' at position '-:l1:c24'.",
                "~$msg[]:$function<$arg,>~");
    }

    @Test
    public void testErrorNoParameterAfterCommaAndBeforeSquareBracket() throws IOException, TemplateException {
        assertParseThrowsException("No parameter before ']' at position '-:l1:c12'.",
                "~$msg[$arg,]~");
    }

    @Test
    public void testErrorNoParameterAfterCommaAndBeforeBracket() throws IOException, TemplateException {
        assertParseThrowsException("No parameter before ')' at position '-:l1:c8'.",
                "~($arg,)~");
    }

    @Test
    public void testErrorInvalidBracket() throws IOException, TemplateException {
        assertParseThrowsException("Corresponding bracket for ')' at position '-:l1:c23' is invalid (found '<' at position '-:l1:c18').",
                "~$msg[]:$function<$arg)~");
    }

    @Test
    public void testFilterCanNotBeMessage() throws IOException, TemplateException {
        assertParseThrowsException("Invalid syntax on closing bracket ']' at position '-:l1:c17'. Expects 'com.github.jfgiraud.temmental.Identifier' token but receives 'com.github.jfgiraud.temmental.Function' token.",
                "~$variable:$f[$b]~");
    }

    @Test
    public void testFunctionExpectInput() throws IOException, TemplateException {
        assertParseThrowsException("Invalid syntax on closing bracket '>' at position '-:l1:c7'. Expects 'com.github.jfgiraud.temmental.Function' token but receives 'com.github.jfgiraud.temmental.Identifier' token.",
                "~$f<$b>~");
    }

    @Test
    public void testFunctionExpectRequiredInput() throws IOException, TemplateException {
        assertParseThrowsException("No identifier before ':' at position '-:l1:c2'.", "~:$f<$b>~");
    }

    @Test
    public void testComplexMessage2() throws IOException, TemplateException {
        assertParseThrowsException("Invalid length for char at position '-:l1:c48').",
                "~$message[$firstname:'upper,$lastname:'replace<'ab',','>]:'quote~");
    }

    @Test
    public void testCommandForTooManyParameters() throws IOException, TemplateException {
        assertParseThrowsException("Invalid syntax on closing bracket '>' at position '-:l1:c27'. Bad number of parameter for command 'for'.", "~$elem~~$l#for<$elem,$elem>~<~$before~>~#for~~$elem~");
    }

    @Test
    public void testCommandSetTooManyParameters() throws IOException, TemplateException {
        assertParseThrowsException("Invalid syntax on closing bracket '>' at position '-:l1:c27'. Bad number of parameter for command 'set'.",
                "~$elem~~$l#set<$elem,$elem>~<~$before~>~#set~~$elem~");
    }

    @Test
    public void testCommandSetNoParameter() throws IOException, TemplateException {
        assertParseThrowsException("Invalid syntax at position '-:l1:c11'. Command 'set' expects one parameter.",
                "~$elem~~$l#set~<~$before~>~#set~~$elem~");
    }

    @Test
    public void testInvalidSyntax2() throws IOException, TemplateException {
        assertParseThrowsException("Parsing exception at position -:l1:c22.",
                "~$date_time:'is_today<#true~</b>~#true~");
    }

    @Test
    public void testOptionalNotCompatibleWithDefault() throws IOException, TemplateException {
        assertParseThrowsException("Non compatible options (?/!) at positions '-:l1:c2' and '-:l1:c18'.", "~$accountNumber?!'unknown[]¡~");
        assertParseThrowsException("Non compatible options (?/!) at positions '-:l1:c18' and '-:l1:c34'.", "~'account_number[$accountNumber?!'unknown[]¡]~");
        assertParseThrowsException("Non compatible options (?/!) at positions '-:l1:c2' and '-:l1:c35'.", "~$account_number?[$accountNumber]!'unknown[]¡~");
    }

    @Test
    public void testNotClosed() throws IOException, TemplateException {
//        assertParseThrowsException("xxx", "~\"hello\":'upper~");
        assertParseThrowsException("Too much objects in the stack for expression '~$toto!123~' at position '-:l1:c1'. One or more tags are not closed ('!'=1).", "~$toto!123~");
        assertParseThrowsException("Too much objects in the stack for expression '~($toto,$tutu~' at position '-:l1:c1'. One or more tags are not closed ('('=1).", "~($toto,$tutu~");
        assertParseThrowsException("Too much objects in the stack for expression '~'account_number<$toto~' at position '-:l1:c1'. One or more tags are not closed ('<'=1).", "~'account_number<$toto~");
        assertParseThrowsException("Too much objects in the stack for expression '~'account_number[$toto~' at position '-:l1:c1'. One or more tags are not closed ('['=1).", "~'account_number[$toto~");
    }

    @Test
    public void testInvertedBrackets() throws IOException, TemplateException {
        //TODO inverted brackets
//        fail();
    }

    protected void assertParseThrowsException(String expectedMessage, String pattern) {
        if (displayRule) {
            displayRule(pattern);
        }

        try {
            template.parseString(pattern, true);
            template.printStructure(System.out);
            fail("An exception must be raised.");
        } catch (Exception e) {
//            e.printStackTrace();
            assertEquals(expectedMessage, e.getMessage());
        }
    }

}
