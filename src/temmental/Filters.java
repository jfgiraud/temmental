package temmental;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

public class Filters {
	
	public static final Map<String, ObjectFilter> HTML;
	public static final Map<String, ObjectFilter> MATH;
	public static final Map<String, ObjectFilter> TEXT;
	public static final Map<String, ObjectFilter> DATE;
	
	static {

	    MATH = new HashMap<String, ObjectFilter>();

	    MATH.put("lt", new ObjectFilter<Integer[], Boolean>() {
	        public Boolean apply(Integer values[]) {
	            if (values.length != 2)
	                throw new RuntimeException(String.format("Filter 'lt' requires only 2 elements. Receives %d element(s).", values.length));
	            return values[0].intValue() < values[1].intValue();
	        }
	    });

	    MATH.put("lt0", new ObjectFilter<Integer, Boolean>() {
	        public Boolean apply(Integer values) {
	            return values.intValue() < 0;
	        }
	    });

	    MATH.put("le", new ObjectFilter<Integer[], Boolean>() {
            public Boolean apply(Integer values[]) {
                if (values.length != 2)
                    throw new RuntimeException(String.format("Filter 'le' requires only 2 elements. Receives %d element(s).", values.length));
                return values[0].intValue() <= values[1].intValue();
            }
        });

	    MATH.put("le0", new ObjectFilter<Integer, Boolean>() {
	        public Boolean apply(Integer values) {
	            return values.intValue() <= 0;
	        }
	    });

	    MATH.put("eq", new ObjectFilter<Integer[], Boolean>() {
            public Boolean apply(Integer values[]) {
                if (values.length != 2)
                    throw new RuntimeException(String.format("Filter 'eq' requires only 2 elements. Receives %d element(s).", values.length));
                return values[0].intValue() == values[1].intValue();
            }
        });
	    
	    MATH.put("eq0", new ObjectFilter<Integer, Boolean>() {
	        public Boolean apply(Integer values) {
	            return values.intValue() == 0;
	        }
	    });
	    
	    MATH.put("ne", new ObjectFilter<Integer[], Boolean>() {
            public Boolean apply(Integer values[]) {
                if (values.length != 2)
                    throw new RuntimeException(String.format("Filter 'ne' requires only 2 elements. Receives %d element(s).", values.length));
                return values[0].intValue() == values[1].intValue();
            }
        });
	    
        MATH.put("ne0", new ObjectFilter<Integer, Boolean>() {
            public Boolean apply(Integer values) {
                return values.intValue() != 0;
            }
        });

        MATH.put("gt", new ObjectFilter<Integer[], Boolean>() {
            public Boolean apply(Integer values[]) {
                if (values.length != 2)
                    throw new RuntimeException(String.format("Filter 'gt' requires only 2 elements. Receives %d element(s).", values.length));
                return values[0].intValue() > values[1].intValue();
            }
        });
	    
        MATH.put("gt0", new ObjectFilter<Integer, Boolean>() {
            public Boolean apply(Integer values) {
                return values.intValue() > 0;
            }
        });

        MATH.put("ge", new ObjectFilter<Integer[], Boolean>() {
            public Boolean apply(Integer values[]) {
                if (values.length != 2)
                    throw new RuntimeException(String.format("Filter 'ge' requires only 2 elements. Receives %d element(s).", values.length));
                return values[0].intValue() >= values[1].intValue();
            }
        });
        
        MATH.put("ge0", new ObjectFilter<Integer, Boolean>() {
            public Boolean apply(Integer values) {
                return values.intValue() >= 0;
            }
        });
        
        MATH.put("odd", new ObjectFilter<Integer, Boolean>() {
            public Boolean apply(Integer value) {
                return value.intValue() % 2 == 1;
            }
        });

        MATH.put("even", new ObjectFilter<Integer, Boolean>() {
            public Boolean apply(Integer value) {
                return value.intValue() % 2 == 0;
            }
        });
        
        MATH.put("not", new ObjectFilter<Boolean, Boolean>() {
            public Boolean apply(Boolean value) {
                return ! value.booleanValue();
            }
        });
        
        MATH.put("size", new ObjectFilter<Collection, Integer>() {
            public Integer apply(Collection parameters) {
                return parameters.size();
            }
        });
        
        MATH.put("is_empty", new ObjectFilter<Collection, Boolean>() {
            public Boolean apply(Collection parameters) {
                return parameters.size() == 0;
            }
        });
        
        MATH.put("is_not_empty", new ObjectFilter<Collection, Boolean>() {
            public Boolean apply(Collection parameters) {
                return parameters.size() != 0;
            }
        });
        
        MATH.put("and", new ObjectFilter<Boolean[], Boolean>() {
            public Boolean apply(Boolean parameters[]) {
                for (int i=0; i < parameters.length; i++) {
                    if (! parameters[i])
                        return false;
                }
                return true;
            }
        });
        
        MATH.put("or", new ObjectFilter<Boolean[], Boolean>() {
            public Boolean apply(Boolean parameters[]) {
                for (int i=0; i < parameters.length; i++) {
                    if (parameters[i])
                        return true;
                }
                return false;
            }
        });
        
        TEXT = new HashMap<String, ObjectFilter>();
        
        TEXT.put("upper", new ObjectFilter<String, String>() {
            public String apply(String value) {
                return value.toUpperCase();
            }
        });
        
        TEXT.put("lower", new ObjectFilter<String,String>() {
            public String apply(String value) {
                return value.toString().toLowerCase();
            }
        });
        
        TEXT.put("length", new ObjectFilter<String,Integer>() {
            public Integer apply(String value) {
                return value.length();
            }
        });
        
	    HTML = new HashMap<String, ObjectFilter>();
	    
	    HTML.put("url", new Filter() {
	        public String apply(String value) {
	            try {
	                return URLEncoder.encode(value, "ISO-8859-1");
	            } catch (UnsupportedEncodingException e) {
	                throw new RuntimeException("Unexpected exception: " + e, e);
	            }
	        }
	    });
	    
	    HTML.put("value", new Filter() {
	        private char what[] = { '<', '>', '&', '"' };
	        private String by[] = { "&lt;", "&gt;", "&amp;", "&quot;" };
	        public String apply(String s) {
	            StringBuffer result = new StringBuffer("");
	            if (s != null) {
	                int m = what.length;
	                for (int i=0,n=s.length(); i<n; i++) {
						 char c = s.charAt(i);
						 int found = -1;
						 for (int j=0; j<m; j++) {
							 if (c == what[j]) {
								 found = j;
								 break;
							 }
						 }
						 if (found != -1)
							 result.append(by[found]);
						 else
							 result.append(c);
					 }
				 }
				 return result.toString();
			 }
	    });
	    
	    DATE = new HashMap<String, ObjectFilter>();
	    
	    DATE.put("before_now", new ObjectFilter<Date, Boolean>() {
            public Boolean apply(Date date) {
                if (date != null) {
                    GregorianCalendar now = new GregorianCalendar();
                    GregorianCalendar end = new GregorianCalendar();
                    end.setTimeInMillis(date.getTime());
                    return now.after(end);
                }
                return false;
            }
        });
	    
	    DATE.put("after_now", new ObjectFilter<Date, Boolean>() {
            public Boolean apply(Date date) {
                if (date != null) {
                    GregorianCalendar now = new GregorianCalendar();
                    GregorianCalendar end = new GregorianCalendar();
                    end.setTimeInMillis(date.getTime());
                    return now.before(end);
                }
                return false;
            }
        });
	    
	    DATE.put("before", new ObjectFilter<Date[], Boolean>() {
            public Boolean apply(Date dates[]) {
                Date first = dates[0];
                Date second = dates[1];
                return first.before(second); 
            }
        });
	    
	    DATE.put("after", new ObjectFilter<Date[], Boolean>() {
            public Boolean apply(Date dates[]) {
                Date first = dates[0];
                Date second = dates[1];
                return first.before(second); 
            }
        });
	}
}
