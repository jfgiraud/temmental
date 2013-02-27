package temmental;

public interface ConditionalFunction<T> {

	boolean condition(T item, int index);
	
}
