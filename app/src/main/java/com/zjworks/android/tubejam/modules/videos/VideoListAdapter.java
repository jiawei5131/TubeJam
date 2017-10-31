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
    private VideoListResponse mVideoListResponse;
    private List<Video> responseVideos;

    // For Toast instance reference
    private Toast[] mToasts;

    public VideoListAdapter(@NonNull Context context) {
        mContext = context;
        mVideoListResponse = null;
        responseVideos = null;
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
        ImageView imageView = holder.thumbnailView;
        ThumbnailDetails thumbnailDetails = responseVideos.get(position).getSnippet().getThumbnails();
        Thumbnail thumbnail = getHighestResolutionThumbnail(thumbnailDetails);

        Glide.with(mContext)
                .load(thumbnail.getUrl())
                .into(imageView);
    }

    @Override
    public int getItemCount() {
        if (responseVideos != null) {
            return responseVideos.size();
        }
        return 0;
    }


    /******************************************************************
     *                        Helper Functions                        *
     ******************************************************************/

    public void swapVideoListResponse(VideoListResponse videoListResponse) {
        mVideoListResponse = videoListResponse;
        responseVideos = videoListResponse.getItems();
        notifyDataSetChanged();
    }

    public void appendVideoListResponse(VideoListResponse videoListResponse) {
        int oldLast = responseVideos.size();
        int newItemCount = videoListResponse.getItems().size();

        mVideoListResponse = videoListResponse;
        responseVideos.addAll(videoListResponse.getItems());
        notifyItemRangeInserted(oldLast, newItemCount);
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
        final ImageView thumbnailView;

        VideoListAdapterViewHolder(View itemView) {
            super(itemView);
            thumbnailView = itemView.findViewById(R.id.iv_master_video_list_item_thumbnail);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            String message = "Item " + getAdapterPosition() + " Clicked!";
            TubeJamUtils.displayToastMessage(mContext, mToasts, message);
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
