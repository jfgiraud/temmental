package temmental2;

import java.util.*;

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
	
	public static String titleize(String s) {
		final String ACTIONABLE_DELIMITERS = " '-/"; // these cause the character following to be capitalized

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

    public static String viewWhiteSpaces(String s) {
        s = s.replace("\n", "\u00b6");
        s = s.replace(" ", "\u00b7");
        //s = s.replace("\t", "\u02c9");
        s = s.replace("\t", "\u2022");
        return s;
    }

    public static String upperize(String s) {
        return s.toUpperCase();
    }

    public static String lowerize(String s) {
        return s.toLowerCase();
    }

    public static String camelize(String s) {
        String[] strings = s.toLowerCase().split("[\\s_\\-]");
        for (int i = 0; i < strings.length; i++){
            strings[i] = StringUtils.capitalize(strings[i]);
        }
        return join("", strings);
    }

    private static final Map<String, String> TRANSLATIONS;

    static {
        String WHITESPACE_CHARS[] = new String[] {
                "\\u0009", // CHARACTER TABULATION
                "\\u000A", // LINE FEED (LF)
                "\\u000B", // LINE TABULATION
                "\\u000C", // FORM FEED (FF)
                "\\u000D", // CARRIAGE RETURN (CR)
                "\\u0020", // SPACE
                "\\u0085", // NEXT LINE (NEL)
                "\\u00A0", // NO-BREAK SPACE
                "\\u1680", // OGHAM SPACE MARK
                "\\u180E", // MONGOLIAN VOWEL SEPARATOR
                "\\u2000", // EN QUAD
                "\\u2001", // EM QUAD
                "\\u2002", // EN SPACE
                "\\u2003", // EM SPACE
                "\\u2004", // THREE-PER-EM SPACE
                "\\u2005", // FOUR-PER-EM SPACE
                "\\u2006", // SIX-PER-EM SPACE
                "\\u2007", // FIGURE SPACE
                "\\u2008", // PUNCTUATION SPACE
                "\\u2009", // THIN SPACE
                "\\u200A", // HAIR SPACE
                "\\u2028", // LINE SEPARATOR
                "\\u2029", // PARAGRAPH SEPARATOR
                "\\u202F", // NARROW NO-BREAK SPACE
                "\\u205F", // MEDIUM MATHEMATICAL SPACE
                "\\u3000" // IDEOGRAPHIC SPACE
        };

        TRANSLATIONS = createMap("\u00c0", "A", "\u00c1", "A", "\u00c2", "A", "\u00c3", "A", "\u00c4", "A", "\u00c5", "A", "\u00c6", "AE", "\u00c7", "C", "\u00c8", "E", "\u00c9", "E",
                "\u00ca", "E", "\u00cb", "E", "\u00cc", "I", "\u00cd", "I", "\u00ce", "I", "\u00cf", "I", "\u00d0", "", "\u00d1", "N", "\u00d2", "O", "\u00d3", "O",
                "\u00d4", "O", "\u00d5", "O", "\u00d6", "O", "\u00d8", "", "\u00d9", "U", "\u00da", "U", "\u00db", "U", "\u00dc", "U", "\u00dd", "Y", "\u00de", "",
                "\u00df", "", "\u00e0", "a", "\u00e1", "a", "\u00e2", "a", "\u00e3", "a", "\u00e4", "a", "\u00e5", "a", "\u00e6", "ae", "\u00e7", "c", "\u00e8", "e",
                "\u00e9", "e", "\u00ea", "e", "\u00eb", "e", "\u00ec", "i", "\u00ed", "i", "\u00ee", "i", "\u00ef", "i", "\u00f0", "", "\u00f1", "n", "\u00f2", "o",
                "\u00f3", "o", "\u00f4", "o", "\u00f5", "o", "\u00f6", "o", "\u00f8", "", "\u00f9", "u", "\u00fa", "u", "\u00fb", "u", "\u00fc", "u", "\u00fd", "y",
                "\u00fe", "", "\u00ff", "y", "\u0100", "A", "\u0101", "a", "\u0102", "A", "\u0103", "a", "\u0104", "A", "\u0105", "a", "\u0106", "C", "\u0107", "c",
                "\u0108", "C", "\u0109", "c", "\u010a", "C", "\u010b", "c", "\u010c", "C", "\u010d", "c", "\u010e", "D", "\u010f", "d", "\u0110", "", "\u0111", "",
                "\u0112", "E", "\u0113", "e", "\u0114", "E", "\u0115", "e", "\u0116", "E", "\u0117", "e", "\u0118", "E", "\u0119", "e", "\u011a", "E", "\u011b", "e",
                "\u011c", "G", "\u011d", "g", "\u011e", "G", "\u011f", "g", "\u0120", "G", "\u0121", "g", "\u0122", "G", "\u0123", "g", "\u0124", "H", "\u0125", "h",
                "\u0126", "", "\u0127", "", "\u0128", "I", "\u0129", "i", "\u012a", "I", "\u012b", "i", "\u012c", "I", "\u012d", "i", "\u012e", "I", "\u012f", "i",
                "\u0130", "I", "\u0131", "", "\u0132", "IJ", "\u0133", "ij", "\u0134", "J", "\u0135", "j", "\u0136", "K", "\u0137", "k", "\u0138", "", "\u0139", "L",
                "\u013a", "l", "\u013b", "L", "\u013c", "l", "\u013d", "L", "\u013e", "l", "\u013f", "L", "\u0140", "l", "\u0141", "", "\u0142", "", "\u0143", "N",
                "\u0144", "n", "\u0145", "N", "\u0146", "n", "\u0147", "N", "\u0148", "n", "\u0149", "n", "\u014a", "", "\u014b", "", "\u014c", "O", "\u014d", "o",
                "\u014e", "O", "\u014f", "o", "\u0150", "O", "\u0151", "o", "\u0152", "", "\u0153", "", "\u0154", "R", "\u0155", "r", "\u0156", "R", "\u0157", "r",
                "\u0158", "R", "\u0159", "r", "\u015a", "S", "\u015b", "s", "\u015c", "S", "\u015d", "s", "\u015e", "S", "\u015f", "s", "\u0160", "S", "\u0161", "s",
                "\u0162", "T", "\u0163", "t", "\u0164", "T", "\u0165", "t", "\u0166", "", "\u0167", "", "\u0168", "U", "\u0169", "u", "\u016a", "U", "\u016b", "u",
                "\u016c", "U", "\u016d", "u", "\u016e", "U", "\u016f", "u", "\u0170", "U", "\u0171", "u", "\u0172", "U", "\u0173", "u", "\u0174", "W", "\u0175", "w",
                "\u0176", "Y", "\u0177", "y", "\u0178", "Y", "\u0179", "Z", "\u017a", "z", "\u017b", "Z", "\u017c", "z", "\u017d", "Z", "\u017e", "z", "\u017f", "s",
                "\u0180", "", "\u0181", "", "\u0182", "", "\u0183", "", "\u0184", "", "\u0185", "", "\u0186", "", "\u0187", "", "\u0188", "", "\u0189", "", "\u018a", "",
                "\u018b", "", "\u018c", "", "\u018d", "", "\u018e", "", "\u018f", "", "\u0190", "", "\u0191", "", "\u0192", "", "\u0193", "", "\u0194", "", "\u0195", "",
                "\u0196", "", "\u0197", "", "\u0198", "", "\u0199", "", "\u019a", "", "\u019b", "", "\u019c", "", "\u019d", "", "\u019e", "", "\u019f", "", "\u01a0", "O",
                "\u01a1", "o", "\u01a2", "", "\u01a3", "", "\u01a4", "", "\u01a5", "", "\u01a6", "", "\u01a7", "", "\u01a8", "", "\u01a9", "", "\u01aa", "", "\u01ab", "",
                "\u01ac", "", "\u01ad", "", "\u01ae", "", "\u01af", "U", "\u01b0", "u", "\u01b1", "", "\u01b2", "", "\u01b3", "", "\u01b4", "", "\u01b5", "", "\u01b6", "",
                "\u01b7", "", "\u01b8", "", "\u01b9", "", "\u01ba", "", "\u01bb", "", "\u01bc", "", "\u01bd", "", "\u01be", "", "\u01bf", "", "\u01c0", "", "\u01c1", "",
                "\u01c2", "", "\u01c3", "", "\u01c4", "DZ", "\u01c5", "Dz", "\u01c6", "dz", "\u01c7", "LJ", "\u01c8", "Lj", "\u01c9", "lj", "\u01ca", "NJ", "\u01cb", "Nj",
                "\u01cc", "nj", "\u01cd", "A", "\u01ce", "a", "\u01cf", "I", "\u01d0", "i", "\u01d1", "O", "\u01d2", "o", "\u01d3", "U", "\u01d4", "u", "\u01d5", "U",
                "\u01d6", "u", "\u01d7", "U", "\u01d8", "u", "\u01d9", "U", "\u01da", "u", "\u01db", "U", "\u01dc", "u", "\u01dd", "", "\u01de", "A", "\u01df", "a",
                "\u01e0", "A", "\u01e1", "a", "\u01e2", "AE", "\u01e3", "ae", "\u01e4", "G", "\u01e5", "g", "\u01e6", "G", "\u01e7", "g", "\u01e8", "K", "\u01e9", "k",
                "\u01ea", "O", "\u01eb", "o", "\u01ec", "O", "\u01ed", "o", "\u01ee", "", "\u01ef", "", "\u01f0", "j", "\u01f1", "DZ", "\u01f2", "Dz", "\u01f3", "dz",
                "\u01f4", "G", "\u01f5", "g", "\u01f6", "", "\u01f7", "", "\u01f8", "N", "\u01f9", "n", "\u01fa", "A", "\u01fb", "a", "\u01fc", "AE", "\u01fd", "ae",
                "\u01fe", "", "\u01ff", "", "\u0200", "A", "\u0201", "a", "\u0202", "A", "\u0203", "a", "\u0204", "E", "\u0205", "e", "\u0206", "E", "\u0207", "e",
                "\u0208", "I", "\u0209", "i", "\u020a", "I", "\u020b", "i", "\u020c", "O", "\u020d", "o", "\u020e", "O", "\u020f", "o", "\u0210", "R", "\u0211", "r",
                "\u0212", "R", "\u0213", "r", "\u0214", "U", "\u0215", "u", "\u0216", "U", "\u0217", "u", "\u0218", "S", "\u0219", "s", "\u021a", "T", "\u021b", "t",
                "\u021c", "", "\u021d", "", "\u021e", "H", "\u021f", "h", "\u0220", "", "\u0221", "", "\u0222", "", "\u0223", "", "\u0224", "", "\u0225", "", "\u0226", "A",
                "\u0227", "a", "\u0228", "E", "\u0229", "e", "\u022a", "O", "\u022b", "o", "\u022c", "O", "\u022d", "o", "\u022e", "O", "\u022f", "o", "\u0230", "O",
                "\u0231", "o", "\u0232", "Y", "\u0233", "y", "\u0234", "", "\u0235", "", "\u0236", "", "\u0237", "", "\u0238", "", "\u0239", "", "\u023a", "", "\u023b", "",
                "\u023c", "", "\u023d", "", "\u023e", "", "\u023f", "", "\u0240", "", "\u0241", "", "\u0242", "", "\u0243", "", "\u0244", "", "\u0245", "", "\u0246", "",
                "\u0247", "", "\u0248", "", "\u0249", "", "\u024a", "", "\u024b", "", "\u024c", "", "\u024d", "", "\u024e", "", "\u024f", "",
                "\u20ac", "eur"
                /*,
                "a", "a", "b", "b", "c", "c", "d", "d", "e", "e", "f", "f", "g", "g", "h", "h", "i", "i",
                "j", "j", "k", "k", "l", "l", "m", "m", "n", "n", "o", "o", "p", "p", "q", "q", "r", "r", "s", "s", "t", "t",
                "u", "u", "v", "v", "w", "w", "x", "x", "y", "y", "z", "z",
                "A", "A", "B", "B", "C", "C", "D", "D", "E", "E", "F", "F", "G", "G", "H", "H", "I", "I",
                "J", "J", "K", "K", "L", "L", "M", "M", "N", "N", "O", "O", "P", "P", "Q", "Q", "R", "R", "S", "S", "T", "T",
                "U", "U", "V", "V", "W", "W", "X", "X", "Y", "Y", "Z", "Z",
                "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
                " ", " ", "_", "_"*/
        );
       /* for (String space : WHITESPACE_CHARS) {
            TRANSLATIONS.put(space, " ");
        } */
    }

    private static String translate(String s) {
        StringBuilder sb = new StringBuilder("");
        for (int i=0; i<s.length(); i++) {
            String c = String.valueOf(s.charAt(i));
            if (TRANSLATIONS.containsKey(c)) {
                sb.append(TRANSLATIONS.get(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }


    public static String unaccentify(String s) {
        return translate(s);
    }

    private static Map<String, String> createMap(String ... whatWith) {
        if (whatWith.length %2 != 0) {
            throw new RuntimeException("Key/value");
        }
        Map<String, String> model = new HashMap<String, String>();
        for (int i=0; i<whatWith.length/2; i++) {
            model.put(whatWith[2*i], whatWith[2*i+1]);
        }
        return  model;
    }

    public static String constantify(String s) {
        String r = unaccentify(s).toUpperCase();
        StringBuilder sb = new StringBuilder("");
        boolean wasSpace = false;
        for (int i=0; i<r.length(); i++) {
            int c = r.codePointAt(i);
            if (Character.isLetterOrDigit(c)) {
                sb.appendCodePoint(c);
                wasSpace = false;
            } else if (Character.isWhitespace(c)) {
                if (! wasSpace)
                    sb.append("_");
                wasSpace = true;
            }
        }
        return sb.toString();
    }

    public static String slugify(String s) {
        String r = unaccentify(s).toLowerCase();
        StringBuilder sb = new StringBuilder("");
        boolean wasSpace = false;
        for (int i=0; i<r.length(); i++) {
            int c = r.codePointAt(i);
            if (Character.isLetterOrDigit(c)) {
                sb.appendCodePoint(c);
                wasSpace = false;
            } else if (Character.isWhitespace(c)) {
                if (! wasSpace)
                    sb.append("-");
                wasSpace = true;
            } else if (c == '\u002d' || c == '\u005f') {
                sb.append("-");
            }
        }
        return sb.toString();
    }
}
