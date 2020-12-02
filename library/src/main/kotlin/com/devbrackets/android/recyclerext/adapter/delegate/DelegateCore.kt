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

class DelegateCore<T, VH: RecyclerView.ViewHolder>(
    protected var delegateApi: DelegateApi<T>,
    protected var adapter: RecyclerView.Adapter<VH>
) {
  protected val binders = SparseArrayCompat<Any>()
  var defaultViewHolderBinder: Any? = null

  val viewHolderBinders: List<ViewHolderBinder<T, VH>>
    get() {
      val binderList: MutableList<ViewHolderBinder<T, VH>> = LinkedList()
      for (i in 0 until binders.size()) {
        binderList.add(binders[binders.keyAt(i)] as ViewHolderBinder<T, VH>)
      }
      return binderList
    }

  fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
    return (getBinderOrThrow(viewType) as ViewHolderBinder<T, VH>).onCreateViewHolder(parent, viewType)
  }

  fun onBindViewHolder(holder: VH, position: Int) {
    val itemViewType = delegateApi.getItemViewType(holder.adapterPosition)
    (getBinderOrThrow(itemViewType) as ViewHolderBinder<T, VH>).onBindViewHolder(holder, delegateApi.getItem(position), position)
    holder.itemView.setTag(R.id.recyclerext_view_type, itemViewType)
  }

  /**
   * @see RecyclerView.Adapter.onViewRecycled
   */
  fun onViewRecycled(holder: VH) {
    holder.invoke {
      it.onViewRecycled(holder)
      true
    }
  }

  /**
   * @see RecyclerView.Adapter.onFailedToRecycleView
   */
  fun onFailedToRecycleView(holder: VH): Boolean {
    return holder.invoke { it.onFailedToRecycleView(holder) }
  }

  /**
   * @see RecyclerView.Adapter.onViewAttachedToWindow
   */
  fun onViewAttachedToWindow(holder: VH) {
    holder.invoke {
      it.onViewAttachedToWindow(holder)
      true
    }
  }

  /**
   * @see RecyclerView.Adapter.onViewDetachedFromWindow
   */
  fun onViewDetachedFromWindow(holder: VH) {
    holder.invoke {
      it.onViewDetachedFromWindow(holder)
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
  fun <B: ViewHolderBinder<T, VH>> registerViewHolderBinder(viewType: Int, binder: B) {
    val oldBinder = binders[viewType] as B?
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
  fun <B: ViewHolderBinder<T, VH>> registerDefaultViewHolderBinder(binder: B?) {
    if (defaultViewHolderBinder === binder) {
      return
    }

    (defaultViewHolderBinder as B?)?.onDetachedFromAdapter(adapter)
    defaultViewHolderBinder = binder?.apply {
      onAttachedToAdapter(adapter)
    }
  }

  /**
   * Retrieves the [ViewHolderBinder] associated with the `viewType` or
   * throws an [IllegalStateException] informing the user that they forgot to register
   * a [ViewHolderBinder] that handles `viewType`
   *
   * @param viewType The type of the view to retrieve the [ViewHolderBinder] for
   * @return The [ViewHolderBinder] that handles the `viewType`
   */
  protected fun <B: ViewHolderBinder<T, VH>> getBinderOrThrow(viewType: Int): B {
    return getBinder(viewType)
        ?: throw IllegalStateException("Unable to create or bind ViewHolders of viewType $viewType because no ViewHolderBinder has been registered for that viewType")
  }

  /**
   * Retrieves the [ViewHolderBinder] associated with the `viewType`
   *
   * @param viewType The type of the view to retrieve the [ViewHolderBinder] for
   * @return The [ViewHolderBinder] that handles the `viewType`
   */
  protected fun <B: ViewHolderBinder<T, VH>> getBinder(viewType: Int): B? {
    return (binders[viewType] ?: defaultViewHolderBinder) as B?
  }

  /**
   * Handles invoking a method on a [ViewHolderBinder] if it handles the specified `holder`,
   * catching any exceptions thrown.
   *
   * @param binderMethodInvoker The method to run on the [ViewHolderBinder]
   * @return The result of `binderMethodInvoker` or `false` if an exception is thrown or no binder handles the `holder`
   */
  private fun VH.invoke(action: (binder: ViewHolderBinder<T, VH>) -> Boolean): Boolean {
    val itemViewType = itemView.getTag(R.id.recyclerext_view_type) as? Int ?: return false
    getBinder<ViewHolderBinder<T,VH>>(itemViewType)?.let { binder ->
      try {
        return action(binder)
      } catch (e: Exception) {
        // Purposefully left blank
      }
    }

    return false
  }
}