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
package com.devbrackets.android.recyclerext.layoutmanager

import android.content.Context
import android.os.Build
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import androidx.annotation.IntRange
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler
import com.devbrackets.android.recyclerext.decoration.SpacerDecoration
import java.lang.ref.WeakReference

/**
 * An extension to the [GridLayoutManager] that provides
 * the ability to specify the width a grid item is supposed to have instead
 * of the number of columns.  It will then determine the number of columns
 * that are possible, enforcing the size specified by adding spacing
 * between the columns to make sure the grid items width isn't resized.
 *
 * **NOTE:** Due to limitations of the [GridLayoutManager] the layout
 * for the grid items should have a width of "match_parent", otherwise the items
 * won't be correctly centered
 */
class AutoColumnGridLayoutManager
/**
 * Constructs the layout manager that will correctly determine the number
 * of possible columns based on the `gridItemWidth` specified.
 *
 * @param context The context to use for the layout manager
 * @param gridItemWidth The width for the items in each column
 */(context: Context, protected var requestedColumnWidth: Int) : GridLayoutManager(context, 1) {
    enum class SpacingMethod {
        ALL, EDGES, SEPARATOR
    }

    protected var spacerDecoration: SpacerDecoration? = null
    protected var rowSpacing = 0
    protected var edgeSpacing = 0
    protected var matchSpacing = false
    protected var minColumnSpacingEdge = 0
    protected var minColumnSpacingSeparator = 0
    protected var spacingMethod = SpacingMethod.ALL
    protected var maxColumnCount = Int.MAX_VALUE
    protected var parent = WeakReference<RecyclerView?>(null)

    /**
     * When the layout manager is attached to the [RecyclerView] then
     * we will run the logic to determine the maximum number of columns
     * allowed with the specified column width.
     *
     * @param recyclerView The [RecyclerView] this layout manager is attached to
     */
    override fun onAttachedToWindow(recyclerView: RecyclerView) {
        super.onAttachedToWindow(recyclerView)
        parent = WeakReference(recyclerView)
        setColumnWidth(requestedColumnWidth)
    }

    /**
     * When the layout manager is detached from the [RecyclerView] then the
     * decoration that correctly spaces the grid items will be removed.
     *
     * @param recyclerView The [RecyclerView] that the layout manager is detaching from
     * @param recycler The [RecyclerView.Recycler]
     */
    override fun onDetachedFromWindow(recyclerView: RecyclerView, recycler: Recycler) {
        super.onDetachedFromWindow(recyclerView, recycler)

        //If we have setup the decoration then remove it
        if (spacerDecoration != null) {
            recyclerView.removeItemDecoration(spacerDecoration!!)
            resetRecyclerPadding(recyclerView)
        }
        parent = WeakReference(null)
    }

    /**
     * Specifies the stable width for the grid items.  This will then be used
     * to determine the maximum number of columns possible.
     *
     * @param gridItemWidth The width for the items in each column
     */
    fun setColumnWidth(gridItemWidth: Int) {
        requestedColumnWidth = gridItemWidth
        spanCount = determineColumnCount(requestedColumnWidth)
    }

    /**
     * Sets the minimum amount of spacing there should be between items in the grid.  This
     * will be used when determining the number of columns possible with the gridItemWidth specified
     * with [.AutoColumnGridLayoutManager] or [.setColumnWidth]
     *
     * @param minColumnSpacing The minimum amount of spacing between items in the grid
     */
    fun setMinColumnSpacing(minColumnSpacing: Int) {
        minColumnSpacingSeparator = minColumnSpacing
        spanCount = determineColumnCount(requestedColumnWidth)
    }

    /**
     * Sets the minimum amount of spacing there should be on the sides of the grid.  This
     * will be used when determining the number of columns possible with the gridItemWidth specified
     * with [.AutoColumnGridLayoutManager] or [.setColumnWidth]
     *
     * @param minEdgeSpacing The minimum amount of spacing that should exist at the edges of the grid
     */
    fun setMinEdgeSpacing(minEdgeSpacing: Int) {
        minColumnSpacingEdge = minEdgeSpacing
        spanCount = determineColumnCount(requestedColumnWidth)
    }

    /**
     * Sets the minimum amount of spacing there should be between edges and items.  This will
     * be used when determining the number of columns possible with the gridItemWidth specified
     * with [.AutoColumnGridLayoutManager] or [.setColumnWidth]
     *
     * @param minSpacingEdge The minimum amount of spacing between the edge of the RecyclerView and the first and last items in each row
     * @param minSpacingSeparator The minimum amount of spacing between items in each row
     */
    fun setMinColumnSpacing(minSpacingEdge: Int, minSpacingSeparator: Int) {
        minColumnSpacingEdge = minSpacingEdge
        minColumnSpacingSeparator = minSpacingSeparator
        spanCount = determineColumnCount(requestedColumnWidth)
    }

    /**
     * Sets the amount of spacing that should be between rows.  This value
     * will be overridden when [.setMatchRowAndColumnSpacing] is set to true
     *
     * @param rowSpacing The amount of spacing that should be between rows [default: 0]
     */
    fun setRowSpacing(rowSpacing: Int) {
        this.rowSpacing = rowSpacing
    }

    /**
     * Enables or disables the ability to match the horizontal and vertical spacing
     * between the grid items.  If set to true this will override the value set with
     * [.setRowSpacing]
     *
     * @param matchSpacing True to keep the horizontal and vertical spacing equal [default: false]
     */
    fun setMatchRowAndColumnSpacing(matchSpacing: Boolean) {
        this.matchSpacing = matchSpacing
    }

    /**
     * Sets the maximum number of columns allowed when determining
     * the count based on the width specified in the constructor or
     * with [.setColumnWidth]
     *
     * @param maxColumns The maximum amount of columns allowed [default: [Integer.MAX_VALUE]]
     */
    fun setMaxColumnCount(@IntRange(from = 1, to = Int.MAX_VALUE.toLong()) maxColumns: Int) {
        maxColumnCount = maxColumns
        spanCount = determineColumnCount(requestedColumnWidth)
    }

    /**
     * Sets the methodology to use when determining the spacing between columns.
     *
     *  *
     * [SpacingMethod.EDGES] will only increase the size of the left-most and right-most spacing,
     * leaving the separators with the value specified by [.setMinColumnSpacing]
     *
     *  *
     * [SpacingMethod.SEPARATOR] will only increase the size of the spacing in between
     * columns, leaving the edges with the value specified by [.setMinColumnSpacing]
     *
     *  *
     * [SpacingMethod.ALL] will increase the size of the spacing along both the edges and
     * the separators, adding based on the relative amounts specified by [.setMinColumnSpacing].
     * (e.g. `setMinColumnSpacing(100, 50)` will result in the edges growing 2px for every 1px the
     * separators grow)
     *
     *
     *
     * @param spacingMethod The method for displaying the spacing
     */
    fun setSpacingMethod(spacingMethod: SpacingMethod) {
        this.spacingMethod = spacingMethod
        spanCount = determineColumnCount(requestedColumnWidth)
    }

    /**
     * Determines the maximum number of columns based on the width of the items.
     * If the `recyclerView`'s width hasn't been determined yet, this
     * will register for the layout that will then perform the functionality to
     * set the number of columns.
     *
     * @param gridItemWidth The width for the items in each column
     * @return The number of allowed columns
     */
    protected fun determineColumnCount(gridItemWidth: Int): Int {
        val recyclerView = parent.get() ?: return 1

        //We need to register for the layout then update the column count
        if (recyclerView.width == 0) {
            val observer = recyclerView.viewTreeObserver
            observer.addOnGlobalLayoutListener(LayoutListener(recyclerView))
            return 1
        }

        //Updates the actual column count and spacing between items
        val columnCount = getColumnCount(recyclerView, gridItemWidth)
        resetRecyclerPadding(recyclerView)
        updateSpacing(recyclerView, gridItemWidth, columnCount)
        return columnCount
    }

    /**
     * Calculates and adds the amount of spacing that needs to be between each
     * column, row, and the edges of the RecyclerView.  This pays attention to
     * the value from [.setSpacingMethod]
     *
     * @param recyclerView The RecyclerView to use for determining the amount of space that needs to be added
     * @param gridItemWidth The requested width for the items
     * @param columnCount The number of columns to display
     */
    protected fun updateSpacing(recyclerView: RecyclerView, gridItemWidth: Int, columnCount: Int) {
        //Sets the decoration for the calculated spacing
        if (spacerDecoration == null) {
            spacerDecoration = SpacerDecoration()
            spacerDecoration!!.setAllowedEdgeSpacing(SpacerDecoration.Companion.EDGE_SPACING_LEFT or SpacerDecoration.Companion.EDGE_SPACING_RIGHT)
            recyclerView.addItemDecoration(spacerDecoration!!)
        }
        edgeSpacing = minColumnSpacingEdge
        var separatorSpacing = minColumnSpacingSeparator / 2


        //Calculates the edge spacing requirements
        val padding = recyclerView.paddingLeft + recyclerView.paddingRight
        val usableWidth = recyclerView.width - padding
        val separatorCount = columnCount - 1
        val spacerCount = 2 * columnCount
        val freeSpace = usableWidth - gridItemWidth * columnCount
        val extraSpace = freeSpace - 2 * minColumnSpacingEdge - separatorCount * minColumnSpacingSeparator

        //If we can add spacing, then we need to calculate how much and where to add it
        if (extraSpace >= spacerCount) {
            if (spacingMethod == SpacingMethod.ALL) {
                val totalMinEdges = minColumnSpacingEdge * 2
                val totalMinSeparators = separatorCount * minColumnSpacingSeparator

                //If the totalMinSpace is 0, then the percentage is edge count / separators + edges
                val totalMinSpace = totalMinEdges + totalMinSeparators
                val edgeSpacePercentage = if (totalMinSpace == 0) (2 / (2 + separatorCount)).toDouble() else totalMinEdges.toDouble() / totalMinSpace.toDouble()
                val totalSeparatorSpace = ((1.0 - edgeSpacePercentage) * freeSpace).toInt()
                separatorSpacing = if (spacerCount == 0) 0 else totalSeparatorSpace / spacerCount
                edgeSpacing = (freeSpace - totalSeparatorSpace) / 2 + separatorSpacing
            } else if (spacingMethod == SpacingMethod.EDGES) {
                edgeSpacing = (freeSpace - separatorSpacing * spacerCount) / 2
            } else { //SEPARATOR
                separatorSpacing = if (spacerCount == 0) 0 else freeSpace / spacerCount
            }
            edgeSpacing -= separatorSpacing
        }

        //Updates the spacing using the decoration and padding
        recyclerView.setPadding(
                recyclerView.paddingLeft + edgeSpacing,
                recyclerView.paddingTop,
                recyclerView.paddingRight + edgeSpacing,
                recyclerView.paddingBottom
        )
        spacerDecoration!!.update(separatorSpacing, if (matchSpacing) separatorSpacing else rowSpacing / 2)
    }

    /**
     * Performs the actual calculation for determining the number of possible
     * columns by using the [.maxColumnCount], [.minColumnSpacingEdge], and
     * [.minColumnSpacingSeparator] in conjunction with the requested width
     * for the items
     *
     * @param recyclerView The RecyclerView to use when determining the possible number of columns
     * @param gridItemWidth The requested width for items to be
     * @return The calculated number of possible columns
     */
    protected fun getColumnCount(recyclerView: RecyclerView, gridItemWidth: Int): Int {
        val padding = recyclerView.paddingLeft + recyclerView.paddingRight
        val usableWidth = recyclerView.width - padding
        var columnCount = Math.min(usableWidth / gridItemWidth, maxColumnCount)
        var usedColumnWidth: Int
        var minRequiredSpacing: Int

        //Decreases the columnCount until the specified min spacing can be achieved.
        do {
            usedColumnWidth = columnCount * gridItemWidth
            minRequiredSpacing = 2 * minColumnSpacingEdge + (columnCount - 1) * minColumnSpacingSeparator

            //If the specified min spacing is reached, return the number of columns
            if (usableWidth - usedColumnWidth - minRequiredSpacing >= 0) {
                return columnCount
            }
            columnCount--
        } while (columnCount > 1)
        return columnCount
    }

    /**
     * Removes the padding previously added to the `recyclerView`
     * for the edge spacing.  If no padding was previously added, then this
     * has no affect
     *
     * @param recyclerView The RecyclerView to remove the padding from
     */
    protected fun resetRecyclerPadding(recyclerView: RecyclerView) {
        recyclerView.setPadding(
                recyclerView.paddingLeft - edgeSpacing,
                recyclerView.paddingTop,
                recyclerView.paddingRight - edgeSpacing,
                recyclerView.paddingBottom
        )
    }

    /**
     * A Listener for the RecyclerView so that we can correctly update the number of columns
     * once the RecyclerView has been sized
     */
    protected inner class LayoutListener(private val recyclerView: RecyclerView) : OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            removeOnGlobalLayoutListener(recyclerView, this)
            val gridLayoutManager = recyclerView.layoutManager as GridLayoutManager?
            gridLayoutManager!!.spanCount = determineColumnCount(requestedColumnWidth)
        }

        fun removeOnGlobalLayoutListener(view: View, listener: OnGlobalLayoutListener) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                view.viewTreeObserver.removeGlobalOnLayoutListener(listener)
            } else {
                view.viewTreeObserver.removeOnGlobalLayoutListener(listener)
            }
        }
    }
}