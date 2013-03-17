package temmental2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import junit.framework.TestCase;

public class StringUtilsTest extends TestCase {

	public void testJoin() {
        List<String> strings = new ArrayList<String>();
        assertEquals("", StringUtils.join("--", strings));
        
        strings.add("foo");
        assertEquals("foo", StringUtils.join("--", strings));
        
        strings.add("bar");
        assertEquals("foo--bar", StringUtils.join("--", strings));
    }
    
    public void testIsNotEmpty() {
        assertTrue(StringUtils.isNotEmpty("foobar"));
        assertFalse(StringUtils.isNotEmpty(""));
        assertFalse(StringUtils.isNotEmpty(null));
    }
    
    public void testIsNullOrEmpty() {
        assertFalse(StringUtils.isEmpty("foobar"));
        assertTrue(StringUtils.isEmpty(""));
        assertTrue(StringUtils.isEmpty(null));
    }
    
    public void testFormatHexString() {
        assertEquals("", StringUtils.hexlify(new byte[0]));
        
        byte[] data2 = { -54, -2, 32, 0 };
        assertEquals("cafe2000", StringUtils.hexlify(data2));
    }
    
    public void testReplace() {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("<b>", "<i>");
        map.put("</b>", "</i>");
        map.put("<i>", "<b>");
        map.put("</i>", "</b>");
        assertEquals("<b>Hello</b> <i>Jeff</i>", StringUtils.replace("<i>Hello</i> <b>Jeff</b>", map));
        assertEquals("<b>Hello</b> <i>Jeff</i>", StringUtils.replace("<i>Hello</i> <b>Jeff</b>", map));
        
        map.clear();
        map.put("<b>", "<i>");
        map.put("<i>", "<u>");
        assertEquals("<u>Hello <i>Jeff", StringUtils.replace("<i>Hello <b>Jeff", map));
    }

    public void testRemove() {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("<b>", "<i>");
        map.put("</b>", "</i>");
        map.put("<i>", "<b>");
        map.put("</i>", "</b>");
        assertEquals("Hello Jeff", StringUtils.remove("<i>Hello</i> <b>Jeff</b>", "<i>", "</i>", "<b>", "</b>"));

        assertEquals("foobarbaz", StringUtils.remove(" foo\tbar  baz ", "\n", " ", "\t"));
    }
    
}
