package com.team.abc;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

/**
 * Load more data when scroll end position of item in recyclerView
 */
public abstract class EndLessScrollListener extends RecyclerView.OnScrollListener {
    private int mPreviousTotal;
    private boolean mIsLoading = true;

    public abstract void seeMore();

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        int totalItem = recyclerView.getAdapter().getItemCount();
        int firstVisibleItem = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
        int countItemVisibleOnUI = recyclerView.getChildCount();

        if (mIsLoading) {
            if (mPreviousTotal < totalItem) {
                mPreviousTotal = totalItem;
                mIsLoading = false;
            }
        }

        if (!mIsLoading && mPreviousTotal - countItemVisibleOnUI <= firstVisibleItem + countItemVisibleOnUI) {
            seeMore();
            mIsLoading = true;
        }
    }

    public void resetData() {
        mPreviousTotal = 0;
        mIsLoading = true;
    }
}
