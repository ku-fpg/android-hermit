package com.kufpg.androidhermit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import com.kufpg.androidhermit.util.CommandDispatcher;
import com.kufpg.androidhermit.util.ConsoleTextView;
import com.kufpg.androidhermit.util.drag.DragImageView;
import com.kufpg.androidhermit.util.drag.DragImageViewLayout;
import com.kufpg.androidhermit.util.drag.DragSinkListener;
import com.slidingmenu.lib.SlidingMenu;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnKeyListener;
import android.view.View.OnLongClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;

public class ConsoleActivity extends StandardActivity {

	public static final int DEFAULT_FONT_SIZE = 15;
	public static final int MAX_FONT_SIZE = 40;
	public static final int MIN_FONT_SIZE = 15;
	public static final int PADDING = 5;
	public static final String TYPEFACE = "fonts/DroidSansMonoDotted.ttf";
	public static final String WHITESPACE = "\\s+";

	private RelativeLayout mCodeLayout;
	private LayoutParams mCodeLayoutParams;
	private View mRootView;
	private SlidingMenu mSlidingMenu;
	private ScrollView mScrollView;
	private EditText mInputEditText;
	private TextView mInputHeader;
	private ConsoleTextView mCurConsoleTextView;
	private ConsoleTextView mPrevConsoleTextView = null;
	private String mTempCommand = null;
	private ArrayList<String> mTempKeywords = new ArrayList<String>();
	private LinkedHashMap<Integer, ConsoleTextView> mCommandHistory = new LinkedHashMap<Integer, ConsoleTextView>();
	private CommandDispatcher mDispatcher;
	private int mCommandCount = 0;
	private boolean mIsSoftKeyboardVisible;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.console);
		setSoftKeyboardVisibility(mIsSoftKeyboardVisible = true);

		mRootView = ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0);
		mRootView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				int rootHeight = mRootView.getRootView().getHeight();
				int heightDiff = rootHeight - mRootView.getHeight();
				if (heightDiff > rootHeight/3) { //This works on Nexus 7s, at the very least
					mIsSoftKeyboardVisible = true;
				} else {
					mIsSoftKeyboardVisible = false;
				}
			}
		});

		mDispatcher = new CommandDispatcher(this);
		mScrollView = (ScrollView) findViewById(R.id.code_scroll_view);
		mCodeLayout = (RelativeLayout) findViewById(R.id.code_scroll_relative_layout);
		mInputHeader = (TextView) findViewById(R.id.code_command_num);
		mInputHeader.setText("hermit<" + mCommandCount + "> ");
		mInputEditText = (EditText) findViewById(R.id.code_input_box);
		mInputEditText.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_ENTER
						&& event.getAction() == KeyEvent.ACTION_UP) {
					String[] inputs = mInputEditText.getText().
							toString().trim().split(WHITESPACE);
					if (CommandDispatcher.isCommand(inputs[0])) {
						if (inputs.length == 1) {
							mDispatcher.runOnConsole(inputs[0]);
						} else {
							mDispatcher.runOnConsole(inputs[0], Arrays.copyOfRange
									(inputs, 1, inputs.length));
						}
					} else {
						addMessage(mInputEditText.getText().toString());
					}
					mInputEditText.setText(""); 
					return true;
				}
				return false;
			}
		});

		//Initialize SlidingMenu properties
		mSlidingMenu = new SlidingMenu(this);
		mSlidingMenu.setMode(SlidingMenu.LEFT_RIGHT);
		mSlidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		mSlidingMenu.setFadeDegree(0.35f);
		mSlidingMenu.setShadowWidthRes(R.dimen.shadow_width);
		mSlidingMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		mSlidingMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
		mSlidingMenu.setMenu(R.layout.drag_n_drop);
		mSlidingMenu.setSecondaryMenu(R.layout.drag_n_drop);

		//This process has to be done twice since the layout is inflated twice. Dumb, but necessary.
		((DragImageViewLayout) mSlidingMenu.getMenu().findViewById(R.id.topleft)).setSlidingMenu(mSlidingMenu);
		((DragImageViewLayout) mSlidingMenu.getMenu().findViewById(R.id.topright)).setSlidingMenu(mSlidingMenu);
		((DragImageViewLayout) mSlidingMenu.getMenu().findViewById(R.id.bottomleft)).setSlidingMenu(mSlidingMenu);
		((DragImageViewLayout) mSlidingMenu.getMenu().findViewById(R.id.bottomright)).setSlidingMenu(mSlidingMenu);
		((DragImageViewLayout) mSlidingMenu.getSecondaryMenu().findViewById(R.id.topleft)).setSlidingMenu(mSlidingMenu);
		((DragImageViewLayout) mSlidingMenu.getSecondaryMenu().findViewById(R.id.topright)).setSlidingMenu(mSlidingMenu);
		((DragImageViewLayout) mSlidingMenu.getSecondaryMenu().findViewById(R.id.bottomleft)).setSlidingMenu(mSlidingMenu);
		((DragImageViewLayout) mSlidingMenu.getSecondaryMenu().findViewById(R.id.bottomright)).setSlidingMenu(mSlidingMenu);

		Typeface typeface = Typeface.createFromAsset(getAssets(), TYPEFACE);
		mInputEditText.setTypeface(typeface);
		mInputHeader.setTypeface(typeface);
	}

	@Override
	public void onRestart() {
		super.onRestart();
		//Since onRestoreInstanceState() isn't called when
		//app sleeps or loses focus
		refreshConsole(mCommandHistory);
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putInt("CmdCount", mCommandCount);
		savedInstanceState.putSerializable("CmdHistory", mCommandHistory);
		savedInstanceState.putBoolean("SoftKeyboardVisibility", mIsSoftKeyboardVisible);
		mCodeLayout.removeAllViews();
		mPrevConsoleTextView = null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		mCommandCount = savedInstanceState.getInt("CmdCount");
		mCommandHistory = (LinkedHashMap<Integer, ConsoleTextView>) savedInstanceState.getSerializable("CmdHistory");
		mIsSoftKeyboardVisible = savedInstanceState.getBoolean("SoftKeyboardVisibility");
		refreshConsole(mCommandHistory);
		setSoftKeyboardVisibility(mIsSoftKeyboardVisible);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		if (mTempCommand != null) {
			menu.setHeaderTitle("Execute " + mTempCommand + " on...");
		} else {
			menu.setHeaderTitle(R.string.context_menu_title);
		}
		int order = 0;
		for (String keyword : mTempKeywords) {
			menu.add(0, v.getId(), order, keyword);
			order++;
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if (item != null) {
			String keywordNStr = item.getTitle().toString();
			if (mTempCommand != null) {
				mDispatcher.runOnConsole(mTempCommand, keywordNStr);
			} else {
				mDispatcher.runKeywordCommand(keywordNStr, keywordNStr); 
			}
		}
		return super.onContextItemSelected(item);
	}

	public void clear() {
		mCodeLayout.removeAllViews();
		mCommandCount = 0;
		mCommandHistory.clear();
		mPrevConsoleTextView = null;
		mInputHeader.setText("hermit<" + mCommandCount + "> ");
	}

	public void exit() {
		finish();
		startActivity(new Intent(this, MainActivity.class));
	}

	/**
	 * Adds a new "line" to the console with msg as its contents.
	 * @param msg
	 */
	public void addMessage(String msg) {
		mCurConsoleTextView = new ConsoleTextView(ConsoleActivity.this, msg, mCommandCount);
		registerForContextMenu(mCurConsoleTextView);
		mCurConsoleTextView.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				mTempCommand = null;
				mTempKeywords = ((ConsoleTextView) v).getKeywords();
				if (!mTempKeywords.isEmpty()) {
					openContextMenu(v);
					return true;
				}
				return false;
			}	
		});
		mCurConsoleTextView.setOnDragListener(new DragSinkListener() {
			@Override
			public void onDragEntered(View dragView, View dragSink) {
				dragSink.setBackground(getResources().getDrawable(R.drawable.console_text_border));
			}

			@Override
			public void onDragExited(View dragView, View dragSink) {
				dragSink.setBackgroundColor(getResources().getColor(android.R.color.transparent));
			}
			
			@Override
			public void onDragEnded(View dragView, View dragSink) {
				dragSink.setBackgroundColor(getResources().getColor(android.R.color.transparent));
			}

			@Override
			public void onDragDropped(View dragView, View dragSink) {
				mTempCommand =  ((DragImageView) dragView).getCommandName();
				mTempKeywords = ((ConsoleTextView) dragSink).getKeywords();
				if (!mTempKeywords.isEmpty())
					openContextMenu(dragSink);
			}
		});
		mCommandHistory.put(mCurConsoleTextView.getId(), mCurConsoleTextView);
		mCommandCount++;

		mCodeLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		if (mPrevConsoleTextView != null)
			mCodeLayoutParams.addRule(RelativeLayout.BELOW, mPrevConsoleTextView.getId());
		mCodeLayout.addView(mCurConsoleTextView, mCodeLayoutParams);

		mScrollView.post(new Runnable() {
			public void run() {
				mScrollView.smoothScrollTo(0, mCurConsoleTextView.getBottom());
			}
		});
		mPrevConsoleTextView = mCurConsoleTextView;
		mInputHeader.setText("hermit<" + mCommandCount + "> ");
	}

	/**
	 * Similar to addMessage(String), but you can add an already built ConsoleTextView as an argument.
	 * Useful for when you have to rotate the screen and reconstruct the console buffer. Note that
	 * this assumes that the ConsoleTextView has already been associated with an appropriate
	 * LayoutParams.
	 * @param ctv
	 */
	public void addTextView(final ConsoleTextView ctv) {
		if (!mCommandHistory.containsKey(ctv.getId())) {
			mCommandHistory.put(ctv.getId(), ctv);
			mCommandCount++;
		}
		mCodeLayout.addView(ctv);

		mScrollView.post(new Runnable() {
			public void run() {
				mScrollView.smoothScrollTo(0, ctv.getBottom());
			}
		});
		mPrevConsoleTextView = ctv;
		mInputHeader.setText("hermit<" + mCommandCount + "> ");
	}

	/**
	 * Re-adds all of the ConsoleTextViews in conjunction with onRestart() and
	 * onRestoreInstanceState(Bundle).
	 * @param cmdHistory Pass as argument, since mCmdHistory could have been
	 * destroyed.
	 */
	private void refreshConsole(LinkedHashMap<Integer,ConsoleTextView> cmdHistory) {
		mCodeLayout.removeAllViews();
		mPrevConsoleTextView = null;
		for (Entry<Integer, ConsoleTextView> entry : cmdHistory.entrySet()) {
			addTextView(entry.getValue());
		}
	}

	private void setSoftKeyboardVisibility(boolean visibility) {
		if (visibility) {
			getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		} else {
			getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		}
	}

}
