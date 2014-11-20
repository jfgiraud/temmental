package temmental;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

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
    public void testUpperize() {
        assertEquals("ÍNTÈRNÁTÎÖNÀLÏZÂÇÏÔN WITH ACCENTS", StringUtils.upperize("ÍntèrnáTîönàlïzâÇïôn with accents"));
    }

    @Test
    public void testLowerize() {
        assertEquals("íntèrnátîönàlïzâçïôn with accents", StringUtils.lowerize("ÍntèrnáTîönàlïzâÇïôn with accents"));
    }

    @Test
    public void testCapitalize() {
        assertEquals("Íntèrnátîönàlïzâçïôn with accents", StringUtils.capitalize("ÍntèrnáTîönàlïzâÇïôn with accents"));
    }

    @Test
    public void testTitle() {
        assertEquals("Íntèrnátîönàlïzâçïôn With Accents", StringUtils.titleize("ÍntèrnáTîönàlïzâÇïôn with accents"));
        assertEquals("The Titleize String Method", StringUtils.titleize("the titleize string method"));
        assertEquals("The Titleize String  Method", StringUtils.titleize("the titleize string  method"));
        assertEquals("", StringUtils.titleize(""));
        assertEquals("Let'S Have Some Fun", StringUtils.titleize("let's have some fun"));
        assertEquals("A-Dash-Separated-String", StringUtils.titleize("a-dash-separated-string"));
        assertEquals("A-Dash-Separated-String", StringUtils.titleize("A-DASH-SEPARATED-STRING"));
        assertEquals("123", StringUtils.titleize("123"));
    }

    @Test
    public void testCamelize() {
        assertEquals("ÍntèrnátîönàlïzâçïônWithAccents", StringUtils.camelize("ÍntèrnáTîönàlïzâÇïôn  with\taccents"));
        assertEquals("ÍntèrnátîönàlïzâçïônWithAccents", StringUtils.camelize("ÍntèrnáTîönàlïzâÇïôn_with_accents"));

        assertEquals("TheCamelizeStringMethod", StringUtils.camelize("the_camelize_string_method"));

        assertEquals("TheCamelizeStringMethod", StringUtils.camelize("-the-camelize-string-method"));
        assertEquals("TheCamelizeStringMethod", StringUtils.camelize("the camelize string method"));
        assertEquals("TheCamelizeStringMethod", StringUtils.camelize(" the camelize string method"));
        assertEquals("TheCamelizeStringMethod", StringUtils.camelize("the camelize string method"));
        assertEquals("", StringUtils.camelize(""));
    }

    @Test
    public void testUnaccentify() {
        assertEquals("InternaTionalizaCion with accents", StringUtils.unaccentify("ÍntèrnáTîönàlïzâÇïôn with accents"));
    }

    @Test
    public void testConstantify() {
        assertEquals("INTERNATIONALIZACION_WITH_ACCENTS", StringUtils.constantify("Íntèrn-áTîönà_lïzâÇïôn  with\taccents[]"));
    }

    @Test
    public void testSlugify() {
        assertEquals("le-tae-xte-ci-dess-ousaeur", StringUtils.slugify("le t~\"à&e   xte ci-dess_ous[]â€"));
    }

    @Test
    public void testReverse() {
        assertEquals(".desrever eb ot txet ehT", StringUtils.reverse("The text to be reversed."));
    }

    @Test
    public void testSplit() {
        assertArrayEquals(new String[]{"Hello", "the", "World!"}, StringUtils.split("Hello*the*World!", "*", -1));
        assertArrayEquals(new String[]{"Hello*the*World!"}, StringUtils.split("Hello*the*World!", "*", 0));
        assertArrayEquals(new String[]{"Hello", "the*World!"}, StringUtils.split("Hello*the*World!", "*", 1));

        assertArrayEquals(new String[]{"Hello", "", "the", "World!"}, StringUtils.split("Hello**the*World!", "*", -1));

        assertArrayEquals(new String[]{"Hello", "the", "World!"}, StringUtils.split("Hello \t the  World!", null, -1));
        assertArrayEquals(new String[]{"Hello \t the  World!"}, StringUtils.split("Hello \t the  World!", null, 0));
        assertArrayEquals(new String[]{"Hello", "the  World! "}, StringUtils.split("Hello \t the  World! ", null, 1));

        assertArrayEquals(new String[]{"abc", "fgh", "", "ij"}, StringUtils.split("abcdefghdedeij", "de", -1));

    }

}
