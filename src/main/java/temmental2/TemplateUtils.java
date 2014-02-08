package temmental2;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;


/**
 * Utilities to create models.
 */
public class TemplateUtils {

    private TemplateUtils() {
    }

//    /**
//     * Returns a list of items for which the given function is true.
//     * @param <T> The type of items
//     * @param items The collection of items
//     * @param function The function to apply on an item. If the result is true, the item is added to the result.
//     * @return a list of items
//     */
//	public static <T> List<T> filter(Iterable<T> items, ConditionalFunction<T> function) {
//		ArrayList<T> result = new ArrayList<T>();
//		int index = 0;
//    	for (T item: items) {
//    		if (function.condition(item, index)) {
//    			result.add(item);
//    		}
//    		index++;
//    	}
//    	return result;
//	}
//
//	/**
//	 * Convert a collection of items to a list of models using the given function.
//	 * @param <T> The type of items
//	 * @param items The collection of items
//	 * @param function The function to use to convert items to models
//	 * @return a list of Models
//	 */
//	public static <T> List<Map<String,Object>> convert(Iterable<T> items, ConvertFunction<T> function) {
//		ArrayList<Map<String, Object>> result = new ArrayList<Map<String,Object>>();
//    	int index = 0;
//    	for (T item: items) {
//    		Map<String, Object> model = new HashMap<String, Object>();
//    		function.populate(model, item, index++);
//    		result.add(model);
//    	}
//    	return result;
//	}
//	
//    /**
//     * Filter and convert a collection of items to a list of models using the given function.
//     * @param <T> The type of items
//     * @param items The collection of items
//     * @param function The function to apply on an item. If the result is true, the item is converted to a model and the model is added to the result. 
//     * @return the list of Models
//     */
//	public static <T> List<Map<String,Object>> filterAndConvert(Iterable<T> items, ConditionalConvertFunction<T> function) {
//		ArrayList<Map<String, Object>> result = new ArrayList<Map<String,Object>>();
//    	int index = 0;
//    	for (T item: items) {
//    		if (function.condition(item, index)) {
//    			Map<String, Object> model = new HashMap<String, Object>();
//    			function.populate(model, item, index++);
//    			result.add(model);
//    		}
//    	}
//    	return result;
//	}    
    
	/**
	 * Creates a list with the given models.
	 * @param map the collection of models
	 * @return a list of Models
	 */
    @SuppressWarnings("unchecked") 
    public static List<Map<String, Object>> createList(Object ... map) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (int i=0; i<map.length; i++) {
            list.add((Map<String, Object>) map[i]);
        }
        return list;
    }

    /**
     * Creates a model: it is an association of key/value pairs.
     * @param map a key followed by a value. It is an enumeration, so this implies an even size. 
     * @return the model
     * @throws TemplateException if there is not an even size of parameters
     */
    public static Map<String, Object> createModel(Object ... map) throws TemplateException {
        if (map.length %2 != 0)
            throw new TemplateException("Invalid number of elements (key/value list implies an even size).");
        Map<String, Object> model = new HashMap<String, Object>();
        for (int i=0; i<map.length/2; i++) {
            model.put((String) map[2*i], map[2*i+1]);
        }
        return model;
    }

    public static <In, Out> List<Out> transform(final List<In> list, final Transform<In,Out> transform) {
        ArrayList<Out> result = new ArrayList<Out>();
        for (In item : list) {
            result.add(transform.apply(item));
        }
        return result;
    }

    /**
     * Reads a properties file
     * @param propertiesFilePath the path of the properties file
     * @return a Properties object
     * @throws FileNotFoundException if the file is not found
     * @throws IOException if the file can not be read
     */
    public static Properties readProperties(String propertiesFilePath) throws FileNotFoundException, IOException {
        Properties messages = new Properties();
        InputStream inStream = new FileInputStream(propertiesFilePath);
        try {
            messages.load(inStream);
        } finally {
            inStream.close();
        }
        return messages;
    }
    
	
}
