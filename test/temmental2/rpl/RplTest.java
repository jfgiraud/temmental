package temmental2.rpl;

import static org.junit.Assert.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.junit.Test;

import temmental2.rpl.RplStack;

public class RplTest {

	
	private void assertStackOp(List expected, List elements, String ops) {
		HashMap<Object, Object> egv = new HashMap<>();
		HashMap<Object, Object> igv = new HashMap<>();

		List elv = new ArrayList<>();
		List ilv = new ArrayList<>();

		assertEquals(expected.getClass(), elements.getClass());
		RplStack e = new RplStack((List<Object>) expected);
		RplStack i = new RplStack((List<Object>) elements);
		i.setGlobalVariables(igv);
		i.setLocalVariables(ilv);

		List<String> tokens = RplStack.tokenize(ops);
		
		Prog p = new Prog();
		p.read_until(tokens, null);
		p.apply(i, null, false, true);
		
		assertEquals(e.getElements(), i.getElements());
		assertEquals(egv, i.getGlobalVariables());
		assertEquals(elv, i.getLocalVariables());
		
		/*
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
		assertNotNull(found);		
		*/
		
		
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
		 assertStackOp(list(), list(3), "5 == \"y\" ift") ;
		 assertStackOp(list(11), list(10, 3), "3 == { 1 + } ift"); 
		 assertStackOp(list(10), list(10, 3), "5 == { 1 + } ift"); 
		 assertStackOp(list(8), list(5), "true { 3 + } ift");

		 // ifte
		 assertStackOp(list("y"), list(3), "3 == \"y\" \"n\" ifte"); 
		 assertStackOp(list("n"), list(3), "5 == \"y\" \"n\" ifte"); 
		 assertStackOp(list(11), list(10, 3), "3 == { 1 + } { 1 - } ifte"); 
		 assertStackOp(list(9), list(10, 3), "5 == { 1 + } { 1 - } ifte"); 
		 System.out.println("=====================");
		 assertStackOp(list(4), list(), "\"abc\" true { length 1 + } { length 1 - } ifte"); 
		 assertStackOp(list(4), list(), "{ \"abc\" true { length 1 + } { length 1 - } ifte } eval"); 
		 assertStackOp(list(2), list(), "{ \"abc\" false { length 1 + } { length 1 - } ifte } eval"); 
		 

	}
	
	protected List list(Object ... objects) {
		return Arrays.asList(objects);
	}

}
