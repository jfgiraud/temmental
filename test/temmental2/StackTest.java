package temmental2;

import static org.junit.Assert.*;

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
	public void testPop() {
		stack.push("a");
		stack.push("b");

		assertEquals(2, stack.depth());
		assertEquals("b", stack.drop());
		assertEquals(1, stack.depth());
		assertEquals("a", stack.drop());
		assertEquals(0, stack.depth());
	}
	
	@Test
	public void testList() {
		stack.push("a");
		stack.push("b");
		stack.tolist(2);
		assertEquals(1, stack.depth());
		List l = (List) stack.drop();
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
		assertEquals("a", stack.drop());
		assertEquals("b", stack.drop());
	}
	
	@Test
	public void testRemove() {
		stack.push("a");
		stack.push("b");
		stack.push("c");
		assertEquals("a", stack.remove(3));
	}
	
	@Test
	public void testRot() {
		stack.push("1");
		stack.push("2");
		stack.push("3");
		stack.rot();
		assertEquals(3, stack.depth());
		assertEquals("1", stack.drop());
		assertEquals("3", stack.drop());
		assertEquals("2", stack.drop());
	}
		
	@Test
	public void testSwapEquals2Rolld() {
		stack.push("a");
		stack.push("b");
		stack.push("c");
		stack.rolld(2);
		assertEquals(3, stack.depth());
		assertEquals("b", stack.drop());
		assertEquals("c", stack.drop());
		assertEquals("a", stack.drop());
	}
	
	@Test
	public void testSwapEquals2RolldBis() {
		stack.push("a");
		stack.push("b");
		stack.rolld(2);
		assertEquals(2, stack.depth());
		assertEquals("a", stack.drop());
		assertEquals("b", stack.drop());
	}
	
	@Test
	public void testDrop() {
		stack.push("a");
		stack.push("b");
		stack.push("c");
		stack.drop();
		assertEquals(2, stack.depth());
		assertEquals("b", stack.drop());
		assertEquals("a", stack.drop());
	}
	
	@Test
	public void testNip() {
		stack.push("a");
		stack.push("b");
		stack.push("c");
		stack.nip();
		assertEquals(2, stack.depth());
		assertEquals("c", stack.drop());
		assertEquals("a", stack.drop());
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
		assertEquals("5", stack.drop());
		assertEquals("4", stack.drop());
		assertEquals("3", stack.drop());
		assertEquals("6", stack.drop());
		assertEquals("2", stack.drop());
		assertEquals("1", stack.drop());
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
		assertEquals("3", stack.drop());
		assertEquals("6", stack.drop());
		assertEquals("5", stack.drop());
		assertEquals("4", stack.drop());
		assertEquals("2", stack.drop());
		assertEquals("1", stack.drop());
	}
	
	@Test
	public void testDup() {
		stack.push("a");
		stack.dup();
		assertEquals(2, stack.depth());
		assertEquals("a", stack.drop());
		assertEquals("a", stack.drop());
	}
	
	@Test
	public void testOver() {
		stack.push("a");
		stack.push("b");
		stack.over();
		assertEquals(3, stack.depth());
		assertEquals("a", stack.drop());
		assertEquals("b", stack.drop());
		assertEquals("a", stack.drop());
	}
}
