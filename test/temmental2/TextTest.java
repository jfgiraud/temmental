package temmental2;

import static org.junit.Assert.*;

import org.junit.Test;

public class TextTest {

	@Test
	public void testText() throws TemplateException {
		Text text = new Text("hello mister", new Cursor("-:l1:c1"));
		
		assertEquals("hello mister", text.writeObject(null, null, null));
	}
	
}
