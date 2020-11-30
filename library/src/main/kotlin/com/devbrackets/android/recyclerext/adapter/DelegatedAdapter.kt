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

/**
 * A [RecyclerView.Adapter] that handles delegating the creation and binding of
 * [RecyclerView.ViewHolder]s with [ViewHolderBinder]s
 * to allow for dynamic lists
 */
abstract class DelegatedAdapter<T> : ActionableAdapter<ViewHolder>(), DelegateApi<T> {
  protected var delegateCore: DelegateCore<T, ViewHolder> = DelegateCore(this, this)

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    return delegateCore.onCreateViewHolder(parent, viewType)
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    delegateCore.onBindViewHolder(holder, position)
  }

  override fun onViewRecycled(holder: ViewHolder) {
    delegateCore.onViewRecycled(holder)
  }

  override fun onFailedToRecycleView(holder: ViewHolder): Boolean {
    return delegateCore.onFailedToRecycleView(holder)
  }

  override fun onViewAttachedToWindow(holder: ViewHolder) {
    delegateCore.onViewAttachedToWindow(holder)
  }

  override fun onViewDetachedFromWindow(holder: ViewHolder) {
    delegateCore.onViewDetachedFromWindow(holder)
  }

  /**
   * Registers the `binder` to handle creating and binding the views of type
   * `viewType`. If a [ViewHolderBinder] has already been specified
   * for the `viewType` then the value will be overwritten with `binder`
   *
   * @param viewType The type of view the [ViewHolderBinder] handles
   * @param binder The [ViewHolderBinder] to handle creating and binding views
   */
  fun <VH: ViewHolder, B: ViewHolderBinder<T, VH>> registerViewHolderBinder(viewType: Int, binder: B) {
    delegateCore.registerViewHolderBinder(viewType, binder as ViewHolderBinder<T, ViewHolder>)
  }

  /**
   * Registers the `binder` to handle creating and binding the views that aren't
   * handled by any binders registered with [registerViewHolderBinder].
   * If a [ViewHolderBinder] has already been specified as the default then the value will be
   * overwritten with `binder`
   *
   * @param binder The [ViewHolderBinder] to handle creating and binding default views
   */
  fun <VH: ViewHolder, B: ViewHolderBinder<T, VH>> registerDefaultViewHolderBinder(binder: B?) {
    delegateCore.registerDefaultViewHolderBinder(binder as ViewHolderBinder<T, ViewHolder>)
  }

}