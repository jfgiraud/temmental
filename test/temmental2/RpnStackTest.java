package temmental2;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class RpnStackTest {

	private RpnStack interpreter;

	@Before
	public void setUp() {
		interpreter = new RpnStack();
	}
	
	@Test
	public void testParseVariable() throws IOException {
		parse("~$variable~");
		assertParsingEquals("$variable");
	}

	@Test
	public void testParseVariableOptional() throws IOException {
		parse("~$variable?~");
		assertParsingEquals("$variable?");
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
		assertParsingEquals(func(func("'function", "$p1"),"$variable"));
	}

	@Test
	public void testParseSimpleQuoteOnVarWithInits() throws IOException {
		parse("~$variable:'function<$p1,$p2>~");
		assertParsingEquals(func(func("'function", "$p1", "$p2"),"$variable"));
	}
	
	@Test
	public void testParseSimpleQuoteOnVarWithInits2() throws IOException {
		parse("~$variable:'function<$p1:'function2,$p2>~");
		assertParsingEquals(func(func("'function", func("'function2", "$p1"), "$p2"),"$variable"));
	}
	
	@Test
	public void testParseSimpleQuoteOnVarWithInits3() throws IOException {
		parse("~$variable:'function<$p1:'function2:'function3,$p2>~");
		assertParsingEquals(func(func("'function", func("'function3", func("'function2", "$p1")), "$p2"),"$variable"));
	}
	
	@Test
	public void testParseSimpleQuoteOnVarWithInitsAndAnother() throws IOException {
		parse("~$variable:'function1<$p1,$p2>:'function2~");
		assertParsingEquals(func("'function2", func(func("'function1", "$p1", "$p2"),"$variable")));
	}
	
	@Test
	public void testParseTwoVarFilterOnVar() throws IOException {
		parse("~$variable:$function1?:$function2~");
		assertParsingEquals(func("$function2", func("$function1?", "$variable")));
	}
	
	@Test
	public void testParseSimpleQuoteOnVarWithInits3b() throws IOException {
		parse("~$variable?:function<$p1:'function2:'function3,$p2?>~");
		assertParsingEquals(func(func("'function", func("'function3", func("'function2", "$p1")), "$p2?"),"$variable?"));
	}
	
	/*

	    def test_parse_simple_quote_function_with_init_on_var(self):
	        self.parse('~$variable:\'function<$p1>')
	        self.assertParsingEquals(func(func('\'function', '$p1'), '$variabl'))
*/
	
	
	
	private String func(String name, String ... parameters) {
		List<String> list = new ArrayList<String>();
		List<String> params = new ArrayList<String>();
		for (String p : parameters) {
			params.add(p);
		}
		list.add(params.toString());
		list.add(name);
		list.add("#func");
		return list.toString();
	}

	private void assertParsingEquals(String expected) {
		assertEquals("["+expected+"]", interpreter.toString());
	}

	private void parse(String s) throws IOException {
		interpreter.parse(s);
	}

}
