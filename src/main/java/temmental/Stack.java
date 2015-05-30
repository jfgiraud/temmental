package temmental;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Stack {

    private List<Object> items;

    public Stack() {
        items = new ArrayList<Object>();
    }

    public Stack(List<Object> items) {
        this.items = items;
    }

    public boolean empty() {
        return items.size() == 0;
    }

    public Object pop() {
        return items.remove(0);
    }

    public void push(Object item) {
        items.add(0, item);
    }

    public int depth() {
        return items.size();
    }

    public Object value(int depth) {
        return items.get(depth-1);
    }

    public void remove(int depth) {
        items.remove(depth-1);
    }

    public Object value() {
        return value(1);
    }

    public void reverse() {
        List<Object> cloned = new ArrayList<Object>(items);
        Collections.reverse(cloned);
        items = cloned;
    }

    public void printStack(PrintWriter out) {
    }

    public void tolist(int depth) {
        List<Object> cloned = new ArrayList<Object>();
        for (int i = 1; i <= depth; i++) {
            cloned.add(0, pop());
        }
        push(cloned);
    }

    public Stack clone()  {
        return new Stack(items);
    }

    public void tolist() {
        tolist((Integer) pop());
    }
}
