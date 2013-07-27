package temmental2;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

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
	public void testDrop() {
		stack.push("a");
		stack.push("b");

		assertEquals(2, stack.depth());
		stack.drop();
		assertEquals("a", stack.value(1));
		stack.drop();
		assertEquals(0, stack.depth());
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
	public void testSwap() {
		stack.push("a");
		stack.push("b");
		stack.swap();
		assertEquals(2, stack.depth());
		assertEquals("a", stack.value(1));
		assertEquals("b", stack.value(2));
	}
	
//	@Test
//	public void testRemove() {
//		stack.push("a");
//		stack.push("b");
//		stack.push("c");
//		assertEquals("a", stack.remove(3));
//	}
	
	@Test
	public void testRot() {
		stack.push("1");
		stack.push("2");
		stack.push("3");
		stack.rot();
		assertEquals(3, stack.depth());
		assertEquals("2", stack.value(3));
		assertEquals("3", stack.value(2));
		assertEquals("1", stack.value(1));
	}
	
	@Test
	public void testDupn() {
		stack.push("1");
		stack.push("2");
		stack.push("3");
		stack.dupn(3);
		assertEquals(6, stack.depth());
		assertEquals("1", stack.value(6));
		assertEquals("2", stack.value(5));
		assertEquals("3", stack.value(4));
		assertEquals("1", stack.value(3));
		assertEquals("2", stack.value(2));
		assertEquals("3", stack.value(1));
	}
	
	@Test
	public void testNDupn() {
		stack.push("1");
		stack.push("2");
		stack.push("3");
		stack.ndupn("new", 3);
		assertEquals(7, stack.depth());
		assertEquals("1", stack.value(7));
		assertEquals("2", stack.value(6));
		assertEquals("3", stack.value(5));
		assertEquals("new", stack.value(4));
		assertEquals("new", stack.value(3));
		assertEquals("new", stack.value(2));
		assertEquals(3, stack.value(1));
	}
	
	@Test
	public void testDupdup() {
		stack.push("1");
		stack.dupdup();
		assertEquals(3, stack.depth());
		assertEquals("1", stack.value(1));
		assertEquals("1", stack.value(2));
		assertEquals("1", stack.value(3));
	}
	
	@Test
	public void testUnrot() {
		stack.push("1");
		stack.push("2");
		stack.push("3");
		stack.unrot();
		assertEquals(3, stack.depth());
		assertEquals("2", stack.value(1));
		assertEquals("1", stack.value(2));
		assertEquals("3", stack.value(3));
	}
		
	@Test
	public void testSwapEquals2Rolld() {
		stack.push("a");
		stack.push("b");
		stack.push("c");
		stack.rolld(2);
		assertEquals(3, stack.depth());
		assertEquals("b", stack.value(1));
		assertEquals("c", stack.value(2));
		assertEquals("a", stack.value(3));
	}
	
	@Test
	public void testSwapEquals2RolldBis() {
		stack.push("a");
		stack.push("b");
		stack.rolld(2);
		assertEquals(2, stack.depth());
		assertEquals("b", stack.value(2));
		assertEquals("a", stack.value(1));
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
	public void testNip() {
		stack.push("a");
		stack.push("b");
		stack.push("c");
		stack.nip();
		assertEquals(2, stack.depth());
		assertEquals("c", stack.value(1));
		assertEquals("a", stack.value(2));
	}
	
	@Test
	public void testRolld() {
		stack.push("1");
		stack.push("2");
		stack.push("3");
		stack.push("4");
		stack.push("5");
		stack.push("6");
		stack.rolld(4);
		assertEquals(6, stack.depth());
		assertEquals("5", stack.value(1));
		assertEquals("4", stack.value(2));
		assertEquals("3", stack.value(3));
		assertEquals("6", stack.value(4));
		assertEquals("2", stack.value(5));
		assertEquals("1", stack.value(6));
	}
	
	@Test
	public void testRoll() {
		stack.push("1");
		stack.push("2");
		stack.push("3");
		stack.push("4");
		stack.push("5");
		stack.push("6");
		stack.roll(4);
		assertEquals(6, stack.depth());
		assertEquals("1", stack.value(6));
		assertEquals("2", stack.value(5));
		assertEquals("4", stack.value(4));
		assertEquals("5", stack.value(3));
		assertEquals("6", stack.value(2));
		assertEquals("3", stack.value(1));
	}
	
	@Test
	public void testRollZero() {
		stack.push("1");
		stack.push("2");
		stack.push("3");
		stack.push("4");
		stack.push("5");
		stack.push("6");
		stack.roll(0);
		assertEquals(6, stack.depth());
		assertEquals("6", stack.value(1));
		assertEquals("5", stack.value(2));
		assertEquals("4", stack.value(3));
		assertEquals("3", stack.value(4));
		assertEquals("2", stack.value(5));
		assertEquals("1", stack.value(6));
	}
	
	@Test
	public void testDup() {
		stack.push("a");
		stack.dup();
		assertEquals(2, stack.depth());
		assertEquals("a", stack.value(1));
		assertEquals("a", stack.value(2));
	}
	
	@Test
	public void testOver() {
		stack.push("a");
		stack.push("b");
		stack.over();
		assertEquals(3, stack.depth());
		assertEquals("a", stack.value(1));
		assertEquals("b", stack.value(2));
		assertEquals("a", stack.value(3));
	}
	
	@Test
	public void testUnvalue() {
		stack.push("a");
		stack.push("b");
		stack.push("c");
		stack.push("d");
		stack.unpick("new", 3);
		assertEquals(4, stack.depth());
		assertEquals("d", stack.value(1));
		assertEquals("c", stack.value(2));
		assertEquals("new", stack.value(3));
		assertEquals("a", stack.value(4));
	}
	
	@Test
	public void testUnvalueZero() {
		stack.push("a");
		stack.push("b");
		stack.unpick("new", 0);
		assertEquals(2, stack.depth());
		assertEquals("b", stack.value(1));
		assertEquals("a", stack.value(2));
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
