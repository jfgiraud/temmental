package temmentalr;

import java.util.ArrayList;
import java.util.List;

public class Stack {
	
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
		return value(1);
	}
	
	public Object value(int i) {
		return this.elements.get(depth()-i);
	}
	
	private Object remove(int i) {
		return this.elements.remove(depth()-i);
	}
	
	protected Object pop() {
		return remove(1);
	}
	

	// without return value
	
	public void push(Object e) {
		this.elements.add(e);
	}
	
	public void pick(int i) {
		Object e = value(i);
		push(e);
	}
	
	public void pick3() {
		pick(3);
	}
	
	public void tolist(int i) {
		List<Object> list = new ArrayList<Object>();
		while (i>0) {
			list.add(0, pop());
			i--;
		}
		push(list);
	}
	
	public void nip() {
		remove(2);
	}
	
	public void rot() {
		roll(3);
	}
	
	public void unrot() {
		rolld(3);
	}
	
	public void rolld(int i) {
		if (i>0) {
			Object e = pop();
			insert(i, e);
		}
	}
	
	public void roll(int i) {
		if (i>0) {
			Object e = remove(i);
			push(e);
		}
	}
	
	public void dup() {
		pick(1);
	}
	
	public void dupn(int n) {
		if (n>0) {
			for (int i=0; i<n; i++) {
				pick(n);
			}
		}
	}
	
	public void dupdup() {
		pick(1);
		pick(1);
	}
	
	public void over() {
		pick(2);
	}
	
	public void dup2() {
		over();
		over();
	}
	
	private void insert(int i, Object e) {
		this.elements.add(depth()-i+1, e);
	}

	public void drop() {
		dropn(1);
	}
	
	public void drop2() {
		dropn(2);
	}
	
	public void dropn(int n) {
		if (n>0) {
			for (int i=0; i<n; i++) {
				pop();
			}
		}
	}
	
	public void clear() {
		this.elements.clear();
	}
	
	public void swap() {
		Object l = pop();
		Object b = pop();
		push(l);
		push(b);
	}
	
	public int depth() {
		return this.elements.size();
	}
	
	public String toString() {
		return this.elements.toString();
	}

	public void unpick(Object e, int i) {
		if (i>0)
			this.elements.set(depth()-i, e);
	}

	public void ndupn(Object e, int n) {
		for (int i=0; i<n; i++) {
			push(e);
		}
		push(n);
	}

}
