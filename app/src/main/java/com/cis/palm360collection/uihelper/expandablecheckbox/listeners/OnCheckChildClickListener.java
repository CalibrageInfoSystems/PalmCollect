package com.cis.palm360collection.uihelper.expandablecheckbox.listeners;
import android.view.View;

import com.cis.palm360collection.uihelper.expandablecheckbox.models.CheckedExpandableGroup;

/**
 * Interface definition for a callback to be invoked when a {@link CheckableChildViewHolder}
 * has been clicked.
 */

//Not Using
public interface OnCheckChildClickListener {
  /**
   * Callback method to be invoked when a child in the {@link CheckableChildRecyclerViewAdapter}
   * has been clicked.
   *
   * @param checked the current check state of the child
   * @param v The view within the RecyclerView that was clicked
   * @param group The {@link CheckedExpandableGroup}  that contains the child that  was clicked
   * @param childIndex The child position within the {@link CheckedExpandableGroup}
   */
  void onCheckChildCLick(View v, boolean checked, CheckedExpandableGroup group, int childIndex);
}
