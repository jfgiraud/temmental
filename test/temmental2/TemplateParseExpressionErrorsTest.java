package temmental2;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

public class TemplateParseExpressionErrorsTest extends AbstractTestTemplate {

	protected Map<String,Object> model;
	protected Properties properties;
	private Template template;
	private boolean displayRule = true;
	
	@Before
	public void setUp() throws FileNotFoundException, TemplateException, IOException {
		model = new HashMap<String, Object>();
		properties = new Properties();
		template = new Template(null, null, properties);
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
		assertParseThrowsException("No function before '<' at position '-:l1:c9'.", 
				"~$msg[]:<$arg>~");
	}
	
	@Test
	public void testErrorNoParameterIdentifier() throws IOException, TemplateException {
		assertParseThrowsException("No parameter before ',' at position '-:l1:c19'.", 
				"~$msg[]:$function<,$arg>~");
	}
	
	@Test
	public void testErrorInvalidBracket() throws IOException, TemplateException {
		assertParseThrowsException("Corresponding bracket for ')' at position '-:l1:c23' is invalid (found '<' at position '-:l1:c18').", 
				"~$msg[]:$function<$arg)~");
	}
	
	@Test
	public void testErrorXXX() throws IOException, TemplateException {
		assertParseThrowsException("No parameter before ',' at position '-:l1:c19'.", 
				"~{$variable}:$function~");
	}
	
	@Test
	public void testComplexMessage2() throws IOException, TemplateException {
		assertParseThrowsException("Invalid length for char at position '-:l1:c54').",
				"~$message[$firstname:'upper,$lastname:'replace<'ab',','>]:'quote~");
	}
	
	
	protected void assertParseThrowsException(String expectedMessage, String pattern) {
		if (displayRule) {
			displayRule(pattern);
		}

		try {
			template.parseString(pattern, true);
			fail("An exception must be raised.");
		} catch (Exception e) {
			assertEquals(expectedMessage, e.getMessage());
		}
	}
	
}
