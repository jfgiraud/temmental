package temmental2.rpl;

import temmental2.Reader;
import temmental2.StackException;

import java.util.ArrayList;
import java.util.List;

public class StartCmd extends Reader implements Command {

    private boolean next;
    private boolean step;
    private List loopst;

    public StartCmd() {
        super(new ArrayList<Object>());
        this.next = false;
        this.step = false;
        this.loopst = new ArrayList<Object>();
    }

    public void tonext() {
        this.loopst = operations;
        this.next = true;
        operations = new ArrayList<Object>();
    }

    public void tostep() {
        this.loopst = operations;
        this.step = true;
        operations = new ArrayList<Object>();
    }

    public void apply(RplStack stack) throws StackException {
        if (next) {
            stack._assert_enough_elements("START", 2);
            stack._assert_number("START", 1, 2);
            Number stop = (Number) stack.pop();
            Number start = (Number) stack.pop();
            Number i = start;
            while (Operations.le(i, stop)) {
                RplStack.push_operations(stack, loopst, false, true);
                i = Operations.add(i, 1);
            }
        } else if (step) {
            stack._assert_enough_elements("START", 2);
            stack._assert_number("START", 1, 2);
            Number stop = (Number) stack.pop();
            Number start = (Number) stack.pop();
            Number i = start;
            while (Operations.le(i, stop)) {
                RplStack.push_operations(stack, loopst, false, true);
                stack._assert_enough_elements("START", 1);
                stack._assert_number("START", 1);
                i = Operations.add(i, (Number) stack.pop());
            }
        }
    }

}
