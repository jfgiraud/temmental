package temmental;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;


public class TemplateUtilsTest extends TestCase {

	private List<Integer> items;
	private List<Map<String, Object>> models;
	private ConvertFunction<Integer> function;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();

		items = new ArrayList<Integer>();
		items.add(1);
		items.add(5);
		items.add(11);
		
		function = new ConvertFunction<Integer>() {
			public void populate(Map<String, Object> model, Integer val, int index) {
				model.put("value", val);
			}
		};
	}

	public void testConvert() {
		models = TemplateUtils.convert(items, function);

		assertEquals(3, models.size());
		assertEquals(1, models.get(0).get("value"));
		assertEquals(5, models.get(1).get("value"));
		assertEquals(11, models.get(2).get("value"));
	}
	
}
