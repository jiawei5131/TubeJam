package com.zjworks.android.tubejam.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Config;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.google.api.services.youtube.model.VideoListResponse;
import com.zjworks.android.tubejam.R;
import com.zjworks.android.tubejam.modules.videos.VideoListAdapter;
import com.zjworks.android.tubejam.modules.videos.VideoListRequestLoader;

/**
 * Created by nemay5131 on 2017-10-18.
 */

public class MasterVideoListFragment extends Fragment
                                     implements LoaderManager.LoaderCallbacks<VideoListResponse>{
    private RecyclerView mMasterVideoListRecyclerView;
    private ProgressBar mProgressBar;
    private VideoListAdapter mVideoListAdapter;
    private int mPosition = RecyclerView.NO_POSITION;

    private static final int POPULAR_VIDEO_LIST_LOADER_ID = 22;

    private static final String TAG = MasterVideoListFragment.class.getSimpleName();

    public MasterVideoListFragment() {}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_master_video_list, container, false);

        mMasterVideoListRecyclerView = rootView.findViewById(R.id.rv_master_video_list);
        mProgressBar = container.findViewById(R.id.progress_bar);

        // Set layout manager
        LinearLayoutManager layoutManager =
                new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        mMasterVideoListRecyclerView.setLayoutManager(layoutManager);

        // Set has fixed length
        mMasterVideoListRecyclerView.setHasFixedSize(true);

        // Set adapter
        mVideoListAdapter = new VideoListAdapter(getContext());
        mMasterVideoListRecyclerView.setAdapter(mVideoListAdapter);

        // Start loading
        LoaderManager loaderManager = getLoaderManager();
        loaderManager.initLoader(POPULAR_VIDEO_LIST_LOADER_ID, null, this);

        return rootView;
    }

    /******************************************************************
     *                        LoaderCallbacks                         *
     *  To load a video list response from YouTube API.               *
     ******************************************************************/
    @Override
    public Loader<VideoListResponse> onCreateLoader(int id, Bundle args) {
        showLoading();

        switch (id) {
            case POPULAR_VIDEO_LIST_LOADER_ID:
                // Load a list of popular videos
                return new VideoListRequestLoader(getContext(),
                        VideoListRequestLoader.GET_POPULAR_VIDEO_LIST_KEY);
            default:
                return new VideoListRequestLoader(getContext(),
                        VideoListRequestLoader.GET_POPULAR_VIDEO_LIST_KEY);
        }
    }

    @Override
    public void onLoadFinished(Loader<VideoListResponse> loader, VideoListResponse data) {
        Log.v(TAG, "=========== video list loader finished " + data.toString());    // test
        mVideoListAdapter.swapVideoListResponse(data);


        if (mPosition == RecyclerView.NO_POSITION) {
            mPosition = 0;
            // Scroll to the head of the list
            mMasterVideoListRecyclerView.smoothScrollToPosition(mPosition);
        }

        if (mVideoListAdapter.getItemCount() != 0) {
            showVideoList();
        } else {
            Log.v(TAG, "onLoadFinished: Video list response is empty.");
        }
    }

    @Override
    public void onLoaderReset(Loader<VideoListResponse> loader) {
        mVideoListAdapter.swapVideoListResponse(null);
    }

    /******************************************************************
     *                        Helper Functions                        *
     ******************************************************************/

    /**
     * Show the progress bar and hide the recycler view
     */
    private void showLoading() {
        // Hide the recycler view
        mMasterVideoListRecyclerView.setVisibility(View.INVISIBLE);
        mProgressBar.setVisibility(View.VISIBLE);
    }

    /**
     * Show the results recycler view and hide the progress bar
     */
    private void showVideoList() {
        // Always hide first
        mProgressBar.setVisibility(View.INVISIBLE);
        mMasterVideoListRecyclerView.setVisibility(View.VISIBLE);
    }
}
