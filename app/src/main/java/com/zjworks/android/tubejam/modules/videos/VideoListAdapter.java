package com.zjworks.android.tubejam.modules.videos;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubeThumbnailLoader;
import com.google.android.youtube.player.YouTubeThumbnailView;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import com.zjworks.android.tubejam.R;
import com.zjworks.android.tubejam.utils.Config;
import com.zjworks.android.tubejam.utils.TubeJamUtils;

import java.util.List;

/**
 * Created by nemay5131 on 2017-10-25.
 */

public class VideoListAdapter extends RecyclerView.Adapter<VideoListAdapter.VideoListAdapterViewHolder> {
    private Context mContext;
    private VideoListResponse mVideoListResposne;
    private List<Video> responseVideos;

    // For Toast instance reference
    private Toast[] mToasts;

    public VideoListAdapter(@NonNull Context context) {
        mContext = context;
        mVideoListResposne = null;
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
        holder.thumbnailView.initialize(Config.DEVELOPER_KEY,
                new YouTubeThumbnailView.OnInitializedListener() {

                    /******************************************************************
                     *           YouTubeThumbnailView.OnInitializedListener           *
                     ******************************************************************/
                    @Override
                    public void onInitializationSuccess(YouTubeThumbnailView youTubeThumbnailView,
                                                        final YouTubeThumbnailLoader youTubeThumbnailLoader) {

                        // Set thumbnail loader listener
                        youTubeThumbnailLoader.setOnThumbnailLoadedListener(
                                new YouTubeThumbnailLoader.OnThumbnailLoadedListener() {

                                    /******************************************************************
                                     *        YouTubeThumbnailLoader.OnThumbnailLoadedListenr         *
                                     ******************************************************************/
                                    @Override
                                    public void onThumbnailLoaded(YouTubeThumbnailView youTubeThumbnailView,
                                                                  String s) {
                                        youTubeThumbnailLoader.release();
                                    }

                                    @Override
                                    public void onThumbnailError(YouTubeThumbnailView youTubeThumbnailView,
                                                                 YouTubeThumbnailLoader.ErrorReason errorReason) {
                                        Toast.makeText(mContext.getApplicationContext(),
                                                "Thumbnail load error", Toast.LENGTH_SHORT).show();
                                    }
                                }
                        );

                        // Set up the video to load
                        String videoId = responseVideos.get(position).getId();
                        youTubeThumbnailLoader.setVideo(videoId);
                    }

                    @Override
                    public void onInitializationFailure(YouTubeThumbnailView youTubeThumbnailView,
                                                        YouTubeInitializationResult errorReason) {
                        String errorMessage =
                                String.format("onInitializationFailure (%1$s)",
                                        errorReason.toString());
                        Toast.makeText(mContext.getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
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
        mVideoListResposne = videoListResponse;
        responseVideos = videoListResponse.getItems();
        notifyDataSetChanged();
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


}
