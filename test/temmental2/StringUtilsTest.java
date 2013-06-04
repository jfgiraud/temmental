package temmental2;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

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
	
}
