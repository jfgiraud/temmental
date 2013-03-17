package temmental2;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;


/**
 * Utilities to create models.
 */
public class TemplateUtils {

    private TemplateUtils() {
    }

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
