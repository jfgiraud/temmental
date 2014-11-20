package temmental;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TextTest {

    @Test
    public void testText() throws TemplateException {
        Text text = new Text("hello mister", new Cursor("-:l1:c1"));

        assertEquals("hello mister", text.writeObject(null, null, null));
    }

}
