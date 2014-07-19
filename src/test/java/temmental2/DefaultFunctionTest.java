package temmental2;

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
        // 'msg[$p1]!123
        DefaultFunction f = new DefaultFunction(
                message(identifier("'msg", p(1, 1)), list(identifier("$p1", p(1, 2)))),
                123
        );

        assertEquals(123, f.writeObject(null, model, null));
    }



    /*
    @Test
    public void testIdentifierOptionalPresentWithDefaultValue() throws TemplateException {
        Identifier variable = new Identifier("$variable", new Cursor("-:l1:c1"), "some thing");

        populateModel("variable", "hello mister");

        assertEquals("hello mister", variable.writeObject(null, model, null));
    }
    @Test
    public void testIdentifierOptionalNotPresentWithSettedDefaultValueString() throws TemplateException {
        Identifier variable = new Identifier("$variable", new Cursor("-:l1:c1"), "some thing");

        assertEquals("some thing", variable.writeObject(null, model, null));
    }

    @Test
    public void testIdentifierOptionalNotPresentWithSettedDefaultValueInt() throws TemplateException {
        Identifier variable = new Identifier("$variable", new Cursor("-:l1:c1"), 123);

        assertEquals(123, variable.writeObject(null, model, null));
    }

    @Test
    public void testDynamicFunctionCharAtTransform3() throws TemplateException, NoSuchMethodException, SecurityException, IOException {
        Functionp f = functionp(identifier("$fp", "-:l1:c2", ""), list(2), identifier("$text", "-:l1:c2"));

        populateModel("text", "Something...");

        assertEquals("Something...", f.writeObject(transforms, model, null));
    }

    @Test
    public void testFunctionVarNotFoundButOptional2() throws TemplateException, IOException {
        // $text:$g!:$f
        Function f = function(identifier("$f", "-:l1:c2"),
                function(identifier("$g", "-:l1:c2", ""), identifier("$text", "-:l1:c2")));

        populateTransform("upper", tupper);
        populateTransform("quote", tquote);

        populateModel("f", "upper");
        populateModel("text", "Something...");

        assertEquals("SOMETHING...", f.writeObject(transforms, model, null));


    }

    @Test
    public void testFunctionVarNotFoundButOptional3() throws TemplateException, IOException {
        Function f = function(identifier("$f", "-:l1:c2", ""), identifier("$text", "-:l1:c2"));

        populateModel("text", "Something...");

        assertEquals("Something...", f.writeObject(transforms, model, null));


    }

    @Test
    public void testIdentifierMessageCanHaveDefaultValue() throws IOException, TemplateException {
        Message message = message(identifier("$message", "-:l1:c1", "default"),
                list(identifier("$firstname", "-:l1:c2"), identifier("$lastname", "-:l1:c3")));

        populateProperty("default", "hello {0} {1}");

        populateModel("firstname", "John");
        populateModel("lastname", "Doe");

        assertEquals("hello John Doe", message.writeObject(null, model, messages));
    }

    @Test
    public void testIdentifierMessageCanHaveDefaultValue2() throws IOException, TemplateException {
        Message message = message(identifier("$message", "-:l1:c1", identifier("$def", "-:l1:c4")),
                list(identifier("$firstname", "-:l1:c2"), identifier("$lastname", "-:l1:c3")));

        populateProperty("default", "hello {0} {1}");

        populateModel("def", "default");
        populateModel("firstname", "John");
        populateModel("lastname", "Doe");

        assertEquals("hello John Doe", message.writeObject(null, model, messages));
    }
    @Test
    public void testIdentifierMessageCanHaveDefaultValue3() throws IOException, TemplateException {
        Message message = message(identifier("$message", "-:l1:c1", identifier("'def", "-:l1:c4")),
                list(identifier("$firstname", "-:l1:c2"), identifier("$lastname", "-:l1:c3")));

        populateProperty("default", "hello {0} {1}");

        populateModel("def", "default");
        populateModel("firstname", "John");
        populateModel("lastname", "Doe");

        assertEquals("hello John Doe", message.writeObject(null, model, messages));
    } */

}
