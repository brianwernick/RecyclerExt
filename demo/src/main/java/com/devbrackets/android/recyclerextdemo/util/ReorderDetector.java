package com.devbrackets.android.recyclerextdemo.util;

import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;

/**
 * TODO: this may be useful to add to the library
 */
public class ReorderDetector implements View.OnTouchListener {
    private static final int LONG_CLICK_MIN_DURATION = 500; //500 ms

    private boolean depressed;
    private boolean stationary = true;
    private Handler delayHandler = new Handler();

    private ReorderDetectorListener listener;

    public ReorderDetector(ReorderDetectorListener reorderDetectorListener) {
        listener = reorderDetectorListener;
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                depressed = true;
                stationary = true;
                startLongClickTimeout(view);
                break;

            case MotionEvent.ACTION_UP:
                depressed = false;
                cancelLongClickTimeout();

                if (stationary) {
                    listener.onClick(view);
                } else {
                    listener.onDone(view);
                }
                break;

            case MotionEvent.ACTION_MOVE:
                stationary = false;
                cancelLongClickTimeout();
                listener.onMove(view, event);
                break;

            case MotionEvent.ACTION_CANCEL:
                depressed = false;
                cancelLongClickTimeout();
                listener.onCancel(view);
                break;

            default:
        }

        return false;
    }

    private void startLongClickTimeout(final View view) {
        delayHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (depressed && stationary) {
                    listener.onLongClick(view);
                }
            }
        }, LONG_CLICK_MIN_DURATION);
    }

    private void cancelLongClickTimeout() {
        delayHandler.removeCallbacksAndMessages(null);
    }

    public interface ReorderDetectorListener {
        void onCancel(View view);
        void onClick(View view);
        void onDone(View view);
        void onLongClick(View view);
        void onMove(View view, MotionEvent event);
    }
}
