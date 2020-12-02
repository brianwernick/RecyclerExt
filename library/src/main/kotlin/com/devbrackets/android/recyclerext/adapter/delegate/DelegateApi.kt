package com.devbrackets.android.recyclerext.adapter.delegate

interface DelegateApi<T> {
  /**
   * Retrieves the item associated with the `position`
   *
   * @param position The position to get the item for
   * @return The item in the `position`
   */
  fun getItem(position: Int): T
  fun getItemViewType(adapterPosition: Int): Int
}