package com.zjworks.android.tubejam.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerFragment;
import com.google.api.services.youtube.model.CommentThreadListResponse;
import com.google.api.services.youtube.model.SearchListResponse;
import com.zjworks.android.tubejam.R;
import com.zjworks.android.tubejam.data.TubeJamVideo;
import com.zjworks.android.tubejam.modules.youtube_api_loaders.comment_threads.CommentThreadsListRequestLoader;
import com.zjworks.android.tubejam.modules.youtube_api_loaders.search.SearchListRequestLoader;
import com.zjworks.android.tubejam.modules.adapters.VideoPlayerControlListAdapter;
import com.zjworks.android.tubejam.utils.Config;

/**
 * Created by nemay5131 on 2017-11-21.
 */

// TODO: play youtube video here
public class VideoPlayerFragment extends Fragment {
    private YouTubePlayerFragment mYouTubePlayerFragment;
    private RecyclerView mVideoControlListRecyclerView;
    private VideoPlayerControlListAdapter mVideoPlayerControlListAdapter;
    private LinearLayoutManager mLayoutManager;
    private LoaderManager.LoaderCallbacks
            <SearchListRequestLoader.SearchListRequestResult> mSearchListRequestLoaderListener;
    private LoaderManager.LoaderCallbacks
            <CommentThreadsListRequestLoader.CommentThreadsListRequestResult> mCommentThreadsListRequestLoaderListener;

    private Context mContext;
    private Activity mActivity;
    private TubeJamVideo mVideoModel;

    private static final int SEARCH_RELATED_VIDEO_LOADER_ID = 31;
    private static final int GET_COMMENT_THREADS_BY_VIDEO_ID_LOADER_ID = 41;

    private static final String SEARCH_RELATED_VIDEO_LOADER_ARGS_VIDEO_ID_KEY
            = "search_related_video_loader_args_video_id_key";
    private static final String COMMENT_THREADS_LOADER_ARGS_VIDEO_ID_KEY
            = "comment_threads_loader_args_video_id_key";

    public VideoPlayerFragment() {}


    /**
     * When this fragment been attached to the activity, prepare for video player initialization
     *
     * @param context The context that initialized this fragment
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        mActivity = (Activity) context;
        initSearchListRequestLoaderListener();
        initCommentThreadsListRequestLoaderListener();

        try {
            mVideoModel =
                    (TubeJamVideo) mActivity
                        .getIntent()
                        .getSerializableExtra(TubeJamVideo.TUBEJAM_VIDEO_OBJECT_KEY);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }


    /**
     * Initialize the view of this video player fragment.
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_video_player, container, false);

        mYouTubePlayerFragment = (YouTubePlayerFragment) mActivity.getFragmentManager()
                .findFragmentById(R.id.fg_youtube_player);
        mVideoControlListRecyclerView = rootView.findViewById(R.id.rv_video_control_list);

        mLayoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
        mVideoControlListRecyclerView.setLayoutManager(mLayoutManager);

        return rootView;
    }


    /**
     * Load necessary data for video player, including YouTube video player and all detailed information
     * of this video.
     *
     * @param view
     * @param savedInstanceState
     */
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (mVideoModel != null) {
            // initialize an adapter for the recycler view
            mVideoPlayerControlListAdapter = new VideoPlayerControlListAdapter(mContext, mVideoModel);
            mVideoControlListRecyclerView.setAdapter(mVideoPlayerControlListAdapter);

            LoaderManager loaderManager = getLoaderManager();

            // load related videos
            Bundle searchRelatedVideoLoaderArgs = new Bundle();
            searchRelatedVideoLoaderArgs.putString(
                    SEARCH_RELATED_VIDEO_LOADER_ARGS_VIDEO_ID_KEY,
                    mVideoModel.getId());
            loaderManager.restartLoader(
                    SEARCH_RELATED_VIDEO_LOADER_ID,
                    searchRelatedVideoLoaderArgs,
                    mSearchListRequestLoaderListener);

            // TODO: load comments
            Bundle getCommentThreadsByVideoIdLoaderArgs = new Bundle();
            getCommentThreadsByVideoIdLoaderArgs.putString(
                    COMMENT_THREADS_LOADER_ARGS_VIDEO_ID_KEY,
                    mVideoModel.getId());
            loaderManager.restartLoader(
                    GET_COMMENT_THREADS_BY_VIDEO_ID_LOADER_ID,
                    getCommentThreadsByVideoIdLoaderArgs,
                    mCommentThreadsListRequestLoaderListener);

            // initialize Youtube video player
            mYouTubePlayerFragment.initialize(Config.DEVELOPER_KEY, new YouTubePlayer.OnInitializedListener() {
                @Override
                public void onInitializationSuccess(YouTubePlayer.Provider provider,
                                                    YouTubePlayer youTubePlayer,
                                                    boolean wasRestored) {
                    // The player was not restored from a previously saved state
                    if (!wasRestored) {
                        youTubePlayer.cueVideo(mVideoModel.getId());
                    }
                }

                @Override
                public void onInitializationFailure(YouTubePlayer.Provider provider,
                                                    YouTubeInitializationResult youTubeInitializationResult) {
                    
                }
            });
        }
    }


    /******************************************************************
     *             LoaderCallbacks SearchListRequestLoader            *
     *  To load a Search List response from YouTube API.              *
     ******************************************************************/

    private void initSearchListRequestLoaderListener() {
        mSearchListRequestLoaderListener = new LoaderManager.LoaderCallbacks<SearchListRequestLoader.SearchListRequestResult>() {
            @Override
            public Loader<SearchListRequestLoader.SearchListRequestResult> onCreateLoader(int id, Bundle args) {
                SearchListRequestLoader.Builder searchListBuilder = new SearchListRequestLoader.Builder(mContext);

                switch (id) {
                    case SEARCH_RELATED_VIDEO_LOADER_ID: {
                        return searchListBuilder.buildSearchRelatedVideoLoader(
                                args.getString(SEARCH_RELATED_VIDEO_LOADER_ARGS_VIDEO_ID_KEY));
                    }

                    default:
                        throw new IllegalArgumentException("Invalid loader id");
                }
            }

            @Override
            public void onLoadFinished(Loader<SearchListRequestLoader.SearchListRequestResult> loader,
                                       SearchListRequestLoader.SearchListRequestResult data) {
                SearchListResponse response = data.getResultResponse();
                Exception error = data.getError();
                int id = loader.getId();

                if (error != null) {
                    // handel exception
                    return;
                }

                switch (id) {
                    case SEARCH_RELATED_VIDEO_LOADER_ID: {
                        mVideoPlayerControlListAdapter.updateRelatedVideos(response);
                        break;
                    }
                }

            }

            @Override
            public void onLoaderReset(Loader<SearchListRequestLoader.SearchListRequestResult> loader) {

            }
        };
    }


    /******************************************************************
     *          LoaderCallbacks CommentThreadsListRequestLoader       *
     *  To load a CommentThreads list response from YouTube API.      *
     ******************************************************************/

    private void initCommentThreadsListRequestLoaderListener() {
        mCommentThreadsListRequestLoaderListener = new LoaderManager.LoaderCallbacks
                <CommentThreadsListRequestLoader.CommentThreadsListRequestResult>() {

            @Override
            public Loader<CommentThreadsListRequestLoader.CommentThreadsListRequestResult> onCreateLoader(int id, Bundle args) {
                CommentThreadsListRequestLoader.Builder commentThreadsListLoaderBuilder =
                        new CommentThreadsListRequestLoader.Builder(mContext);

                switch (id) {
                    case GET_COMMENT_THREADS_BY_VIDEO_ID_LOADER_ID: {
                        return commentThreadsListLoaderBuilder
                                .buildCommentThreadsListByVideoIdRequestLoader(
                                        args.getString(COMMENT_THREADS_LOADER_ARGS_VIDEO_ID_KEY));
                    }
                }
                return null;
            }

            @Override
            public void onLoadFinished(Loader<CommentThreadsListRequestLoader.CommentThreadsListRequestResult> loader,
                                       CommentThreadsListRequestLoader.CommentThreadsListRequestResult data) {
                CommentThreadListResponse result = data.getResultResponse();
                Exception error = data.getError();
                int id = loader.getId();

                if (error != null) {
                    // handel error
                    return;
                }

                switch (id) {
                    case GET_COMMENT_THREADS_BY_VIDEO_ID_LOADER_ID: {
                        mVideoPlayerControlListAdapter.updateCommentThreads(result);
                        break;
                    }
                }
            }

            @Override
            public void onLoaderReset(Loader<CommentThreadsListRequestLoader.CommentThreadsListRequestResult> loader) {

            }
        };
    }



    /******************************************************************
     *                          Helper Methods                        *
     ******************************************************************/
    public void handelLoadRelatedVideoFinished(SearchListResponse result) {

    }


}
