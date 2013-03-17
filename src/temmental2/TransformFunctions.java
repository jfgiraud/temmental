package temmental2;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class TransformFunctions {
	
	public static final Map<String, Transform> MATH;
	public static final Map<String, Transform> CONDITIONAL;
//	public static final Map<String, Transform> TEXT;
//	public static final Map<String, Transform> DATE;
	
	private static int compareTo(Number n1, Number n2) {
	    // ignoring null handling
	    BigDecimal b1 = new BigDecimal(n1.doubleValue());
	    BigDecimal b2 = new BigDecimal(n2.doubleValue());
	    return b1.compareTo(b2);
	}
	
	static {
		MATH = new HashMap<String, Transform>();
		CONDITIONAL = new HashMap<String, Transform>();

	    MATH.put("gt", new Transform<Number, Transform>() {
			@Override
			public Transform apply(final Number withValue) {
				return new Transform<Number, Boolean>() {
					@Override
					public Boolean apply(Number value) {
						return compareTo(value, withValue) > 0;
					}
				};
			}
	    });
	    
	    MATH.put("ge", new Transform<Number, Transform>() {
			@Override
			public Transform apply(final Number withValue) {
				return new Transform<Number, Boolean>() {
					@Override
					public Boolean apply(Number value) {
						return compareTo(value, withValue) >= 0;
					}
				};
			}
	    });
	    
	    MATH.put("lt", new Transform<Number, Transform>() {
			@Override
			public Transform apply(final Number withValue) {
				return new Transform<Number, Boolean>() {
					@Override
					public Boolean apply(Number value) {
						return compareTo(value, withValue) < 0;
					}
				};
			}
	    });
	    
	    MATH.put("le", new Transform<Number, Transform>() {
			@Override
			public Transform apply(final Number withValue) {
				return new Transform<Number, Boolean>() {
					@Override
					public Boolean apply(Number value) {
						return compareTo(value, withValue) <= 0;
					}
				};
			}
	    });
	    
	    MATH.put("eq", new Transform<Number, Transform>() {
			@Override
			public Transform apply(final Number withValue) {
				return new Transform<Number, Boolean>() {
					@Override
					public Boolean apply(Number value) {
						return compareTo(value, withValue) == 0;
					}
				};
			}
	    });
	    
	    MATH.put("ne", new Transform<Number, Transform>() {
			@Override
			public Transform apply(final Number withValue) {
				return new Transform<Number, Boolean>() {
					@Override
					public Boolean apply(Number value) {
						return compareTo(value, withValue) != 0;
					}
				};
			}
	    });
	    
	    MATH.put("not", new Transform<Boolean, Boolean>() {
	    	@Override
	    	public Boolean apply(Boolean value) {
	    		return ! value;
	    	}
	    });
	    
	    MATH.put("empty", new Transform<Collection, Boolean>() {
	    	@Override
	    	public Boolean apply(Collection collection) {
	    		return collection.size() == 0;
	    	}
	    });
	    
	    MATH.put("not_empty", new Transform<Collection, Boolean>() {
	    	@Override
	    	public Boolean apply(Collection collection) {
	    		return collection.size() != 0;
	    	}
	    });
	    
	    MATH.put("size", new Transform<Collection, Integer>() {
	    	@Override
	    	public Integer apply(Collection collection) {
	    		return collection.size();
	    	}
	    });
	    
	    MATH.put("int", new Transform<String, Integer>() {
	    	@Override
	    	public Integer apply(String value) {
	    		return Integer.valueOf(value);
	    	}
	    });
	    
	    CONDITIONAL.put("ifel", new Transform<Object[], Transform>() {
			@Override
			public Transform apply(final Object[] values) {
				return new Transform<Boolean, Object>() {
					@Override
					public Object apply(Boolean value) {
						if (value.booleanValue()) {
							return values[0];
						} else {
							return values[1];	
						}
					}
				};
			}
	    });
	    
	    MATH.put("odd", new Transform<Number, Boolean>() {
	    	@Override
	    	public Boolean apply(Number value) {
	    		if (value instanceof Short || value instanceof Integer || value instanceof Long || value instanceof Byte || value instanceof BigInteger)
	    			return value.longValue() % 2 == 1;
	    		return false;
	    	}
	    });
	    
	    MATH.put("even", new Transform<Number, Boolean>() {
	    	@Override
	    	public Boolean apply(Number value) {
	    		if (value instanceof Short || value instanceof Integer || value instanceof Long || value instanceof Byte || value instanceof BigInteger)
	    			return value.longValue() % 2 == 0;
	    		return false;
	    	}
	    });
	}
}
