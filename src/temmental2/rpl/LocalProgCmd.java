package temmental2.rpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import temmental2.Reader;
import temmental2.StackException;

public class LocalProgCmd extends Reader implements Command {
	
	private List vars;
	private List after;
	private List prog;
	
	public LocalProgCmd() {
		super(new ArrayList<>());
		this.vars = new ArrayList<>();
		this.after = new ArrayList<>();
		this.prog = new ArrayList<>();
	}
	
	public void tovars() {
		Collections.reverse(operations);
		this.vars = operations;
		operations = new ArrayList<>();
	}

	public void toprog() {
		this.prog = operations;
		operations = new ArrayList<>();
	}

	public void toafter() {
		this.after = operations;
		operations = new ArrayList<>();
	}

	@Override
	public void apply(RplStack stack) throws StackException {
		stack._set_local_vars(vars, "->");
		RplStack.push_operations(stack, prog, false, true);
		stack._restore_vars();
		RplStack.push_operations(stack, after, false, true);
	}

}
