package temmental2.rpl;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import temmental2.StackException;

public class Function {

	private String token;
	String original;

	public Function(String token, String original) {
		this.token = token;
		this.original = original;
	}

	@Override
	public String toString() {
		return "Function:"+token;
	}
	
	public boolean equals(Object other) {
		return (other instanceof Function) 
				&& ((Function) other).token.equals(token)
				&& ((Function) other).original.equals(original);
	}
	
	public void apply(RplStack stack) throws StackException {
		try {
			System.out.println("-before-----------------------"+token);
			stack.printStack(System.out);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		for (Method m : stack.getClass().getMethods()) {
			
			if (m.getName().equals(token)) {
				try {
					m.invoke(stack);
				} catch (IllegalAccessException e) {
					throw new StackException(e.getMessage());
				} catch (IllegalArgumentException e) {
					throw new StackException(e.getMessage());
				} catch (InvocationTargetException e) {
					e.printStackTrace();
					throw new StackException(e.getMessage());
				}
			}
		}
		try {
			System.out.println("-after-----------------------"+token);
			stack.printStack(System.out);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}
