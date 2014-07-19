package temmental2.rpl;

import temmental2.Reader;
import temmental2.StackException;

import java.util.ArrayList;
import java.util.List;

public class CaseCmd extends Reader implements Command {

    private List condition;
    private List iftrue;
    private List<ACase> cases;
    private List d3fault;

    class ACase {
        List conditions;
        List iftrue;
    }

    public CaseCmd() {
        super(new ArrayList<Object>());
        this.condition = new ArrayList();
        this.iftrue = new ArrayList();
        this.cases = new ArrayList();
        this.d3fault = new ArrayList();
    }


    public void tocond() {
        this.condition = operations;
        operations = new ArrayList<Object>();
    }

    public void totrue() {
        this.iftrue = operations;
        operations = new ArrayList<Object>();
    }

    public void todefault() {
        this.d3fault = operations;
        operations = new ArrayList<Object>();
    }

    public void append() {
        ACase acase = new ACase();
        acase.conditions = condition;
        acase.iftrue = iftrue;
        cases.add(acase);
        condition = new ArrayList<Object>();
        iftrue = new ArrayList<Object>();
        operations = new ArrayList<Object>();
    }

    public void apply(RplStack stack) throws StackException {
        boolean found = false;
        for (ACase acase : cases) {
            RplStack.push_operations(stack, acase.conditions, false, true);
            boolean r = (Boolean) stack.pop();
            if (r) {
                found = true;
                RplStack.push_operations(stack, acase.iftrue, false, true);
                break;
            }
        }
        if (!found) {
            RplStack.push_operations(stack, d3fault, false, true);
        }

    }

}
