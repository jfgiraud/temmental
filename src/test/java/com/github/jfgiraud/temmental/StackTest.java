package com.github.jfgiraud.temmental;

import com.github.jfgiraud.temmental.Stack;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class StackTest {

    private Stack stack;

    @Before
    public void setUp() {
        stack = new Stack();
    }

    @Test
    public void testPush() {
        stack.push("a");
        assertEquals(1, stack.depth());

        stack.push("b");
        assertEquals(2, stack.depth());
    }

    @Test
    public void testList() {
        stack.push("a");
        stack.push("b");
        stack.tolist(2);
        assertEquals(1, stack.depth());
        List l = (List) stack.value(1);
        assertEquals(2, l.size());
        assertEquals("a", l.get(0));
        assertEquals("b", l.get(1));
    }

    @Test
    public void testvalue() {
        stack.push("a");
        stack.push("b");
        stack.push("c");
        assertEquals(3, stack.depth());
        assertEquals("a", stack.value(3));
        assertEquals("b", stack.value(2));
        assertEquals("c", stack.value(1));
    }

    @Test
    public void tesreverse() {
        stack.push("a");
        stack.push("b");
        stack.push("c");
        stack.reverse();
        assertEquals(3, stack.depth());
        assertEquals("a", stack.value(1));
        assertEquals("b", stack.value(2));
        assertEquals("c", stack.value(3));
    }

    @Test
    public void testToListI() {
        stack.push("a");
        stack.push("b");
        stack.tolist(2);
        assertEquals(1, stack.depth());
        Object o = stack.pop();
        assertTrue(o instanceof List);
        assertEquals(Arrays.asList("a", "b"), (List) o);
    }

    @Test
    public void testToList() {
        stack.push("a");
        stack.push("b");
        stack.push(2);
        stack.tolist();
        assertEquals(1, stack.depth());
        Object o = stack.pop();
        assertTrue(o instanceof List);
        assertEquals(Arrays.asList("a", "b"), (List) o);
    }

}
