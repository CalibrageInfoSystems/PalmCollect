package com.cis.palm360collection.uihelper;

import android.util.SparseBooleanArray;

import androidx.recyclerview.widget.RecyclerView;

import com.cis.palm360collection.cloudhelper.Log;

import java.util.ArrayList;
import java.util.List;

//To Get Selected Item in Recycler Adapter
public abstract class SelectableAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

	private static final String TAG = SelectableAdapter.class.getSimpleName();

	public SparseBooleanArray selectedItems;

	public SelectableAdapter() {
		selectedItems = new SparseBooleanArray();
	}

	/**
	 * Indicates if the item at position position is selected
	 * @param position Position of the item to check
	 * @return true if the item is selected, false otherwise
	 */
	public boolean isSelected(int position) {
		return getSelectedItems().contains(position);
	}

	/**
	 * Toggle the selection status of the item at a given position
	 * @param position Position of the item to toggle the selection status for
	 */
	public synchronized void toggleSelection(int position) {
		if (selectedItems.get(position, false)) {
			Log.v(TAG, "@@@ deleting selected item");
			selectedItems.delete(position);
		} else {
			Log.v(TAG, "@@@ adding selected item");
			selectedItems.put(position, true);
		}
		notifyItemChanged(position);
	}

	/**
	 * Clear the selection status for all items
	 */
	public void clearSelection() {
		List<Integer> selection = getSelectedItems();
		selectedItems.clear();
		for (Integer i : selection) {
			notifyItemChanged(i);
		}
	}

	/**
	 * Count the selected items
	 * @return Selected items count
	 */
	public int getSelectedItemCount() {
		return selectedItems.size();
	}

	/**
	 * Indicates the list of selected items
	 * @return List of selected items ids
	 */
	public List<Integer> getSelectedItems() {
		List<Integer> items = new ArrayList<>(selectedItems.size());
		for (int i = 0; i < selectedItems.size(); ++i) {
			items.add(selectedItems.keyAt(i));
		}
		return items;
	}
}