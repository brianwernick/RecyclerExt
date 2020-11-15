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
package com.devbrackets.android.recyclerext.widget

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.content.res.TypedArray
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Handler
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.Animation
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.*
import androidx.annotation.IntRange
import androidx.appcompat.widget.AppCompatDrawableManager
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.devbrackets.android.recyclerext.R
import com.devbrackets.android.recyclerext.animation.FastScrollBubbleVisibilityAnimation
import com.devbrackets.android.recyclerext.animation.FastScrollHandleVisibilityAnimation
import kotlin.math.abs

/**
 * A class that provides the functionality of a fast scroll
 * for the attached [RecyclerView]
 */
class FastScroll : FrameLayout {
  companion object {
    private const val TAG = "FastScroll"
    const val INVALID_POPUP_ID: Long = -1
  }

  protected var popupCallbacks: PopupCallbacks? = null
  protected lateinit var handle: ImageView
  protected lateinit var bubble: TextView
  protected var recyclerView: RecyclerView? = null
  protected var scrollListener = RecyclerScrollListener()
  protected var delayHandler = Handler()
  protected var handleHideRunnable = HandleHideRunnable()
  protected var bubbleHideRunnable = BubbleHideRunnable()

  /**
   * The provider to override the animations for the popup bubble and drag handle
   */
  protected var animationProvider: AnimationProvider? = null

  /**
   * Specifies the alignment the popup bubble has in relation to the drag handle,
   * see [BubbleAlignment] for more details
   *
   * This can also be specified with `re_bubble_alignment` in xml
   */
  protected var bubbleAlignment = BubbleAlignment.TOP
  protected var viewHeight = 0

  @JvmField
  protected var showBubble = false

  /**
   * The minimum amount of pages to be contained in the list before the
   * FastScroll will be displayed. This will only have an affect if [hideOnShortLists]
   * is enabled. If the `minDisplayPageCount` is set to 0 the FastScroll will always be shown
   */
  @IntRange(from = 0L)
  var minDisplayPageCount = 4
    set(value) {
      field = value
      calculatedMinDisplayHeight = viewHeight * value
    }

  protected var calculatedMinDisplayHeight = 0


  /**
   * Specifies if the FastScroll should be hidden when the list is shorter.
   * This value is determined by [.setMinDisplayPageCount]
   */
  var hideOnShortLists = true

  /**
   * Specifies if the drag handle can hide after a short delay (see [handleHideDelay])
   * after scrolling has completely stopped
   */
  protected var hideHandleAllowed = true
  protected var draggingHandle = false

  /**
   * Specifies if clicks on the track should scroll to that position
   */
  protected var trackClicksAllowed = false

  // The offset for the finger from the center of the drag handle
  protected var fingerCenterOffset = 0f

  /**
   * The delay used when hiding the drag handle, which occurs after scrolling
   * has completely stopped in Milliseconds
   */
  var handleHideDelay: Long = 1_000

  /**
   * The delay used when hiding the bubble which occurs after the drag handle
   * is released in Milliseconds
   */
  var bubbleHideDelay: Long = 0

  protected var requestedHandleVisibility = false
  protected var currentSectionId = INVALID_POPUP_ID

  constructor(context: Context) : super(context) {
    init(context, null)
  }

  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
    init(context, attrs)
  }

  @TargetApi(Build.VERSION_CODES.HONEYCOMB)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
    init(context, attrs)
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
    init(context, attrs)
  }

  override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()
    recyclerView?.removeOnScrollListener(scrollListener)
  }

  override fun onSizeChanged(w: Int, h: Int, oldW: Int, oldH: Int) {
    super.onSizeChanged(w, h, oldW, oldH)
    viewHeight = h
    calculatedMinDisplayHeight = h * minDisplayPageCount
  }

  override fun onTouchEvent(event: MotionEvent): Boolean {
    //Filters out touch events we don't need to handle
    if (!draggingHandle && event.action != MotionEvent.ACTION_DOWN) {
      return super.onTouchEvent(event)
    }

    when (event.action) {
        MotionEvent.ACTION_DOWN -> {
            //Verifies the event is within the allowed coordinates
            if (ignoreTouchDown(event.x, event.y)) {
                return false
            }
            delayHandler.removeCallbacks(handleHideRunnable)
            updateHandleVisibility(true)
            if (bubble.visibility != VISIBLE) {
                updateBubbleVisibility(true)
            }
            draggingHandle = true
            val halfHandle = handle.height / 2f
            fingerCenterOffset = handle.y + halfHandle - event.y
            fingerCenterOffset = boxValue(fingerCenterOffset, halfHandle)
            handle.isSelected = true
            setBubbleAndHandlePosition(event.y + fingerCenterOffset)
            setRecyclerViewPosition(event.y + fingerCenterOffset)
            return true
        }
        MotionEvent.ACTION_MOVE -> {
            setBubbleAndHandlePosition(event.y + fingerCenterOffset)
            setRecyclerViewPosition(event.y + fingerCenterOffset)
            return true
        }
        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
            draggingHandle = false
            handle.isSelected = false
            hideBubbleDelayed()
            hideHandleDelayed()
            return true
        }
    }
    return super.onTouchEvent(event)
  }

  /**
   * Links this widget to the `recyclerView`. This is necessary for the
   * FastScroll to function.
   *
   * @param recyclerView The [RecyclerView] to attach to
   */
  fun attach(recyclerView: RecyclerView) {
    if (showBubble && recyclerView.adapter !is PopupCallbacks) {
      Log.e(TAG, "The RecyclerView Adapter specified needs to implement " + PopupCallbacks::class.java.simpleName)
      return
    }

    this.recyclerView = recyclerView
    if (showBubble) {
      popupCallbacks = recyclerView.adapter as PopupCallbacks?
    }

    recyclerView.addOnScrollListener(scrollListener)
    recyclerView.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
        override fun onPreDraw(): Boolean {
            recyclerView.viewTreeObserver.removeOnPreDrawListener(this)
            scrollListener.onScrolled(recyclerView, 0, 0)
            return true
        }
    })
  }

  /**
   * Specifies if the popup bubble should be shown when the handle is
   * being dragged.
   *
   * @param showBubble `true` if the popup bubble should be shown
   */
  fun setShowBubble(showBubble: Boolean) {
    if (this.showBubble == showBubble) {
      return
    }

    this.showBubble = showBubble
    if (recyclerView == null || !showBubble) {
      return
    }

    if (recyclerView!!.adapter !is PopupCallbacks) {
      Log.e(TAG, "The RecyclerView Adapter specified needs to implement " + PopupCallbacks::class.java.simpleName)
      return
    }

    popupCallbacks = recyclerView!!.adapter as PopupCallbacks?
  }

  /**
   * Sets the text color for the popup bubble
   * This can also be specified with `re_bubble_text_color` in xml
   *
   * @param colorRes The resource id for the color
   */
  fun setTextColorRes(@ColorRes colorRes: Int) {
    setTextColor(getColor(colorRes))
  }

  /**
   * Sets the text color for the popup bubble
   * This can also be specified with `re_bubble_text_color` in xml
   *
   * @param color The integer representation for the color
   */
  fun setTextColor(@ColorInt color: Int) {
    bubble.setTextColor(color)
  }

  /**
   * Sets the text size of the popup bubble via the `dimeRes`
   * This can also be specified with `re_bubble_text_size` in xml
   *
   * @param dimenRes The dimension resource for the text size
   */
  fun setTextSize(@DimenRes dimenRes: Int) {
    bubble.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimensionPixelSize(dimenRes).toFloat())
  }

  /**
   * Sets the text size of the popup bubble, interpreted as "scaled pixel" units.
   * This size is adjusted based on the current density and user font size preference.
   * This can also be specified with `re_bubble_text_size` in xml
   *
   * @param size The scaled pixel size
   */
  fun setTextSize(size: Float) {
    bubble.textSize = size
  }

  /**
   * Tints the popup bubble background (see [.setBubbleDrawable]) with the
   * color defined by `colorRes`.
   * This can also be specified with `re_bubble_color` in xml
   *
   * @param colorRes The resource id for the color to tint the popup bubble with
   */
  fun setBubbleTintRes(@ColorRes colorRes: Int) {
    setBubbleTint(getColor(colorRes))
  }

  /**
   * Tints the popup bubble background (see [.setBubbleDrawable]) with the
   * specified color.
   * This can also be specified with `re_bubble_color` in xml
   *
   * @param tint The integer representation for the tint color
   */
  fun setBubbleTint(@ColorInt tint: Int) {
    bubble.background = tint(getDrawable(R.drawable.recyclerext_fast_scroll_bubble), tint)
  }

  /**
   * Sets the background drawable for the popup bubble.
   * This can also be specified with `re_bubble_background` in xml
   *
   * @param drawable The drawable for the popup bubble background
   */
  fun setBubbleDrawable(drawable: Drawable?) {
    bubble.background = drawable
  }

  /**
   * Tints the drag handle background (see [.setHandleDrawable]) with the
   * color defined by `colorRes`.
   * This can also be specified with `re_handle_color` in xml
   *
   * @param colorRes The resource id for the color to tint the drag handle with
   */
  fun setHandleTintRes(@ColorRes colorRes: Int) {
    setHandleTint(getColor(colorRes))
  }

  /**
   * Tints the drag handle background (see [.setHandleDrawable] with the specified color.
   * This can also be specified with `re_handle_color` in xml
   *
   * @param tint The integer representation for the tint color
   */
  fun setHandleTint(@ColorInt tint: Int) {
    handle.background = tint(getDrawable(R.drawable.recyclerext_fast_scroll_handle), tint)
  }

  /**
   * Sets the drawable for the drag handle.
   * This can also be specified with `re_handle_background` in xml
   *
   * @param drawable The drawable for the drag handle background
   */
  fun setHandleDrawable(drawable: Drawable?) {
    handle.background = drawable
  }

  /**
   * The base initialization method (called from constructors) that
   * inflates and configures the widget.
   *
   * @param context The context of the widget
   * @param attrs The attributes associated with the widget
   */
  protected fun init(context: Context, attrs: AttributeSet?) {
    val inflater = LayoutInflater.from(context)
    inflater.inflate(R.layout.recyclerext_fast_scroll, this, true)

    bubble = findViewById(R.id.recyclerext_fast_scroll_bubble)
    handle = findViewById(R.id.recyclerext_fast_scroll_handle)

    bubble.visibility = GONE
    readAttributes(context, attrs)
  }

  /**
   * Reads the attributes associated with this view, setting any values found
   *
   * @param context The context to retrieve the styled attributes with
   * @param attrs The [AttributeSet] to retrieve the values from
   */
  protected fun readAttributes(context: Context, attrs: AttributeSet?) {
    if (attrs == null || isInEditMode) {
      return
    }

    context.obtainStyledAttributes(attrs, R.styleable.FastScroll).also {
      retrieveBubbleAttributes(it)
      retrieveHandleAttributes(it)
    }.recycle()
  }

  /**
   * Retrieves the xml attributes associated with the popup bubble.
   * This includes the drawable, tint color, alignment, font options, etc.
   *
   * @param typedArray The array of attributes to use
   */
  protected fun retrieveBubbleAttributes(typedArray: TypedArray) {
    showBubble = typedArray.getBoolean(R.styleable.FastScroll_re_show_bubble, true)
    bubbleAlignment = BubbleAlignment[typedArray.getInt(R.styleable.FastScroll_re_bubble_alignment, 3)]

    var textColor = getColor(R.color.recyclerext_fast_scroll_bubble_text_color_default)
    textColor = typedArray.getColor(R.styleable.FastScroll_re_bubble_text_color, textColor)

    var textSize = resources.getDimensionPixelSize(R.dimen.recyclerext_fast_scroll_bubble_text_size_default)
    textSize = typedArray.getDimensionPixelSize(R.styleable.FastScroll_re_bubble_text_size, textSize)

    var backgroundDrawable = getDrawable(typedArray, R.styleable.FastScroll_re_bubble_background)
    var backgroundColor = getColor(R.color.recyclerext_fast_scroll_bubble_color_default)

    backgroundColor = typedArray.getColor(R.styleable.FastScroll_re_bubble_color, backgroundColor)
    if (backgroundDrawable == null) {
      backgroundDrawable = tint(getDrawable(R.drawable.recyclerext_fast_scroll_bubble), backgroundColor)
    }

    bubble.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize.toFloat())
    bubble.setTextColor(textColor)
    bubble.background = backgroundDrawable
  }

  /**
   * Retrieves the xml attributes associated with the drag handle.
   * This includes the drawable and tint color
   *
   * @param typedArray The array of attributes to use
   */
  protected fun retrieveHandleAttributes(typedArray: TypedArray) {
    var backgroundDrawable = getDrawable(typedArray, R.styleable.FastScroll_re_handle_background)
    var backgroundColor = getColor(R.color.recyclerext_fast_scroll_handle_color_default)

    backgroundColor = typedArray.getColor(R.styleable.FastScroll_re_handle_color, backgroundColor)
    if (backgroundDrawable == null) {
      backgroundDrawable = tint(getDrawable(R.drawable.recyclerext_fast_scroll_handle), backgroundColor)
    }

    handle.background = backgroundDrawable
  }

  /**
   * Determines if the [MotionEvent.ACTION_DOWN] event should be ignored.
   * This occurs when the event position is outside the bounds of the drag handle or
   * the track (of the drag handle) when disabled (see [.setTrackClicksAllowed]
   *
   * @param xPos The x coordinate of the event
   * @param yPos The y coordinate of the event
   * @return `true` if the event should be ignored
   */
  protected fun ignoreTouchDown(xPos: Float, yPos: Float): Boolean {
    //Verifies the event is within the allowed X coordinates
    if (ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_LTR) {
      if (xPos < handle.x - ViewCompat.getPaddingStart(handle)) {
        return true
      }
    } else {
      if (xPos > handle.x + handle.width + ViewCompat.getPaddingStart(handle)) {
        return true
      }
    }
    if (!trackClicksAllowed) {
      //Enforces selection to only occur on the handle
      if (yPos < handle.y - handle.paddingTop || yPos > handle.y + handle.height + handle.paddingBottom) {
        return true
      }
    }
    return false
  }

  /**
   * Updates the scroll position of the [.recyclerView]
   * by determining the adapter position proportionally related to
   * the `y` position
   *
   * @param y The y coordinate to find the adapter position for
   */
  protected fun setRecyclerViewPosition(y: Float) {
    //Boxes the ratio between handleCenter and height - handleCenter
    val ratio: Float
    val halfHandle = handle.height / 2
    ratio = if (y <= halfHandle) {
      0f
    } else if (y >= viewHeight - halfHandle) {
      1f
    } else {
      (y - halfHandle) / (viewHeight - handle.height)
    }

    //Performs the scrolling and updates the bubble
    scrollToLocation(ratio)
    updateBubbleText(recyclerView!!.computeVerticalScrollOffset().toFloat() / recyclerView!!.computeVerticalScrollRange().toFloat())
  }

  /**
   * Informs the [.recyclerView] that we need to smoothly scroll
   * to the requested position.
   *
   * @param ratio The scroll location as a ratio of the total in the range [0, 1]
   */
  protected fun scrollToLocation(ratio: Float) {
    val scrollRange = recyclerView!!.computeVerticalScrollRange() - recyclerView!!.computeVerticalScrollExtent()
    if (scrollRange > 0) {
      val deltaY = (ratio * scrollRange).toInt() - recyclerView!!.computeVerticalScrollOffset()
      recyclerView!!.scrollBy(0, deltaY)
    }
  }

  /**
   * Updates the position both the drag handle and popup bubble
   * have in relation to y (the users finger)
   *
   * @param y The position to place the drag handle at
   */
  protected fun setBubbleAndHandlePosition(y: Float) {
    val handleHeight = handle.height
    val handleY = (y - handleHeight / 2).toInt().coerceIn(0, viewHeight - handleHeight).toFloat()
    handle.y = handleY
    if (showBubble) {
      setBubblePosition(handleY)
    }
  }

  /**
   * Updates the position of the popup bubble in relation to the
   * drag handle. This depends on the value of [.bubbleAlignment]
   *
   * @param handleY The position the drag handle has for relational alignment
   */
  protected fun setBubblePosition(handleY: Float) {
    val maxY = viewHeight - bubble.height
    val handleCenter = handleY + handle.height / 2
    val handleBottom = handleY + handle.height
    when (bubbleAlignment) {
        BubbleAlignment.TOP -> bubble.y = handleY.toInt().coerceIn(0, maxY).toFloat()
        BubbleAlignment.CENTER -> bubble.y = (handleCenter - bubble.height / 2).toInt().coerceIn(0, maxY).toFloat()
        BubbleAlignment.BOTTOM -> bubble.y = (handleBottom - bubble.height).toInt().coerceIn(0, maxY).toFloat()
        BubbleAlignment.BOTTOM_TO_TOP -> bubble.y = (handleY - bubble.height).toInt().coerceIn(0, maxY).toFloat()
        BubbleAlignment.TOP_TO_BOTTOM -> bubble.y = handleBottom.toInt().coerceIn(0, maxY).toFloat()
        BubbleAlignment.BOTTOM_TO_CENTER -> bubble.y = (handleCenter - bubble.height).toInt().coerceIn(0, maxY).toFloat()
    }
  }

  protected fun updateBubbleText(ratio: Float) {
    if (!showBubble || popupCallbacks == null) {
      return
    }

    val itemCount = recyclerView!!.adapter!!.itemCount
    val position = (ratio * itemCount).toInt().coerceIn(0, itemCount - 1)
    val sectionId = popupCallbacks!!.getSectionId(position)
    if (currentSectionId != sectionId) {
      currentSectionId = sectionId
      bubble.text = popupCallbacks!!.getPopupText(position, sectionId)
    }
  }

  /**
   * Updates the visibility of the bubble representing the current location.
   * Typically this bubble will contain the first letter of the section that
   * is at the top of the RecyclerView.
   *
   * @param toVisible `true` if the bubble should be visible at the end of the animation
   */
  protected fun updateBubbleVisibility(toVisible: Boolean) {
    if (!showBubble) {
      return
    }

    bubble.clearAnimation()
    Log.d(TAG, "updating bubble visibility $toVisible")
    bubble.startAnimation(getBubbleAnimation(bubble, toVisible))
  }

  /**
   * Updates the visibility of the drag handle, storing the requested state
   * so that we aren't continuously requesting visibility animations.
   *
   * @param toVisible `true` if the drag handle should be visible at the end of the change
   */
  protected fun updateHandleVisibility(toVisible: Boolean) {
    if (requestedHandleVisibility == toVisible) {
      return
    }

    requestedHandleVisibility = toVisible
    handle.clearAnimation()

    Log.d(TAG, "updating handle visibility $toVisible")
    handle.startAnimation(getHandleAnimation(handle, toVisible))
  }

  /**
   * Handles the functionality to delay the hiding of the
   * bubble if the bubble is shown
   */
  protected fun hideBubbleDelayed() {
    delayHandler.removeCallbacks(bubbleHideRunnable)
    if (showBubble && !draggingHandle) {
      delayHandler.postDelayed(bubbleHideRunnable, bubbleHideDelay)
    }
  }

  /**
   * Handles the functionality to delay the hiding of the
   * handle
   */
  protected fun hideHandleDelayed() {
    delayHandler.removeCallbacks(handleHideRunnable)
    if (hideHandleAllowed && !draggingHandle) {
      delayHandler.postDelayed(handleHideRunnable, handleHideDelay)
    }
  }

  /**
   * Retrieves the animation for hiding or showing the popup bubble
   *
   * @param bubble The view representing the popup bubble to animate
   * @param toVisible `true` if the animation should show the bubble
   * @return The animation for hiding or showing the bubble
   */
  protected fun getBubbleAnimation(bubble: View, toVisible: Boolean): Animation {
    return animationProvider?.getBubbleAnimation(bubble, toVisible)
        ?: FastScrollBubbleVisibilityAnimation(bubble, toVisible)
  }

  /**
   * Retrieves the animation for hiding or showing the drag handle
   *
   * @param handle The view representing the handle to animate
   * @param toVisible `true` if the animation should show the handle
   * @return The animation for hiding or showing the handle
   */
  protected fun getHandleAnimation(handle: View, toVisible: Boolean): Animation {
    return animationProvider?.getHandleAnimation(handle, toVisible)
        ?: FastScrollHandleVisibilityAnimation(handle, toVisible)
  }

  /**
   * Tints the `drawable` with the `color`
   *
   * @param drawable The drawable to ting
   * @param color The color to tint the `drawable` with
   * @return The tinted `drawable`
   */
  protected fun tint(drawable: Drawable?, @ColorInt color: Int): Drawable? {
    return drawable?.apply {
      colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY)
    }
  }

  /**
   * A utility method to retrieve a drawable that correctly abides by the
   * theme in Lollipop (API 23) +
   *
   * @param resourceId The resource id for the drawable
   * @return The drawable associated with `resourceId`
   */
  protected fun getDrawable(@DrawableRes resourceId: Int): Drawable? {
    return ResourcesCompat.getDrawable(resources, resourceId, context.theme)
  }

  /**
   * Retrieves the specified image drawable in a manner that will correctly
   * wrap VectorDrawables on platforms that don't natively support them
   *
   * @param typedArray The TypedArray containing the attributes for the view
   * @param index The index in the `typedArray` for the drawable
   */
  @SuppressLint("RestrictedApi")
  protected fun getDrawable(typedArray: TypedArray, index: Int): Drawable? {
    val imageResId = typedArray.getResourceId(index, 0)
    return if (imageResId == 0) {
      null
    } else AppCompatDrawableManager.get().getDrawable(context, imageResId)
  }

  /**
   * A utility method to retrieve a color that correctly abides by the
   * theme in Marshmallow (API 23) +
   *
   * @param res The resource id associated with the requested color
   * @return The integer representing the color associated with `res`
   */
  @ColorInt
  protected fun getColor(@ColorRes res: Int): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      resources.getColor(res, context.theme)
    } else resources.getColor(res)
  }


  /**
   * Enforces a restriction on the `value` to be at most or at least
   * the `amount`
   *
   * @param value The value value to make sure is no smaller or larger than the `amount`
   * @param amount The largest amount the `value` can have
   * @return The value boxed to the amount
   */
  protected fun boxValue(value: Float, amount: Float): Float {
    if (abs(value) < abs(amount)) {
      return value
    }

    return if (value < 0) -amount else amount
  }

  /**
   * Listens to the scroll position changes of the parent (RecyclerView)
   * so that the handle will always have the correct position
   */
  protected inner class RecyclerScrollListener : RecyclerView.OnScrollListener() {
    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
      val verticalRange = recyclerView.computeVerticalScrollRange()

      //Makes sure the FastScroll is correctly hidden on shorter lists
      if (hideOnShortLists && (verticalRange < calculatedMinDisplayHeight || calculatedMinDisplayHeight == 0)) {
        updateHandleVisibility(false)
        return
      }

      //Makes sure the handle is shown when scrolling
      updateHandleVisibility(true)
      delayHandler.removeCallbacks(handleHideRunnable)
      if (handle.isSelected) {
        return
      }

      hideHandleDelayed()
      val ratio = recyclerView.computeVerticalScrollOffset().toFloat() / (verticalRange - recyclerView.computeVerticalScrollExtent()).toFloat()
      val halfHandleHeight = (handle.height / 2).toFloat()
      setBubbleAndHandlePosition((viewHeight - handle.height) * ratio + halfHandleHeight)
    }
  }

  /**
   * Runnable used to delay the hiding of the drag handle
   */
  protected inner class HandleHideRunnable : Runnable {
    override fun run() {
      updateHandleVisibility(false)
    }
  }

  /**
   * Runnable used to delay the hiding of the popup bubble
   */
  protected inner class BubbleHideRunnable : Runnable {
    override fun run() {
      updateBubbleVisibility(false)
    }
  }

  /**
   * A contract that allows the user to provide particular Fast Scroll
   * animations for hiding and showing the drag handle and popup bubble
   */
  interface AnimationProvider {
    /**
     * Retrieves the animation to use for showing or hiding the popup bubble.
     * By default this uses the simple alpha animation [FastScrollBubbleVisibilityAnimation]
     *
     * @param bubble The view that represents the popup bubble
     * @param toVisible `true` if the returned animation should handle showing the bubble
     * @return The custom animation for hiding or showing the popup bubble, null to use the default
     */
    fun getBubbleAnimation(bubble: View, toVisible: Boolean): Animation?

    /**
     * Retrieves the animation to use for showing or hiding the drag handle.
     * By default this uses the simple alpha animation [FastScrollHandleVisibilityAnimation]
     *
     * @param handle The view that represents the drag handle
     * @param toVisible `true` if the returned animation should handle showing the drag handle
     * @return The custom animation for hiding or showing the drag handle, null to use the default
     */
    fun getHandleAnimation(handle: View, toVisible: Boolean): Animation?
  }

  /**
   * Callback used to request the title for the fast scroll bubble
   * when enabled.
   */
  interface PopupCallbacks {
    /**
     * Called when the section id specified with [.getSectionId] changes,
     * indicating the popup text needs to be changed. This will only be called if
     * [.setShowBubble] is true.
     *
     * @param position The position for the item with the `sectionId`
     * @param sectionId The id for the section the text is associated with
     * @return The text for the bubble
     */
    fun getPopupText(position: Int, sectionId: Long): String

    /**
     * Called for each item as the list is scrolled via the FastScroll to determine what the
     * items section is. This will only be called if [.setShowBubble] is true.
     *
     * @param position The position to determine the section id for
     * @return The id associated with the section the `position` is a member of
     */
    @IntRange(from = INVALID_POPUP_ID)
    fun getSectionId(@IntRange(from = 0) position: Int): Long
  }

  /**
   * Alignment types associated with the popup bubble (see [.setShowBubble]
   */
  enum class BubbleAlignment {
    /**
     * The top of the popup bubble is even with the top of the drag handle
     */
    TOP,

    /**
     * The center (y) of the popup bubble is even with the center (y) of the drag handle
     */
    CENTER,

    /**
     * The bottom of the popup bubble is even with the bottom of the drag handle
     */
    BOTTOM,

    /**
     * The bottom of the popup bubble is even with the top of the drag handle
     */
    BOTTOM_TO_TOP,

    /**
     * The top of the popup bubble is even with the bottom of the drag handle
     */
    TOP_TO_BOTTOM,

    /**
     * The bottom of the popup bubble is even with the center (y) of the drag handle
     */
    BOTTOM_TO_CENTER;

    companion object {
      operator fun get(@IntRange(from = 0, to = 5) index: Int): BubbleAlignment {
        return values()[index]
      }
    }
  }
}