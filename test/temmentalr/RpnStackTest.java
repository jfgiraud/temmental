package temmentalr;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
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
	public void testParseVariableRequired() throws IOException, TemplateException {
	    parse("~$text_to_replace~");
	    assertParsingEquals(eval("$text_to_replace"));
	    populateModel("text_to_replace", "Some text data...");
	    assertWriteEquals("Some text data...");
	}
	
	@Test
	public void testParseVariableRequiredButNotFound() throws IOException, TemplateException {
	    parse("~$text_to_replace~");
	    assertParsingEquals(eval("$text_to_replace"));
	    populateModel();
	    assertWriteThrowsException("Key 'text_to_replace' is not present or has null value in the model map.");
	}
	
	// -- 
	
	@Test
	public void testParseVariable() throws IOException {
		parse("~$variable~");
		assertParsingEquals(eval("$variable"));
	}

	@Test
	public void testParseVariableOptional() throws IOException {
		parse("~$variable?~");
		assertParsingEquals(eval("$variable?"));
	}
	
	@Test
	public void testParseSimpleQuoteOnVar() throws IOException {
		parse("~$variable:'function~");
		assertParsingEquals(func("'function", "$variable"));
	}
	
	@Test
	public void testParseTwoQuoteOnVar() throws IOException {
		parse("~$variable:'function1:'function2~");
		assertParsingEquals(func("'function2", func("'function1", "$variable")));
	}

	@Test
	public void testParseSimpleQuoteOnVarWithInit() throws IOException {
		parse("~$variable:'function<$p1>~");
		assertParsingEquals(func(func("'function", "$p1"), "$variable"));
	}

	@Test
	public void testParseSimpleQuoteOnVarWithInits() throws IOException {
		parse("~$variable:'function<$p1,$p2>~");
		assertParsingEquals(func(func("'function", "$p1", "$p2"), "$variable"));
	}
	
	@Test
	public void testParseSimpleQuoteOnVarWithInits2() throws IOException {
		parse("~$variable:'function<$p1:'function2,$p2>~");
		assertParsingEquals(func(func("'function", func("'function2", "$p1"), "$p2"), "$variable"));
	}
	
	@Test
	public void testParseSimpleQuoteOnVarWithInits3() throws IOException {
		parse("~$variable:'function<$p1:'function2:'function3,$p2>~");
		assertParsingEquals(func(func("'function", func("'function3", func("'function2", "$p1")), "$p2"), "$variable"));
	}
	
	@Test
	public void testParseSimpleQuoteOnVarWithInitsAndAnother() throws IOException {
		parse("~$variable:'function1<$p1,$p2>:'function2~");
		assertParsingEquals(func("'function2", func(func("'function1", "$p1", "$p2"), "$variable")));
	}
	
	@Test
	public void testParseTwoVarFilterOnVar() throws IOException {
		parse("~$variable:$function1?:$function2~");
		assertParsingEquals(func("$function2", func("$function1?", "$variable")));
	}
	
	@Test
	public void testParseSimpleQuoteOnVarWithInits3b() throws IOException {
		parse("~$variable?:function<$p1:'function2:'function3,$p2?>~");
		assertParsingEquals(func(func("'function", func("'function3", func("'function2", "$p1")), "$p2?"), "$variable?"));
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

	private void assertParsingEquals(String expected) {
		expected = "["+expected+"]";
		expected = expected.replace("$", "\\$").replace("[", "\\[").replace("]", "\\]").replace("?", "\\?");
		System.out.println(expected);
		System.out.println(interpreter.toString());
		assertTrue(interpreter.toString().matches(expected));
	}

	private void parse(String s) throws IOException {
		interpreter.parse(s, "-", 1, 1);
	}

	private void assertWriteEquals(String expected) throws IOException, TemplateException {
		StringWriter out = new StringWriter();
		interpreter.write(out, model);
		assertEquals(expected, out.toString());
	}

	private void assertWriteThrowsException(String expected) {
		StringWriter out = new StringWriter();
		try {
			interpreter.write(out, model);
			fail("An exception must be raised.");
		} catch (Exception e) {
			assertEquals(expected, e.getMessage());
		}
		
	}


}
