package temmental;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class DefaultFunctionTest extends AbstractTestElement {

    @Test
    public void testNotNull() throws TemplateException, IOException {

        DefaultFunction f = new DefaultFunction(
                new Identifier("$elem1", c(1, 1)),
                "something"
        );

        populateModel("elem1", "something...");

        assertEquals("something...", f.writeObject(null, model, null));
    }

    @Test
    public void testNull() throws TemplateException, IOException {

        DefaultFunction f = new DefaultFunction(
                new Identifier("$elem1", c(1, 1)),
                new Identifier("$elem2", c(1, 2))
        );

        populateModel("elem2", "something...");

        assertEquals("something...", f.writeObject(null, model, null));
    }

    @Test
    public void testMessage() throws TemplateException, IOException {
        // 'msg[$p1]!123¡
        DefaultFunction f = new DefaultFunction(
                message(identifier("'msg", p(1, 1)), list(identifier("$p1", p(1, 2)))),
                123
        );

        assertEquals(123, f.writeObject(null, model, null));
    }

}
