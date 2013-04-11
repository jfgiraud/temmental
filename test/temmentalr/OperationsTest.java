package temmentalr;

import static org.junit.Assert.*;

import org.junit.Test;

public class OperationsTest {

	@Test
	public void testAdd() {
		assertResult(4, Operations.add(1, 3));
		assertResult(4L, Operations.add(1, 3L));
		assertResult(4.0, Operations.add(1F, 3.0));
		assertResult(4.0, Operations.add(1, 3.0));
	}
	
	@Test
	public void testSub() {
		assertResult(-2, Operations.sub(1, 3));
		assertResult(-2L, Operations.sub(1, 3L));
		assertResult(-2F, Operations.sub(1F, 3L));
		assertResult(-2.0, Operations.sub(1F, 3.0));
	}

	@Test
	public void testMul() {
		assertResult(15, Operations.mul(5, 3));
		assertResult(15L, Operations.mul(5, 3L));
		assertResult(15L, Operations.mul(5L, 3));
		assertResult(15.0, Operations.mul(5.0, 3));
	}
	
//	@Test
//	public void testSub() {
//		assertResult(4, Operations.add(1, 3));
//		assertResult(4L, Operations.add(1, 3L));
//	}
	
	private void assertResult(Number expected, Number result) {
		assertEquals(expected, result);
		assertTrue(expected.getClass().equals(result.getClass()));
	}

}
