package com.kufpg.armatus;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import pl.polidea.treeview.demo.TreeListViewDemo;

import com.kufpg.armatus.console.ConsoleActivity;
import com.kufpg.armatus.dialog.TerminalNotInstalledDialog;
import com.kufpg.armatus.test.TestActivity;
import com.kufpg.armatus.util.FileIOUtils;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends StandardActivity {

	private TextView mButtonsView;
	private Button mLockButton, mUnlockButton, mAsyncButton, mTreeButton, mConsoleButton,
	mPinchZoomButton, mTestActivityButton, mTerminalButton;
	private CheckBox mProgressCheckBox;
	private int mNumTextChanges = 0;
	private boolean mIsLocked = false;
	private final ReentrantLock mLock = new ReentrantLock(true);
	private final Condition mLockInEffect = mLock.newCondition();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);

		mButtonsView = (TextView) findViewById(R.id.code_text_view);
		setCodeText(mNumTextChanges, mIsLocked);
		mLockButton = (Button) findViewById(R.id.lock_button);
		mUnlockButton = (Button) findViewById(R.id.unlock_button);
		mAsyncButton = (Button) findViewById(R.id.async_button);
		mProgressCheckBox = (CheckBox) findViewById(R.id.progress_checkbox);
		mTreeButton = (Button) findViewById(R.id.tree_button);
		mConsoleButton = (Button) findViewById(R.id.console_button);
		mPinchZoomButton = (Button) findViewById(R.id.pinchzoom_button);
		mTestActivityButton = (Button) findViewById(R.id.test_activity_button);
		mTerminalButton = (Button) findViewById(R.id.terminal_activity_button);

		mLockButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mLock.lock();
				try {
					while (mIsLocked) {
						try {
							mLockInEffect.await();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					mIsLocked = true;
					mLockButton.setEnabled(false);
					mNumTextChanges++;
					setCodeText(mNumTextChanges, mIsLocked);
				} finally {
					mLock.unlock();
				}
			}
		});

		mUnlockButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mLock.lock();
				try {
					if (mIsLocked) {
						mLockInEffect.signal();
						mIsLocked = false;
						mLockButton.setEnabled(true);
						setCodeText(mNumTextChanges, mIsLocked);
					}
				} finally {
					mLock.unlock();
				}
			}
		});

		mAsyncButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast imgToast = new Toast(MainActivity.this);
				ImageView imgView = new ImageView(MainActivity.this);
				imgToast.setView(imgView);
				imgToast.setDuration(Toast.LENGTH_LONG);
				FileIOUtils
				.downloadImage(
						"http://3.bp.blogspot.com/-GYJu10jKqEw/Td2bEbUSzkI/AAAAAAAAAG0/m7t15oHOLWc/s1600/haskell-curry-says.png",
						imgView, mProgressCheckBox.isChecked(),
						MainActivity.this);
				imgToast.show();
			}
		});

		mTreeButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(MainActivity.this, TreeListViewDemo.class));
			}
		});

		mConsoleButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(MainActivity.this, ConsoleActivity.class));
			}
		});

		mPinchZoomButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(MainActivity.this, TextSizePinchZoomActivity.class));
			}
		});

		mTestActivityButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(MainActivity.this, TestActivity.class));
			}
		});

		mTerminalButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String packageName = "jackpal.androidterm";
				boolean installed = appInstalledOrNot(MainActivity.this, packageName);  
				if (installed) {
					Intent i = new Intent("jackpal.androidterm.RUN_SCRIPT");
					i.addCategory(Intent.CATEGORY_DEFAULT);
					i.putExtra("jackpal.androidterm.iInitialCommand", "echo 'Hello, Armatus!'");
					startActivity(i);
				} else {
					TerminalNotInstalledDialog tnid = new TerminalNotInstalledDialog();
					tnid.show(getFragmentManager(), "tnid");
				}
			}
		});

	}

	public static List<Integer> getIndexesInString(String searchable, String keyword) {
		List<Integer> indexesList = new ArrayList<Integer>();
		int index = searchable.indexOf(keyword);
		if (index != -1) {
			indexesList.add(index);
		}
		while (index >=0){
			index = searchable.indexOf(keyword, index+keyword.length());
			indexesList.add(index);
		}
		return indexesList;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		return true;
	}

	private void setCodeText(int numTextChanges, boolean isLocked) {
		mButtonsView.setText("Button pushed " + numTextChanges
				+ " times. (Status: " + (isLocked ? "locked" : "unlocked")
				+ ".)");
	}

}
