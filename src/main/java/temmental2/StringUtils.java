package temmental2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class StringUtils {
	
//	private static String[] whitespace_chars =  new String[] { " ", "\t"};
	
	String whitespace_chars =  ""       /* dummy empty string for homogeneity */
            + "\\u0009" // CHARACTER TABULATION
            + "\\u000A" // LINE FEED (LF)
            + "\\u000B" // LINE TABULATION
            + "\\u000C" // FORM FEED (FF)
            + "\\u000D" // CARRIAGE RETURN (CR)
            + "\\u0020" // SPACE
            + "\\u0085" // NEXT LINE (NEL) 
            + "\\u00A0" // NO-BREAK SPACE
            + "\\u1680" // OGHAM SPACE MARK
            + "\\u180E" // MONGOLIAN VOWEL SEPARATOR
            + "\\u2000" // EN QUAD 
            + "\\u2001" // EM QUAD 
            + "\\u2002" // EN SPACE
            + "\\u2003" // EM SPACE
            + "\\u2004" // THREE-PER-EM SPACE
            + "\\u2005" // FOUR-PER-EM SPACE
            + "\\u2006" // SIX-PER-EM SPACE
            + "\\u2007" // FIGURE SPACE
            + "\\u2008" // PUNCTUATION SPACE
            + "\\u2009" // THIN SPACE
            + "\\u200A" // HAIR SPACE
            + "\\u2028" // LINE SEPARATOR
            + "\\u2029" // PARAGRAPH SEPARATOR
            + "\\u202F" // NARROW NO-BREAK SPACE
            + "\\u205F" // MEDIUM MATHEMATICAL SPACE
            + "\\u3000" // IDEOGRAPHIC SPACE
            ;        
	
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

	public static String[] split(String string, String sep, int max) {
		if (sep == null) {
			List<String> v = new ArrayList<String>();
			
			StringBuilder sb = new StringBuilder();
			boolean inWord = true;
			
			for (char c : string.toCharArray()) {
				if (max == 0) {
					sb.append(c);
					continue;
				}
				if (inWord) {
					if (Character.isWhitespace(c)) {
						v.add(sb.toString());
						sb = new StringBuilder();
						inWord = false;
					} else {
						sb.append(c);
					}
				} else {
					if (! Character.isWhitespace(c)) {
						inWord = true;
						max--;
						sb.append(c);
					} 
				}
			}
			if (sb.toString().length() >= 1) {
				v.add(sb.toString());
			}
			return v.toArray(new String[]{});
		}
		// abcdefghdedeuuis
		// Hello*the*World!
		// 012345678901234567
		List<String> v = new ArrayList<String>();
		int i = 0;
		int n = 0;
		while (i < string.length()) {
			if (max >= 0 && n >= max) {
				v.add(string.substring(i));
				break;
			}
			int f = string.indexOf(sep, i);
			if (f >= 0) {
				v.add(string.substring(i, f));
				i = f + sep.length();
				n++;
			} else {
				v.add(string.substring(i));
				break;
			}
		}
		return v.toArray(new String[]{});
	}
}
