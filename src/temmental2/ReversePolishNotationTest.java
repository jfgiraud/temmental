package temmental2;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;

public class ReversePolishNotationTest {

	@Test
	public void test() {
		String[] input = "( 1 + 2 ) * ( 3 / 4 ) ^ ( 5 + 6 )".split(" ");
		String[] output = ReversePolishNotation.infixToRPN(input);
		assertArrayEquals(new String[] { "1", "2", "+", "3", "4", "/", "5", "6", "+", "^", "*" }, output);
	}

}
