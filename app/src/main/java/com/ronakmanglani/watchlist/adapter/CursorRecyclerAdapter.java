package com.ronakmanglani.watchlist.adapter;

import android.content.Context;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.support.v7.widget.RecyclerView;

public abstract class CursorRecyclerAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH>{

    private static final String LOG_TAG = CursorRecyclerAdapter.class.getSimpleName();

    private int rowIdColumn;
    private boolean dataIsValid;

    private Cursor mCursor;
    private DataSetObserver mDataSetObserver;

    public CursorRecyclerAdapter(Context context, Cursor cursor) {
        mCursor = cursor;
        dataIsValid = cursor != null;
        rowIdColumn = dataIsValid ? mCursor.getColumnIndex("_id") : -1;
        mDataSetObserver = new NotifyingDataSetObserver();
        if (dataIsValid) {
            mCursor.registerDataSetObserver(mDataSetObserver);
        }
    }

    public Cursor getCursor() {
        return mCursor;
    }
    public Cursor swapCursor(Cursor newCursor) {
        if (newCursor == mCursor) {
            return null;
        }
        final Cursor oldCursor = mCursor;
        if (oldCursor != null && mDataSetObserver != null) {
            oldCursor.unregisterDataSetObserver(mDataSetObserver);
        }
        mCursor = newCursor;
        if (mCursor != null) {
            if (mDataSetObserver != null) {
                mCursor.registerDataSetObserver(mDataSetObserver);
            }
            rowIdColumn = newCursor.getColumnIndexOrThrow("_id");
            dataIsValid = true;
            notifyDataSetChanged();
        } else {
            rowIdColumn = -1;
            dataIsValid = false;
            notifyDataSetChanged();
        }
        return oldCursor;
    }

    @Override
    public int getItemCount() {
        if (dataIsValid && mCursor != null) {
            return mCursor.getCount();
        }
        return 0;
    }
    @Override
    public long getItemId(int position) {
        if (dataIsValid && mCursor != null && mCursor.moveToPosition(position)){
            return mCursor.getLong(rowIdColumn);
        }
        return 0;
    }
    @Override
    public void setHasStableIds(boolean hasStableIds) {
        super.setHasStableIds(true);
    }

    @Override
    public void onBindViewHolder(VH viewHolder, int position) {
        if (!dataIsValid) {
            throw new IllegalStateException("This should only be called when Cursor is valid");
        }
        if (!mCursor.moveToPosition(position)) {
            throw new IllegalStateException("Could not move Cursor to position: " + position);
        }
        onBindViewHolder(viewHolder, mCursor);
    }
    public abstract void onBindViewHolder(VH viewHolder, Cursor cursor);

    private class NotifyingDataSetObserver extends DataSetObserver{
        @Override
        public void onChanged() {
            super.onChanged();
            dataIsValid = true;
            notifyDataSetChanged();
        }
        @Override
        public void onInvalidated() {
            super.onInvalidated();
            dataIsValid = false;
            notifyDataSetChanged();
        }
    }
}
