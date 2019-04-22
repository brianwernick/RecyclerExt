package androidx.recyclerview.widget;

import androidx.recyclerview.widget.RecyclerView;

public class StatefulItemTouchHelper extends ItemTouchHelper {

    private int actionState = ItemTouchHelper.ACTION_STATE_IDLE;
    private OnActionStateChangedListener onActionStateChangedListener;


    public StatefulItemTouchHelper(Callback callback) {
        super(callback);
    }

    @Override
    void select(RecyclerView.ViewHolder selected, int actionState) {
        if (this.actionState != actionState) {
            if (onActionStateChangedListener != null) {
                onActionStateChangedListener.onActionStateChanged(actionState);
            }
        }
        this.actionState = actionState;
        super.select(selected, actionState);
    }

    public void setOnActionStateChangedListener(OnActionStateChangedListener onActionStateChangedListener) {
        this.onActionStateChangedListener = onActionStateChangedListener;
    }

    public interface OnActionStateChangedListener {
        void onActionStateChanged(int actionState);
    }
}
