/*
 * Copyright (C) 2017 Brian Wernick
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
import androidx.recyclerview.widget.RecyclerView

/**
 * A delegated handler for the [RecyclerView.ViewHolder]s
 * that the [DelegatedAdapter] uses to create and bind each view type
 */
abstract class ViewHolderBinder<T, VH : RecyclerView.ViewHolder> {
  abstract fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH
  abstract fun onBindViewHolder(holder: VH, item: T, position: Int)

  fun onAttachedToAdapter(adapter: RecyclerView.Adapter<VH>) {
    // Purposefully left blank
  }

  fun onDetachedFromAdapter(adapter: RecyclerView.Adapter<VH>) {
    // Purposefully left blank
  }

  fun onViewRecycled(holder: VH) {
    // Purposefully left blank
  }

  fun onFailedToRecycleView(holder: VH): Boolean {
    return false
  }

  fun onViewAttachedToWindow(holder: VH) {
    // Purposefully left blank
  }

  fun onViewDetachedFromWindow(holder: VH) {
    // Purposefully left blank
  }
}