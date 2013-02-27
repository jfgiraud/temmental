package temmental2;

public interface ConditionalFunction<T> {

	boolean condition(T item, int index);
	
}
