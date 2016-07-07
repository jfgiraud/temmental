package com.github.jfgiraud.temmental;

import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ArrayTest extends AbstractTestElement {

    @Test
    public void testArray() throws TemplateException, NoSuchMethodException, SecurityException, IOException {

        Transform<List<Integer>, Integer> add = new Transform<List<Integer>, Integer>() {
            public Integer apply(List<Integer> values) {
                int s = 0;
                for (int i : values) {
                    s += i;
                }
                return s;
            }
        };

        Function f = function(identifier("$f", p(1, 12)),
                array(p(7, 7), identifier("$b1", p(1, 3)), identifier("$b2", p(1, 7))));

        populateTransform("somme", add);

        populateModel("f", "somme");
        populateModel("b1", 5);
        populateModel("b2", 8);

        assertEquals(13, f.writeObject(transforms, model, null));
    }

    @Test
    public void testArrayNullValue() throws TemplateException, NoSuchMethodException, SecurityException, IOException {

        Transform<List<Integer>, Integer> add = new Transform<List<Integer>, Integer>() {
            public Integer apply(List<Integer> values) {
                int s = 0;
                for (int i : values) {
                    s += i;
                }
                return s;
            }
        };

        Transform<Integer, Integer> toNull = new Transform<Integer, Integer>() {
            public Integer apply(Integer value) {
                return null;
            }
        };

        // ~($b1,$b2:'z):$f~
        // 12345678901234567

        Function f = function(identifier("$f", p(1, 15)),
                array(p(1, 2), identifier("$b1", p(1, 3)),
                        function(identifier("'z", p(1, 11)),
                                identifier("$b2", p(1, 7)))));

        populateTransform("somme", add);
        populateTransform("z", toNull);

        populateModel("f", "somme");
        populateModel("b1", 5);
        populateModel("b2", 8);

        try {
            assertEquals(13, f.writeObject(transforms, model, null));
            fail("An exception must be raised.");
        } catch (Exception e) {
            //e.printStackTrace();
            assertEquals("Unable to render array at position '-:l1:c2'. Required parameter #2 is null.", e.getMessage());
        }
    }

}
