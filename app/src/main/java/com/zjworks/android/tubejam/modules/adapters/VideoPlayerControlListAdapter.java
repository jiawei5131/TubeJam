package com.zjworks.android.tubejam.modules.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.api.services.youtube.model.CommentThread;
import com.google.api.services.youtube.model.CommentThreadListResponse;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.SearchResultSnippet;
import com.google.api.services.youtube.model.Thumbnail;
import com.google.api.services.youtube.model.ThumbnailDetails;
import com.zjworks.android.tubejam.R;
import com.zjworks.android.tubejam.data.TubeJamVideo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nemay5131 on 2018-01-24.
 *
 * This class will be responsible for displaying all complicated information including introduction,
 * related videos list, comments etc of a youtube video in a single swipe list.
 */

public class VideoPlayerControlListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final int VIEW_TYPE_TITLE = 1;
    public static final int VIEW_TYPE_CONTROL_TAB = 2;
    public static final int VIEW_TYPE_CHANEL = 3;
    public static final int VIEW_TYPE_INTRO = 4;
    public static final int VIEW_TYPE_RELATED_VIDEO = 5;
    public static final int VIEW_TYPE_COMMENT = 6;

    private static final int POSITION_TITLE = 0;
    private static final int POSITION_CONTROL_TAB = 1;
    private static final int POSITION_CHANEL = 2;
    private static final int POSITION_INTRO = 3;
    private static final int POSITION_FIRST_RELATED_VIDEO = 4;

    private static final int MAX_RELATED_VIDEO_NUM = 15;

    private static final String SEARCH_RESULT_ID_KIND_VIDEO = "youtube#video";

    private static final int LIST_BASE_LENGTH = 4;  // title, control tab, chanel, intro
    private List<SearchResult> mRelatedVideos;
    private List<CommentThread> mCommentThreads;

    private final Context mContext;
    private TubeJamVideo mVideoModel;

    private TitleViewHolder mTitleViewHolder;
    private ControlTabViewHolder mControlTabViewHolder;
    private ChanelViewHolder mChanelViewHolder;
    private IntroViewHolder mIntroViewHolder;


    public VideoPlayerControlListAdapter(Context context, TubeJamVideo video) {
        this.mContext = context;
        this.mVideoModel = video;
    }


    /**
     This gets called when each new ViewHolder is created. This happens when the RecyclerView
     * is laid out. Enough ViewHolders will be created to fill the screen and allow for scrolling.
     *
     * @param parent The parent that these ViewHolders are contained within.
     * @param viewType  The type of the view holder
     * @return A new view holder that holds the View for each list item according to its viewType
     */
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layoutId;
        View view;
        RecyclerView.ViewHolder viewHolder;

        switch (viewType) {
            case VIEW_TYPE_TITLE: {
                layoutId = R.layout.video_control_list_item_title;
                view = LayoutInflater.from(mContext).inflate(layoutId, parent, false);
                view.setFocusable(true);

                viewHolder = new TitleViewHolder(view);
                mTitleViewHolder = (TitleViewHolder) viewHolder;
                break;
            }

            case VIEW_TYPE_CONTROL_TAB: {
                layoutId = R.layout.video_control_list_item_control_tab;
                view = LayoutInflater.from(mContext).inflate(layoutId, parent, false);
                view.setFocusable(true);

                viewHolder = new ControlTabViewHolder(view);
                mControlTabViewHolder = (ControlTabViewHolder) viewHolder;
                break;
            }

            case VIEW_TYPE_CHANEL: {
                layoutId = R.layout.video_control_list_item_chanel;
                view = LayoutInflater.from(mContext).inflate(layoutId, parent, false);
                view.setFocusable(false);

                viewHolder = new ChanelViewHolder(view);
                mChanelViewHolder = (ChanelViewHolder) viewHolder;
                break;
            }

            case VIEW_TYPE_INTRO: {
                layoutId = R.layout.video_control_list_item_intro;
                view = LayoutInflater.from(mContext).inflate(layoutId, parent, false);
                view.setFocusable(false);

                viewHolder = new IntroViewHolder(view);
                mIntroViewHolder = (IntroViewHolder) viewHolder;
                break;
            }

            case VIEW_TYPE_RELATED_VIDEO: {
                layoutId = R.layout.video_control_list_item_related_video;
                view = LayoutInflater.from(mContext).inflate(layoutId, parent, false);
                view.setFocusable(true);
                viewHolder = new RelatedVideoViewHolder(view);
                break;
            }

            case VIEW_TYPE_COMMENT: {
                layoutId = R.layout.video_control_list_item_comment;
                view = LayoutInflater.from(mContext).inflate(layoutId, parent, false);
                view.setFocusable(true);
                viewHolder = new CommentViewHolder(view);
                break;
            }

            default:
                throw new IllegalArgumentException("Invalid view type, value of " + viewType);
        }

        return viewHolder;
    }


    /**
     * This is called when the list item at position is to be displayed. We want to bind correct
     * data for display. For each type of holder, there is different information to bind.
     *
     * @param holder the view holder to display the information
     * @param position the position of the holder
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);

        switch (viewType) {
            case VIEW_TYPE_TITLE: {
                ((TitleViewHolder) holder).displayTitle(mVideoModel);
                break;
            }

            case VIEW_TYPE_CONTROL_TAB: {
                ((ControlTabViewHolder) holder).displayControlTab(mVideoModel);
                break;
            }

            case VIEW_TYPE_CHANEL: {
                ((ChanelViewHolder) holder).displayChanel(mVideoModel);
                break;
            }

            case VIEW_TYPE_INTRO: {
                ((IntroViewHolder) holder).displayIntro(mVideoModel);
                break;
            }

            case VIEW_TYPE_RELATED_VIDEO: {
                ((RelatedVideoViewHolder) holder).displayRelatedVideo(position);
                break;
            }

            case VIEW_TYPE_COMMENT: {
                ((CommentViewHolder) holder).displayComment(position);
                break;
            }
        }
    }

    @Override
    public int getItemCount() {
        int count = LIST_BASE_LENGTH;

        if (mRelatedVideos != null) {
            count += mRelatedVideos.size();
        }

        if (mCommentThreads != null) {
            count += mCommentThreads.size();
        }

        return count;
    }


    /**
     * Get the view type of the view holder at a given position.
     * @param position  a 0 based int position
     * @return  view type of the item at the position
     */
    @Override
    public int getItemViewType(int position) {
        if (position == POSITION_TITLE) {
            return VIEW_TYPE_TITLE;
        } else if (position == POSITION_CONTROL_TAB) {
            return VIEW_TYPE_CONTROL_TAB;
        } else if (position == POSITION_CHANEL) {
            return VIEW_TYPE_CHANEL;
        } else if (position == POSITION_INTRO) {
            return VIEW_TYPE_INTRO;
        } else if (mRelatedVideos != null
                && position >= POSITION_FIRST_RELATED_VIDEO
                && position <= POSITION_FIRST_RELATED_VIDEO + mRelatedVideos.size() - 1) {
            return VIEW_TYPE_RELATED_VIDEO;
        }

        return VIEW_TYPE_COMMENT;
    }


    /**
     * Update the response search results of related videos, and reflect the results on the display
     * @param searchListResponse
     */
    public void updateRelatedVideos(SearchListResponse searchListResponse) {
        int oldLen = getItemCount(), newLen;
        mRelatedVideos = new ArrayList<SearchResult>();

        for (SearchResult result : searchListResponse.getItems()) {
            if (result.getId().getKind().equals(SEARCH_RESULT_ID_KIND_VIDEO)) {
                mRelatedVideos.add(result.clone());
            }
        }

        // notify that the list has been updated
        newLen = getItemCount();
        notifyItemRangeInserted(oldLen, newLen);
    }


    /**
     * Update the list of comment thread in this list so that the data reflects comments on the display
     * @param commentThreadListResponse
     */
    public void updateCommentThreads(CommentThreadListResponse commentThreadListResponse) {
        int oldLen = getItemCount(), newLen;
        mCommentThreads = commentThreadListResponse.getItems();

        // notify that the list has been updated
        newLen = getItemCount();
        notifyItemRangeInserted(oldLen, newLen);
    }

    /******************************************************************
     *                           View Holders                         *
     *      Multiple view holders for a complex list in the player    *
     *  fragment                                                      *
     ******************************************************************/

    /**
     * This list item is responsible for displaying the title of the video
     */
    public class TitleViewHolder extends RecyclerView.ViewHolder
                                    implements View.OnClickListener {
        private TextView mTitleTextView;

        public TitleViewHolder(View itemView) {
            super(itemView);

            mTitleTextView = itemView.findViewById(R.id.tv_video_player_title);
            mTitleTextView.setOnClickListener(TitleViewHolder.this);
        }

        private void displayTitle(TubeJamVideo video) {
            mTitleTextView.setText(video.getTitle());
        }


        @Override
        public void onClick(View view) {
            if (mIntroViewHolder != null) {
                mIntroViewHolder.toggleDescriptionVisibility();
            }
        }
    }


    /**
     * This list item is responsible for displaying the control tab of the video, including
     * like, dislike, share, add to list etc. This item will be able to interact with the
     * user
     */
    public class ControlTabViewHolder extends RecyclerView.ViewHolder {
        private TextView mLikeTextView, mDislikeTextView, mShareTextView, mAddToListTextView;

        public ControlTabViewHolder(View itemView) {
            super(itemView);

            mLikeTextView = itemView.findViewById(R.id.tv_video_player_like);
            mDislikeTextView = itemView.findViewById(R.id.tv_video_player_dislike);
            mShareTextView = itemView.findViewById(R.id.tv_video_player_share);
            mAddToListTextView = itemView.findViewById(R.id.tv_video_player_add_to_list);
        }

        private void displayControlTab(TubeJamVideo video) {
            mLikeTextView.setText(video.getLikeCount());
            mDislikeTextView.setText(video.getDislikeCount());
        }
    }


    /**
     * This list item is responsible for displaying the chanel or the author of the video.
     */
    public class ChanelViewHolder extends RecyclerView.ViewHolder {
        private TextView mChanelTitleTextView;

        public ChanelViewHolder(View itemView) {
            super(itemView);

            mChanelTitleTextView = itemView.findViewById(R.id.tv_video_player_chanel_title);
        }

        private void displayChanel(TubeJamVideo video) {
            mChanelTitleTextView.setText(video.getChannelName());
        }
    }


    /**
     * This list item is responsible for displaying the detailed description of the video
     */
    public class IntroViewHolder extends RecyclerView.ViewHolder {
        private TextView mReleaseDateTextView, mDescriptionTextView;

        public IntroViewHolder(View itemView) {
            super(itemView);

            mReleaseDateTextView = itemView.findViewById(R.id.tv_video_player_release_date);
            mDescriptionTextView = itemView.findViewById(R.id.tv_video_player_description);
            mDescriptionTextView.setVisibility(View.GONE);
        }

        private void displayIntro(TubeJamVideo video) {
            mReleaseDateTextView.setText(video.getPublishDate().toString());
            mDescriptionTextView.setText(video.getDescription());
        }

        public void toggleDescriptionVisibility() {
            if (mDescriptionTextView.getVisibility() == View.GONE) {
                mDescriptionTextView.setVisibility(View.VISIBLE);
            } else {
                mDescriptionTextView.setVisibility(View.GONE);
            }
        }
    }


    /**
     * This list item is responsible for displaying a video that is related to the playing video.
     * The displayed information will include a snap, title, author and play count.
     */
    public class RelatedVideoViewHolder extends RecyclerView.ViewHolder {
        private TextView mRelatedVideoTitleTextView, mRelatedVIdeoAuthorTextView;
        private ImageView mRelatedVideoThumbnail;

        public RelatedVideoViewHolder(View itemView) {
            super(itemView);
            mRelatedVideoTitleTextView = itemView.findViewById(R.id.tv_video_player_related_video_title);
            mRelatedVideoThumbnail = itemView.findViewById(R.id.iv_video_player_related_video_thumbnail);
        }

        private void displayRelatedVideo(int position) {
            SearchResult result = mRelatedVideos.get(getVideoPosition(position));
            SearchResultSnippet snippet = result.getSnippet();
            Thumbnail thumbnail = getSuitableRelatedVideoThumbnail(snippet.getThumbnails());

            mRelatedVideoTitleTextView.setText(snippet.getTitle());
            Glide.with(mContext)
                    .load(thumbnail.getUrl())
                    .into(mRelatedVideoThumbnail);
        }

        /**
         * Get the correct position of the video in the list of mRelatedVideos
         * @param position
         * @return
         */
        private int getVideoPosition(int position) {
            return (position - POSITION_FIRST_RELATED_VIDEO);
        }


        /**
         * Get a reasonable resolution thumbnail
         * @param thumbnailDetails
         * @return
         */
        private Thumbnail getSuitableRelatedVideoThumbnail(ThumbnailDetails thumbnailDetails) {
            if (thumbnailDetails.getMedium() != null) {
                return thumbnailDetails.getMedium();
            }

            // default
            return thumbnailDetails.getDefault();
        }
    }


    /**
     * This list item is responsible for displaying a comment of the video.
     * The displayed information will include contents of the comment, author, publish date etc.
     */
    public class CommentViewHolder extends RecyclerView.ViewHolder {
        private TextView mCommentTextView;

        public CommentViewHolder(View itemView) {
            super(itemView);

            mCommentTextView = itemView.findViewById(R.id.tv_video_player_comment_content);
        }

        private void displayComment(int position) {
            CommentThread commentThread = mCommentThreads.get(getCommentPosition(position));
            mCommentTextView.setText(
                    commentThread
                    .getSnippet()
                    .getTopLevelComment()
                    .getSnippet()
                    .getTextDisplay());
        }

        private int getCommentPosition(int position) {
            int relatedVideoLen = mRelatedVideos == null ? 0 : mRelatedVideos.size();
            return (position - POSITION_FIRST_RELATED_VIDEO - relatedVideoLen);
        }
    }
}
