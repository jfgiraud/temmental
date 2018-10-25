package com.github.jfgiraud.temmental;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.github.jfgiraud.temmental.StringUtils.viewWhiteSpaces;

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

    void setvalue(int depth, Object o) {
        items.add(depth - 1, o);
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
        for (int i=depth(); i>0; i--) {
            out.write(i + ": ");
            Object o = value(i);
            if (o instanceof String) {
                out.write(viewWhiteSpaces(String.valueOf(o)));
            } else {
                out.write(String.valueOf(o));
            }
            out.write('\n');
        }
        out.flush();
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
