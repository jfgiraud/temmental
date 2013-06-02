package temmental2;

import java.util.Iterator;
import java.util.List;

public class StringUtils {
	
	public static int countOccurrences(String haystack, char needle) {
	    int count = 0;
	    for (int i=0; i < haystack.length(); i++) {
	        if (haystack.charAt(i) == needle) {
	             count++;
	        }
	    }
	    return count;
	}
	
	/**
	 * Concatenate a list of words with intervening occurrences of sep.
	 * @param sep the separator.
	 * @param words the string list.
	 * @return the resulting string.
	 */
	public static String join(String sep, List<String> words) {
		StringBuffer s = new StringBuffer();

		for (Iterator<String> iterator = words.iterator(); iterator.hasNext(); ) {
			s.append(iterator.next());
			if (iterator.hasNext()) {
				s.append(sep);
			}
		}

		return s.toString();
	}

	/**
	 * Concatenate a list of <code>words</code> with intervening occurrences of <code>sep</code>.
	 * @param sep the separator.
	 * @param words the string list.
	 * @return the resulting string.
	 */
	public static String join(String sep, String ... words) {
		StringBuffer s = new StringBuffer();

		for (int i=0; i < words.length; i++) {
			s.append(words[i]);
			if (i < words.length-1) {
				s.append(sep);
			}
		}

		return s.toString();
	}

	public static String capitalize(String s) {
		if (s.length() == 0) 
			return s;
		else
			return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();

	}
	
	public static String lstrip(String s) {
		return s.replaceAll("^\\s+", "");
	}

	public static String rstrip(String s) {
		return s.replaceAll("\\s+$", "");
	}

	public static String strip(String s) {
		s = s.replaceAll("^\\s+", "");
		return s.replaceAll("\\s+$", "");
	}

	public static String reverse(String s) {
		return new StringBuilder(s).reverse().toString();
	}
	
	public static String titlelize(String s) {

		final String ACTIONABLE_DELIMITERS = " '-/"; // these cause the character following
		// to be capitalized

		StringBuilder sb = new StringBuilder();
		boolean capNext = true;

		for (char c : s.toCharArray()) {
			c = (capNext)
					? Character.toUpperCase(c)
							: Character.toLowerCase(c);
					sb.append(c);
					capNext = (ACTIONABLE_DELIMITERS.indexOf((int) c) >= 0); // explicit cast not needed
		}
		return sb.toString();
	}
}
