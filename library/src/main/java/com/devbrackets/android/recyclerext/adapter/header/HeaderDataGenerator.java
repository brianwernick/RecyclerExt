package com.devbrackets.android.recyclerext.adapter.header;

import android.support.annotation.NonNull;
import android.support.v4.util.LongSparseArray;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles calculating the headers and offsets within an adapter
 * and generates the related {@link HeaderItem}s. This can be used
 * to asynchronously calculate the information necessary for the
 * Header adapters that can be used with the {@link android.support.v7.util.DiffUtil}
 */
public class HeaderDataGenerator {
    public interface DataSource {
        /**
         * Return the stable ID for the header at <code>childPosition</code> or {@link RecyclerView#NO_ID}
         *
         * @param childPosition The position of the item ignoring headers
         * @return the stable ID of the header at childPosition
         */
        long getHeaderId(int childPosition);

        /**
         * Returns the total number of children in the data set
         *
         * @return The total number of children
         */
        int getChildCount();
    }

    /**
     * Calculates the information necessary to display headers
     * using the associated {@link DataSource}
     *
     * @param dataSource The data to use when calculating the header information
     * @return The resulting {@link HeaderData}
     */
    @NonNull
    public HeaderData calculate(@NonNull DataSource dataSource) {
        return calculate(new HeaderData(), dataSource);
    }

    /**
     * Calculates the information necessary to display headers
     * using the associated {@link DataSource}
     *
     * @param reuseData A {@link HeaderData} that will be reused instead of creating a new one
     * @param dataSource The data to use when calculating the header information
     * @return The <code>reuseData</code> that has been populated with the header information
     */
    @NonNull
    public HeaderData calculate(@NonNull HeaderData reuseData, @NonNull DataSource dataSource) {
        reuseData.headerItems.clear();
        reuseData.headerChildCountMap.clear();
        HeaderItem currentItem = null;

        for (int i = 0; i < dataSource.getChildCount(); i++) {
            long id = dataSource.getHeaderId(i);
            if (id == RecyclerView.NO_ID) {
                continue;
            }

            //Updates the child count for the headerId
            Integer childCount = reuseData.headerChildCountMap.get(id);
            childCount = (childCount == null) ? 1 : childCount + 1;
            reuseData.headerChildCountMap.put(id, childCount);

            //Adds new headers to the list when detected
            if (currentItem == null || currentItem.getId() != id) {
                int position = i + (reuseData.showHeaderAsChild ? 0 : reuseData.headerItems.size());
                currentItem = new HeaderItem(id, position);
                reuseData.headerItems.add(currentItem);
            }
        }

        return reuseData;
    }

    public static class HeaderData {
        /**
         * Stores the number of child items associated with each header id.
         * (Key: HeaderId, Value: childCount)
         */
        @NonNull
        public LongSparseArray<Integer> headerChildCountMap = new LongSparseArray<>();
        @NonNull
        public List<HeaderItem> headerItems = new ArrayList<>();

        public boolean showHeaderAsChild = false;
    }
}
