package com.kufpg.armatus.util;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;

public class StickyButton extends Button {
	private OnClickListener mOnClickListener;
	private boolean mIsStuck = false;
	private final ReentrantLock mLock = new ReentrantLock(true);
	private final Condition mLockInEffect = mLock.newCondition();

	public StickyButton(Context context) {
		super(context);
		init();
	}

	public StickyButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public StickyButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		super.setOnClickListener(new OnClickListener() {
			@Override
			public final void onClick(View v) {
				lock(true);
			}
		});
	}
	
	@Override
	public void setOnClickListener(OnClickListener listener) {
		mOnClickListener = listener;
	}

	@Override
	public Parcelable onSaveInstanceState() {
		Parcelable superState = super.onSaveInstanceState();
		SavedState ss = new SavedState(superState);
		ss.isStuck = isStuck();
		return ss;
	}

	@Override
	public void onRestoreInstanceState(Parcelable state) {
		if (!(state instanceof SavedState)) {
			super.onRestoreInstanceState(state);
		}
		
		SavedState ss = (SavedState) state;
		super.onRestoreInstanceState(ss.getSuperState());
		if (ss.isStuck) {
			lock(false);
		}
		
	}
	
	public boolean isStuck() {
		return mIsStuck;
	}

	public void unstick() {
		mLock.lock();
		try {
			if (mIsStuck) {
				mLockInEffect.signal();
				mIsStuck = false;
				setEnabled(true);
			}
		} finally {
			mLock.unlock();
		}
	}
	
	private void lock(boolean callListener) {
		mLock.lock();
		try {
			while (mIsStuck) {
				try {
					mLockInEffect.await();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			mIsStuck = true;
			setEnabled(false);
			if (callListener && mOnClickListener != null) {
				mOnClickListener.onClick(this);
			}
		} finally {
			mLock.unlock();
		}
	}

	protected static class SavedState extends BaseSavedState {
		boolean isStuck;

		SavedState(Parcelable superState) {
			super(superState);
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			super.writeToParcel(dest, flags);
			dest.writeInt(isStuck ? 1 : 0);
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
			isStuck = (in.readInt() != 0);
		}
	}

}
