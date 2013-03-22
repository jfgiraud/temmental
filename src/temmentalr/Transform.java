package temmentalr;

/**
 * Interface to implement to create a display transformer. It transforms the value of <code>In</code> type to an object of <code>Out</code> type  
 * @param <In> The input class
 * @param <Out> The output class
 */
public interface Transform<In, Out> {

	/**
	 * Return an object of <code>Out</code> type initialized with the specified <code>In</code> object. 
	 * This object is displayed or is given to the next ObjectFilter of the chain. 
	 * @param value the specified object
	 * @return an object of <code>Out</code> type or <code>null</code> (in this case, the filter chains ends) 
	 */
    public abstract Out apply(In value);
	
}
