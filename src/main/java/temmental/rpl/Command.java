package temmental.rpl;

import temmental.StackException;

public interface Command {

    public abstract void apply(RplStack stack) throws StackException;

}
