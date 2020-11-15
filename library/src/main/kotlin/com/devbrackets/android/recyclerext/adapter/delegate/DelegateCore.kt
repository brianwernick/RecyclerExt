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
package com.devbrackets.android.recyclerext.adapter.delegate

import android.view.ViewGroup
import androidx.collection.SparseArrayCompat
import androidx.recyclerview.widget.RecyclerView
import com.devbrackets.android.recyclerext.R
import java.util.*

class DelegateCore<VH : RecyclerView.ViewHolder, T>(
    protected var delegateApi: DelegateApi<T>,
    protected var adapter: RecyclerView.Adapter<VH>
) {
  protected val binders = SparseArrayCompat<ViewHolderBinder<VH, T>>()
  var defaultViewHolderBinder: ViewHolderBinder<VH, T>? = null
    protected set

  fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
    return getBinderOrThrow(viewType).onCreateViewHolder(parent, viewType)
  }

  fun onBindViewHolder(holder: VH, position: Int) {
    val itemViewType = delegateApi.getItemViewType(holder.adapterPosition)
    getBinderOrThrow(itemViewType).onBindViewHolder(holder, delegateApi.getItem(position), position)
    holder.itemView.setTag(R.id.recyclerext_view_type, itemViewType)
  }

  /**
   * @see RecyclerView.Adapter.onViewRecycled
   */
  fun onViewRecycled(holder: RecyclerView.ViewHolder) {
    invokeBinderMethod(holder) { binder ->
      binder.onViewRecycled(holder as VH)
      true
    }
  }

  /**
   * @see RecyclerView.Adapter.onFailedToRecycleView
   */
  fun onFailedToRecycleView(holder: RecyclerView.ViewHolder): Boolean {
    return invokeBinderMethod(holder) { binder -> binder.onFailedToRecycleView(holder as VH) }
  }

  /**
   * @see RecyclerView.Adapter.onViewAttachedToWindow
   */
  fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
    invokeBinderMethod(holder) { binder ->
      binder.onViewAttachedToWindow(holder as VH)
      true
    }
  }

  /**
   * @see RecyclerView.Adapter.onViewDetachedFromWindow
   */
  fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
    invokeBinderMethod(holder) { binder ->
      binder.onViewDetachedFromWindow(holder as VH)
      true
    }
  }

  /**
   * Registers the `binder` to handle creating and binding the views of type
   * `viewType`. If a [ViewHolderBinder] has already been specified
   * for the `viewType` then the value will be overwritten with `binder`
   *
   * @param viewType The type of view the [ViewHolderBinder] handles
   * @param binder The [ViewHolderBinder] to handle creating and binding views
   */
  fun registerViewHolderBinder(viewType: Int, binder: ViewHolderBinder<VH, T>) {
    val oldBinder = binders[viewType]
    if (oldBinder != null && oldBinder === binder) {
      return
    }

    oldBinder?.onDetachedFromAdapter(adapter)
    binders.put(viewType, binder)
    binder.onAttachedToAdapter(adapter)
  }

  /**
   * Registers the `binder` to handle creating and binding the views that aren't
   * handled by any binders registered with [.registerViewHolderBinder].
   * If a [ViewHolderBinder] has already been specified as the default then the value will be
   * overwritten with `binder`
   *
   * @param binder The [ViewHolderBinder] to handle creating and binding default views
   */
  fun registerDefaultViewHolderBinder(binder: ViewHolderBinder<VH, T>?) {
    if (defaultViewHolderBinder === binder) {
      return
    }

    defaultViewHolderBinder?.onDetachedFromAdapter(adapter)
    defaultViewHolderBinder = binder?.apply {
      onAttachedToAdapter(adapter)
    }
  }

  val viewHolderBinders: List<ViewHolderBinder<VH, T>>
    get() {
      val binderList: MutableList<ViewHolderBinder<VH, T>> = LinkedList()
      for (i in 0 until binders.size()) {
        binderList.add(binders[binders.keyAt(i)]!!)
      }
      return binderList
    }

  /**
   * Retrieves the [ViewHolderBinder] associated with the `viewType` or
   * throws an [IllegalStateException] informing the user that they forgot to register
   * a [ViewHolderBinder] that handles `viewType`
   *
   * @param viewType The type of the view to retrieve the [ViewHolderBinder] for
   * @return The [ViewHolderBinder] that handles the `viewType`
   */
  protected fun getBinderOrThrow(viewType: Int): ViewHolderBinder<VH, T> {
    return getBinder(viewType)
        ?: throw IllegalStateException("Unable to create or bind ViewHolders of viewType $viewType because no ViewHolderBinder has been registered for that viewType")
  }

  /**
   * Retrieves the [ViewHolderBinder] associated with the `viewType`
   *
   * @param viewType The type of the view to retrieve the [ViewHolderBinder] for
   * @return The [ViewHolderBinder] that handles the `viewType`
   */
  protected fun getBinder(viewType: Int): ViewHolderBinder<VH, T>? {
    return binders[viewType] ?: defaultViewHolderBinder
  }

  /**
   * Handles invoking a method on a [ViewHolderBinder] if it handles the specified `holder`,
   * catching any exceptions thrown.
   *
   * @param holder The holder used to determine the correct [ViewHolderBinder]
   * @param binderMethodInvoker The method to run on the [ViewHolderBinder]
   * @return The result of `binderMethodInvoker` or `false` if an exception is thrown or no binder handles the `holder`
   */
  protected fun invokeBinderMethod(holder: RecyclerView.ViewHolder, binderMethodInvoker: BinderMethodInvoker<VH, T>): Boolean {
    val itemViewType = holder.itemView.getTag(R.id.recyclerext_view_type) as? Int ?: return false

    getBinder(itemViewType)?.let { binder ->
      try {
        return binderMethodInvoker.binderMethod(binder)
      } catch (e: Exception) {
        // Purposefully left blank
      }
    }

    return false
  }

  protected fun interface BinderMethodInvoker<VH : RecyclerView.ViewHolder, T> {
    fun binderMethod(binder: ViewHolderBinder<VH, T>): Boolean
  }
}