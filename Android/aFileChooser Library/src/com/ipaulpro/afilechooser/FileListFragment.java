/* 
 * Copyright (C) 2012 Paul Burke
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */ 

package com.ipaulpro.afilechooser;

import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;

import java.io.File;
import java.util.List;

/**
 * Fragment that displays a list of Files in a given path.
 * 
 * @version 2012-10-28
 * 
 * @author paulburke (ipaulpro)
 * 
 */
public class FileListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<List<File>> {

	private static final int LOADER_ID = 0;
	private static final int ID_SELECT = 42;
	private static final int ID_GO_INTO = 43;

	private FileListAdapter mAdapter;
	private String mPath;

	/**
	 * Create a new instance with the given file path.
	 * 
	 * @param path The absolute path of the file (directory) to display.
	 * @return A new Fragment with the given file path. 
	 */
	public static FileListFragment newInstance(String path) {
		FileListFragment fragment = new FileListFragment();
		Bundle args = new Bundle();
		args.putString(FileChooserActivity.PATH, path);
		fragment.setArguments(args);

		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mAdapter = new FileListAdapter(getActivity());
		mPath = getArguments() != null ? getArguments().getString(
				FileChooserActivity.PATH) : Environment
				.getExternalStorageDirectory().getAbsolutePath();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		setEmptyText(getString(R.string.empty_directory));
		setHasOptionsMenu(true);
		setListAdapter(mAdapter);
		setListShown(false);
		getLoaderManager().initLoader(LOADER_ID, null, this);
		registerForContextMenu(getListView());

		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.action_bar, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.go_to_parent) {
			File file = new File(mPath);
			String fileParent = file.getParent();
			if (fileParent != null) {
				((FileChooserActivity) getActivity()).onFileSelected(new File(fileParent));
			}
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
		File file = null;
		String type = "";
		FileListAdapter adapter = (FileListAdapter) getListView().getAdapter();
		if (adapter != null) {
			file = (File) adapter.getItem(info.position);
		}
		if (file.isDirectory()) {
			type = "directory";
		} else {
			type = "file";
		}
		menu.setHeaderTitle("Choose action for this " + type + ':');
		menu.add(Menu.NONE, ID_SELECT, 0, "Select this " + type);
		if (file.isDirectory()) {
			menu.add(Menu.NONE, ID_GO_INTO, 1, "Go into this " + type);
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		FileListAdapter adapter = (FileListAdapter) getListView().getAdapter();
		File file = null;
		if (adapter != null) {
			file = (File) adapter.getItem(info.position);
			mPath = file.getAbsolutePath();
		}
		switch (item.getItemId()) {
		case ID_SELECT:
			if (file.isDirectory()) {
				((FileChooserActivity) getActivity()).onDirectorySelected(file);
			} else {
				((FileChooserActivity) getActivity()).onFileSelected(file);
			}
			return true;
		case ID_GO_INTO:
			((FileChooserActivity) getActivity()).onFileSelected(file);
			return true;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		FileListAdapter adapter = (FileListAdapter) l.getAdapter();
		if (adapter != null) {
			File file = (File) adapter.getItem(position);
			mPath = file.getAbsolutePath();
			((FileChooserActivity) getActivity()).onFileSelected(file);
		}
	}

	@Override
	public Loader<List<File>> onCreateLoader(int id, Bundle args) {
		return new FileLoader(getActivity(), mPath);
	}

	@Override
	public void onLoadFinished(Loader<List<File>> loader, List<File> data) {
		mAdapter.setListItems(data);

		if (isResumed())
			setListShown(true);
		else
			setListShownNoAnimation(true);
	}

	@Override
	public void onLoaderReset(Loader<List<File>> loader) {
		mAdapter.clear();
	}
}