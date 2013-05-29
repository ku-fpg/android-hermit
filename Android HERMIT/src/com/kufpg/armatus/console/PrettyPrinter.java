package com.kufpg.armatus.console;

import android.text.Html;
import android.widget.TextView;

import com.kufpg.armatus.console.CommandDispatcher;

/**
 * Colors ConsoleEntry text depending on its usage of Keywords.
 */
public class PrettyPrinter {
	public static final String RED = "#CC060B";
	public static final String GREEN = "#1DDA1C";
	public static final String BLUE = "#0090D3";
	public static final String GRAY = "#969696";
	public static final String PURPLE = "#7300FB";
	public static final String YELLOW = "#FDFD0D";
	
	public static void setPrettyText(TextView tv, String text) {
		String res = "";
		//Make sure to sanitize string for HTML parsing
		String[] sentence = //TextUtils.htmlEncode(text) ;; Disable this for now; it breaks newlines
				text.split(ConsoleActivity.WHITESPACE);
		for (String word : sentence) {
			String color = null;
			if (CommandDispatcher.isKeyword(word)) {
				color = CommandDispatcher.getKeyword(word).getColor();
				res += "<font color='" + color + "'>" +	word + "</font> ";
			} else {
				res += word + " ";
			}
		};
		res.trim();
		tv.setText(Html.fromHtml(res));
	}
}