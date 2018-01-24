package com.zjworks.android.tubejam.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.services.youtube.model.VideoListResponse;
import com.zjworks.android.tubejam.R;
import com.zjworks.android.tubejam.activities.MainActivity;
import com.zjworks.android.tubejam.modules.videos.VideoListAdapter;
import com.zjworks.android.tubejam.modules.videos.VideoListRequestLoader;
import com.zjworks.android.tubejam.utils.TubeJamUtils;

/**
 * Created by nemay5131 on 2017-10-18.
 */

public class MasterVideoListFragment extends Fragment
                                     implements LoaderManager.LoaderCallbacks<VideoListRequestLoader.VideoListRequestResult>,
                                                SwipeRefreshLayout.OnRefreshListener {
    private SwipeRefreshLayout mSwipeContainer;
    private RecyclerView mMasterVideoListRecyclerView;
    private ProgressBar mProgressBar, mBottomProgressBar;
    private VideoListAdapter mVideoListAdapter;
    private LinearLayoutManager mLayoutManager;
    private Context mContext;
    private VideoListAdapter.OnVideoListItemClickedListener mListener;

    private int mPosition = RecyclerView.NO_POSITION;

    // Loader Key
    private static final int POPULAR_VIDEO_LIST_LOADER_ID = 21;
    private static final int POPULAR_VIDEO_LIST_LOADER_NEXT_PAGE_ID = 22;

    // Loading Mode to distinguish load new list or more
    private static final int LOAD_NEW_LIST = 0;
    private static final int LOAD_MORE = 1;
    private int mLoadingMode = 0;

    private static final String TAG = MasterVideoListFragment.class.getSimpleName();

    public MasterVideoListFragment() {}

    /**
     * Obtain information of the context that used the fragment
     * @param context
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;

        // Check if the attaching activity has implemented required interface
        try {
            mListener = (VideoListAdapter.OnVideoListItemClickedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement VideoListAdapter.OnVideoListItemClickedListener");
        }
    }


    @NonNull
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @NonNull ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_master_video_list, container, false);

        mSwipeContainer = rootView.findViewById(R.id.swipe_container);
        mMasterVideoListRecyclerView = rootView.findViewById(R.id.rv_master_video_list);
        mProgressBar = container.findViewById(R.id.progress_bar);
        mBottomProgressBar = container.getRootView().findViewById(R.id.progress_bar_on_bottom);

        // Set layout manager
        mLayoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
        mMasterVideoListRecyclerView.setLayoutManager(mLayoutManager);

        // Set has fixed length
        mMasterVideoListRecyclerView.setHasFixedSize(true);

        return rootView;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize adapter
        mVideoListAdapter = new VideoListAdapter(mContext, mListener);

        // Adapter and scroll listener
        mMasterVideoListRecyclerView.setAdapter(mVideoListAdapter);
        mMasterVideoListRecyclerView.addOnScrollListener(new OnBottomReachedListener());

        // Start loading immediately
        startLoadingNewList();

        // Set swipe to refresh container
        mSwipeContainer.setOnRefreshListener(this);
    }



    /******************************************************************
     *                        LoaderCallbacks                         *
     *  To load a video list response from YouTube API.               *
     ******************************************************************/
    private boolean isLoadingData = false;  // Prevent multiple loadings

    @Override
    public Loader<VideoListRequestLoader.VideoListRequestResult> onCreateLoader(int id, Bundle args) {
        // prevent other loaders start loading
        isLoadingData = true;
        showLoading();

        VideoListRequestLoader.Builder listRequestBuilder
                = new VideoListRequestLoader.Builder(getContext());
        switch (id) {
            case POPULAR_VIDEO_LIST_LOADER_ID:
                // Loader for a list of popular videos
                return listRequestBuilder
                        .buildMostPopularVideoListLoader();
            case POPULAR_VIDEO_LIST_LOADER_NEXT_PAGE_ID:
                return listRequestBuilder
                        .buildNextPageLoader(mVideoListAdapter.getVIdeoListResponse());
            default:
                return listRequestBuilder.buildMostPopularVideoListLoader();
        }
    }

    @Override
    public void onLoadFinished(Loader<VideoListRequestLoader.VideoListRequestResult> loader,
                               VideoListRequestLoader.VideoListRequestResult data) {
        VideoListResponse response = data.getResultResponse();
        Exception error = data.getError();
        int id = loader.getId();

        // Handel existing errors
        if (error != null) {
            handelLoadException(error);
            return;
        }

        // Handel result response
        switch (id) {
            case POPULAR_VIDEO_LIST_LOADER_ID:
                handelLoadNewListFinished(response);
                break;
            case POPULAR_VIDEO_LIST_LOADER_NEXT_PAGE_ID:
                handelLoadNextPageFinished(response);
                break;
        }

        // unlock
        isLoadingData = false;
        showVideoList();
    }

    @Override
    public void onLoaderReset(Loader<VideoListRequestLoader.VideoListRequestResult> loader) {
        mVideoListAdapter.swapVideoListResponse(null);
    }

    /******************************************************************
     *              SwipeRefreshLayout.OnRefreshListener              *
     *                          Pull to refresh                       *
     ******************************************************************/

    /**
     * Pull to refresh
     */
    @Override
    public void onRefresh() {
        startLoadingNewList();
    }

    /******************************************************************
     *                  RecyclerView.OnScrollListener                 *
     ******************************************************************/

    /**
     * Load more when reached the bottom of the list
     */
    private class OnBottomReachedListener extends RecyclerView.OnScrollListener {

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            int pastVisiblesItems, visibleItemCount, totalItemCount;

            if(dy > 0) {
                // Check for scroll down
                visibleItemCount = mLayoutManager.getChildCount();
                totalItemCount = mLayoutManager.getItemCount();
                pastVisiblesItems = mLayoutManager.findFirstVisibleItemPosition();

                if (!isLoadingData) {
                    // prevent multiple loading request
                    if ( (visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                        startLoadingMore();
                    }
                }
            }
        }
    }


    /******************************************************************
     *                        Helper Functions                        *
     ******************************************************************/

    /**
     * Handel the exception throwed by the async task loader
     * @param error
     */
    private void handelLoadException(Exception error) {
        if (error instanceof GooglePlayServicesAvailabilityIOException) {
            TubeJamUtils.showGooglePlayServicesAvailabilityErrorDialog(
                    (Activity) getContext(),
                    ((GooglePlayServicesAvailabilityIOException) error)
                            .getConnectionStatusCode());
        } else if (error instanceof UserRecoverableAuthIOException) {
            ((Activity) mContext).startActivityForResult(
                    ((UserRecoverableAuthIOException) error).getIntent(),
                    TubeJamUtils.REQUEST_AUTHORIZATION);
        } else {
            // want to display an error
//            mErrorMessageTextView.setText("The following error occurred:\n"
//                    + error.getMessage());
        }
    }


    /**
     *
     * @param response
     */
    private void handelLoadNewListFinished(VideoListResponse response) {
        mVideoListAdapter.swapVideoListResponse(response);

        if (mPosition == RecyclerView.NO_POSITION) {
            mPosition = 0;
            // Scroll to the head of the list
            mMasterVideoListRecyclerView.smoothScrollToPosition(mPosition);
        }
    }


    /**
     *
     * @param response
     */
    private void handelLoadNextPageFinished(VideoListResponse response) {
        mVideoListAdapter.appendVideoListResponse(response);
    }

    /**
     * Notified to load more data to the list
     */
    private void startLoadingMore() {
        // re-use the original loader
        mLoadingMode = LOAD_MORE;
        LoaderManager loaderManager = getLoaderManager();
        loaderManager.restartLoader(POPULAR_VIDEO_LIST_LOADER_NEXT_PAGE_ID, null, this);
    }

    /**
     * Start loading the new list immediately
     */
    private void startLoadingNewList() {
        mLoadingMode = LOAD_NEW_LIST;
        LoaderManager loaderManager = getLoaderManager();
        loaderManager.restartLoader(POPULAR_VIDEO_LIST_LOADER_ID, null, this);
    }

    /**
     * Show the progress bar and hide the recycler view
     */
    private void showLoading() {

        // show a progress bar
        switch (mLoadingMode) {
            case LOAD_NEW_LIST:
                // Hide the recycler view
                mMasterVideoListRecyclerView.setVisibility(View.INVISIBLE);

                if (!mSwipeContainer.isRefreshing()) {
                    // If not refreshing, show the progress bar
                    mProgressBar.setVisibility(View.VISIBLE);
                }
                break;
            case LOAD_MORE:
                // Show the bottom progress bar
                mBottomProgressBar.setVisibility(View.VISIBLE);
        }

    }

    /**
     * Show the results recycler view and hide the progress bar
     */
    private void showVideoList() {
        // Always hide first
        switch (mLoadingMode) {
            case LOAD_NEW_LIST:
                // Not refreshing, hence hide the progress bar
                if (!mSwipeContainer.isRefreshing()) {
                    mProgressBar.setVisibility(View.INVISIBLE);
                }

                // dismiss the loading indicator
                if (mSwipeContainer.isRefreshing()) {
                    mSwipeContainer.setRefreshing(false);
                }

                // Show the recycler view
                mMasterVideoListRecyclerView.setVisibility(View.VISIBLE);
                break;
            case LOAD_MORE:
                // Load more to append the list, hence dismiss the bottom progress bar
                mBottomProgressBar.setVisibility(View.GONE);
        }
    }


}
