package temmentalr;

import java.math.BigDecimal;
import java.math.BigInteger;

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
}
