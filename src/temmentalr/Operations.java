package temmentalr;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Comparator;

public class Operations {

	public static Number add(Number a, Number b) {
		if (a instanceof Double || b instanceof Double) {
			return a.doubleValue() + b.doubleValue();
		}
		if (a instanceof Float || b instanceof Float) {
			return a.floatValue() + b.floatValue();
		}
		if (a instanceof Long || b instanceof Long) {
			return a.longValue() + b.longValue();
		}
		if (a instanceof Integer || b instanceof Integer) {
			return a.intValue() + b.intValue();
		}
		if (a instanceof Short || b instanceof Short) {
			return a.shortValue() + b.shortValue();
		}
		if (a instanceof Byte || b instanceof Byte) {
			return a.byteValue() + b.byteValue();
		}
		throw new RuntimeException("Add case not implemented.");
	}
	
	public static Number mul(Number a, Number b) {
		if (a instanceof Double || b instanceof Double) {
			return a.doubleValue() * b.doubleValue();
		}
		if (a instanceof Float || b instanceof Float) {
			return a.floatValue() * b.floatValue();
		}
		if (a instanceof Long || b instanceof Long) {
			return a.longValue() * b.longValue();
		}
		if (a instanceof Integer || b instanceof Integer) {
			return a.intValue() * b.intValue();
		}
		if (a instanceof Short || b instanceof Short) {
			return a.shortValue() * b.shortValue();
		}
		if (a instanceof Byte || b instanceof Byte) {
			return a.byteValue() * b.byteValue();
		}
		throw new RuntimeException("Add case not implemented.");
	}
	
	public static Number sub(Number a, Number b) {
		if (a instanceof Double || b instanceof Double) {
			return a.doubleValue() - b.doubleValue();
		}
		if (a instanceof Float || b instanceof Float) {
			return a.floatValue() - b.floatValue();
		}
		if (a instanceof Long || b instanceof Long) {
			return a.longValue() - b.longValue();
		}
		if (a instanceof Integer || b instanceof Integer) {
			return a.intValue() - b.intValue();
		}
		if (a instanceof Short || b instanceof Short) {
			return a.shortValue() - b.shortValue();
		}
		if (a instanceof Byte || b instanceof Byte) {
			return a.byteValue() - b.byteValue();
		}
		throw new RuntimeException("Add case not implemented.");
	}

	public static Number mod(Number a, Number b) {
		if (a instanceof Double || b instanceof Double) {
			return a.doubleValue() % b.doubleValue();
		}
		if (a instanceof Float || b instanceof Float) {
			return a.floatValue() % b.floatValue();
		}
		if (a instanceof Long || b instanceof Long) {
			return a.longValue() % b.longValue();
		}
		if (a instanceof Integer || b instanceof Integer) {
			return a.intValue() % b.intValue();
		}
		if (a instanceof Short || b instanceof Short) {
			return a.shortValue() % b.shortValue();
		}
		if (a instanceof Byte || b instanceof Byte) {
			return a.byteValue() % b.byteValue();
		}
		throw new RuntimeException("Add case not implemented.");
	}

	public static Number neg(Number a) {
		return mul(a, -1);
	}

	public static boolean even(Number a) {
		if (a instanceof Double) {
			return a.doubleValue() % 2 == 0;
		}
		if (a instanceof Float) {
			return a.floatValue() % 2 == 0;
		}
		if (a instanceof Long) {
			return a.longValue() % 2 == 0;
		}
		if (a instanceof Integer) {
			return a.intValue() % 2 == 0;
		}
		if (a instanceof Short) {
			return a.shortValue() % 2 == 0;
		}
		if (a instanceof Byte) {
			return a.byteValue() % 2 == 0;
		}
		throw new RuntimeException("Add case not implemented.");
	}
	
	public static boolean odd(Number a) {
		return ! even(a);
	}

	public static boolean lt(Number a, Number b) {
		if (a instanceof Double || b instanceof Double) {
			return a.doubleValue() < b.doubleValue();
		}
		if (a instanceof Float || b instanceof Float) {
			return a.floatValue() < b.floatValue();
		}
		if (a instanceof Long || b instanceof Long) {
			return a.longValue() < b.longValue();
		}
		if (a instanceof Integer || b instanceof Integer) {
			return a.intValue() < b.intValue();
		}
		if (a instanceof Short || b instanceof Short) {
			return a.shortValue() < b.shortValue();
		}
		if (a instanceof Byte || b instanceof Byte) {
			return a.byteValue() < b.byteValue();
		}
		throw new RuntimeException("Add case not implemented.");
	}

	public static boolean gt(Number a, Number b) {
		if (a instanceof Double || b instanceof Double) {
			return a.doubleValue() > b.doubleValue();
		}
		if (a instanceof Float || b instanceof Float) {
			return a.floatValue() > b.floatValue();
		}
		if (a instanceof Long || b instanceof Long) {
			return a.longValue() > b.longValue();
		}
		if (a instanceof Integer || b instanceof Integer) {
			return a.intValue() > b.intValue();
		}
		if (a instanceof Short || b instanceof Short) {
			return a.shortValue() > b.shortValue();
		}
		if (a instanceof Byte || b instanceof Byte) {
			return a.byteValue() > b.byteValue();
		}
		throw new RuntimeException("Add case not implemented.");
	}
	
	public static boolean le(Number a, Number b) {
		if (a instanceof Double || b instanceof Double) {
			return a.doubleValue() <= b.doubleValue();
		}
		if (a instanceof Float || b instanceof Float) {
			return a.floatValue() <= b.floatValue();
		}
		if (a instanceof Long || b instanceof Long) {
			return a.longValue() <= b.longValue();
		}
		if (a instanceof Integer || b instanceof Integer) {
			return a.intValue() <= b.intValue();
		}
		if (a instanceof Short || b instanceof Short) {
			return a.shortValue() <= b.shortValue();
		}
		if (a instanceof Byte || b instanceof Byte) {
			return a.byteValue() <= b.byteValue();
		}
		throw new RuntimeException("Add case not implemented.");
	}

	public static boolean ge(Number a, Number b) {
		if (a instanceof Double || b instanceof Double) {
			return a.doubleValue() >= b.doubleValue();
		}
		if (a instanceof Float || b instanceof Float) {
			return a.floatValue() >= b.floatValue();
		}
		if (a instanceof Long || b instanceof Long) {
			return a.longValue() >= b.longValue();
		}
		if (a instanceof Integer || b instanceof Integer) {
			return a.intValue() >= b.intValue();
		}
		if (a instanceof Short || b instanceof Short) {
			return a.shortValue() >= b.shortValue();
		}
		if (a instanceof Byte || b instanceof Byte) {
			return a.byteValue() >= b.byteValue();
		}
		throw new RuntimeException("Add case not implemented.");
	}
	
	public static boolean eq(Number a, Number b) {
		return (new NumberComparator()).compare(a, b) == 0;
	}
	
	public static boolean ne(Number a, Number b) {
		return (new NumberComparator()).compare(a, b) != 0;
	}
	
	static class NumberComparator implements Comparator<Number> {
		public int compare(Number number1, Number number2) {
			if (((Object) number2).getClass().equals(((Object) number1).getClass())) {
				// both numbers are instances of the same type!
				if (number1 instanceof Comparable) {
					// and they implement the Comparable interface
					return ((Comparable) number1).compareTo(number2);
				}
			}
			// for all different Number types, let's check there double values
			if (number1.doubleValue() < number2.doubleValue())
				return -1;
			if (number1.doubleValue() > number2.doubleValue())
				return 1;
			return 0;
		}
	}		
}
