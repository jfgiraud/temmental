package temmental2;

import static temmental2.TemplateUtils.createModel;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;

import junit.framework.TestCase;

import org.junit.Test;

public class TransformFunctionsTest extends TestCase {

	private NewTemplate template;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		template = new NewTemplate();
	}
	
	@Test
	public void testGt() throws IOException, TemplateException {
		template.addTransform("gt", TransformFunctions.MATH.get("gt"));
		assertParseAndApplyEquals("true", "~$number:'gt<$value>~", "number", 5, "value", 3);
		assertParseAndApplyEquals("true", "~$number:'gt<$value>~", "number", 5, "value", 4.8);
		assertParseAndApplyExceptionEquals("Invalid filter chain. Filter 'gt' expects 'java.lang.Number'. It receives 'java.lang.String'. Unable to render ''gt' at position '-:l1:c9'.", "~$number:'gt<$value>~", "number", 5, "value", "8");
		assertParseAndApplyExceptionEquals("Invalid filter chain. Filter 'gt' expects 'java.lang.Number'. It receives 'java.lang.String'. Unable to render ''gt' at position '-:l1:c9'.", "~$number:'gt<$value>~", "number", "5", "value", 8);
	}

	@Test
	public void testEq() throws IOException, TemplateException {
		template.addTransform("eq", TransformFunctions.MATH.get("eq"));
		assertParseAndApplyEquals("true", "~$number:'eq<$value>~", "number", 5, "value", 5);
		assertParseAndApplyEquals("false", "~$number:'eq<$value>~", "number", 5, "value", 4.8);
		assertParseAndApplyExceptionEquals("Invalid filter chain. Filter 'eq' expects 'java.lang.Number'. It receives 'java.lang.String'. Unable to render ''eq' at position '-:l1:c9'.", "~$number:'eq<$value>~", "number", 5, "value", "8");
		assertParseAndApplyExceptionEquals("Invalid filter chain. Filter 'eq' expects 'java.lang.Number'. It receives 'java.lang.String'. Unable to render ''eq' at position '-:l1:c9'.", "~$number:'eq<$value>~", "number", "5", "value", 8);
	}
	
	@Test
	public void testOddEven() throws IOException, TemplateException {
		template.addTransform("odd", TransformFunctions.MATH.get("odd"));
		template.addTransform("even", TransformFunctions.MATH.get("even"));
		assertParseAndApplyEquals("true", "~$number:'odd~", "number", 5);
		assertParseAndApplyEquals("false", "~$number:'even~", "number", 5);
		assertParseAndApplyEquals("false", "~$number:'odd~", "number", 6);
		assertParseAndApplyEquals("true", "~$number:'even~", "number", 6);
		assertParseAndApplyEquals("false", "~$number:'odd~", "number", 5.2);
		assertParseAndApplyEquals("false", "~$number:'even~", "number", 5.2);
	}
	
	@Test
	public void testNot() throws IOException, TemplateException {
		template.addTransform("not", TransformFunctions.MATH.get("not"));
		assertParseAndApplyEquals("true", "~$bool:'not~", "bool", false);
		assertParseAndApplyEquals("false", "~$bool:'not~", "bool", true);
		
//		assertParseAndApplyEquals("true", "~false:'not~");
//		assertParseAndApplyEquals("false", "~true:'not~");
	}
	
	@Test
	public void testEmpty() throws IOException, TemplateException {
		template.addTransform("empty", TransformFunctions.MATH.get("empty"));
		template.addTransform("not_empty", TransformFunctions.MATH.get("not_empty"));
		template.addTransform("size", TransformFunctions.MATH.get("size"));
		template.addTransform("not", TransformFunctions.MATH.get("not"));
		template.addTransform("eq", TransformFunctions.MATH.get("eq"));
		
		assertParseAndApplyEquals("false", "~$collection:'empty", "collection", Arrays.asList(1, 2, 3));
		assertParseAndApplyEquals("true", "~$collection:'not_empty", "collection", Arrays.asList(1, 2, 3));
		assertParseAndApplyEquals("false", "~$collection:'size:'eq<24>", "collection", Arrays.asList(1, 2, 3));
		assertParseAndApplyEquals("true", "~$collection:'size:'eq<3>", "collection", Arrays.asList(1, 2, 3));
	}
	
	public void testInt() throws IOException, TemplateException {
		template.addTransform("eq", TransformFunctions.MATH.get("eq"));
		template.addTransform("int", TransformFunctions.MATH.get("int"));
		assertParseAndApplyEquals("true", "~$n:'int:'eq<3>~", "n", "3");
	}
	
	public void testBool() throws IOException, TemplateException {
		template.addTransform("ifel", TransformFunctions.CONDITIONAL.get("ifel"));
		assertParseAndApplyEquals("On", "~$b:'ifel<\"On\",\"Off\">~", "b", Boolean.TRUE);
		assertParseAndApplyEquals("Off", "~$b:'ifel<\"On\",\"Off\">~", "b", Boolean.FALSE);
	}
	
	private void assertParseAndApplyExceptionEquals(String expected, String pattern, Object ... map) {
		try {
			StringWriter out = new StringWriter();
			Node node = template.parse(pattern);
			template.printFile(out, createModel(map));
			fail("An exception must be raised.");
		} catch(Exception e) {
			assertEquals(expected, e.getMessage());
		}
	}

	private void assertParseAndApplyEquals(String expected, String pattern, Object ... map) throws IOException, TemplateException {
		Node node = template.parse(pattern);
		System.out.println(node.representation());
		StringWriter out = new StringWriter();
		template.printFile(out, createModel(map));
		assertEquals(expected, out.toString());
	}

}
