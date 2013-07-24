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

public class TemplateTest extends AbstractTestTemplate {

	protected Stack stack;

	protected Template template;
	protected Map<String,Object> model;
	protected Properties properties;

	private boolean displayRule = true;
	
	@Before
	public void setUp() throws FileNotFoundException, TemplateException, IOException {
		properties = new Properties();
		template = new Template(null, null, properties);
		model = new HashMap<String, Object>();
		stack = new Stack();
	}
	
	@Test
	public void testText() throws IOException, TemplateException {
		parse("Lorem ipsum dolor sit amet, consectetur adipiscing elit.");
		assertParsingEquals(text("Lorem ipsum dolor sit amet, consectetur adipiscing elit.", p(1, 1)));
	}
	
	@Test
	public void testTildeCharacterMustBeEscapedForText() throws IOException, TemplateException {
		parse("\\~Lorem ipsum dolor sit\\~ amet, consectetur adipiscing elit.\\~");
//             1 23456789 123456789 12 3456789 123456789 123456789 12345678 901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901
		assertParsingEquals(text("~Lorem ipsum dolor sit~ amet, consectetur adipiscing elit.~", p(1, 1)));
		
		parse("\\~Lorem ipsum dolor sit amet, consectetur adipiscing elit.\\~");
		assertParsingEquals(text("~Lorem ipsum dolor sit amet, consectetur adipiscing elit.~", p(1, 1)));
	
		parse("Lorem ipsum dolor sit amet, consectetur adipiscing elit.\\~");
		assertParsingEquals(text("Lorem ipsum dolor sit amet, consectetur adipiscing elit.~", p(1, 1)));
	}
	
	@Test
	public void testAnExceptionIsRaisedWhenTildeIsNotEscaped() throws IOException, TemplateException {
		assertParsingThrowsException("End of parsing. Character '~' is not escaped at position '-:l1:c59'.", 
				"\\~Lorem ipsum dolor sit\\~ amet, consectetur adipiscing elit.~");
	}

	@Test
	public void testAnExpressionIsBetweenTwoTildeCharactersWhichAreNotEscaped() throws IOException, TemplateException {
		parse("Some text...~$data~And after");

		assertParsingEquals(text("Some text...", p(1, 1)),
				toparse("~$data~", p(1, 13)), 
				text("And after", p(1, 20)));
	}

	@Test
	public void testReturnCharacterShouldChangeTheLineNumber() throws IOException, TemplateException {
		parse("Lorem\nipsum~$data~consectetur\nadipiscing elit.");

		assertParsingEquals(text("Lorem\nipsum", p(1, 1)),
				toparse("~$data~", p(2, 6)), 
				text("consectetur\nadipiscing elit.", p(2, 13)));
	}
	
	@Test
	public void testTildeShouldBeEscapedInStringParameters() throws IOException, TemplateException {
		assertParsingThrowsException("End of parsing. Character '~' is not escaped at position '-:l1:c42'.", 
				"Some text...~$data[\"some~thing\"]~And after");
		
		parse("Some text...~$data[\"some\\~thing\"]~And after");
		
		assertParsingEquals(text("Some text...", p(1, 1)),
				toparse("~$data[\"some~thing\"]~", p(1, 13)),
				text("And after", p(1, 34)));
				
	}
	
	@Test
	public void testAnExceptionIsRaisedWhenAnExpressionEndsWithoutClosingTildeCharacterOnEndOfLine() throws IOException, TemplateException {
		assertParsingThrowsException("End of parsing. Character '~' is not escaped at position '-:l1:c45'.", 
				"~$hello['mister[$firstname],$lastname:'upper]");
	}	
	
	@Test
	public void testMessage() throws IOException, TemplateException {
		parse("Text before~$hello['mister[$firstname],$lastname:'upper]~Text after");
		assertParsingEquals(
				text("Text before", p(1, 1)), 
				toparse("~$hello['mister[$firstname],$lastname:'upper]~", p(1, 12)), 
				text("Text after", p(1, 58)));
	}
	
	@Test
	public void testComplexeExpression() throws IOException, TemplateException {
		parse("Some text... With something inside tildes '~$hello['mister[$firstname],$lastname:'upper:'quote<\"'\">]:'replaceAll<\"a\",\"*\">~' and that's all. The ~$end~ ~$b2~!");
		assertParsingEquals(text("Some text... With something inside tildes '", p(1, 1)), 
				toparse("~$hello['mister[$firstname],$lastname:'upper:'quote<\"'\">]:'replaceAll<\"a\",\"*\">~", p(1, 44)), 
				text("' and that's all. The ", p(1, 123)),
				toparse("~$end~", p(1, 145)),
				text(" ", p(1, 151)),
				toparse("~$b2~", p(1, 152)),
				text("!", p(1, 157))
				);
	}
	
	@Test
	public void testForLoop() throws IOException, TemplateException {
		parse("~#for $models~a~$v~a~#/for~");
		//     123456789012345678901234567890
		assertParsingEquals(
				toparse("~#for $models~", p(1, 1)),
				text("a", p(1, 15)),
				toparse("~$v~", p(1, 16)),
				text("a", p(1, 20)),
				toparse("~#/for~", p(1, 21))
				);
	}
	
	@Test
	public void testForLoop2() throws IOException, TemplateException {
		parse("~#for $models~~$v~~#/for~");
		//     123456789012345678901234567890
		assertParsingEquals(
				toparse("~#for $models~", p(1, 1)),
				toparse("~$v~", p(1, 15)),
				toparse("~#/for~", p(1, 19))
				);
	}
	
	@Test
	public void testSimpleExpression() throws IOException, TemplateException {
		parse("~$anAloneVariable~");
		assertParsingEquals(toparse("~$anAloneVariable~", p(1, 1)));
	}
	
	protected void parse(String s) throws IOException, TemplateException {
		if (displayRule) {
			displayRule(s);
		}
		template.parseString(s, false);
	}

	protected void assertParsingEquals(Object ... expectedStack) throws IOException {
		Stack stack = template.getStack();
		assertEquals(expectedStack.length, stack.depth());
		for (int i=0; i<expectedStack.length; i++) {
			assertEquals("Invalid element #" + (i+1), expectedStack[expectedStack.length-i-1], stack.value(i+1));
		}
	}

	protected void assertParsingThrowsException(String expectedMessage, String pattern) {
		try {
			parse(pattern);
			Stack stack = template.getStack();
			stack.printStack(System.out);
			fail("An exception must be raised.");
		} catch (Exception e) {
			e.printStackTrace(System.err);
			assertEquals(expectedMessage, e.getMessage());
		}
	}}
