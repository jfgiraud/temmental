package temmental2.rpl;

import static org.junit.Assert.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.junit.Test;

import temmental2.StackException;
import temmental2.rpl.RplStack;

public class RplStackTest {

	private void assertStackOp(List expected, Object ret, List elements, String op, Object ... args) {
		HashMap<Object, Object> egv = new HashMap<>();
		HashMap<Object, Object> igv = new HashMap<>();

		List elv = new ArrayList<>();
		List ilv = new ArrayList<>();

		assertEquals(expected.getClass(), elements.getClass());
		RplStack e = new RplStack((List<Object>) expected);
		RplStack i = new RplStack((List<Object>) elements);
		i.setGlobalVariables(igv);
		i.setLocalVariables(ilv);

		Method found = null;
		for (Method m : i.getClass().getMethods()) {
			if (m.getName().equals(op)) {
				found = m;
				break;
			}
		}
		assertNotNull("Method '" + op + "' is not found.", found);		
		try {
			Object r;
			if (found.getParameterTypes().length>0) {
				List aargs = new ArrayList<>(); 
				for (int idx=0; idx<args.length; idx++) {
					if (idx < found.getParameterTypes().length)
						aargs.add(args[idx]);
					else
						i.push(args[idx]);
				}
				args = aargs.toArray();
				r = found.invoke(i, args);
			} else {
				for (Object o : args) {
					i.push(o);
				}
				r = found.invoke(i);
			}
			assertEquals(egv, i.getGlobalVariables());
			assertEquals(elv, i.getLocalVariables());
			assertEquals(e.getElements(), i.getElements());
			assertEquals(r, ret);
		} catch (IllegalAccessException e1) {
			e1.printStackTrace();
			fail("IllegalAccessException");
		} catch (IllegalArgumentException e1) {
			e1.printStackTrace();
			fail("IllegalArgumentException");
		} catch (InvocationTargetException e1) {
			e1.printStackTrace();
			if (ret != null) {
				assertEquals(((Exception) ret).getMessage(),  e1.getCause().getMessage());
			} else {
				fail("IllegalArgumentException");
			}
		}

	}

	protected List list(Object ... objects) {
		return Arrays.asList(objects);
	}

	@Test
	public void test() {
		assertStackOp(list(1,2,3), (Object) null, list(1,2), "push", 3);

		assertStackOp(list(7), null, list(5,2), "add");
		assertStackOp(list(3), null, list(5,2), "sub");
		assertStackOp(list(10), null, list(5,2), "mul");
		assertStackOp(list(2.5), null, list(5,2), "div");
		assertStackOp(list(true), null, list(5,5), "eq");
		assertStackOp(list(false), null, list(5,7), "eq");
		assertStackOp(list(false), null, list(5,5), "ne");
		assertStackOp(list(true), null, list(5,7), "ne");
		assertStackOp(list(false), null, list(5,5), "lt");
		assertStackOp(list(true), null, list(5,7), "lt");
		assertStackOp(list(true), null, list("A","B"), "lt");
		assertStackOp(list(false), null, list(7,5), "lt");
		assertStackOp(list(true), null, list(5,5), "le");
		assertStackOp(list(true), null, list(5,7), "le");
		assertStackOp(list(false), null, list(7,5), "le");
		assertStackOp(list(false), null, list(5,5), "gt");
		assertStackOp(list(false), null, list(5,7), "gt");
		assertStackOp(list(true), null, list(7,5), "gt");
		assertStackOp(list(true), null, list(5,5), "ge");
		assertStackOp(list(false), null, list(5,7), "ge");
		assertStackOp(list(true), null, list(7,5), "ge");

		assertStackOp(list(true), null, list(true,true), "and");
		assertStackOp(list(false), null, list(true,false), "and");
		assertStackOp(list(false), null, list(false,true), "and");
		assertStackOp(list(false), null, list(false,false), "and");
		assertStackOp(list(true), null, list(true,true), "or");
		assertStackOp(list(true), null, list(true,false), "or");
		assertStackOp(list(true), null, list(false,true), "or");
		assertStackOp(list(false), null, list(false,false), "or");
		assertStackOp(list(true), null, list(false), "not");
		assertStackOp(list(false), null, list(true), "not");
		assertStackOp(list(), new StackException("NOT Error: Bad Argument Type"), list(8), "not");
		assertStackOp(list(false), null, list(true,true), "xor");
		assertStackOp(list(true), null, list(true,false), "xor");
		assertStackOp(list(true), null, list(false,true), "xor");
		assertStackOp(list(false), null, list(false,false), "xor");
		assertStackOp(list(), new StackException("XOR Error: Bad Argument Type"), list(5,6), "xor");

		
		
		assertStackOp(list("a"), null, list(true,"a"), "ift");
	    assertStackOp(list(), null, list(false,"a"), "ift");
	    assertStackOp(list("a", "b"), new StackException("IFT Error: Bad Argument Type"), list("a","b"), "ift");

	    assertStackOp(list("y"), null, list(true,"y","n"), "ifte");
	    assertStackOp(list("n"), null, list(false,"y","n"), "ifte");
	    assertStackOp(list("a", "y", "n"), new StackException("IFTE Error: Bad Argument Type"), list("a","y","n"), "ifte");
		
	    assertStackOp(list("LOREM IPSUM"), null, list("lorem ipsum"), "upper");
	    assertStackOp(list("lorem ipsum"), null, list("LOREM IPSUM"), "lower");
	    assertStackOp(list("Hello the world. bye"), null, list("hello the world. BYE"), "capitalize");
	    assertStackOp(list("Hello The World. Bye"), null, list("hello the world. BYE"), "title");
//	    assertStackOp(list("Lorem Ipsum"), null, list("lorem ipsum"), "camelize");
	    assertStackOp(list(11), null, list("LOREM IPSUM"), "length");
	    
	    assertStackOp(list(true), null, list("LOREM IPSUM", "LOREM"), "startswith");
	    assertStackOp(list(false), null, list("LOREM IPSUM", "LORM"), "startswith");
	    
	    assertStackOp(list(false), null, list("LOREM IPSUM", "LOREM"), "endswith");
	    assertStackOp(list(true), null, list("LOREM IPSUM", "IPSUM"), "endswith");
	    
	    assertStackOp(list("LOREM IPSUM \t"), null, list("\t  LOREM IPSUM \t"), "lstrip");
	    assertStackOp(list("\t  LOREM IPSUM"), null, list("\t  LOREM IPSUM \t"), "rstrip");
	    assertStackOp(list("LOREM IPSUM"), null, list("\t  LOREM IPSUM \t"), "strip");
	    
	    assertStackOp(list("nam olleh"), null, list("hello man"), "reverse");
	    assertStackOp(list("L*REM IPSUM"), null, list("LOREM IPSUM", "O", "*"), "replace");
	    assertStackOp(list("L*OREM IPSUM"), null, list("LOREM IPSUM", "O", "*O"), "replace");
	    
	    
	    assertStackOp(list(list("lorem", "ipsum", "dolores", "est")), null, list("lorem ipsum dolores est"), "split");
	    assertStackOp(list(list("lorem", "ipsum dolores est")), null, list("lorem ipsum dolores est"), "split", " ", 1);
	    assertStackOp(list(list("lorem ipsum dolores est")), null, list("lorem ipsum dolores est"), "split", " ", 0);
	    assertStackOp(list(list("lorem", "ipsum dolores est")), null, list("lorem ipsum dolores est", " ", 1), "split");

	    assertStackOp(list(list("lorem", "ipsum", "dolores", "est")), null, list("lorem ipsum dolores est"), "rsplit");
	    assertStackOp(list(list("lorem ipsum dolores", "est")), null, list("lorem ipsum dolores est"), "rsplit", " ", 1);
	    assertStackOp(list(list("lorem ipsum dolores", "est")), null, list("lorem ipsum dolores est", " ", 1), "rsplit");
	    
	    
	    
	    /*""", "",  "", "", , 
		  "split", "rsplit",*/ 

	}

}
