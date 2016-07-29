package com.github.jfgiraud.temmental;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

public class IdentifierTest extends AbstractTestElement {

    @Test
    public void testIdentifierSyntax() throws TemplateException {
        assertInvalidSyntaxThrowsAnException("Invalid identifier syntax for '$var$iable' at position '-:l1:c1'.", "$var$iable");
        assertInvalidSyntaxThrowsAnException("Invalid identifier syntax for '$var?iable' at position '-:l1:c1'.", "$var?iable");
        assertInvalidSyntaxThrowsAnException("Invalid identifier syntax for ''variable?' at position '-:l1:c1'.", "'variable?");
        assertInvalidSyntaxThrowsAnException("Invalid identifier syntax for ''variable!' at position '-:l1:c1'.", "'variable!");
        assertInvalidSyntaxThrowsAnException("Invalid identifier syntax for '$var.iable' at position '-:l1:c1'.", "$var.iable");
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
    public void testIdentifierOptionalPresentButNull() throws TemplateException {
        Identifier variable = new Identifier("$variable?", new Cursor("-:l1:c1"));

        populateModel("variable", null);

        try {
            variable.writeObject(null, model, null);
            fail("An exception must be raised.");
        } catch (TemplateIgnoreRenderingException e) {
            assertEquals("Ignore rendering because key 'variable' is not present or has null value in the model map at position '-:l1:c1'.", e.getMessage());
        }
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

    private void assertInvalidSyntaxThrowsAnException(String expected, String expr) {
        try {
            new Identifier(expr, new Cursor("-:l1:c1"));
            fail("An exception must be raised.");
        } catch (TemplateException e) {
            assertEquals(expected, e.getMessage());
        }
    }

}
