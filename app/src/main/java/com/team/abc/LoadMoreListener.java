package com.team.abc;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;


public abstract class LoadMoreListener extends RecyclerView.OnScrollListener {
    private int mPreviousTotal;
    private boolean mLoading = true;
    private int mThreshold;

    public LoadMoreListener(int threshold) {
        mThreshold = threshold;
    }

    public abstract void onLoadMore();

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        int visibleItemCount = recyclerView.getChildCount();
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        int totalItemCount = layoutManager.getItemCount();
        int firstVisibleItem = layoutManager.findFirstVisibleItemPosition();

        if (mLoading) {
            if (totalItemCount > mPreviousTotal) {
                mLoading = false;
                mPreviousTotal = totalItemCount;
            }
        }
        if (!mLoading && (totalItemCount - visibleItemCount)
                <= (firstVisibleItem + mThreshold)) {
            onLoadMore();
            mLoading = true;
        }
    }

    public void resetValue() {
        mPreviousTotal = 0;
        mLoading = true;
    }
}
