package temmentalr;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import static temmentalr.TemplateUtils.createModel;

public class RpnStackTest {

	private RpnStack interpreter;
	private Map<String,Object> model;

	@Before
	public void setUp() {
		interpreter = new RpnStack();
		model = new HashMap<String, Object>();
	}
	
	@Test
	public void testValidIdentifiers() {
		assertTrue(RpnStack.isValidIdentifier("'foo"));
		assertFalse(RpnStack.isValidIdentifier("foo"));
		assertTrue(RpnStack.isValidIdentifier("$foo"));
		assertTrue(RpnStack.isValidIdentifier("$foo?"));
		assertFalse(RpnStack.isValidIdentifier("'foo?"));
		assertTrue(RpnStack.isValidIdentifier("'foo"));
	}
	
	@Test
	public void testParseText() throws IOException, TemplateException {
		parse("Some text data...");
		assertParsingEquals(text("Some text data..."));
		assertWriteEquals("Some text data...");
	}

	@Test
	public void testParseTextWithQuote() throws IOException, TemplateException { 
	    parse("Some text data... with 'b");
	    assertParsingEquals(text("Some text data... with 'b"));
	    assertWriteEquals("Some text data... with 'b");
	}
	
	@Test
	public void testParseTextWithDollar() throws IOException, TemplateException { 
	    parse("Some text data... with $b");
	    assertParsingEquals(text("Some text data... with $b"));
	    assertWriteEquals("Some text data... with $b");
	}
	
	@Test
	public void testParseTextWithTilde() throws IOException, TemplateException {
	    parse("Some text data... with ~~");
	    assertParsingEquals(text("Some text data... with ~"));
	    assertWriteEquals("Some text data... with ~");
	}

	@Test
	public void testAccents() throws IOException, TemplateException {
	    parse("Text with accents: ÍntèrnáTîönàlïzâÇïôn");
	    assertParsingEquals(text("Text with accents: ÍntèrnáTîönàlïzâÇïôn"));
	    assertWriteEquals("Text with accents: ÍntèrnáTîönàlïzâÇïôn");
	}

	@Test
	public void testParseVariableRequired() throws IOException, TemplateException {
	    parse("~$text_to_replace~");
	    assertParsingEquals(eval("$text_to_replace"));
	    populateModel("text_to_replace", "Some text data...");
	    assertWriteEquals("Some text data...");
	}
	
	@Test
	public void testVariableWithTextBeforeAndAfter() throws IOException, TemplateException {
	    parse("The city of ~$city~, with a population of ~$population~ inhabitants, is the ~$rank~th largest city in ~$state~.");
	    assertParsingEquals(text("The city of "), eval("$city"), text(", with a population of "), eval("$population"), text(" inhabitants, is the "), eval("$rank"), text("th largest city in "), eval("$state"), text("."));
	    populateModel("city", "Bordeaux", "population", 242945, "rank", 9, "state", "France");
	    assertWriteEquals("The city of Bordeaux, with a population of 242945 inhabitants, is the 9th largest city in France.");
	}
	
	@Test
	public void testVariableRequiredButNotFound() throws IOException, TemplateException {
	    parse("~$text_to_replace~");
	    assertParsingEquals(eval("$text_to_replace"));
	    populateModel();
	    assertWriteThrowsException("Key 'text_to_replace' is not present or has null value in the model map.");
	}
	
	@Test
	public void testVariableIsOptionalAndNotFound() throws IOException, TemplateException {
	    parse("~$text_to_replace?~");
	    assertParsingEquals(eval("$text_to_replace?"));
	    assertWriteEquals("");
	}

	@Test
	public void testVariableIsOptionalAndFound() throws IOException, TemplateException {
	    parse("~$text_to_replace?~");
	    assertParsingEquals(eval("$text_to_replace?"));
	    populateModel("text_to_replace", "Some text data...");
	    assertWriteEquals("Some text data...");
	}

	@Test
	public void testQuoteFunction() throws IOException, TemplateException {
		parse("The uppercase of '~$text~' is '~$text:'upper~'");
		assertParsingEquals(text("The uppercase of '"), eval("$text"), text("' is '"), func("'upper", "$text"), text("'"));
		populateModel("text", "Eleanor of Aquitaine");
		assertWriteEquals("The uppercase of 'Eleanor of Aquitaine' is: 'ELEANOR OF AQUITAINE'");
	}
	
	// -- 
	
	
	
	@Test
	public void testParseTwoQuoteOnVar() throws IOException, TemplateException {
		parse("~$variable:'function1:'function2~");
		assertParsingEquals(func("'function2", func("'function1", "$variable")));
	}

	@Test
	public void testParseSimpleQuoteOnVarWithInit() throws IOException, TemplateException {
		parse("~$variable:'function<$p1>~");
		assertParsingEquals(func(func("'function", "$p1"), "$variable"));
	}

	@Test
	public void testParseSimpleQuoteOnVarWithInits() throws IOException, TemplateException {
		parse("~$variable:'function<$p1,$p2>~");
		assertParsingEquals(func(func("'function", "$p1", "$p2"), "$variable"));
	}
	
	@Test
	public void testParseSimpleQuoteOnVarWithInits2() throws IOException, TemplateException {
		parse("~$variable:'function<$p1:'function2,$p2>~");
		assertParsingEquals(func(func("'function", func("'function2", "$p1"), "$p2"), "$variable"));
	}
	
	@Test
	public void testParseSimpleQuoteOnVarWithInits3() throws IOException, TemplateException {
		parse("~$variable:'function<$p1:'function2:'function3,$p2>~");
		assertParsingEquals(func(func("'function", func("'function3", func("'function2", "$p1")), "$p2"), "$variable"));
	}
	
	@Test
	public void testParseSimpleQuoteOnVarWithInitsAndAnother() throws IOException, TemplateException {
		parse("~$variable:'function1<$p1,$p2>:'function2~");
		assertParsingEquals(func("'function2", func(func("'function1", "$p1", "$p2"), "$variable")));
	}
	
	@Test
	public void testParseTwoVarFilterOnVar() throws IOException, TemplateException {
		parse("~$variable:$function1?:$function2~");
		assertParsingEquals(func("$function2", func("$function1?", "$variable")));
	}
	
	@Test
	public void testParseExceptionForTransformFunction() throws IOException, TemplateException {
		assertParseThrowsException("Invalid identifier syntax for 'function' at '-:l1:c13'.", "~$variable?:function~");
		assertParseThrowsException("Invalid identifier syntax for 'function2' at '-:l1:c24'.", "~$variable?:'function1:function2~");
		assertParseThrowsException("Invalid identifier syntax for 'function2' at '-:l1:c29'.", "~$variable?:'function1<$p1>:function2~");
		assertParseThrowsException("Invalid identifier syntax for 'function3' at '-:l1:c28'.", "~$variable?:'function1<$p1:function3>:'function2~");
	}
	
	/*

	    def test_parse_simple_quote_function_with_init_on_var(self):
	        self.parse('~$variable:\'function<$p1>')
	        self.assertParsingEquals(func(func('\'function', '$p1'), '$variabl'))
*/
	
	private List<Object> list(Object ... objects) {
		List<Object> list = new ArrayList<Object>();
		for (Object o : objects) {
			list.add(o);
		}
		return list;
	}
	
	private String eval(String text) {
		return list(text, "[-:l\\d+:c\\d+, #pos]", "#eval").toString();
	}
	
	private String text(String text) {
		return list(text, "#text").toString();
	}
	
	private String func(String name, Object ... parameters) {
		if (! name.startsWith("["))
			name = eval(name);
		List<Object> params = new ArrayList<Object>();
		for (Object o : parameters) {
			if ((o instanceof String) && ! ((String) o).startsWith("[")) 
				params.add(eval((String) o));
			else 
				params.add(o);
		}
		return list(params.toString(), name, "#func").toString();
	}

	
	
	private void populateModel(Object ... map) throws TemplateException {
		model.putAll(createModel(map));
	}

	@Test
	public void testMacthes() {
		assertTrue("[-:l1:c12, #pos]".matches("[-:l\\d+:c\\d+, #pos]".replace("$", "\\$").replace("[", "\\[").replace("]", "\\]")));
	}

	private void assertParsingEquals(String ... expectedStack) {
		String shouldBe = "\\[";
		for (String expected : expectedStack) {
			if (! shouldBe.equals("\\["))
				shouldBe += ", ";
			expected = expected.replace("$", "\\$").replace("[", "\\[").replace("]", "\\]").replace("?", "\\?");
			shouldBe += expected;
		}
		shouldBe += "\\]";
//		System.out.println(interpreter.toString());
//		System.out.println(shouldBe);
		assertTrue(interpreter.toString().matches(shouldBe));
	}

	private void parse(String s) throws IOException, TemplateException {
		interpreter.clear();
		interpreter.parse(s, "-", 1, 1);
	}

	private void assertWriteEquals(String expected) throws IOException, TemplateException {
		StringWriter out = new StringWriter();
		interpreter.write(out, model);
		assertEquals(expected, out.toString());
	}

	private void assertWriteThrowsException(String expectedMessage) {
		StringWriter out = new StringWriter();
		try {
			interpreter.write(out, model);
			fail("An exception must be raised.");
		} catch (Exception e) {
			assertEquals(expectedMessage, e.getMessage());
		}
		
	}

	private void assertParseThrowsException(String expectedMessage, String pattern) {
		try {
			parse(pattern);
			interpreter.printStack(System.out);
			fail("An exception must be raised.");
		} catch (Exception e) {
			e.printStackTrace(System.err);
			assertEquals(expectedMessage, e.getMessage());
		}
	}


}
