package temmental2.rpl;

import org.junit.Test;
import temmental2.StackException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class RplTest {

    private void assertStackOp(List expected, List elements, String ops) {
        assertStackOp(expected, elements, ops, null);
    }

    private void assertStackOp(List expected, List elements, String ops, StackException err) {
        HashMap egv = new HashMap();
        HashMap igv = new HashMap();

        List elv = new ArrayList();
        List ilv = new ArrayList();

        assertEquals(expected.getClass(), elements.getClass());
        RplStack e = new RplStack((List) expected);
        RplStack i = new RplStack((List) elements);
        i.setGlobalVariables(igv);
        i.setLocalVariables(ilv);

        List<String> tokens = RplStack.tokenize(ops);

        Throwable oo = null;
        try {
            Prog p = new Prog();
            p.read_until(tokens, null);
            p.apply(i, null, false, true);
        } catch (Throwable ooo) {
            oo = ooo;
        }

        if (err != null) {
            assertEquals(err, oo);
        } else {
            if (oo != null) {
                fail(oo.getMessage());
            }
            assertEquals(e.getElements(), i.getElements());
            assertEquals(egv, i.getGlobalVariables());
            assertEquals(elv, i.getLocalVariables());
        }
    }


    /*
    def assertTokenize(expected, pattern):
            tokens = tokenize(pattern)
            assert expected == tokens, "EXPECTED: %s ACTUAL: %s" % (str(expected), str(tokens))

        def assertStackOp(expected, elements, ops, ret=None):
            tokens = tokenize(ops)
            if True:
                print '*' * 80
                print expected, elements, ops
                print tokens
                print '-' * 80


            assert type(expected) == type(elements)
            if type(expected) == tuple:
                egv = expected[1]
                elv = expected[2]
                expected = expected[0]
                igv = elements[1]
                ilv = elements[2]
                elements = elements[0]
            else:
                egv = {}
                elv = []
                igv = {}
                ilv = []



            i=Stack(elements)
            e=Stack(expected)
            i.g_variables = igv
            i.l_variables = ilv

            try:
                p = Prog()
                p.read_until(tokens, None)
                if True:
                    print p.s
                p.apply(i, executeFunction=True)
                assert e == i, "EXPECTED: %s ACTUAL: %s" % (str(e), str(i))
                assert egv == i.g_variables
                assert elv == i.l_variables
            except AssertionError, e:
                if ret is not None:
                    assert ret.message == e.message, e.message
                else:
                    raise e


                    */
    @Test
    public void test() {
        assertStackOp(list(3, 123), list(3), "123");
        assertStackOp(list(3, 7), list(3), "7");
        assertStackOp(list(3, new Prog(list(2, 5))), list(3), "{ 2 5 }");

        assertStackOp(list(3, new Prog(list(2, "abc", 5))), list(3), "{ 2 \"abc\" 5 }");
        assertStackOp(list(4), list(3), "1 add");
        assertStackOp(list(4), list(3), "1 +");
        assertStackOp(list(3, 1, 2), list(3), "1 2");
        assertStackOp(list(3, true), list(3), "true");

        assertStackOp(list(3, 1, "hello"), list(3), "1 \"hello\"");
        assertStackOp(list(3, 1, "hello"), list(3), "1 \"hello\" eval");
        assertStackOp(list(3, 1), list(3), "1 eval");
        assertStackOp(list(3, new Prog(list(2, 5, new Function("add", "+")))), list(3), "{ 2 5 + }");
        assertStackOp(list(3, 7), list(3), "{ 2 5 + } eval");
        assertStackOp(list(3, new Prog(list(5, "abc", 5)), 7), list(3), "{ { 5 \"abc\" 5 } 2 5 + } eval");

        assertStackOp(list(1, new Prog(list(2, 3, new Function("add", "+"))), 1), list(), "{ 1 { 2 3 + } 1 } eval");

        assertStackOp(list(2, new Prog(list(1, new Function("eval", "eval")))), list(2), "{ 1 eval }");
        assertStackOp(list(new Prog(list(new Prog(list(2, 3, new Function("add", "+"))), new Function("eval", "eval"), 1, new Function("add", "+")))), list(), "{ { 2 3 + } eval 1 + }");
        assertStackOp(list(6), list(), "{ { 2 3 + } eval 1 + } eval");

        // ift
        assertStackOp(list("y"), list(3), "3 == \"y\" ift");
        assertStackOp(list("y"), list(3), "3 == \"y\" ift");
        assertStackOp(list(), list(3), "5 == \"y\" ift");
        assertStackOp(list(11), list(10, 3), "3 == { 1 + } ift");
        assertStackOp(list(10), list(10, 3), "5 == { 1 + } ift");
        assertStackOp(list(8), list(5), "true { 3 + } ift");

        // ifte
        assertStackOp(list("y"), list(3), "3 == \"y\" \"n\" ifte");
        assertStackOp(list("n"), list(3), "5 == \"y\" \"n\" ifte");
        assertStackOp(list(11), list(10, 3), "3 == { 1 + } { 1 - } ifte");
        assertStackOp(list(9), list(10, 3), "5 == { 1 + } { 1 - } ifte");
        assertStackOp(list(4), list(), "\"abc\" true { length 1 + } { length 1 - } ifte");
        assertStackOp(list(4), list(), "{ \"abc\" true { length 1 + } { length 1 - } ifte } eval");
        assertStackOp(list(2), list(), "{ \"abc\" false { length 1 + } { length 1 - } ifte } eval");

        // if/then/end
        assertStackOp(list(), list(3), "{ if 5 > then 1 end } eval");
        assertStackOp(list(), list(3), "if 5 > then 1 end");
        assertStackOp(list(33), list(3), "if 5 > then 1 end 33");
        assertStackOp(list(1), list(6), "if 5 > then 1 end eval");
        assertStackOp(list(1), list(6), "{ if 5 > then 1 end } eval");

        // if/then/else/end
        assertStackOp(list(0), list(3), "{ if 5 > then 1 else 0 end } eval");
        assertStackOp(list(101), list(100, 3), "{ if 5 <= then 1 + else 1 - end } eval");
        assertStackOp(list(0), list(3), "if 5 > then 1 else 0 end eval");

        // case
        assertStackOp(list("eq"), list(5), "case dup 5 == then \"eq\" end dup 5 < then \"less\" end dup 5 > then \"more\" end end eval swap drop");
        assertStackOp(list("more"), list(6), "case dup 5 == then \"eq\" end dup 5 < then \"less\" end dup 5 > then \"more\" end end eval swap drop");
        assertStackOp(list("less"), list(4), "case dup 5 == then \"eq\" end dup 5 < then \"less\" end dup 5 > then \"more\" end end eval swap drop");
        assertStackOp(list("eq"), list(5), "case dup 5 == then \"eq\" end dup 5 < then \"less\" end dup 5 > then \"more\" end end eval swap drop");
        assertStackOp(list("more"), list(6), "case dup 5 == then \"eq\" end dup 5 < then \"less\" end dup 5 > then \"more\" end end eval swap drop");
        assertStackOp(list("found"), list(1), "{ case dup 1 == then \"found\" end \"not found\" end swap drop } eval");
        assertStackOp(list("not found"), list(4), "{ case dup 1 == then \"found\" end \"not found\" end swap drop } eval");

        // start/next
        assertStackOp(list("hello", "hello", "hello"), list(), "{ 0 1 + 3 start \"hello\" next } eval");
        assertStackOp(list("hello", "hello", "hello"), list(), "{ 0 start \"hello\" next } eval", new StackException("START Error: Too Few Arguments"));
        assertStackOp(list("hello", "hello", "hello"), list(), "{ 0 1 + \"a\" start \"hello\" next } eval", new StackException("START Error: Bad Argument Type"));
        assertStackOp(list("hello", "hello", "hello"), list(), "{ 1 3 start \"hello\" next } eval");
        assertStackOp(list("hello", "hello", "hello"), list(), "1 3 start \"hello\" next eval");
        assertStackOp(list("hello", "hello"), list(), "{ 1.5 3 start \"hello\" next } eval");
        assertStackOp(list(5), list(), "0 { 1 5 start 1 + next } eval");
        assertStackOp(list(5), list(), "0 1 5 start 1 + next");
        assertStackOp(list(5, 555), list(), "0 1 5 start 1 + next 555");

        // start/step
        assertStackOp(list("hello", "hello", "hello"), list(), "{ 1 5 start \"hello\" 2 step } eval");
        assertStackOp(list(7), list(), "{ 1 1 5 start 2 + 2 step } eval");

        // local prog
        assertStackOp(list(5), list(8), "{ -> d { 3 2 + } } eval");
        assertStackOp(list(16), list(8), "{ -> d { d 2 * } } eval");
        assertStackOp(list(16, 3), list(8), "{ -> d { d 2 * } 2 1 + } eval");
        assertStackOp(list(13), list(2, 3), "{ -> a b { a 2 * b 3 * + } } eval");
        assertStackOp(list(48), list(8), "{ -> d { d 2 * { -> d { d 3 * } } eval } } eval");
//	        assertStackOp(([16,67],{Variable('a'): 66},[]), ([8],{Variable('a'): 66},[]), '{ -> d { d 2 * } a 1 + } eval')
//	        assertStackOp(([16,68],{Variable('d'): 66},[]), ([8],{Variable('d'): 66},[]), '{ -> d { d 2 * } 2 d + } eval')

        // for/step
        assertStackOp(list(1, 3, 5), list(), "1 5 for a a 2 step");

        // for/next
        assertStackOp(list(45), list(), "0 1 5 for a a { -> b { b 2 * } } eval a + + next");
        assertStackOp(list(45), list(), "0 1 5 for a a dup { -> b { b 2 * + } } eval + next");

        // while
        assertStackOp(list(5, 4, 3, 2, 1, 0), list(), "5 while dup 0 > repeat dup 1 - end");

        // do
        assertStackOp(list(15, 16, 17, 18), list(), "5 do dup 10 + swap 1 + until dup 8 <= end drop");

    }

    protected List list(Object... objects) {
        return Arrays.asList(objects);
    }

}
