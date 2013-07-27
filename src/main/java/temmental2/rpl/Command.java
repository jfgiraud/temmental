package temmental2.rpl;

import temmental2.StackException;

public interface Command {

	public abstract void apply(RplStack stack) throws StackException;
	
}
