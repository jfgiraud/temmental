package temmental2;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

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

        try {
            variable.writeObject(null, model, null);
            fail("An exception must be raised.");
        } catch (TemplateIgnoreRenderingException e) {
            assertEquals("Ignore rendering because key 'variable' is not present or has null value in the model map at position '-:l1:c1'.", e.getMessage());
        }
	}

    @Test
    public void testIdentifierOptionalNotPresent2() throws TemplateException {
        Identifier variable = new Identifier("$variable!", new Cursor("-:l1:c1"));

        assertEquals("", variable.writeObject(null, model, null));
    }

    @Test
    public void testIdentifierOptionalNotPresentWithDefaultValue() throws TemplateException {
        Identifier variable = new Identifier("$variable!", new Cursor("-:l1:c1"));

        assertEquals("", variable.writeObject(null, model, null));
    }

    @Test
    public void testIdentifierOptionalPresentWithDefaultValue() throws TemplateException {
        Identifier variable = new Identifier("$variable!\"some thing\"", new Cursor("-:l1:c1"));

        populateModel("variable", "hello mister");

        assertEquals("hello mister", variable.writeObject(null, model, null));
    }
    @Test
    public void testIdentifierOptionalNotPresentWithSettedDefaultValueString() throws TemplateException {
        Identifier variable = new Identifier("$variable!\"some thing\"", new Cursor("-:l1:c1"));

        assertEquals("some thing", variable.writeObject(null, model, null));
    }

    @Test
    public void testIdentifierOptionalNotPresentWithSettedDefaultValueInt() throws TemplateException {
        Identifier variable = new Identifier("$variable!123", new Cursor("-:l1:c1"));

        assertEquals(123, variable.writeObject(null, model, null));
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
