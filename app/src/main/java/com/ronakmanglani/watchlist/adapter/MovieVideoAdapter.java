package com.ronakmanglani.watchlist.adapter;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.ronakmanglani.watchlist.R;
import com.ronakmanglani.watchlist.model.Video;
import com.ronakmanglani.watchlist.util.VolleySingleton;

import java.util.ArrayList;

public class MovieVideoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;                                    // Context of calling activity
    private final OnItemClickListener onItemClickListener;      // Click Listener
    public ArrayList<Video> videos;                             // Videos to display

    // Constructor
    public MovieVideoAdapter(Context context,
                             ArrayList<Video> videos,
                             OnItemClickListener onItemClickListener) {
        this.context = context;
        this.videos = videos;
        this.onItemClickListener = onItemClickListener;
    }

    // Returns size of ArrayList
    @Override
    public int getItemCount() {
        return videos.size();
    }

    // Inflate layout
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewGroup v = (ViewGroup) LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video, parent, false);
        return new VideoViewHolder(v, onItemClickListener);
    }

    // Insert data into the layout
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        Video video = videos.get(position);
        VideoViewHolder holder = (VideoViewHolder) viewHolder;
        holder.videoImage.setImageUrl(video.imageURL, VolleySingleton.getInstance(context).imageLoader);
        holder.videoTitle.setText(video.title);
    }

    // ViewHolder for Listing
    public class VideoViewHolder extends RecyclerView.ViewHolder {
        final CardView videoItem;
        final TextView videoTitle;
        final NetworkImageView videoImage;

        public VideoViewHolder(final ViewGroup itemView, final OnItemClickListener onItemClickListener) {
            super(itemView);
            videoItem = (CardView) itemView.findViewById(R.id.video_item);
            videoTitle = (TextView) itemView.findViewById(R.id.video_name);
            videoImage = (NetworkImageView) itemView.findViewById(R.id.video_image);

            videoItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onItemClickListener.onItemClicked(getAdapterPosition());
                }
            });
        }
    }

    // Interface to respond to clicks
    public interface OnItemClickListener {
        void onItemClicked(final int position);
    }
}