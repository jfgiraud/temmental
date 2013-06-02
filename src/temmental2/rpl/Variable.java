package temmental2.rpl;

import java.util.List;
import java.util.Map;

public class Variable {

	private String name;

	public Variable(String name) {
		this.name = name;
	}
	
	public boolean equals(Object other) {
		return (other instanceof Variable) && ((Variable) other).name.equals(name);
	}
	
	public void apply(RplStack stack) {
//		List lv = stack.getLocalVariables();
//        if (lv.size() > 0 && ((Map) (lv.get(lv.size()-1))).containsKey(name)) {
//        	stack.push(stack.l_variables[-1][self])
//        }

		throw new RuntimeException("-----"+name);
		//        		
//            stack.push(stack.l_variables[-1][self])
//        elif stack.g_variables.has_key(self):
//            stack.push(stack.g_variables[self])
//        else: 
//            stack.push(self)		
	}

}
