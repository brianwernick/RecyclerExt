/*
 * Copyright (C) 2017 - 2020 Brian Wernick
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
package com.devbrackets.android.recyclerext.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.devbrackets.android.recyclerext.adapter.delegate.DelegateApi
import com.devbrackets.android.recyclerext.adapter.delegate.DelegateCore
import com.devbrackets.android.recyclerext.adapter.delegate.ViewHolderBinder
import com.devbrackets.android.recyclerext.adapter.header.HeaderApi

/**
 * A [RecyclerView.Adapter] that handles delegating the creation and binding of
 * [RecyclerView.ViewHolder]s with [ViewHolderBinder]s
 * to allow for dynamic lists
 */
abstract class DelegatedHeaderAdapter<T> : HeaderAdapter<ViewHolder, ViewHolder>(), DelegateApi<T> {
  protected var headerDelegateCore: DelegateCore<T, ViewHolder> = DelegateCore(HeaderDelegateApi(), this)
  protected var childDelegateCore: DelegateCore<T, ViewHolder> = DelegateCore(ChildDelegateApi(), this)

  override fun onCreateHeaderViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    return headerDelegateCore.onCreateViewHolder(parent, viewType)
  }

  override fun onCreateChildViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    return childDelegateCore.onCreateViewHolder(parent, viewType)
  }

  override fun onBindHeaderViewHolder(holder: ViewHolder, firstChildPosition: Int) {
    headerDelegateCore.onBindViewHolder(holder, firstChildPosition)
  }

  override fun onBindChildViewHolder(holder: ViewHolder, childPosition: Int) {
    childDelegateCore.onBindViewHolder(holder, childPosition)
  }

  override fun onViewRecycled(holder: ViewHolder) {
    val delegateCore = if (core.isHeader(holder.adapterPosition)) headerDelegateCore else childDelegateCore
    delegateCore.onViewRecycled(holder)
  }

  override fun onFailedToRecycleView(holder: ViewHolder): Boolean {
    val delegateCore = if (core.isHeader(holder.adapterPosition)) headerDelegateCore else childDelegateCore
    return delegateCore.onFailedToRecycleView(holder)
  }

  override fun onViewAttachedToWindow(holder: ViewHolder) {
    val delegateCore = if (core.isHeader(holder.adapterPosition)) headerDelegateCore else childDelegateCore
    delegateCore.onViewAttachedToWindow(holder)
  }

  override fun onViewDetachedFromWindow(holder: ViewHolder) {
    val delegateCore = if (core.isHeader(holder.adapterPosition)) headerDelegateCore else childDelegateCore
    delegateCore.onViewDetachedFromWindow(holder)
  }

  /**
   * Registers the `binder` to handle creating and binding the headers of type
   * `viewType`. If a [ViewHolderBinder] has already been specified
   * for the `viewType` then the value will be overwritten with `binder`
   *
   * @param viewType The type of view the [ViewHolderBinder] handles
   * @param binder The [ViewHolderBinder] to handle creating and binding views
   */
  fun <B: ViewHolderBinder<T, *>> registerHeaderViewHolderBinder(viewType: Int, binder: B) {
    val headerViewType = viewType or HeaderApi.HEADER_VIEW_TYPE_MASK
    headerDelegateCore.registerViewHolderBinder(headerViewType, binder as ViewHolderBinder<T, ViewHolder>)
  }

  /**
   * Registers the `binder` to handle creating and binding the children of type
   * `viewType`. If a [ViewHolderBinder] has already been specified
   * for the `viewType` then the value will be overwritten with `binder`
   *
   * @param viewType The type of view the [ViewHolderBinder] handles
   * @param binder The [ViewHolderBinder] to handle creating and binding views
   */
  fun <B: ViewHolderBinder<T, *>> registerChildViewHolderBinder(viewType: Int, binder: B) {
    val childViewType = viewType and HeaderApi.HEADER_VIEW_TYPE_MASK.inv()
    childDelegateCore.registerViewHolderBinder(childViewType, binder as ViewHolderBinder<T, ViewHolder>)
  }

  /**
   * Registers the `binder` to handle creating and binding the views that aren't
   * handled by any binders registered with [.registerHeaderViewHolderBinder].
   * If a [ViewHolderBinder] has already been specified as the default then the value will be
   * overwritten with `binder`
   *
   * @param binder The [ViewHolderBinder] to handle creating and binding default views
   */
  fun <B: ViewHolderBinder<T, *>> registerDefaultHeaderViewHolderBinder(binder: B?) {
    headerDelegateCore.registerDefaultViewHolderBinder(binder as ViewHolderBinder<T, ViewHolder>)
  }

  /**
   * Registers the `binder` to handle creating and binding the views that aren't
   * handled by any binders registered with [.registerChildViewHolderBinder].
   * If a [ViewHolderBinder] has already been specified as the default then the value will be
   * overwritten with `binder`
   *
   * @param binder The [ViewHolderBinder] to handle creating and binding default views
   */
  fun <B: ViewHolderBinder<T, *>> registerDefaultViewHolderBinder(binder: B?) {
    childDelegateCore.registerDefaultViewHolderBinder(binder as ViewHolderBinder<T, ViewHolder>)
  }

  protected inner class HeaderDelegateApi : DelegateApi<T> {
    override fun getItem(position: Int): T {
      return this@DelegatedHeaderAdapter.getItem(position)
    }

    override fun getItemViewType(adapterPosition: Int): Int {
      if (adapterPosition < 0 || adapterPosition >= headerData.adapterPositionItemMap.size()) {
        return 0
      }

      val item = headerData.adapterPositionItemMap[adapterPosition]
      return if (item != null) {
        item.itemViewType and HeaderApi.HEADER_VIEW_TYPE_MASK.inv()
      } else 0
    }
  }

  protected inner class ChildDelegateApi : DelegateApi<T> {
    override fun getItem(position: Int): T {
      return this@DelegatedHeaderAdapter.getItem(position)
    }

    override fun getItemViewType(adapterPosition: Int): Int {
      if (adapterPosition < 0 || adapterPosition >= headerData.adapterPositionItemMap.size()) {
        return 0
      }

      val item = headerData.adapterPositionItemMap[adapterPosition]
      return if (item != null) {
        item.itemViewType and HeaderApi.HEADER_VIEW_TYPE_MASK.inv()
      } else 0
    }
  }
}