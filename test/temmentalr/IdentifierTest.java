package temmentalr;

import static org.junit.Assert.*;

import org.junit.Test;

public class IdentifierTest {

	@Test
	public void testIdentifierValidities() {
		assertTrue(Identifier.isValid("'foo"));
		assertFalse(Identifier.isValid("foo"));
		assertTrue(Identifier.isValid("$foo"));
		assertTrue(Identifier.isValid("$foo?"));
		assertFalse(Identifier.isValid("'foo?"));
		assertTrue(Identifier.isValid("'foo"));
	}

}
