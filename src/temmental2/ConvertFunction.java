package temmental2;

import java.util.Map;

public interface ConvertFunction<T> {

	/**
	 * Function to implement to populate the given model.
	 * @param model The model to populate
	 * @param val the object to use to populate the model
	 * @param index the index of the object if you want to specify in the model
	 */
	void populate(Map<String, Object> model, T val, int index);

}
