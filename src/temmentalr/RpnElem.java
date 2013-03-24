package temmentalr;

import java.util.Map;

public interface RpnElem {

	Object writeObject(Map<String, Transform> functions, Map<String, Object> model) throws TemplateException;
	
	String getWord();

	String getPos();
	
}
