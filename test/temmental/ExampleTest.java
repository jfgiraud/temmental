package temmental;

import static temmental.TemplateUtils.convert;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.TestCase;

public class ExampleTest extends TestCase {

    private Template template;
    private HashMap<String, ObjectFilter> filters;
    private HashMap<String, Object> model;
    private Properties properties;

    class Fruit {
        String name;
        public Fruit(String name) {
            this.name = name;
        }
        @Override
        public String toString() {
            return "Fruit: " + name;
        }
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        filters = new HashMap<String, ObjectFilter>();
        filters.putAll(Filters.MATH);
        filters.putAll(Filters.TEXT);
        
        filters.put("add1", new ObjectFilter<Integer,Integer>() {
            public Integer apply(Integer value) {
                return value + 1;
            }
        });
        filters.put("2str", new ObjectFilter<Object[],String[]>() {
            public String[] apply(Object[] parameters) {
                String s[] = new String[parameters.length];
                for (int i=0; i<parameters.length; i++) {
                    s[i] = parameters[i].toString();
                }
                return s;
            }
        });
        filters.put("first", new ObjectFilter<Object[],Object>() {
            public Object apply(Object[] parameters) {
                return parameters[0];
            }
        });
        filters.put("join", new ObjectFilter<Object[],Object>() {
            public Object apply(Object parameters[]) {
            	String r = "";
            	boolean first = true;
            	for (Object p : parameters) {
            		r += ((! first) ? "*" : "") + p.toString();
            		first = false;
            	}
            	return r;
            }
        });
        filters.put("castdate", new ObjectFilter<Object,Date>() {
            public Date apply(Object parameter) {
                return (Date) parameter;
            }
        });
        filters.put("int", new ObjectFilter<String,Integer>() {
            public Integer apply(String parameter) {
                return Integer.parseInt(parameter);
            }
        });
        filters.put("quote", new Filter() {
            public String apply(String value) {
                return "'" + value + "'";			
            }
        });
        filters.put("sqldate", new ObjectFilter<Date, String>() {
            public String apply(Date value) {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                return formatter.format(value);
            }
        });
        filters.put("ifelse", new ObjectFilter<Object[],Object>() {
            public Object apply(Object parameters[]) {
                return (Boolean) parameters[0] ? parameters[1] : parameters[2];           
            }
        });
        
        model = new HashMap<String, Object>();

        properties = new Properties();
        properties.put("orange", "Orange");
        properties.put("lemon", "Lemon");
        properties.put("hello", "Hello World!");
        properties.put("hellox", "Hello {0}!");
        properties.put("helloxy", "Hello {0} and {1}!");
        
        properties.put("numberOfElements", "There {0,choice,0#is no element|1#one element|1<are {0} elements} at {1,time} on {1,date}.");

        template = new Template(null, filters, properties, Locale.ENGLISH);
    }

    public void test_000a() {
        String pattern = Template.FINAL_EXPR;
        assertTrue("~tag~".matches(pattern));
        assertTrue("~tag?~".matches(pattern));
        assertTrue("~tag:upper~".matches(pattern));
        assertTrue("~tag[]~".matches(pattern));
        assertTrue("~tag?[]~".matches(pattern));
        assertTrue("~tag?[firstname]~".matches(pattern));
        assertTrue("~tag[]:upper~".matches(pattern));
        assertTrue("~tag[firstname]:upper~".matches(pattern));
        assertTrue("~tag[firstname?:lower]:upper~".matches(pattern));
        assertTrue("~tag[firstname:lower:upper]:upper~".matches(pattern));
        assertTrue("~tag[firstname:lower,lastname:upper:lower]:upper:lower~".matches(pattern));
        assertTrue("~tag?[firstname:lower,lastname?:upper:lower,firstname]:upper:lower~".matches(pattern));
        assertTrue("~'sentence[]:lower~".matches(pattern));
        assertTrue("~'sentence[firstname:lower,lastname:upper:lower]:lower~".matches(pattern));
    }
    
    public void test_000b() throws IOException, TemplateException {
    	model.put("fruit", "lemon");
        assertEquals("~lemon~", template.formatForTest("~~~fruit~~~", model));
        assertEquals("~~lemon~~", template.formatForTest("~~~~~fruit~~~~~", model));
        assertEquals("~fruit~", template.formatForTest("~~fruit~~", model));
        assertEquals("~~fruit~~", template.formatForTest("~~~~fruit~~~~", model));
        assertEquals("~~~fruit~~~", template.formatForTest("~~~~~~fruit~~~~~~", model));
    }

    
    // simple replacement
    public void test_001() throws IOException, TemplateException {
        model.put("software", "temmental");
        assertEquals("The tag is replaced by its value (temmental)", template.formatForTest("The tag is replaced by its value (~software~)", model));
    }

    // simple replacement with one or more filters    
    public void test_002a() throws IOException, TemplateException {
        model.put("software", "temmental");
        assertEquals("You can apply one filter (TEMMENTAL)", template.formatForTest("You can apply one filter (~software:upper~)", model));
    }
    
    public void test_002b() throws IOException, TemplateException {
        model.put("software", "temmental");
        assertEquals("You can apply one or more filters ('TEMMENTAL')", template.formatForTest("You can apply one or more filters (~software:upper:quote~)", model));
    }
    
    // simple replacement with ? flag
    public void test_003a() throws IOException, TemplateException {
        model.put("software", "temmental");
        assertEquals("If the key is present in the model, the tag is rendered with the '?' flag (TEMMENTAL)", template.formatForTest("If the key is present in the model, the tag is rendered with the '?' flag (~software?:upper~)", model));
    }
    
    public void test_003b() throws IOException, TemplateException {
        assertEquals("If the key is not present in the model, the tag isn't rendered with the '?' flag ()", template.formatForTest("If the key is not present in the model, the tag isn't rendered with the '?' flag (~software?:upper~)", model));
    }
    
    // property without parameters
    public void test_004a() throws IOException, TemplateException {
        assertEquals("Hello World!", template.formatForTest("~'hello[]~", model));
        assertEquals("hello world!", template.formatForTest("~'hello[]:lower~", model));
        assertEquals("'hello world!'", template.formatForTest("~'hello[]:lower:quote~", model));
    }
    public void test_004b() throws IOException, TemplateException {
        try {
            template.parse("~'hello~");
            fail("An exception must be raised.");
        } catch (TemplateException e) {
            assertEquals("Invalid syntax in '~'hello~' (flag ' requires []).", e.getMessage());
        } catch (Exception e) {
            fail("Bad exception.");
        }
    }
    public void test_004c() throws IOException, TemplateException {
        try {
            template.parse("~'hello?[]~");
            fail("An exception must be raised.");
        } catch (TemplateException e) {
            assertEquals("Invalid syntax in '~'hello?[]~' (flags ' and ? are mutually exclusive).", e.getMessage());
        } catch (Exception e) {
            fail("Bad exception.");
        }
    }
    public void test_004d() throws IOException, TemplateException {
        try {
            template.parse("~'unknown[firstname]~");
            fail("An exception must be raised.");
        } catch (TemplateException e) {
            assertEquals("Key 'unknown' is not present in the property map to render '~'unknown[firstname]~'.", e.getMessage());
        } catch (Exception e) {
        	e.printStackTrace();
            fail(String.format("Bad exception. %s", e.getMessage()));
        }
    }
    
    // dynamic property without parameters
    public void test_005a() throws IOException, TemplateException {
        model.put("tag", "hello");
        assertEquals("Hello World!", template.formatForTest("~tag[]~", model));
        assertEquals("hello world!", template.formatForTest("~tag[]:lower~", model));
        assertEquals("hello world!", template.formatForTest("~tag?[]:lower~", model));
        assertEquals("'hello world!'", template.formatForTest("~tag[]:lower:quote~", model));
    }
    public void test_005b() throws IOException, TemplateException {
        assertEquals("", template.formatForTest("~tag?[]~", model));
        assertEquals("", template.formatForTest("~tag?[]:lower~", model));
        assertEquals("", template.formatForTest("~tag?[]:lower~", model));
        assertEquals("", template.formatForTest("~tag?[]:lower:quote~", model));
    }
    
    // property with parameters
    public void test_006a() throws IOException, TemplateException {
        model.put("firstname", "Jeff");
        assertEquals("Hello Jeff!", template.formatForTest("~'hellox[firstname]~", model));
        assertEquals("hello jeff!", template.formatForTest("~'hellox[firstname]:lower~", model));
        assertEquals("'hello jeff!'", template.formatForTest("~'hellox[firstname]:lower:quote~", model));
        assertEquals("'Hello jeff!'", template.formatForTest("~'hellox[firstname:lower]:quote~", model));
        assertEquals("'Hello jeff and jeff!'", template.formatForTest("~'helloxy[firstname:lower,firstname:lower]:quote~", model));
    }
    public void test_006b() throws IOException, TemplateException {
        try {
            template.parse("~'hellox~");
            fail("An exception must be raised.");
        } catch (TemplateException e) {
            assertEquals("Invalid syntax in '~'hellox~' (flag ' requires []).", e.getMessage());
        } catch (Exception e) {
            fail("Bad exception.");
        }
    }
    public void test_006c() throws IOException, TemplateException {
        try {
            template.parse("~'hellox?[]~");
            fail("An exception must be raised.");
        } catch (TemplateException e) {
            assertEquals("Invalid syntax in '~'hellox?[]~' (flags ' and ? are mutually exclusive).", e.getMessage());
        } catch (Exception e) {
            fail("Bad exception.");
        }
    }
    public void test_006d() throws IOException, TemplateException {
        model.remove("firstname");
        assertEquals("", template.formatForTest("~'hellox[firstname?:lower]:quote~", model));
        try {
            template.formatForTest("~'hellox[firstname]~", model);
            fail("An exception must be raised.");
        } catch (TemplateException e) {
            assertEquals("Key 'firstname' is not present or has null value in the model map to render '~'hellox[firstname]~'.", e.getMessage());
        } catch (Exception e) {
            fail("Bad exception.");
        }
    }
    
    // dynamic property with parameters
    public void test_007a() throws IOException, TemplateException {
        model.put("tag", "hellox");
        model.put("firstname", "Jeff");
        assertEquals("Hello Jeff!", template.formatForTest("~tag[firstname]~", model));
        assertEquals("hello jeff!", template.formatForTest("~tag[firstname]:lower~", model));
        assertEquals("hello jeff!", template.formatForTest("~tag?[firstname]:lower~", model));
        assertEquals("'hello jeff!'", template.formatForTest("~tag[firstname]:lower:quote~", model));
        assertEquals("'Hello jeff!'", template.formatForTest("~tag[firstname:lower]:quote~", model));
    }
    public void test_007b() throws IOException, TemplateException {
        model.put("firstname", "Jeff");
        assertEquals("", template.formatForTest("~tag?[]~", model));
        assertEquals("", template.formatForTest("~tag?[]:lower~", model));
        assertEquals("", template.formatForTest("~tag?[]:lower~", model));
        assertEquals("", template.formatForTest("~tag?[]:lower:quote~", model));
    }
    public void test_007c() throws IOException, TemplateException {
        model.put("tag", "hellox");
        assertEquals("", template.formatForTest("~tag[firstname?]~", model));
        assertEquals("", template.formatForTest("~tag[firstname?]:lower~", model));
        assertEquals("", template.formatForTest("~tag[firstname?]:lower:quote~", model));
        assertEquals("", template.formatForTest("~tag[firstname?:lower]:quote~", model));
    }
    public void test_007d() throws IOException, TemplateException {
        model.put("firstname", "Jeff");
        try {
            template.formatForTest("~tag[firstname]~", model);
            fail("An exception must be raised.");
        } catch (TemplateException e) {
            assertEquals("Key 'tag' is not present or has null value in the model map to render '~tag[firstname]~'.", e.getMessage());
        } catch (Exception e) {
            fail("Bad exception.");
        }
    }
    public void test_007e() throws IOException, TemplateException {
        model.put("tag", "hellox");
        try {
            template.formatForTest("~tag[firstname]~", model);
            fail("An exception must be raised.");
        } catch (TemplateException e) {
            assertEquals("Key 'firstname' is not present or has null value in the model map to render '~tag[firstname]~'.", e.getMessage());
        } catch (Exception e) {
            fail("Bad exception.");
        }
    }
    public void test_007f() throws IOException, TemplateException {
        model.put("tag", "unknown");
        model.put("firstname", "Jeff");
        try {
            template.formatForTest("~tag[firstname]~", model);
            fail("An exception must be raised.");
        } catch (TemplateException e) {
            assertEquals("Key 'unknown' is not present in the property map to render '~tag[firstname]~'.", e.getMessage());
        } catch (Exception e) {
            fail("Bad exception.");
        }
    }
    
    // property or dynamic property with parameters
    public void test_008a() throws IOException, TemplateException {
        model.put("now", new GregorianCalendar(2010, 4, 31, 13, 55).getTime());
        model.put("n", 125);
        assertEquals("there are 125 elements at 1:55:00 pm on may 31, 2010.", template.formatForTest("~'numberOfElements[n,now]:lower~", model));
    }
    public void test_008b() throws IOException, TemplateException {
        model.put("tag", "numberOfElements");
        model.put("now", new GregorianCalendar(2010, 4, 31, 13, 55).getTime());
        model.put("n", 125);
        assertEquals("there are 125 elements at 1:55:00 pm on may 31, 2010.", template.formatForTest("~tag[n,now]:lower~", model));
    }
    public void test_008c() throws IOException, TemplateException {
        model.put("tag", "numberOfElements");
        model.put("now", new GregorianCalendar(2010, 4, 31, 13, 55).getTime());
        model.put("n", "125");
        assertEquals("there are 125 elements at 1:55:00 pm on may 31, 2010.", template.formatForTest("~tag[n:int,now]:lower~", model));
    }
    public void test_008d() throws IOException, TemplateException {
        model.put("tag", "numberOfElements");
        model.put("now", new GregorianCalendar(2010, 4, 31, 13, 55).getTime());
        model.put("n", "125");
        try {
            template.formatForTest("~tag[n,now]:lower~", model);
            fail("An exception must be raised.");
        } catch (IllegalArgumentException e) {
            assertEquals("Cannot format given Object as a Number", e.getMessage());
        } catch (Exception e) {
            fail("Bad exception.");
        }
    }
    public void test_008e() throws IOException, TemplateException {
        model.put("tag", "numberOfElements");
        model.put("now", new GregorianCalendar(2010, 4, 31, 13, 55).getTime());
        model.put("n", "125");
        assertEquals("there are 125 elements at 1:55:00 pm on may 31, 2010.", template.formatForTest("~'numberOfElements[n?:int,now]:lower~", model));
    }
    
    public void test_008f() throws IOException, TemplateException {
        model.put("now", new GregorianCalendar(2010, 4, 31, 13, 55).getTime());
        assertEquals("", template.formatForTest("~'numberOfElements[n?:int,now]:lower~", model));
    }
    
    // to string conversion
    public void test_009a() throws IOException, TemplateException {
        model.put("numberOfElements", 5);
        assertEquals("there are 5 elements.", template.formatForTest("there are ~numberOfElements~ elements.", model));
    }
    public void test_009b() throws IOException, TemplateException {
        model.put("numberOfElements", 5);
        assertEquals("there are 5 elements.", template.formatForTest("there are ~numberOfElements:upper~ elements.", model));
    }
    public void test_009c() throws IOException, TemplateException {
        model.put("fruit", new Fruit("apple"));
        assertEquals("Fruit: apple", template.formatForTest("~fruit~", model));
    }
    
    public void test_010a() throws IOException, TemplateException {
        try {
            template.formatForTest("~fruits#list~~n~~#list~", model);
            fail("An exception must be raised.");
        } catch (TemplateException e) {
            assertEquals("Key 'fruits' is not present or has null value in the model map to render '~fruits#list~'.", e.getMessage());
        } catch (Exception e) {
            fail("Bad exception.");
        }
    }
    
    public void test_010b() throws IOException, TemplateException {
        assertEquals("", template.formatForTest("~fruits?#list~~index~.~fruit~/~#list~", model));
    }
    
    public void test_010c() throws IOException, TemplateException {
        List<Map<String, Object>> models = createList(
                createModel("index", 0, "fruit", "orange"),
                createModel("index", 1, "fruit", "apple"),
                createModel("index", 2));
        model.put("fruit", "lemon");
        model.put("fruits", models);
        assertEquals("3", template.formatForTest("~fruits:size~", model));
        assertEquals("lemon/0.orange/1.apple/2.lemon/lemon", template.formatForTest("~fruit~/~fruits#list~~index~.~fruit~/~#list~~fruit~", model));
    }

    public void test_010d() throws IOException, TemplateException, IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        List<Fruit> fruits = new ArrayList<Fruit>();
        fruits.add(new Fruit("orange"));
        fruits.add(new Fruit("lemon"));
        fruits.add(new Fruit("apple"));
        model.put("fruit", "lemon");
        model.put("fruits", fruits);
        filters.put("toModel", new ObjectFilter<ArrayList<Fruit>,List<Map<String,Object>>>() {
            public List<Map<String, Object>> apply(ArrayList<Fruit> fruits) {
                return convert(fruits, new ConvertFunction<Fruit>() {
                    public void populate(Map<String, Object> model, Fruit f, int index) {
                        model.put("fruit", f.name);
                        model.put("index", index);
                    }
                });
            }
        });
        assertEquals("lemon/0.orange/1.lemon/2.apple/lemon", template.formatForTest("~fruit~/~fruits:toModel#list~~index~.~fruit~/~#list~~fruit~", model));
    }
    
    public void test_010g() throws IOException, TemplateException, IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        List<Fruit> fruits = new ArrayList<Fruit>();
        fruits.add(new Fruit("orange"));
        fruits.add(new Fruit("lemon"));
        fruits.add(new Fruit("apple"));
        model.put("fruit", "lemon");
        model.put("fruits", fruits);
        filters.put("toModel", new ObjectFilter<ArrayList<Fruit>,List<Map<String,Object>>>() {
            public List<Map<String, Object>> apply(ArrayList<Fruit> fruits) {
                return convert(fruits, new ConvertFunction<Fruit>() {
                    public void populate(Map<String, Object> model, Fruit f, int index) {
                        model.put("fruit", f.name);
                        model.put("index", index);
                        throw new RuntimeException("toto"); 
                    }
                });
            }
        });
        try {
            template.formatForTest("~fruit~/~fruits:toModel#list~~index~.~fruit~/~#list~~fruit~", model);
            fail("An exception must be raised.");
        } catch (TemplateException e) {
            assertEquals("Unable to apply filter to render '~fruits:toModel#list~'.", e.getMessage());
        } catch (Exception e) {
            e.printStackTrace(System.err);
            fail(e.getMessage());
        }
    }
    
    public void test_010e() throws IOException, TemplateException {
        //TODO
        List<Fruit> fruits = new ArrayList<Fruit>();
        fruits.add(new Fruit("orange"));
        fruits.add(new Fruit("lemon"));
        fruits.add(new Fruit("apple"));
        model.put("fruits", fruits);
        model.put("fruit", "lemon");
        filters.put("toModel", new ObjectFilter<Fruit,Map<String,Object>>() {
            public Map<String, Object> apply(Fruit o) {
                Map<String, Object> model = new HashMap<String, Object>();
                model.put("name", o.name);
                return model;
            }
        });
//        filters.put("get<name>", new ObjectFilter<Map<String,Object>,Object>() {
//            @Override
//            public Object apply(Map<String, Object> o) {
//                return o.get("name");
//            }
//        });
        filters.put("get", new ObjectFilter<Object[],Object>() {
            public Object apply(Object[] parameters) {
                Map m = (Map) parameters[0];
                String k = (String) parameters[1];
                return m.get(k);
            }
        });
        assertEquals("3", template.formatForTest("~fruits:size~", model));
        properties.put("name", "name");
        // TODO ajouter index dans <fruit,index> 
        assertEquals("lemon/orange/lemon/apple/lemon", template.formatForTest("~fruit~/~fruits#list<fruit>~~(fruit:toModel,'name[]):get~/~#list~~fruit~", model));
    }
    
    @SuppressWarnings("unchecked")
	private List<Map<String, Object>> createList(Object ... map) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (int i=0; i<map.length; i++) {
            list.add((Map<String, Object>) map[i]);
        }
        return list;
    }
    
    private Map<String, Object> createModel(Object ... map) {
        if (map.length %2 != 0)
            fail("Key/value");
        Map<String, Object> model = new HashMap<String, Object>();
        for (int i=0; i<map.length/2; i++) {
            model.put((String) map[2*i], map[2*i+1]);
        }
        return model;
    }
    
    public void test_010f() throws IOException, TemplateException {
        final List<Map<String, Object>> list2 = createList(createModel("n", "21-"), createModel("n", "22-"));

        ArrayList<Map<String, Object>> list1 = new ArrayList<Map<String, Object>>();
        list1.add(createModel("n", "11-", "mylist2", list2));
        list1.add(createModel("n", "12-", "mylist2", list2));

        model.put("n", "default-");
        model.put("mylist1", list1);

        assertEquals("default-11-21-22-11-12-21-22-12-default-", template.formatForTest("~n~~mylist1#list~~n~~mylist2#list~~n~~#list~~n~~#list~~n~", model));
    }

    // chain filter
    public void test_011a() throws IOException, TemplateException {
        model.put("index", "134");
        try {
            template.formatForTest("~index:size~", model);
            fail("An exception must be raised.");
        } catch (TemplateException e) {
        }
    }
    
    public void test_011b() throws IOException, TemplateException {
        model.put("index", 134);
        model.put("total", 200);
        assertEquals("'134'", template.formatForTest("~(index,total):first:quote~", model));
    }
    
    public void test_011c() throws IOException, TemplateException {
        model.put("index", 134);
        model.put("total", 200);
        assertEquals("'134'", template.formatForTest("~(index,total):first:quote~", model));
    }
    
    public void test_011d() throws IOException, TemplateException {
        model.put("now", new GregorianCalendar(2010, 4, 31, 13, 55).getTime());
        assertEquals("2010-05-31 13:55:00", template.formatForTest("~now:sqldate~", model));
    }
    
    public void test_011e() throws IOException, TemplateException {
        model.put("now", new GregorianCalendar(2010, 4, 31, 13, 55).getTime());
        assertEquals("2010-05-31 13:55:00", template.formatForTest("~(now):first:castdate:sqldate~", model));
    }
    
    public void test_011g() throws IOException, TemplateException {
        try {
            template.parse("~(firstname,lastname?):upper~");
            fail("An exception must be raised.");
        } catch (TemplateException e) {
            assertEquals("Invalid filter chain. Unable to render '~(firstname,lastname?):upper~'. Filter 'upper' expects 'java.lang.String' but will receive an array.", e.getMessage());
        } catch (Exception e) {
            fail("Bad exception.");
        }
    }
    
    public void test_011h() throws IOException, TemplateException {
        try {
            template.parse("~(firstname,lastname?):upper~");
            fail("An exception must be raised.");
        } catch (TemplateException e) {
            assertEquals("Invalid filter chain. Unable to render '~(firstname,lastname?):upper~'. Filter 'upper' expects 'java.lang.String' but will receive an array.", e.getMessage());
        } catch (Exception e) {
            fail("Bad exception.");
        }
    }

    public void test_011i() throws IOException, TemplateException {
        try {
            template.parse("~(mylist):size~");
            fail("An exception must be raised.");
        } catch (TemplateException e) {
            assertEquals("Invalid filter chain. Unable to render '~(mylist):size~'. Filter 'size' expects 'java.util.Collection' but will receive an array.", e.getMessage());
        }
    }

    public void test_011j() throws IOException, TemplateException {
        model.put("index", "134");
        try {
            template.formatForTest("~index:size~", model);
            fail("An exception must be raised.");
        } catch (TemplateException e) {
            assertEquals("Invalid filter chain. Filter 'size' expects 'java.util.Collection'. It receives 'java.lang.String'. Unable to render '~index:size~'.", e.getMessage());
        } catch (Exception e) {
            fail("Bad exception.");
        }
    }

    public void test_011k() throws IOException, TemplateException {
        try {
            template.formatForTest("~(mylist1):size~", model);
            fail("An exception must be raised.");
        } catch (TemplateException e) {
            assertEquals("Invalid filter chain. Unable to render '~(mylist1):size~'. Filter 'size' expects 'java.util.Collection' but will receive an array.", e.getMessage());
        } catch (Exception e) {
            fail("Bad exception.");
        }
    }
    
    public void test_011l() throws IOException, TemplateException {
        model.put("index", 1);
        assertEquals("2", template.formatForTest("~index:add1:upper~", model));
    }
    
    public void test_011m() throws IOException, TemplateException {
        try {
            model.put("index", "134");
            model.put("firstname", "jeff");
            assertEquals("134", template.formatForTest("~(index,firstname):2str:upper~", model));
            fail("An exception must be raised.");
        } catch (TemplateException e) {
            e.printStackTrace();
            assertEquals("Invalid filter chain. Filter 'upper' expects 'java.lang.String'. It receives 'java.lang.String[]'. Unable to render '~(index,firstname):2str:upper~'.", e.getMessage());
        } catch (Exception e) {
            fail("Bad exception.");
        }
    }

    public void test_011n() throws IOException, TemplateException {
        try {
            template.parse("~(firstname,lastname?):upper~");
            fail("An exception must be raised.");
        } catch (TemplateException e) {
            assertEquals("Invalid filter chain. Unable to render '~(firstname,lastname?):upper~'. Filter 'upper' expects 'java.lang.String' but will receive an array.", e.getMessage());
        } catch (Exception e) {
            fail("Bad exception.");
        }
    }


    public void test_011o() throws IOException, TemplateException {
        model.put("index", 1);
        model.put("total", 2);
        assertEquals("true", template.formatForTest("~(index,total):lt~", model));
    }

    public void test_011p() throws IOException, TemplateException {
        try {
            model.put("index", 2);
            template.formatForTest("~(index):upper~", model);
            fail("An exception must be raised.");
        } catch (TemplateException e) {
            assertEquals("Invalid filter chain. Unable to render '~(index):upper~'. Filter 'upper' expects 'java.lang.String' but will receive an array.", e.getMessage());
        } catch (Exception e) {
            fail("Bad exception.");
        } 
    }    

    public void test_011q() throws IOException, TemplateException {
        model.put("index", 2);
        assertEquals("2", template.formatForTest("~index:upper~", model));
    }    
    
    public void test_011r() throws IOException, TemplateException {
        model.put("index", 2);
        model.put("firstname", "jeff");
        assertEquals("2", template.formatForTest("~(index,firstname):2str:first~", model));
    }    
    
    public void test_011s() throws IOException, TemplateException {
        model.put("index", 1);
        try {
            template.formatForTest("~index:lower:add1~", model);
            fail("An exception must be raised.");
        } catch (TemplateException e) {
            assertEquals("Invalid filter chain. Unable to render '~index:lower:add1~'. Filter 'lower' produces 'java.lang.String'. Filter 'add1' expects 'java.lang.Integer'.", e.getMessage());
        } catch (Exception e) {
            fail("Bad exception.");
        }
    }
    
    // #true
    
    public void test_012a() throws IOException, TemplateException {
        model.put("boolean", Boolean.TRUE);
        assertEquals("hello", template.formatForTest("~boolean#true~hello~#true~", model));
    }
    
    public void test_012b() throws IOException, TemplateException {
        model.put("boolean", Boolean.FALSE);
        assertEquals("", template.formatForTest("~boolean#true~hello~#true~", model));
    }
    
    public void test_012c() throws IOException, TemplateException {
        try {
            model.put("index", 0);
            template.formatForTest("~index#true~hello~#true~", model);
            fail("An exception must be raised.");
        } catch (TemplateException e) {
            assertEquals("Key 'index' is not a boolean [java.lang.Integer]. Unable to render '~index#true~'.", e.getMessage());
        } catch (Exception e) {
            fail("Bad exception.");
        }
    }

    public void test_012d() throws IOException, TemplateException {
        model.put("index", 1);
        assertEquals("hello", template.formatForTest("~index:odd#true~hello~#true~", model));
    }

    public void test_012e() throws IOException, TemplateException {
        assertEquals("", template.formatForTest("~boolean?#true~the key is not defined, so the text is not displayed~#true~", model));
    }

    public void test_012f() throws IOException, TemplateException {
        model.put("index", 1);
        model.put("nb", 2);
        assertEquals("the condition is true, so the text is displayed", template.formatForTest("~(index,nb):lt#true~the condition is true, so the text is displayed~#true~", model));
    }
    
    public void test_012g() throws IOException, TemplateException {
        model.put("nb", 2);
        assertEquals("", template.formatForTest("~(index?,nb):lt#true~the condition is true, so the text is displayed~#true~", model));
    }
    
    public void test_012h() throws IOException, TemplateException {
        assertEquals("", template.formatForTest("~index?:odd#true~hello~#true~", model));
    }
    
    // #false
    
    public void test_015a() throws IOException, TemplateException {
        model.put("boolean", Boolean.TRUE);
        assertEquals("", template.formatForTest("~boolean#false~hello~#false~", model));
    }
    
    public void test_015b() throws IOException, TemplateException {
        model.put("boolean", Boolean.FALSE);
        assertEquals("hello", template.formatForTest("~boolean#false~hello~#false~", model));
    }
    
    public void test_015c() throws IOException, TemplateException {
        try {
            model.put("index", 0);
            template.formatForTest("~index#false~hello~#false~", model);
            fail("An exception must be raised.");
        } catch (TemplateException e) {
            assertEquals("Key 'index' is not a boolean [java.lang.Integer]. Unable to render '~index#false~'.", e.getMessage());
        } catch (Exception e) {
            fail("Bad exception.");
        } 
    }

    public void test_015d() throws IOException, TemplateException {
        model.put("index", 0);
        assertEquals("hello", template.formatForTest("~index:odd#false~hello~#false~", model));
    }

    public void test_015e() throws IOException, TemplateException {
        assertEquals("", template.formatForTest("~boolean?#false~the key is not defined, so the text is not displayed~#false~", model));
    }

    public void test_015f() throws IOException, TemplateException {
        model.put("index", 5);
        model.put("nb", 2);
        assertEquals("the condition is true, so the text is displayed", template.formatForTest("~(index,nb):lt#false~the condition is true, so the text is displayed~#false~", model));
    }
    
    public void test_015g() throws IOException, TemplateException {
        model.put("nb", 2);
        assertEquals("", template.formatForTest("~(index?,nb):lt#false~the condition is true, so the text is displayed~#false~", model));
    }
    
    public void test_015h() throws IOException, TemplateException {
        assertEquals("", template.formatForTest("~index?:odd#false~hello~#false~", model));
    }

    public void test_015i() {
        try {
            template.parse("~boolean#false~hello~#true~");
            fail("An exception must be raised.");
        } catch (TemplateException e) {
            assertEquals("Invalid syntax. The open tag 'false' doesn't match the close tag 'true'. Unable to render '~boolean#false~'.", e.getMessage());
        } catch (Exception e) {
            fail("Bad exception.");
        } 
    }
    
    public void test_015j() {
        try {
            template.parse("~boolean1?#false~~boolean2#true~hello~#true~~#true~");
            fail("An exception must be raised.");
        } catch (TemplateException e) {
            assertEquals("Invalid syntax. The open tag 'false' doesn't match the close tag 'true'. Unable to render '~boolean1?#false~'.", e.getMessage());
        } catch (Exception e) {
            fail("Bad exception.");
        }  
    }
    
    // files
    
    public void test_13a() throws IOException, TemplateException {
        template = new Template("test/test.tpl", filters, properties, Locale.ENGLISH);
        StringWriter out = new StringWriter();

        List<Map<String, Object>> models = createList(
                createModel("index", 0, "fruit", "orange"),
                createModel("index", 1, "fruit", "apple"),
                createModel("index", 2));
        model.put("fruits", models);
        model.put("firstname", "John");
        model.put("lastname", "Doe");
        
        template.printSection(out, "test", model);
        assertEquals("Hello John Doe!\n\nYou like these fruits:\n\n  0 orange\n\n  1 apple\n\n  2 \n\n", out.toString());
    }

    public void test_13b() throws IOException, TemplateException {
        try {
            template = new Template("test/test.tpl", filters, properties, Locale.ENGLISH);
            StringWriter out = new StringWriter();
            template.printSection(out, "unknownSection", model);
            fail("aille");
        } catch (TemplateException e) {
            assertEquals("Section 'unknownSection' not found.", e.getMessage());
        } catch (Exception e) {
            fail("Bad exception.");
        }
    }

    public void test_13c() throws IOException, TemplateException {
        template = new Template("test/test.tpl", filters, properties, Locale.ENGLISH);
        StringWriter out = new StringWriter();
        model.remove("firstname");
        try {
            template.printSection(out, "test", model);
            fail("An exception must be raised.");
        } catch (TemplateException e) {
            assertEquals("Key 'firstname' is not present or has null value in the model map to render '~firstname~' at position 'test/test.tpl:2'.", e.getMessage());
        } catch (Exception e) {
            fail("Bad exception.");
        }
    }
    
    public void test_014a() throws IOException, TemplateException {
        model.put("firstname1", "John");
        model.put("firstname2", "Jane");
        model.put("index", 1);
        model.put("total", 2);
        assertEquals("HELLO JOHN AND JANE!", template.formatForTest("~('helloxy[firstname1,firstname2]:upper,firstname1:upper,total):first~", model));
    }

    public void test_014b() throws IOException, TemplateException {
        model.put("firstname1", "John");
        model.put("firstname2", "Jane");
        model.put("index", 1);
        model.put("total", 2);
        model.put("tag", "helloxy");
        assertEquals("HELLO JOHN AND JANE!", template.formatForTest("~(tag[firstname1,firstname2]:upper,firstname1:upper,total):first~", model));
    }

    public void test_030a() throws IOException, TemplateException {
        model.put("firstname", "John");
        assertEquals("John", template.formatForTest("~firstname#set<myvar>~~myvar~~#set~", model));
    }

    public void test_030c() throws IOException, TemplateException {
        try {
            template.formatForTest("~firstname?#set<myvar>~~myvar~~#set~", model);
            fail("An exception must be raised.");
        } catch (TemplateException e) {
            assertEquals("Key 'myvar' is not present or has null value in the model map to render '~myvar~'.", e.getMessage());
        } catch (Exception e) {
            fail("Bad exception.");
        }
    }
    
    public void test_030d() throws IOException, TemplateException {
        model.put("index", 1);
        model.put("total", 2);
        assertEquals("less", template.formatForTest("~(index,total):lt#set<b>~~b#true~less~#true~~#set~", model));
    }
    
    public void test_030e() throws IOException, TemplateException {
        model.put("b", "toto");
        model.put("index", 1);
        model.put("total", 2);
        assertEquals("totolesstoto", template.formatForTest("~b~~(index,total):lt#set<b>~~b#true~less~#true~~#set~~b~", model));
    }

    public void test_030f() throws IOException, TemplateException {
        model.put("index", 1);
        assertEquals("1", template.formatForTest("~index~", model));
        assertEquals("2", template.formatForTest("~index:add1~", model));
        assertEquals("21", template.formatForTest("~index:add1#set<index>~~index~~#set~~index~", model));
    }

    
    public void test_016d() throws IOException, TemplateException {
        model.put("astring", "hello");
        assertEquals("blabla", template.formatForTest("~astring:length:gt0#true~blabla~#true~", model));
    }

    public void test_017() throws IOException, TemplateException {
        model.put("b", true);
        assertEquals("Orange", template.formatForTest("~(b,'orange[],'lemon[]):ifelse~", model));
    }

    public void test_017b() throws IOException, TemplateException {
        model.put("b", true);
        model.put("msg", "bonjour");
        assertEquals("bonjour", template.formatForTest("~(b,msg,'lemon[]):ifelse~", model));
    }

    public void test_031() throws IOException, TemplateException {
    	model.put("b", false);
    	model.put("msg", "bonjour");
        model.put("firstnames", Arrays.asList("Jeff", "Peg"));
        assertEquals("Hello JEFF and PEG!", template.formatForTest("~(b,msg,'helloxy[@firstnames:upper]):ifelse~", model));
        assertEquals("Hello JEFF and PEG!", template.formatForTest("~'helloxy[@firstnames:upper]~", model));
    }
    
    public void test_031a() throws IOException, TemplateException {
    	model.put("b", false);
    	model.put("msg", "bonjour");
        model.put("firstnames1", Arrays.asList("Jeff"));
        
        assertEquals("", template.formatForTest("~(b,msg,'helloxy[@firstnames1:upper,@firstnames2?]):ifelse~", model));
    }
    
    public void test_031b() throws IOException, TemplateException {
    	model.put("b", false);
    	model.put("msg", "bonjour");
        model.put("firstnames1", Arrays.asList("Jeff"));
        model.put("firstnames2", Arrays.asList("Peg"));
        
        assertEquals("Hello JEFF and Peg!", template.formatForTest("~(b,msg,'helloxy[@firstnames1:upper,@firstnames2?]):ifelse~", model));
    }
    
    public void test_031c() throws IOException, TemplateException {
    	model.put("b", false);
    	model.put("msg", "bonjour");
        model.put("firstnames1", "Jeff");
        model.put("firstnames2", Arrays.asList("Peg"));
        
        try {
        	assertEquals("Hello JEFF and Peg!", template.formatForTest("~(b,msg,'helloxy[@firstnames1:upper,@firstnames2?]):ifelse~", model));
            fail("An exception must be raised.");
        } catch (TemplateException e) {
            assertEquals("Key 'firstnames1' is not an array or an Iterable object in the model map to render '~(b,msg,'helloxy[@firstnames1:upper,@firstnames2?]):ifelse~'.", e.getMessage());
        } catch (Exception e) {
            fail("Bad exception. ");
        }
    }
    
    public void test_031d() throws IOException, TemplateException {
    	model.put("firstname1", "Jeff");
    	model.put("firstname2", "Peg");
        model.put("firstnames", Arrays.asList("Jeff", "Peg"));
        
        assertEquals("Jeff*Peg", template.formatForTest("~(firstname1,firstname2):join~", model));
        assertEquals("Jeff*Peg*Peg", template.formatForTest("~(@firstnames,firstname2):join~", model));
    }
    
    public void test_031e() throws IOException, TemplateException {
    	model.put("firstname1", "Jeff");
    	model.put("firstname2", "Peg");
        model.put("firstnames", "Jeff");
    }
    
    public void test_xxx() throws IOException, TemplateException {
/*        Pattern p = Pattern.compile("file:([^;]*)");
        Matcher m = p.matcher("totofile:tutu;file:titi");
        if (m.find()) System.out.println(m.group(1));
        if (m.find()) System.out.println(m.group(1));
        */
    	String t[] = new String[] { "t"};
    	assertTrue(t.getClass().isArray());
    }

}
