package temmental2;

import org.junit.Test;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class StringUtilsTest {

	@Test
	public void testCountOccurrences() {
		assertEquals(3, StringUtils.countOccurrences("hello the world", 'l'));
	}

	@Test
	public void testJoin() {
		assertEquals("apple or orange or lemon", StringUtils.join(" or ", Arrays.asList("apple", "orange", "lemon")));
		assertEquals("apple or orange or lemon", StringUtils.join(" or ", "apple", "orange", "lemon"));
	}

	@Test
	public void testLStrip() {
		assertEquals("a word ", StringUtils.lstrip("\t a word "));
	}

	@Test
	public void testRStrip() {
		assertEquals("\t a word", StringUtils.rstrip("\t a word\t "));
	}
	
	@Test
	public void testStrip() {
		assertEquals("a word", StringUtils.strip("\t a word\t "));
	}
	
	@Test
	public void testCapitalize() {
		assertEquals("Íntèrnátîönàlïzâçïôn with accents", StringUtils.capitalize("ÍntèrnáTîönàlïzâÇïôn with accents"));
	}
	
	@Test
	public void testTitle() {
		assertEquals("Íntèrnátîönàlïzâçïôn With Accents", StringUtils.titlelize("ÍntèrnáTîönàlïzâÇïôn with accents"));
	}
	
	@Test
	public void testReverse() {
		assertEquals(".desrever eb ot txet ehT", StringUtils.reverse("The text to be reversed."));
	}
	
	@Test 
	public void testSplit() {
		assertArrayEquals(new String[] { "Hello", "the", "World!" }, StringUtils.split("Hello*the*World!", "*", -1));
		assertArrayEquals(new String[] { "Hello*the*World!" }, StringUtils.split("Hello*the*World!", "*", 0));
		assertArrayEquals(new String[] { "Hello", "the*World!" }, StringUtils.split("Hello*the*World!", "*", 1));
		
		assertArrayEquals(new String[] { "Hello", "", "the", "World!" }, StringUtils.split("Hello**the*World!", "*", -1));
		
		assertArrayEquals(new String[] { "Hello", "the", "World!" }, StringUtils.split("Hello \t the  World!", null, -1));
		assertArrayEquals(new String[] { "Hello \t the  World!" }, StringUtils.split("Hello \t the  World!", null, 0));
		assertArrayEquals(new String[] { "Hello", "the  World! " }, StringUtils.split("Hello \t the  World! ", null, 1));
	
		assertArrayEquals(new String[] { "abc", "fgh", "", "ij" }, StringUtils.split("abcdefghdedeij", "de", -1));
		
	}

    @Test
    public void testTree() throws IOException {
        List<Object> list = Arrays.asList("a", "b", Arrays.asList("c", "d"), "e");
        StringUtils.tree(new PrintWriter(System.out), 1, list);
        fail();
    }
	
}
