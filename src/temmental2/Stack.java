package temmental2;

import java.util.ArrayList;
import java.util.List;

public class Stack {
	
	private List<Object> elements;
	
	public Stack() {
		elements = new ArrayList<Object>();
	}
	
	public void push(Object e) {
		this.elements.add(e);
	}
	
	public void push(Object ... elements) {
		for (Object e : elements) {
			this.elements.add(e);
		}
	}
	
	public Object pick(int i) {
		return this.elements.get(depth()-i);
	}
	
	public Object last() {
		return pick(1);
	}
	
	public Object remove(int i) {
		return this.elements.remove(depth()-i);
	}
	
	public void tolist(int i) {
		List<Object> list = new ArrayList<Object>();
		while (i>0) {
			list.add(0, drop());
			i--;
		}
		push(list);
	}
	
	public void nip() {
		remove(2);
	}
	
	public void rot() {
		Object e = remove(3);
		push(e);
	}
	
	public void rolld(int i) {
		Object e = drop();
		insert(-i, e);
	}
	
	public void roll(int i) {
		Object e = remove(i);
		push(e);
	}
	
	public void dup() {
		Object e = pick(1);
		push(e);
	}
	
	public void over() {
		Object e = pick(2);
		push(e);
	}
	
	private void insert(int i, Object e) {
		this.elements.add(depth()+i+1, e);
	}

	public Object drop() {
		return remove(1);
	}
	
	public void swap() {
		Object l = drop();
		Object b = drop();
		push(l);
		push(b);
	}
	
	public int depth() {
		return this.elements.size();
	}
	
	public String toString() {
		return this.elements.toString();
	}

}
