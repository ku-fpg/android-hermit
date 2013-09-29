package edu.kufpg.armatus.util;

import android.text.Editable;

/**
 * Utility class containing methods useful for string analysis and transformation.
 */
public class StringUtils {

	/**
	 * A regular expression that matches several kinds of whitespace characters, including
	 * {@link #NBSP} and newlines.
	 */
	public static final String WHITESPACE = "\\s+";

	/**
	 * A non-breaking space string. Using this instead of a regular space string (" ") will
	 * prevent {@link android.widget.TextView TextViews} from applying their normal
	 * line-breaking behavior.
	 */
	public static final String NBSP = "\u00A0";

	/**
	 * A non-breaking space character. Using this instead of a regular space character (' ')
	 * will prevent {@link android.widget.TextView TextViews} from applying their normal
	 * line-breaking behavior.
	 */
	public static final char NBSP_CHAR = '\u00A0';

	private StringUtils() {}

	public static int countOccurrences(String haystack, String needle) {
		return haystack.length() - haystack.replace(needle, "").length();
	}

	public static int findFirstWordIndex(CharSequence sentence) {
		for (int i = 0; i < sentence.length(); i++) {
			if (!String.valueOf(sentence.charAt(i)).matches(WHITESPACE)) {
				return i;
			}
		}
		return 0;
	}

	public static int findLastWordIndex(CharSequence sentence) {
		boolean lastWordFound = false;
		for (int i = sentence.length() - 1; i >= 0; i--) {
			if (String.valueOf(sentence.charAt(i)).matches(WHITESPACE)) {
				if (lastWordFound) {
					return i + 1;
				}
			} else {
				lastWordFound = true;
			}
		}
		return 0;
	}

	/**
	 * Returns a string with all regular spaces replaced by non-breaking spaces.
	 * @param str The string to apply character wrap to.
	 * @return a string with all spaces replaced by {@link #NBSP}.
	 */
	public static String withCharWrap(String str) {
		return str.replace(" ", NBSP);
	}

	/**
	 * Returns an {@link Editable} with all regular spaces replaced by non-breaking spaces.
	 * @param editable The {@code Editable} object to apply character wrap to.
	 * @return an {@code Editable} object with all spaces replaced by {@link #NBSP}.
	 */
	public static Editable withCharWrap(Editable editable) {
		for (int i = 0; i < editable.length(); i++) {
			if (editable.charAt(i) == ' ') {
				editable.replace(i, i+1, NBSP);
			}
		}
		return editable;
	}

	/**
	 * Returns a string with all non-breaking spaces replaced by regular spaces.
	 * @param str The string from which character wrap should be removed.
	 * @return a string with all non-breaking spaces replaced by regular ones.
	 */
	public static String withoutCharWrap(String str) {
		return str.replace(NBSP, " ");
	}

	/**
	 * Returns an {@link Editable} with all non-breaking spaces replaced by regular spaces.
	 * @param str The {@code Editable} object from which character wrap should be removed.
	 * @return an {@code Editable} object with all non-breaking spaces replaced by regular
	 * ones.
	 */
	public static Editable withoutCharWrap(Editable editable) {
		for (int i = 0; i < editable.length(); i++) {
			if (editable.charAt(i) == NBSP_CHAR) {
				editable.replace(i, i+1, " ");
			}
		}
		return editable;
	}
	
	public static String withoutFirstLine(String str) {
		return str.substring(str.indexOf('\n') + 1);
	}

	public static String tightenSpacing(String str) {
		for (int i = 0; i < str.length(); i++) {
			while (i < str.length() - 1 && str.charAt(i) == '\n' && str.charAt(i+1) == '\n') {
				str = str.substring(0, i) + str.substring(i+1, str.length());
			}
		}
		return str.trim();
	}

	public static String trim(String str) {
		int start = 0, last = str.length() - 1;
		int end = last;
		while ((start <= end) && (str.charAt(start) <= ' ' || str.charAt(start) == NBSP_CHAR)) {
			start++;
		}
		while ((end >= start) && (str.charAt(end) <= ' ' || str.charAt(end) == NBSP_CHAR)) {
			end--;
		}
		if (start == 0 && end == last) {
			return str;
		}
		return str.substring(start, end + 1);
	}

}
