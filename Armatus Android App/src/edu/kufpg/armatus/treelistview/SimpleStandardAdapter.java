package edu.kufpg.armatus.treelistview;

import java.util.Arrays;
import java.util.Set;

import pl.polidea.treelistview.AbstractTreeListAdapter;
import pl.polidea.treelistview.TreeNodeInfo;
import pl.polidea.treelistview.TreeStateManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import edu.kufpg.armatus.R;

/**
 * This is a very simple adapter that provides very basic tree view with a
 * checkboxes and simple item description.
 * 
 */
class SimpleStandardAdapter extends AbstractTreeListAdapter<Long> {

	private final Set<Long> mSelected;

	private final OnCheckedChangeListener mOnCheckedChange = new OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(final CompoundButton buttonView,
				final boolean isChecked) {
			final Long id = (Long) buttonView.getTag();
			changeSelected(isChecked, id);
		}

	};

	private void changeSelected(final boolean isChecked, final Long id) {
		if (isChecked) {
			mSelected.add(id);
		} else {
			mSelected.remove(id);
		}
	}

	public SimpleStandardAdapter(final TreeListViewDemo treeViewListDemo,
			final Set<Long> selected,
			final TreeStateManager<Long> treeStateManager,
			final int numberOfLevels) {
		super(treeViewListDemo, treeStateManager, numberOfLevels);
		mSelected = selected;
	}

	private String getDescription(final long id) {
		final Integer[] hierarchy = getManager().getHierarchyDescription(id);
		return "Node " + id + Arrays.asList(hierarchy);
	}

	@Override
	public View getNewChildView(final TreeNodeInfo<Long> treeNodeInfo) {
		final LinearLayout viewLayout = (LinearLayout) getActivity()
				.getLayoutInflater().inflate(R.layout.tree_list_view_demo_item, null);
		return updateView(viewLayout, treeNodeInfo);
	}

	@Override
	public LinearLayout updateView(final View view,
			final TreeNodeInfo<Long> treeNodeInfo) {
		final LinearLayout viewLayout = (LinearLayout) view;
		final TextView descriptionView = (TextView) viewLayout
				.findViewById(R.id.demo_list_item_description);
		final TextView levelView = (TextView) viewLayout
				.findViewById(R.id.demo_list_item_level);
		descriptionView.setText(getDescription(treeNodeInfo.getId()));
		levelView.setText(Integer.toString(treeNodeInfo.getLevel()));
		final CheckBox box = (CheckBox) viewLayout
				.findViewById(R.id.demo_list_checkbox);
		box.setTag(treeNodeInfo.getId());
		if (treeNodeInfo.isWithChildren()) {
			box.setVisibility(View.GONE);
		} else {
			box.setVisibility(View.VISIBLE);
			box.setChecked(mSelected.contains(treeNodeInfo.getId()));
		}
		box.setOnCheckedChangeListener(mOnCheckedChange);
		return viewLayout;
	}

	@Override
	public void handleItemClick(final View view, final Object id) {
		final Long longId = (Long) id;
		final TreeNodeInfo<Long> info = getManager().getNodeInfo(longId);
		if (info.isWithChildren()) {
			super.handleItemClick(view, id);
		} else {
			final ViewGroup vg = (ViewGroup) view;
			final CheckBox cb = (CheckBox) vg
					.findViewById(R.id.demo_list_checkbox);
			cb.performClick();
		}
	}

	@Override
	public long getItemId(final int position) {
		return getTreeId(position);
	}
}