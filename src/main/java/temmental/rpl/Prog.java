package temmental.rpl;

import temmental.Reader;

import java.util.ArrayList;
import java.util.List;

public class Prog extends Reader {

    public String toString() {
        return "Prog" + operations;
    }

    ;

    public Prog() {
        this(new ArrayList());
    }

    Prog(List operations) {
        super(operations);
    }


    public void apply(RplStack stack, List operations, boolean execProg, boolean execFunc) {
        if (operations == null) {
            operations = getOperations();
        }
        RplStack.push_operations(stack, operations, execProg, execFunc);
    }

    public boolean equals(Object other) {
        return (other instanceof Prog) && ((Prog) other).operations.equals(operations);
    }
}
