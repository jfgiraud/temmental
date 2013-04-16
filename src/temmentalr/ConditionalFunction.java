package temmentalr;

public interface ConditionalFunction<T> {

	boolean condition(T item, int index);
	
}
