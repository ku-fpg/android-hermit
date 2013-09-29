package edu.kufpg.armatus.console;

import edu.kufpg.armatus.R;
import edu.kufpg.armatus.util.StringUtils;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

/**
 * Used in {@link ConsoleActivity} to display console entries. This class defines special
 * {@link AdapterView.OnItemClickListener#onItemClick(AdapterView, View, int, long)
 * onItemClick(AdapterView, View, int, long)} and {@link ActionMode} behavior.
 */
public class ConsoleListView extends ListView {
	/** Reference to the current console. */
	private ConsoleActivity mConsole;

	/** Reference to the {@link ListView}'s action mode (if visible). */
	private ActionMode mActionMode;

	/** Reference to {@link #mActionMode}'s callback (if visible). */
	private ActionModeCallback mCallback;

	/** Reference to the {@link MenuItem} that allows for swapping  {@link ConsoleEntry}
	 * keywords. */
	private MenuItem mSwapItem;

	private MenuItem mTransformItem;

	/** Tracks which {@link ConsoleEntry ConsoleEntries} are currently checked, since
	 * {@link android.widget.AbsListView#CHOICE_MODE_MULTIPLE CHOICE_MODE_MULTIPLE}'s
	 * checking behavior is not desirable. */
	private SparseBooleanArray mPrevCheckedStates = new SparseBooleanArray();

	/** Tracks if {@link #mActionMode} is visible. */
	private boolean mActionModeVisible = false;

	public ConsoleListView(Context context) {
		super(context);
		init(context);
	}

	public ConsoleListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public ConsoleListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	/**
	 * Initializes variables and sets special
	 * {@link AdapterView.OnItemClickListener#onItemClick(AdapterView, View, int, long)
	 * onItemClick(AdapterView, View, int, long)} behavior.
	 * @param context The {@link Context} to use.
	 */
	private void init(Context context) {
		mConsole = (ConsoleActivity) context;
		mCallback = new ActionModeCallback();
		setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (position != getCount() - 1) {
					boolean showActionMode = true;
					if (mPrevCheckedStates.get(position) == true) {
						mPrevCheckedStates.delete(position);
					} else {
						mPrevCheckedStates.put(position, true);
					}
					if (mPrevCheckedStates.size() == 0) {
						showActionMode = false;
					}
					setActionModeVisible(showActionMode);
					refreshActionMode();
				}
			}
		});
	}

	@Override
	public Parcelable onSaveInstanceState() {
		Parcelable superState = super.onSaveInstanceState();
		SavedState ss = new SavedState(superState);
		ss.checkedStates = mPrevCheckedStates;
		return ss;
	}

	@Override
	public void onRestoreInstanceState(Parcelable state) {
		if (!(state instanceof SavedState)) {
			super.onRestoreInstanceState(state);
		}

		SavedState ss = (SavedState) state;
		super.onRestoreInstanceState(ss.getSuperState());
		mPrevCheckedStates = ss.checkedStates;
		if (mPrevCheckedStates.size() > 0) {
			setActionModeVisible(true);
			for (int i = 0; i < mPrevCheckedStates.size(); i++) {
				setItemChecked(mPrevCheckedStates.keyAt(i), true);
			}
		}
		refreshActionMode();
	}

	/**
	 * Shows or hides the {@link ListView}'s {@link ActionMode}.
	 * @param visible {@code true} if the {@code ActionMode} should be shown,
	 * {@code false} if it should be hidden.
	 */
	public void setActionModeVisible(boolean visible) {
		if (visible && !mActionModeVisible) {
			mActionMode = startActionMode(mCallback);
		} else if (!visible && mActionModeVisible) {
			mActionMode.finish();
		}
	}

	/**
	 * Returns whether a particular {@link ConsoleEntry} is currently shown to the
	 * user on-screen.
	 * @param entryIndex The index of the entry to look up.
	 * @return {@code true} if the entry is currently visible to the user.
	 */
	public boolean isEntryVisible(int entryIndex) {
		return getFirstVisiblePosition() <= entryIndex && entryIndex <= getLastVisiblePosition();
	}

	/**
	 * Returns whether the the {@link ListView}'s {@link ActionMode} is visible.
	 * @return {@code true} if the {@code ActionMode} is visible.
	 */
	public boolean isActionModeVisible() {
		return mActionModeVisible;
	}

	private void refreshActionMode() {
		if (mActionModeVisible) {
			switch (mPrevCheckedStates.size()) {
			case 1:
				mActionMode.setSubtitle("One entry selected");
				mSwapItem.setVisible(true);
				mTransformItem.setVisible(true);
				break;
			default:
				mActionMode.setSubtitle(mPrevCheckedStates.size() + " entries selected");
				mSwapItem.setVisible(false);
				mTransformItem.setVisible(false);
				break;
			}
		}
	}

	/**
	 * Defines the behavior of {@code ConsoleListView}'s {@link ActionMode} callback,
	 * such as item click behavior and subtitle updating.
	 */
	private class ActionModeCallback implements Callback {
		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.console_list_view_action_mode, menu);
			mode.setTitle("Select entries");
			mSwapItem = menu.findItem(R.id.console_list_view_swap);
			mTransformItem = menu.findItem(R.id.console_list_view_transform);
			return true;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			mActionModeVisible = true;
			return true;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			switch (item.getItemId()) {
			case R.id.console_list_view_copy:
				StringBuilder copyBuilder = new StringBuilder();
				for (int i = 0; i < mPrevCheckedStates.size(); i++) {
					copyBuilder.append(((ConsoleEntry) getItemAtPosition(mPrevCheckedStates.keyAt(i)))
							.getFullContents()).append('\n');
				}
				copyBuilder.deleteCharAt(copyBuilder.length() - 1); //Remove final newline
				ClipboardManager clipboard = (ClipboardManager) mConsole.getSystemService(Context.CLIPBOARD_SERVICE);
				ClipData copiedText = ClipData.newPlainText("copiedText",
						StringUtils.withoutCharWrap(copyBuilder.toString()));
				clipboard.setPrimaryClip(copiedText);
				mConsole.showToast((mPrevCheckedStates.size() == 1 ? "Entry" : "Entries") + " copied to clipboard!");
				mode.finish();
				return true;
			case R.id.console_list_view_select:
				int[] checkedEntries = new int[mPrevCheckedStates.size()];
				for (int i = 0; i < mPrevCheckedStates.size(); i++) {
					checkedEntries[i] = mPrevCheckedStates.keyAt(i);
				}
				mConsole.showEntrySelectionDialog(checkedEntries);
				mode.finish();
				return true;
			case R.id.console_list_view_swap:
				if (mPrevCheckedStates.size() == 1) {
					ConsoleEntry entry = (ConsoleEntry) getItemAtPosition(mPrevCheckedStates.keyAt(0));
					if (entry.getShortContents().toString().split(StringUtils.WHITESPACE).length > 1) {
						mConsole.showKeywordSwapDialog(entry.getEntryNum(), entry.getShortContents().toString());
					}
					mode.finish();
				}
				return true;
			case R.id.console_list_view_transform:
				if (mPrevCheckedStates.size() == 1) {
					ConsoleEntry entry = (ConsoleEntry) getItemAtPosition(mPrevCheckedStates.keyAt(0));
					if (entry.getShortContents().toString().split(StringUtils.WHITESPACE).length > 1) {
						mConsole.showEntryTransformDialog(entry);
					}
					mode.finish();
				}
				return true;
			}
			return false;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			clearChoices();
			requestLayout();
			mPrevCheckedStates.clear();
			mActionModeVisible = false;
		}

	}

	protected static class SavedState extends BaseSavedState {
		SparseBooleanArray checkedStates;

		SavedState(Parcelable superState) {
			super(superState);
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			super.writeToParcel(dest, flags);
			dest.writeSparseBooleanArray(checkedStates);
		}

		public static final Parcelable.Creator<SavedState> CREATOR
		= new Parcelable.Creator<SavedState>() {
			public SavedState createFromParcel(Parcel in) {
				return new SavedState(in);
			}

			public SavedState[] newArray(int size) {
				return new SavedState[size];
			}
		};

		private SavedState(Parcel in) {
			super(in);
			checkedStates = in.readSparseBooleanArray();
		}
	}

}
