package temmental2;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CursorTest {

    private Stack linesLength;
    private Cursor cursor;

    @Before
    public void setUp() {
        linesLength = new Stack();
        cursor = new Cursor("-", 1, 0, linesLength);
    }

    private void write(String s) {
        for (int i = 0; i < s.length(); i++) {
            cursor.next(s.charAt(i));
        }
    }

    private void assertPosition(String pos) {
        assertEquals(pos, cursor.getPosition());
    }

    @Test
    public void testNextChar() {
        write("z");
        assertPosition("-:l1:c1");
    }

    @Test
    public void testNextCharNL() {
        write("z\nz");
        assertPosition("-:l2:c1");
        assertEquals(1, linesLength.depth());
        assertEquals((Integer) 1, (Integer) linesLength.pop());
    }

    @Test
    public void testMove1l() {
        write("hello");
        assertPosition("-:l1:c5");
        cursor.move1l();
        assertPosition("-:l1:c4");
    }

    @Test
    public void testMovel() {
        write("hello\nmister");
        assertPosition("-:l2:c6");
        cursor.movel("r", 0);
        assertPosition("-:l2:c5");
        cursor.movel("iste", 0);
        assertPosition("-:l2:c1");
        cursor.movel("m", 0);
        assertPosition("-:l1:c6");
        cursor.movel("\n", 0);
        assertPosition("-:l1:c5");
        cursor.movel("hello", 0);
        assertPosition("-:l1:c0");
    }

    @Test
    public void testMovel2() {
        write("hello\nmister");
        cursor.movel("hello\nmister", 0);
        assertPosition("-:l1:c0");
    }

    @Test
    public void testMovelNl() {
        write("hello\nmister");
        cursor.movel("\nmister", 0);
        assertPosition("-:l1:c5");
    }

    @Test
    public void testMovelNl2() {
        write("hello\nmister\njohn doe");
        cursor.movel("\nmister\njohn doe", 0);
        assertPosition("-:l1:c5");
    }

    @Test
    public void testMove1r() {
        linesLength.push(3);
        cursor = new Cursor("-", 1, 4, linesLength);
        cursor.move1r();
        assertEquals("-:l1:c5", cursor.getPosition());
    }
}
