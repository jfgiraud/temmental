package temmental;

/**
 * Interface to implement to create a display transformer. It transforms a <code>String</code> to a <code>String</code>.
 * 
 * Example:
 * 
 * <pre>
 * <code>
 *     Filter upperFilter = new Filter() {
 *     		public String apply(String value) {
 *     			return value.toUpperCase();
 *     		}
 *     };
 * </code>
 * </pre>
 */
public interface Filter extends ObjectFilter<String, String> {

	/**
	 * Takes a <code>String</code> and return a <code>String</code> to display or pass to the next filter of the chain.
	 * @param value The specified string
	 */
    public abstract String apply(String value);
    
}
