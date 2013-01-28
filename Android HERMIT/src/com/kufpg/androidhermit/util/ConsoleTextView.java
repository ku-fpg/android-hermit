package com.kufpg.androidhermit.util;

import java.io.Serializable;
import java.util.HashMap;

import com.kufpg.androidhermit.ConsoleActivity;
import com.kufpg.androidhermit.R;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.widget.TextView;

public class ConsoleTextView extends TextView implements Serializable {

	private static final long serialVersionUID = 492620301229198361L;

	public final static int DEFAULT_FONT_SIZE = 15;
	public final static int MAX_FONT_SIZE = 40;
	public final static int MIN_FONT_SIZE = 10;
	public final static String TYPEFACE = "fonts/DroidSansMonoDotted.ttf";

	private int mCommandOrderNum;

	public ConsoleTextView(Context context) {
		super(context);
		setupView(context, null, 0);
	}

	public ConsoleTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setupView(context, null, 0);
	}

	public ConsoleTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setupView(context, null, 0);
	}

	public ConsoleTextView(Context context, String msg, int cmdOrderNum) {
		super(context);
		setupView(context, msg, cmdOrderNum);
	}

	protected void setupView(Context context, String msg, int cmdOrderNum) {
		this.addTextChangedListener(new PrettyPrinter());
		Typeface mTypeface = Typeface.createFromAsset(context.getAssets(), TYPEFACE);

		this.setTypeface(mTypeface);
		this.setTextColor(Color.WHITE);
		this.setTextSize(DEFAULT_FONT_SIZE);
		this.setGravity(Gravity.BOTTOM);
		// TODO: Make a better ID system
		this.setId((int) System.currentTimeMillis());
		this.setText("hermit<" + cmdOrderNum + "> ");
		if (msg != null)
			this.append(msg);

		mCommandOrderNum = cmdOrderNum;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if(event.getAction() == MotionEvent.ACTION_DOWN)
		{
			setBackground(getResources().getDrawable(R.drawable.console_text_border));
		}
		else if(event.getAction() == MotionEvent.ACTION_UP)
		{
			setBackgroundColor(Color.parseColor("#80000000"));
		}
		return super.onTouchEvent(event);	
	}

	public class PrettyPrinter implements TextWatcher {
		public static final String RED = "red";
		public static final String BLUE = "blue";
		public static final String GREEN = "green";
		
		private HashMap<String,String> mKeywordMap = new HashMap<String, String> ();
		private String lastText = null;

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			if (!s.toString().equals(lastText)) {
				lastText = s.toString();

				String res = "";
				//Make sure to sanitize string for HTML parsing
				String[] sentence = TextUtils.htmlEncode(s.toString()).split(" ");
				for (String word : sentence) {
					String color = null;
					if (word.equals(RED)) {
						color = "#CC060B";
					} else if (word.equals(GREEN)) {
						color = "#1DDA1C";
					} else if (word.equals(BLUE)) {
						color = "#0090D3";
					}

					if (color != null) {
						res += "<font color='" + color + "'>" +
								//"<a href='console://test' style='text-decoration:none;'>" +
								//In the future, the above line could be used to hyperlink commands
								word + //"</a>
								"</font> ";
					} else {
						res += word + " ";
					}
				};
				ConsoleTextView.this.setText(Html.fromHtml(res));
			}
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

		@Override
		public void afterTextChanged(Editable s) {}

		public boolean isKeyword(String query) {
			return mKeywordMap.containsKey(query);
		}
	}

}
