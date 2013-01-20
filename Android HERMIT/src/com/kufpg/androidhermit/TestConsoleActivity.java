package com.kufpg.androidhermit;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import com.kufpg.androidhermit.util.CommandDispatcher;
import com.kufpg.androidhermit.util.ConsoleTextView;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;

public class TestConsoleActivity extends StandardActivity {

	private RelativeLayout mRr;
	private LayoutParams mLp;
	private ScrollView mSv;
	private EditText mEt;
	private TextView mTv;
	private ConsoleTextView mCtv;
	private View recent = null;

	private LinkedHashMap<Integer, ConsoleTextView> mCmdHistory = new LinkedHashMap<Integer, ConsoleTextView>();
	private int mCmdCount = 0;
	private CommandDispatcher mDispatcher;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.test_console);

		mDispatcher = new CommandDispatcher(this);
		mSv = (ScrollView) findViewById(R.id.code_scroll_view);
		mRr = (RelativeLayout) findViewById(R.id.code_scroll_relative_layout);
		mTv = (TextView) findViewById(R.id.code_command_num);
		mTv.setText("hermit<" + mCmdCount + "> ");
		mEt = (EditText) findViewById(R.id.code_input_box);
		mEt.setOnKeyListener(new EditText.OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_ENTER
						&& event.getAction() == KeyEvent.ACTION_UP) {
					String[] inputs = mEt.getText().toString().split(" ");
					if(mDispatcher.isCommand(inputs[0])) {
						if(inputs.length == 1) {
							mDispatcher.execute(inputs[0]);
						} else {
							mDispatcher.execute(inputs[0], Arrays.copyOfRange
									(inputs, 1, inputs.length));
						}
					} else {
						addMessage(mEt.getText().toString());
					}
					mEt.setText(""); 
					return true;
				}
				return false;
			}
		});

		Typeface mTypeface = Typeface.createFromAsset(getAssets(), ConsoleTextView.TYPEFACE);
		mEt.setTypeface(mTypeface);
		mTv.setTypeface(mTypeface);
	}

	@Override
	public void onRestart() {
		super.onRestart();
		//Since onRestoreInstanceState() isn't called when
		//app sleeps or loses focus
		refreshConsole(mCmdHistory);
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putInt("CmdCount", mCmdCount);
		savedInstanceState.putSerializable("CmdHistory", mCmdHistory);
		mRr.removeAllViews();
		recent = null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		mCmdCount = savedInstanceState.getInt("CmdCount");
		mCmdHistory = (LinkedHashMap<Integer, ConsoleTextView>) savedInstanceState.getSerializable("CmdHistory");
		refreshConsole(mCmdHistory);
	}

	public void clear() {
		mRr.removeAllViews();
		mCmdCount = 0;
		mCmdHistory.clear();
		recent = null;
		mTv.setText("hermit<" + mCmdCount + "> ");
	}
	
	public void exit() {
		this.finish();
		startActivity(new Intent(this, MainActivity.class));
	}

	/**
	 * Adds a new "line" to the console with msg as its contents.
	 * @param msg
	 */
	public void addMessage(String msg) {
		mCtv = new ConsoleTextView(TestConsoleActivity.this, msg, mCmdCount);
		mCmdHistory.put(mCtv.getId(), mCtv);
		mCmdCount++;

		mLp = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		if (recent != null)
			mLp.addRule(RelativeLayout.BELOW, recent.getId());
		mRr.addView(mCtv, mLp);

		mSv.post(new Runnable() {
			public void run() {
				mSv.smoothScrollTo(0, mCtv.getBottom());
			}
		});
		recent = mCtv;
		mTv.setText("hermit<" + mCmdCount + "> ");
	}

	/**
	 * Similar to addMessage(String), but you can add an already built ConsoleTextView as an argument.
	 * Useful for when you have to rotate the screen and reconstruct the console buffer.
	 * @param ctv
	 */
	public void addTextView(final ConsoleTextView ctv) {
		if (!mCmdHistory.containsKey(ctv.getId())) {
			mCmdHistory.put(ctv.getId(), ctv);
			mCmdCount++;
		}

		mLp = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		if (recent != null)
			mLp.addRule(RelativeLayout.BELOW, recent.getId());
		mRr.addView(ctv, mLp);

		mSv.post(new Runnable() {
			public void run() {
				mSv.smoothScrollTo(0, ctv.getBottom());
			}
		});
		recent = ctv;
		mTv.setText("hermit<" + mCmdCount + "> ");
	}

	/**
	 * Re-adds all of the ConsoleTextViews in conjunction with onRestart() and
	 * onRestoreInstanceState(Bundle).
	 * @param cmdHistory Pass as argument, since mCmdHistory could have been
	 * destroyed.
	 */
	private void refreshConsole(LinkedHashMap<Integer,ConsoleTextView> cmdHistory) {
		mRr.removeAllViews();
		recent = null;
		for (Entry<Integer, ConsoleTextView> entry : cmdHistory.entrySet()) {
			addTextView(entry.getValue());
		}
	}

}
