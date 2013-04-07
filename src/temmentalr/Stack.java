package temmentalr;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

class Stack {
	
	private List<Object> elements;
	
	public Stack() {
		elements = new ArrayList<Object>();
	}
	
	public Stack(List<Object> tocopy) {
		this();
		elements.addAll(tocopy);
	}
	
	// with return value
	
	public Object value() {
		if (depth()==0)
			throw new StackException("VALUE: Empty stack.");
		return value(1);
	}
	
	public Object value(int i) {
		if (i>depth())
			throw new StackException("VALUE: Too few arguments.");
		return this.elements.get(depth()-i);
	}
	
	private Object remove(int i) {
		return this.elements.remove(depth()-i);
	}
	
	protected Object pop() {
		if (depth()==0)
			throw new StackException("POP: Empty stack.");
		return remove(1);
	}
	

	// without return value
	
	public void push(Object e) {
		this.elements.add(e);
	}
	
	public void dup() {
		if (depth()==0)
			throw new StackException("DUP: Too few arguments.");
		pick(1);
	}
	
	public void pick(int i) {
		if (i>depth()) 
			throw new StackException("PICK: Too few arguments.");
		Object e = value(i);
		push(e);
	}
	
	public void pick3() {
		pick(3);
	}
	
	public void nip() {
		if (depth()<2)
			throw new StackException("NIP: Too few arguments.");
		remove(2);
	}
	
	public void rot() {
		if (depth()<3)
			throw new StackException("ROT: Too few arguments.");
		roll(3);
	}
	
	public void unrot() {
		if (depth()<3)
			throw new StackException("UNROT: Too few arguments.");
		rolld(3);
	}
	
	public void rolld(int i) {
		if (i>0) {
			if (i>depth())
				throw new StackException("ROLLD: Too few arguments.");
			Object e = pop();
			insert(i, e);
		}
	}
	
	public void roll(int i) {
		if (i>0) {
			if (i>depth())
				throw new StackException("ROLLD: Too few arguments.");
			Object e = remove(i);
			push(e);
		}
	}
	
	public void swap() {
		if (depth()<2)
			throw new StackException("SWAP: Too few arguments.");
		Object l = pop();
		Object b = pop();
		push(l);
		push(b);
	}
	
	public void dupdup() {
		if (depth()<1)
			throw new StackException("DUPDUP: Too few arguments.");
		pick(1);
		pick(1);
	}
	
	public void over() {
		if (depth()<2)
			throw new StackException("OVER: Too few arguments.");
		pick(2);
	}
	
	public void dup2() {
		if (depth()<2)
			throw new StackException("DUP2: Too few arguments.");
		over();
		over();
	}
	
	public void drop() {
		if (depth()<1)
			throw new StackException("DROP: Too few arguments.");
		dropn(1);
	}
	
	public void drop2() {
		if (depth()<2)
			throw new StackException("DROP2: Too few arguments.");
		dropn(2);
	}
	
	public void dropn(int n) {
		if (n>0) {
			if (n>depth())
				throw new StackException("DROPN: Too few arguments.");
			for (int i=0; i<n; i++) {
				pop();
			}
		}
	}
	
	public void unpick(Object e, int i) {
		if (i>0) {
			if (i>depth()) {
				throw new StackException("UNPICK: Too few arguments.");
			}
			this.elements.set(depth()-i, e);
		}
	}
	
	public void dupn(int n) {
		if (n>0) {
			if (n>depth()) {
				throw new StackException("DUPN: Too few arguments.");
			}
			for (int i=0; i<n; i++) {
				pick(n);
			}
		}
	}
	
	public void tolist(int i) {
		if (i>depth()) {
			throw new StackException("TOLIST: Too few arguments.");
		}
		List<Object> list = new ArrayList<Object>();
		while (i>0) {
			list.add(0, pop());
			i--;
		}
		push(list);
	}
	
	public void get(int i) {
		if (depth()<0) {
			throw new StackException("GET: Too few arguments.");
		}
		Object o = pop();
		if (! (o instanceof List)) {
			throw new StackException("GET: Bad argument type.");
		}
		List l = (List) o;
		if (i<1 || i>l.size()) {
			throw new StackException("GET: Bad argument value.");
		}
		push(l.get(i-1));
	}
	
	public void ndupn(Object e, int n) {
		for (int i=0; i<n; i++) {
			push(e);
		}
		push(n);
	}
	
	private void insert(int i, Object e) {
		this.elements.add(depth()-i+1, e);
	}

	
	public void clear() {
		this.elements.clear();
	}
	
	public int depth() {
		return this.elements.size();
	}
	
	public boolean empty() {
		return depth() == 0;
	}
	
	public String toString() {
		return this.elements.toString();
	}
	
	public void printStack(PrintStream out) throws IOException {
		printStack(new PrintWriter(out));
	}
	
	public void printStack(PrintWriter out) throws IOException {
		for (int i=depth(); i>0; i--) {
			out.write(i + ": ");
			out.write(((String) value(i).toString()));
			out.write('\n');
		}
		out.flush();
	}

}
