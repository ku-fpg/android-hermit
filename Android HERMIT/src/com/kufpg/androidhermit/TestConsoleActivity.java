package com.kufpg.androidhermit;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import com.kufpg.androidhermit.util.ConsoleTextView;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.ScrollView;

public class TestConsoleActivity extends StandardActivity {

	private RelativeLayout rr;
	private View recent = null;
	private LayoutParams lp;
	private ScrollView sv;
	private EditText et;

	private HashMap<Integer, ConsoleTextView> cmdHistory = new HashMap<Integer, ConsoleTextView>();
	private ConsoleTextView tv;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.test_console);

		sv = (ScrollView) findViewById(R.id.code_scroll_view);
		rr = (RelativeLayout) findViewById(R.id.code_scroll_relative_layout);
		et = (EditText) findViewById(R.id.code_input_box);
		et.setOnKeyListener(new EditText.OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_ENTER
						&& event.getAction() == KeyEvent.ACTION_UP) {
					addMessage("Time: " + System.currentTimeMillis());
					return true;
				}
				return false;
			}
		});
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putSerializable("cmdHistory", cmdHistory);
		Iterator<Entry<Integer, ConsoleTextView>> cmdIter = cmdHistory.entrySet().iterator();
		while (cmdIter.hasNext()) {
			Entry<Integer, ConsoleTextView> curEntry =(Entry<Integer, ConsoleTextView>) cmdIter.next();
			rr.removeView(curEntry.getValue());
		}
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		cmdHistory = (HashMap<Integer, ConsoleTextView>) savedInstanceState.getSerializable("cmdHistory");
		Iterator<Entry<Integer, ConsoleTextView>> cmdIter = cmdHistory.entrySet().iterator();
		while (cmdIter.hasNext()) {
			Entry<Integer, ConsoleTextView> curEntry = (Entry<Integer, ConsoleTextView>) cmdIter.next();
			addTextView(curEntry.getValue());
		}
	}

	/**
	 * Adds a new "line" to the console with msg as its contents.
	 * @param msg
	 */
	private void addMessage(String msg) {
		tv = new ConsoleTextView(TestConsoleActivity.this, msg);
		cmdHistory.put(tv.getId(), tv);

		lp = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		if (recent != null)
			lp.addRule(RelativeLayout.BELOW, recent.getId());
		rr.addView(tv, lp);

		sv.post(new Runnable() {
			public void run() {
				sv.smoothScrollTo(0, tv.getBottom());
			}
		});
		recent = tv;
	}

	/**
	 * Similar to addMessage(String), but you can add an already built ConsoleTextView as an argument.
	 * Useful for when you have to rotate the screen and reconstruct the console buffer.
	 * @param ctv
	 */
	private void addTextView(final ConsoleTextView ctv) {
		if (!cmdHistory.containsKey(ctv.getId())) {
			cmdHistory.put(ctv.getId(), ctv);
		}

		lp = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		if (recent != null)
			lp.addRule(RelativeLayout.BELOW, recent.getId());
		rr.addView(ctv, lp);

		sv.post(new Runnable() {
			public void run() {
				sv.smoothScrollTo(0, ctv.getBottom());
			}
		});
		recent = ctv;
	}

}
