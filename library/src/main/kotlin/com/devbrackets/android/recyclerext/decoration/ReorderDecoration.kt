/*
 * Copyright (C) 2016 Brian Wernick
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
package com.devbrackets.android.recyclerext.decoration

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import androidx.annotation.IdRes
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import androidx.recyclerview.widget.RecyclerView.OnItemTouchListener

/**
 * An ItemDecoration that performs the functionality to show the reordering of
 * list items without any space between items.
 */
class ReorderDecoration(private val recyclerView: RecyclerView) : ItemDecoration(), OnItemTouchListener {
  companion object {
    const val NO_POSITION = -1
    const val INVALID_RESOURCE_ID = 0
    private const val MAX_EDGE_DETECTION_THRESHOLD = 0.5f
    private const val DEFAULT_EDGE_SCROLL_SPEED = 0.5f
    private const val DEFAULT_EDGE_DETECTION_THRESHOLD = 0.01f
  }

  private enum class DragState {
    DRAGGING, ENDED
  }

  enum class LayoutOrientation {
    VERTICAL, HORIZONTAL
  }

  interface ReorderListener {
    /**
     * Called when the user drag event ends, informing the listener of the changed position
     *
     * @param originalPosition The position the dragged view started at
     * @param newPosition The position the dragged view should be saved as
     */
    fun onItemReordered(originalPosition: Int, newPosition: Int)

    /**
     * Called when the animation for the view position has finished.  This should be used for
     * actually updating the backing data structure
     *
     * @param originalPosition The position the dragged view started at
     * @param newPosition The position the dragged view should be saved as
     */
    fun onItemPostReordered(originalPosition: Int, newPosition: Int)
  }

  private var dragState = DragState.ENDED

  /**
   * Sets the orientation of the current layout.  This will aid in the calculations for
   * edgeScrolling [.setEdgeScrollingEnabled] and determining the new position
   * in the list on [.endReorder]
   *
   * @param orientation The layouts orientation
   */
  var orientation = LayoutOrientation.VERTICAL

  /**
   * Retrieves whether the items should start scrolling once the view being reordered
   * hits the edge of the containing view.
   *
   * @return True if edge scrolling is enabled
   */
  /**
   * Sets whether the items should start scrolling once the view being reordered
   * hits the edge of the containing view.
   *
   * @param enabled True to scroll once the view being reordered hits the edge
   */
  var isEdgeScrollingEnabled = true
  private var edgeDetectionThreshold = DEFAULT_EDGE_DETECTION_THRESHOLD
  private var edgeScrollSpeed = DEFAULT_EDGE_SCROLL_SPEED
  private var fingerOffset: PointF? = null
  private var dragItem: BitmapDrawable? = null
  private var selectedDragItemPosition = NO_POSITION
  private var selectedDragItemNewPosition = NO_POSITION
  private var floatingItemStartingBounds: Rect? = null
  private var floatingItemBounds: Rect? = null
  private var newViewStart = 0
  private val eventPosition = PointF(0F, 0F)
  private val floatingItemCenter = PointF(0F, 0F)
  private var dragHandleId = INVALID_RESOURCE_ID
  private var reorderListener: ReorderListener? = null
  private var smoothFinishAnimationListener: SmoothFinishAnimationListener? = null

  override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
    dragItem?.draw(c)
  }

  override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
    super.getItemOffsets(outRect, view, parent, state)
    if (dragState == DragState.ENDED) {
      finishReorder(view)
      return
    }

    val itemPosition = recyclerView.getChildAdapterPosition(view)
    if (itemPosition == selectedDragItemPosition) {
      view.visibility = View.INVISIBLE
      return
    }

    //Make sure the view is visible
    view.visibility = View.VISIBLE

    //Calculate the new offsets
    updateFloatingItemCenter()
    setVerticalOffsets(view, itemPosition, floatingItemCenter, outRect)
    setHorizontalOffsets(view, itemPosition, floatingItemCenter, outRect)
  }

  /**
   * This will determine two things.
   * 1. If we need to handle the touch event
   * 2. If reordering needs to start due to dragHandle being clicked
   */
  override fun onInterceptTouchEvent(recyclerView: RecyclerView, event: MotionEvent): Boolean {
    if (dragState == DragState.DRAGGING) {
      return true
    }

    if (dragHandleId == INVALID_RESOURCE_ID) {
      return false
    }

    val itemView = recyclerView.findChildViewUnder(event.x, event.y) ?: return false
    val handleView = itemView.findViewById<View>(dragHandleId)
    if (handleView == null || handleView.visibility != View.VISIBLE) {
      return false
    }

    val handlePosition = IntArray(2)
    handleView.getLocationOnScreen(handlePosition)

    //Determine if the MotionEvent is inside the handle
    if (event.rawX >= handlePosition[0] &&
        event.rawX <= handlePosition[0] + handleView.width &&
        event.rawY >= handlePosition[1] &&
        event.rawY <= handlePosition[1] + handleView.height
    ) {
      startReorder(itemView, event)
      return true
    }

    return false
  }

  override fun onTouchEvent(recyclerView: RecyclerView, event: MotionEvent) {
    if (dragState != DragState.DRAGGING) {
      return
    }

    when (event.action) {
        MotionEvent.ACTION_UP -> {
            if (selectedDragItemPosition != NO_POSITION) {
                reorderListener?.let {
                    selectedDragItemNewPosition = calculateNewPosition()
                    it.onItemReordered(selectedDragItemPosition, selectedDragItemNewPosition)
                }
            }
            endReorder()
            return
        }
        MotionEvent.ACTION_CANCEL -> {
            endReorder()
            return
        }
    }

    //Finds the new location
    eventPosition.x = event.x
    eventPosition.y = event.y

    //Updates the floating views bounds
    dragItem?.let {
      updateFloatingItemCenter()

      //Make sure the dragItem bounds are correct
      updateVerticalBounds(eventPosition, floatingItemCenter)
      updateHorizontalBounds(eventPosition, floatingItemCenter)
      it.bounds = floatingItemBounds!!
    }

    //Perform the edge scrolling if necessary
    performVerticalEdgeScroll(eventPosition)
    performHorizontalEdgeScroll(eventPosition)
    recyclerView.invalidateItemDecorations()
  }

  override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
    // Purposefully left blank
  }

  /**
   * Sets the listener to be informed of reorder events
   *
   * @param listener The ReorderListener to use
   */
  fun setReorderListener(listener: ReorderListener?) {
    reorderListener = listener
    smoothFinishAnimationListener = SmoothFinishAnimationListener(reorderListener)
  }

  /**
   * Sets the id for the view that will act as an immediate drag handle.
   * This means that once the view has been touched that the drag will be
   * started.
   *
   * @param handleId The Resource ID for the drag handle or [.INVALID_RESOURCE_ID]
   */
  fun setDragHandleId(@IdRes handleId: Int) {
    dragHandleId = handleId
  }

  /**
   * Sets the percent amount in relation to the size of the recyclerView
   * for the edge scrolling to use.
   *
   * @param speed The percent amount [0.0 - 1.0]
   */
  fun setEdgeScrollSpeed(speed: Float) {
    if (edgeScrollSpeed < 0 || edgeScrollSpeed > 1) {
      return
    }

    edgeScrollSpeed = speed
  }

  /**
   * Retrieves the edge scroll speed
   *
   * @return [default: {@value #DEFAULT_EDGE_SCROLL_SPEED}]
   */
  fun getEdgeScrollSpeed(): Float {
    return edgeScrollSpeed
  }

  /**
   * Retrieves the edge threshold for the edge scrolling.
   *
   * @return The current edge threshold [0.0 - {@value #MAX_EDGE_DETECTION_THRESHOLD}] [default: {@value #DEFAULT_EDGE_DETECTION_THRESHOLD}]
   */
  /**
   * Sets the percent threshold at the edges of the recyclerView to start the
   * edge scrolling.  This threshold can be between 0 (no edge) and {@value #MAX_EDGE_DETECTION_THRESHOLD}
   * (half of the recyclerView)
   *
   * @param threshold The edge scrolling threshold [0.0 - {@value #MAX_EDGE_DETECTION_THRESHOLD}]
   */
  var edgeThreshold: Float
    get() = edgeDetectionThreshold
    set(threshold) {
      if (threshold < 0 || threshold > MAX_EDGE_DETECTION_THRESHOLD) {
        return
      }

      edgeDetectionThreshold = threshold
    }

  /**
   * Manually starts the reorder process for the specified view.  This should not be used if the [.setDragHandleId] is
   * set and should control the reordering.
   *
   * @param view The View to start reordering
   * @param startMotionEvent The MotionEvent that starts the reorder
   */
  fun startReorder(view: View, startMotionEvent: MotionEvent?) {
    if (dragState == DragState.DRAGGING) {
      return
    }

    if (startMotionEvent != null) {
      fingerOffset = PointF(startMotionEvent.rawX, startMotionEvent.rawY).apply {
        val rawViewLoc = IntArray(2)
        view.getLocationOnScreen(rawViewLoc)

        x = rawViewLoc[0] - x
        y = rawViewLoc[1] - y
      }

    }

    dragState = DragState.DRAGGING
    dragItem = createDragBitmap(view)
    selectedDragItemPosition = recyclerView.getChildAdapterPosition(view)
  }

  /**
   * Ends the reorder process.  This should only be called if [.startReorder] has been
   * manually called.
   */
  fun endReorder() {
    if (dragState != DragState.DRAGGING) {
      return
    }

    dragState = DragState.ENDED
    fingerOffset = null
    dragItem = null
    selectedDragItemPosition = NO_POSITION
    recyclerView.invalidateItemDecorations()
  }

  /**
   * Calculates the position the item should have when it is dropped.
   *
   * @return The new position for the item
   */
  fun calculateNewPosition(): Int {
    val itemsOnScreen = recyclerView.layoutManager!!.childCount
    updateFloatingItemCenter()

    var before = 0
    var pos = 0
    var after = Int.MAX_VALUE
    for (screenPosition in 0 until itemsOnScreen) {

      //Grabs the view at screenPosition
      val view = recyclerView.layoutManager!!.getChildAt(screenPosition)
      if (view!!.visibility != View.VISIBLE) {
        continue
      }

      //Makes sure we don't compare to itself
      val itemPos = recyclerView.getChildAdapterPosition(view)
      if (itemPos == selectedDragItemPosition) {
        continue
      }

      //Performs the Vertical position calculations
      if (orientation == LayoutOrientation.VERTICAL) {
        val viewMiddleY = view.top + (view.height / 2).toFloat()
        if (floatingItemCenter.y > viewMiddleY && itemPos > before) {
          before = itemPos
          pos = screenPosition
        } else if (floatingItemCenter.y <= viewMiddleY && itemPos < after) {
          after = itemPos
          pos = screenPosition
        }
      }

      //Performs the Horizontal position calculations
      if (orientation == LayoutOrientation.HORIZONTAL) {
        val viewMiddleX = view.left + (view.width / 2).toFloat()
        if (floatingItemCenter.x > viewMiddleX && itemPos > before) {
          before = itemPos
          pos = screenPosition
        } else if (floatingItemCenter.x <= viewMiddleX && itemPos < after) {
          after = itemPos
          pos = screenPosition
        }
      }
    }

    val newPosition: Int
    if (after != Int.MAX_VALUE) {
      if (after < selectedDragItemPosition) {
        newPosition = after
        updateNewViewStart(pos, true)
      } else {
        newPosition = after - 1
        updateNewViewStart(pos - 1, false)
      }
    } else {
      if (before < selectedDragItemPosition) {
        before++
        pos++
      }

      newPosition = before
      updateNewViewStart(pos, false)
    }

    return newPosition
  }

  /**
   * Updates the stored position for the start of the view.  This will be the
   * top when Vertical and left when Horizontal.
   *
   * @param childPosition The position of the view in the RecyclerView
   * @param draggedUp True if the view has been moved up or to the left
   */
  private fun updateNewViewStart(childPosition: Int, draggedUp: Boolean) {
    val view = recyclerView.layoutManager!!.getChildAt(childPosition) ?: return
    val start = if (orientation == LayoutOrientation.VERTICAL) view.top else view.left
    var viewDimen = if (orientation == LayoutOrientation.VERTICAL) view.height else view.width

    viewDimen *= if (draggedUp) -1 else 1
    newViewStart = start + if (view.visibility == View.VISIBLE) viewDimen else 0
  }

  /**
   * Retrieves the new center for the bitmap representing the item being dragged
   */
  private fun updateFloatingItemCenter() {
    floatingItemCenter.x = floatingItemBounds!!.left + (floatingItemStartingBounds!!.width() / 2).toFloat()
    floatingItemCenter.y = floatingItemBounds!!.top + (floatingItemStartingBounds!!.height() / 2).toFloat()
  }

  /**
   * Updates the vertical view offsets if the dragging view has been moved around the `view`.
   * This happens when the dragging view starts above the `view` and has been dragged
   * below it, or vice versa.
   *
   * @param view The view to compare with the dragging items current and original positions
   * @param itemPosition The position for the `view`
   * @param middle The center of the floating item
   * @param outRect The [Rect] to update the position in
   */
  private fun setVerticalOffsets(view: View, itemPosition: Int, middle: PointF, outRect: Rect) {
    if (orientation == LayoutOrientation.HORIZONTAL) {
      return
    }

    if (itemPosition > selectedDragItemPosition && view.top < middle.y) {
      var amountUp = (middle.y - view.top) / view.height.toFloat()
      if (amountUp > 1) {
        amountUp = 1f
      }

      outRect.top = (-(floatingItemBounds!!.height() * amountUp)).toInt()
      outRect.bottom = (floatingItemBounds!!.height() * amountUp).toInt()
    } else if (itemPosition < selectedDragItemPosition && view.bottom > middle.y) {
      var amountDown = (view.bottom.toFloat() - middle.y) / view.height.toFloat()
      if (amountDown > 1) {
        amountDown = 1f
      }

      outRect.top = (floatingItemBounds!!.height() * amountDown).toInt()
      outRect.bottom = (-(floatingItemBounds!!.height() * amountDown)).toInt()
    }
  }

  /**
   * Updates the horizontal view offsets if the dragging view has been moved around the `view`.
   * This happens when the dragging view starts before the `view` and has been dragged
   * after it, or vice versa.
   *
   * @param view The view to compare with the dragging items current and original positions
   * @param itemPosition The position for the `view`
   * @param middle The center of the floating item
   * @param outRect The [Rect] to update the position in
   */
  private fun setHorizontalOffsets(view: View, itemPosition: Int, middle: PointF, outRect: Rect) {
    if (orientation == LayoutOrientation.VERTICAL) {
      return
    }

    if (itemPosition > selectedDragItemPosition && view.left < middle.x) {
      var amountRight = (middle.x - view.left) / view.width.toFloat()
      if (amountRight > 1) {
        amountRight = 1f
      }

      outRect.left = (-(floatingItemBounds!!.width() * amountRight)).toInt()
      outRect.right = (floatingItemBounds!!.width() * amountRight).toInt()
    } else if (itemPosition < selectedDragItemPosition && view.right > middle.x) {
      var amountLeft = (view.right.toFloat() - middle.x) / view.width.toFloat()
      if (amountLeft > 1) {
        amountLeft = 1f
      }

      outRect.left = (floatingItemBounds!!.width() * amountLeft).toInt()
      outRect.right = (-(floatingItemBounds!!.width() * amountLeft)).toInt()
    }
  }

  /**
   * Performs the functionality to detect and initiate the scrolling of vertical
   * lists when the view being dragged has reached an end of the containing
   * [RecyclerView]
   *
   * @param fingerPosition The current position for the dragging finger
   */
  private fun performVerticalEdgeScroll(fingerPosition: PointF) {
    if (!isEdgeScrollingEnabled || orientation == LayoutOrientation.HORIZONTAL) {
      return
    }

    var scrollAmount = 0f
    if (fingerPosition.y > recyclerView.height * (1 - edgeDetectionThreshold)) {
      scrollAmount = fingerPosition.y - recyclerView.height * (1 - edgeDetectionThreshold)
    } else if (fingerPosition.y < recyclerView.height * edgeDetectionThreshold) {
      scrollAmount = fingerPosition.y - recyclerView.height * edgeDetectionThreshold
    }

    scrollAmount *= edgeScrollSpeed
    recyclerView.scrollBy(0, scrollAmount.toInt())
  }

  /**
   * Performs the functionality to detect and initiate the scrolling of horizontal
   * lists when the view being dragged has reached an end of the containing
   * [RecyclerView]
   *
   * @param fingerPosition The current position for the dragging finger
   */
  private fun performHorizontalEdgeScroll(fingerPosition: PointF) {
    if (!isEdgeScrollingEnabled || orientation == LayoutOrientation.VERTICAL) {
      return
    }

    var scrollAmount = 0f
    if (fingerPosition.x > recyclerView.width * (1 - edgeDetectionThreshold)) {
      scrollAmount = fingerPosition.x - recyclerView.width * (1 - edgeDetectionThreshold)
    } else if (fingerPosition.x < recyclerView.width * edgeDetectionThreshold) {
      scrollAmount = fingerPosition.x - recyclerView.width * edgeDetectionThreshold
    }

    scrollAmount *= edgeScrollSpeed
    recyclerView.scrollBy(scrollAmount.toInt(), 0)
  }

  /**
   * Updates the vertical position for the floating bitmap that represents the
   * view being dragged.
   *
   * @param fingerPosition The current position of the dragging finger
   * @param viewMiddle The center of the view being dragged
   */
  private fun updateVerticalBounds(fingerPosition: PointF, viewMiddle: PointF) {
    if (orientation == LayoutOrientation.HORIZONTAL) {
      return
    }

    floatingItemBounds!!.top = fingerPosition.y.toInt()
    fingerOffset?.let {
      floatingItemBounds!!.top += it.y.toInt()
    }

    if (floatingItemBounds!!.top < -viewMiddle.y) {
      floatingItemBounds!!.top = (-viewMiddle.y).toInt()
    }

    floatingItemBounds!!.bottom = floatingItemBounds!!.top + floatingItemStartingBounds!!.height()
  }

  /**
   * Updates the horizontal position for the floating bitmap that represents the
   * view being dragged.
   *
   * @param fingerPosition The current position of the dragging finger
   * @param viewMiddle The center of the view being dragged
   */
  private fun updateHorizontalBounds(fingerPosition: PointF, viewMiddle: PointF) {
    if (orientation == LayoutOrientation.VERTICAL) {
      return
    }

    floatingItemBounds!!.left = fingerPosition.x.toInt()
    fingerOffset?.let {
      floatingItemBounds!!.left += it.x.toInt()
    }

    if (floatingItemBounds!!.left < -viewMiddle.x) {
      floatingItemBounds!!.left = (-viewMiddle.x).toInt()
    }

    floatingItemBounds!!.right = floatingItemBounds!!.left + floatingItemStartingBounds!!.width()
  }

  /**
   * Generates the Bitmap that will be used to represent the view being dragged across the screen
   *
   * @param view The view to create the drag bitmap from
   * @return The bitmap representing the drag view
   */
  private fun createDragBitmap(view: View): BitmapDrawable {
    floatingItemStartingBounds = Rect(view.left, view.top, view.right, view.bottom)
    floatingItemBounds = Rect(floatingItemStartingBounds)

    val bitmap = Bitmap.createBitmap(floatingItemStartingBounds!!.width(), floatingItemStartingBounds!!.height(), Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    view.draw(canvas)
    val retDrawable = BitmapDrawable(view.resources, bitmap)
    retDrawable.bounds = floatingItemBounds!!
    return retDrawable
  }

  /**
   * Animates the dragged views position to the final resting position
   *
   * @param view The view to animate
   */
  private fun finishReorder(view: View) {
    smoothFinishAnimationListener?.setPositions(selectedDragItemPosition, selectedDragItemNewPosition)
    selectedDragItemPosition = NO_POSITION
    view.visibility = View.VISIBLE

    //Performs the ending animation
    if (recyclerView.getChildAdapterPosition(view) == selectedDragItemNewPosition) {
      selectedDragItemNewPosition = NO_POSITION
      val startYDelta = if (orientation == LayoutOrientation.VERTICAL) floatingItemBounds!!.top - newViewStart else 0
      val startXDelta = if (orientation == LayoutOrientation.HORIZONTAL) floatingItemBounds!!.left - newViewStart else 0
      val anim = SmoothFinishAnimation(startYDelta, startXDelta, smoothFinishAnimationListener)
      view.startAnimation(anim)
    }
  }

  /**
   * Used to animate the final position for the dragged view so that it doesn't pop when
   * dragged to the bottom of the list.
   */
  private class SmoothFinishAnimation(startYDelta: Int, startXDelta: Int, listener: AnimationListener?) : TranslateAnimation(startXDelta.toFloat(), 0f, startYDelta.toFloat(), 0f) {
    private fun setup() {
      duration = DURATION.toLong()
      interpolator = FastOutSlowInInterpolator()
    }

    companion object {
      private const val DURATION = 100 //milliseconds
    }

    init {
      setAnimationListener(listener)
      setup()
    }
  }

  /**
   * Listens to the [com.devbrackets.android.recyclerext.decoration.ReorderDecoration.SmoothFinishAnimation]
   * and properly informs the [.reorderListener] when the animation is complete
   */
  private class SmoothFinishAnimationListener(private val listener: ReorderListener?) : Animation.AnimationListener {
    private var startPosition = 0
    private var endPosition = 0
    fun setPositions(startPosition: Int, endPosition: Int) {
      this.startPosition = startPosition
      this.endPosition = endPosition
    }

    override fun onAnimationStart(animation: Animation) {
      //Purposefully left blank
    }

    override fun onAnimationEnd(animation: Animation) {
      listener?.onItemPostReordered(startPosition, endPosition)
    }

    override fun onAnimationRepeat(animation: Animation) {
      //Purposefully left blank
    }
  }
}