package com.devbrackets.android.recyclerext.decoration;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.devbrackets.android.recyclerext.R;
import com.devbrackets.android.recyclerext.adapter.RecyclerHeaderAdapter;

/**
 * A RecyclerView Decoration that allows specific views (e.g. headers
 * from RecyclerHeaderAdapter) to be persisted when the reach the top of the
 * RecyclerView's frame.
 */
public class StickyViewDecoration extends RecyclerView.ItemDecoration {
    @Nullable
    private BitmapDrawable stickyItem;

    public StickyViewDecoration(RecyclerView parent) {
        parent.addOnScrollListener(new StickyViewScrollListener());
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        if (stickyItem != null) {
            stickyItem.draw(c);
        }
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
    }

    /**
     * Generates the Bitmap that will be used to represent the view stuck at the top of the
     * parent RecyclerView.
     *
     * @param view The view to create the drag bitmap from
     * @return The bitmap representing the drag view
     */
    private BitmapDrawable createStickyViewBitmap(View view) {
        Rect stickyViewBounds = new Rect(0, 0, view.getRight(), view.getBottom());

        Bitmap bitmap = Bitmap.createBitmap(stickyViewBounds.width(), stickyViewBounds.height(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        BitmapDrawable retDrawable = new BitmapDrawable(view.getResources(), bitmap);
        retDrawable.setBounds(stickyViewBounds);

        return retDrawable;
    }




    private class StickyViewScrollListener extends RecyclerView.OnScrollListener {
        private long currentStickyId = 0;

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            View firstVisible = findFirstVisibleView(recyclerView);
            if (firstVisible == null) {
                return;
            }

            //Retrieves the type of view and makes sure we only use the header views
            Integer type = (Integer)firstVisible.getTag(R.id.sticky_view_type_tag);
            if (type == null || type != RecyclerHeaderAdapter.VIEW_TYPE_HEADER) {
                return;
            }

            //TODO: we need to animate/scroll with the view
            Long headerId = (Long)firstVisible.getTag(R.id.sticky_view_header_id);
            if (headerId != null && headerId != currentStickyId) {
                stickyItem = createStickyViewBitmap(firstVisible);
                currentStickyId = headerId;
            }
        }

        @Nullable
        private View findFirstVisibleView(RecyclerView recyclerView) {
            int attachedViewCount = recyclerView.getLayoutManager().getChildCount();
            if (attachedViewCount <= 0) {
                return null;
            }

            View firstView = null;
            int[] windowLocation = new int[2];
            int currentMinPosition = Integer.MAX_VALUE;

            //Iterates through all the visible views, finding the first (topmost or leftmost) one
            for (int viewIndex = 0; viewIndex < attachedViewCount; viewIndex++) {
                View view = recyclerView.getLayoutManager().getChildAt(viewIndex);
                view.getLocationInWindow(windowLocation);

                //TODO: currently only vertical
                int startLoc = windowLocation[1];

                //Performs the comparison to determine if the current view is before all others
                if (view.getVisibility() == View.VISIBLE && startLoc < currentMinPosition) {
                    currentMinPosition = startLoc;
                    firstView = view;
                }

                //We can exit early when the view has to be the first one
                if (currentMinPosition <= 0) {
                    break;
                }
            }

            return firstView;
        }
    }
}