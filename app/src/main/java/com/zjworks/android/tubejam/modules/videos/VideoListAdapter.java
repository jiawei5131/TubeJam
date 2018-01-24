package com.zjworks.android.tubejam.modules.videos;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.api.services.youtube.model.Thumbnail;
import com.google.api.services.youtube.model.ThumbnailDetails;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import com.zjworks.android.tubejam.R;
import com.zjworks.android.tubejam.utils.TubeJamUtils;

import java.util.List;

/**
 * Created by nemay5131 on 2017-10-25.
 */

public class VideoListAdapter extends RecyclerView.Adapter<VideoListAdapter.VideoListAdapterViewHolder> {

    private Context mContext;
    private OnVideoListItemClickedListener mOnVideoListItemClickedListener;
    private VideoListResponse mVideoListResponse;
    private List<Video> mResponseVideos;

    // For Toast instance reference
    private Toast[] mToasts;

    /******************************************************************
     *           Interface OnVideoListItemClickedListener             *
     ******************************************************************/
    public interface OnVideoListItemClickedListener {
        void onVideoListItemClicked(Video item);
    }


    public VideoListAdapter(@NonNull Context context, OnVideoListItemClickedListener listener) {
        mContext = context;
        mOnVideoListItemClickedListener = listener;
        mVideoListResponse = null;
        mResponseVideos = null;
    }


    /**
     * Inflate a view that display video thumbnail and title, and initialize a view holder
     *
     * @param parent
     * @param viewType
     * @return
     */
    @Override
    public VideoListAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Initialize Toast instance reference
        mToasts = new Toast[1];

        // Set up a recycler view
        View videoListItemView = LayoutInflater
                                 .from(mContext)
                                 .inflate(R.layout.master_video_list_item, parent, false);

        videoListItemView.setFocusable(true);
        return new VideoListAdapterViewHolder(videoListItemView);
    }


    /**
     * Initialize the thumbnail image and other information.
     *
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(VideoListAdapterViewHolder holder, final int position) {
        ImageView imageView = holder.mThumbnailView;
        ThumbnailDetails thumbnailDetails = mResponseVideos.get(position).getSnippet().getThumbnails();
        Thumbnail thumbnail = getHighestResolutionThumbnail(thumbnailDetails);

        Glide.with(mContext)
                .load(thumbnail.getUrl())
                .into(imageView);
    }

    @Override
    public int getItemCount() {
        if (mResponseVideos != null) {
            return mResponseVideos.size();
        }
        return 0;
    }


    /******************************************************************
     *                        Helper Functions                        *
     ******************************************************************/

    public void swapVideoListResponse(VideoListResponse videoListResponse) {
        mVideoListResponse = videoListResponse;
        mResponseVideos = videoListResponse == null ? null : videoListResponse.getItems();
        notifyDataSetChanged();
    }

    public void appendVideoListResponse(VideoListResponse videoListResponse) {
        int oldVideosSize = 0, newVideosSize = 0;

        if (videoListResponse != null) {
            List<Video> newVideos = videoListResponse.getItems();
            newVideosSize = newVideos.size();

            if (mResponseVideos != null ) {
                // Append to the original list
                oldVideosSize = mResponseVideos.size();
                mResponseVideos.addAll(newVideos);
            } else {
                // mResponseVideos == null
                mResponseVideos = newVideos;
            }
        }

        mVideoListResponse = videoListResponse;
        notifyItemRangeInserted(oldVideosSize, newVideosSize);
    }

    @NonNull
    public VideoListResponse getVIdeoListResponse() {
        return mVideoListResponse;
    }

    /******************************************************************
     *                           View Holder                          *
     ******************************************************************/

    /**
     * View holder that holds a list item that shows thumbnail and information of a YouTube video
     */
    class VideoListAdapterViewHolder extends RecyclerView.ViewHolder
                                     implements View.OnClickListener {
        final ImageView mThumbnailView;

        VideoListAdapterViewHolder(View itemView) {
            super(itemView);
            mThumbnailView = itemView.findViewById(R.id.iv_master_video_list_item_thumbnail);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            String message = "Item " + getAdapterPosition() + " Clicked!";
            TubeJamUtils.displayToastMessage(mContext, mToasts, message);
            int position = getAdapterPosition();

            // start player fragment
            mOnVideoListItemClickedListener.onVideoListItemClicked(mResponseVideos.get(position));
        }
    }


    private Thumbnail getHighestResolutionThumbnail(ThumbnailDetails thumbnailDetails) {
        if (thumbnailDetails.getStandard() != null) {
            // standard
            return thumbnailDetails.getStandard();
        } else if (thumbnailDetails.getHigh() != null) {
            // high
            return thumbnailDetails.getHigh();
        } else if (thumbnailDetails.getMedium() != null) {
            // medium
            return  thumbnailDetails.getMedium();
        }
        // default
        return thumbnailDetails.getDefault();
    }


}
