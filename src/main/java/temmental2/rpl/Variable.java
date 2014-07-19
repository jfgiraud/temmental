package temmental2.rpl;

import java.util.List;
import java.util.Map;

public class Variable {

    private String name;

    public Variable(String name) {
        this.name = name;
    }

    String getName() {
        return name;
    }

    public boolean equals(Object other) {
        return (other instanceof Variable) && ((Variable) other).name.equals(name);
    }

    @Override
    public String toString() {
        return "V<" + name + ">";
    }

    public void apply(RplStack stack) {
        List lv = stack.getLocalVariables();
        if (lv.size() > 0 && ((Map) (lv.get(lv.size() - 1))).containsKey(name)) {
            stack.push(((Map) (lv.get(lv.size() - 1))).get(name));
        } else if (stack.getGlobalVariables().containsKey(name)) {
            stack.push(stack.getGlobalVariables().get(name));
        } else {
            stack.push(this);
        }

//		stack.push( 33 );
    }

}
