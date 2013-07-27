package temmental2;

import static org.junit.Assert.*;

import org.junit.Test;

public class IdentifierTest extends AbstractTestElement {

	@Test
	public void testIdentifierSyntax() throws TemplateException {
		assertInvalidSyntaxThrowsAnException("Invalid identifier syntax for '$var$iable' at position '-:l1:c1'.", "$var$iable");
		assertInvalidSyntaxThrowsAnException("Invalid identifier syntax for '$var?iable' at position '-:l1:c1'.", "$var?iable");
		assertInvalidSyntaxThrowsAnException("Invalid identifier syntax for ''variable?' at position '-:l1:c1'.", "'variable?");
		assertInvalidSyntaxThrowsAnException("Invalid identifier syntax for ''variable!' at position '-:l1:c1'.", "'variable!");
	}

	@Test
	public void testIdentifierRequired() throws TemplateException {
		Identifier variable = new Identifier("$variable", new Cursor("-:l1:c1"));
		
		populateModel("variable", "hello mister");
		
		assertEquals("hello mister", variable.writeObject(null, model, null));
	}
	
	@Test
	public void testIdentifierOptionalPresent() throws TemplateException {
		Identifier variable = new Identifier("$variable?", new Cursor("-:l1:c1"));
		
		populateModel("variable", "hello mister");
		
		assertEquals("hello mister", variable.writeObject(null, model, null));
	}
	
	@Test
	public void testIdentifierOptionalNotPresent() throws TemplateException {
		Identifier variable = new Identifier("$variable?", new Cursor("-:l1:c1"));
		
		assertNull(variable.writeObject(null, model, null));
	}

	private void assertInvalidSyntaxThrowsAnException(String expected, String expr) {
		try {
			new Identifier(expr, new Cursor("-:l1:c1"));
			fail("An exception must be raised.");
		} catch (TemplateException e) {
			assertEquals(expected, e.getMessage());
		}
	}

}
