package com.ronakmanglani.watchlist.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.toolbox.NetworkImageView;
import com.ronakmanglani.watchlist.R;
import com.ronakmanglani.watchlist.api.ApiHelper;
import com.ronakmanglani.watchlist.api.VolleySingleton;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PhotoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    public ArrayList<String> photoList;
    private final OnPhotoClickListener onPhotoClickListener;

    // Constructor
    public PhotoAdapter(Context context, ArrayList<String> photoList, OnPhotoClickListener onPhotoClickListener) {
        this.context = context;
        this.photoList = photoList;
        this.onPhotoClickListener = onPhotoClickListener;
    }

    // RecyclerView methods
    @Override
    public int getItemCount() {
        return photoList.size();
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewGroup v = (ViewGroup) LayoutInflater.from(parent.getContext()).inflate(R.layout.item_photo, parent, false);
        return new PhotoViewHolder(v, onPhotoClickListener);
    }
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        PhotoViewHolder holder = (PhotoViewHolder) viewHolder;
        int imageSize = (int) context.getResources().getDimension(R.dimen.photo_item_width);
        holder.photoImage.setImageUrl(ApiHelper.getImageURL(photoList.get(position), imageSize), VolleySingleton.getInstance(context).imageLoader);
    }

    // ViewHolder
    public class PhotoViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.photo_item)  View photoItem;
        @BindView(R.id.photo_image) NetworkImageView photoImage;

        public PhotoViewHolder(final ViewGroup itemView, final OnPhotoClickListener onPhotoClickListener) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            photoItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onPhotoClickListener.onPhotoClicked(getAdapterPosition());
                }
            });
        }
    }

    // Click listener interface
    public interface OnPhotoClickListener {
        void onPhotoClicked(final int position);
    }
}