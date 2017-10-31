package com.zjworks.android.tubejam.modules.videos;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.VideoListResponse;
import com.zjworks.android.tubejam.utils.Authorizer;
import com.zjworks.android.tubejam.utils.TubeJamUtils;

import java.io.IOException;
import java.util.HashMap;

/**
 * Created by nemay5131 on 2017-10-18.
 */

public class VideoListRequestLoader extends AsyncTaskLoader<VideoListResponse> {
    private static final String TAG = VideoListRequestLoader.class.getSimpleName();

    public static final int GET_POPULAR_VIDEO_LIST_KEY = 31;
    public static final int GET_POPULAR_VIDEO_LIST_NEXT_PAGE_KEY = 32;

    private static final long MAX_RESULT = 10;

    private YouTube mService;
    private Exception mLastError;
    private VideoListResponse mLastResponse;
    private HashMap<String, String> mParameters;
    private int mModeKey;

    private VideoListRequestLoader(Context context,
                                  int mode) {
        super(context);

        // get YouTube service
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        mService = new YouTube
                .Builder(transport, jsonFactory, Authorizer.getCredential())
                .setApplicationName(TubeJamUtils.getApplicationName(context))
                .build();

        mModeKey = mode;
        mParameters = new HashMap<>();
    }


    /**
     * Handle the request before start loading
     */
    @Override
    protected void onStartLoading() {
        // set the parameters for the request
        mParameters.put("part", "snippet,contentDetails,statistics");
        switch (mModeKey) {
            case GET_POPULAR_VIDEO_LIST_KEY:
                // want to get a list of popular videos
                mParameters.put("chart", "mostPopular");
                mParameters.put("regionCode", "US");
                mParameters.put("videoCategoryId", "");
                break;
            case GET_POPULAR_VIDEO_LIST_NEXT_PAGE_KEY:
                // want to get a list of popular videos
                mParameters.put("chart", "mostPopular");
                mParameters.put("regionCode", "US");
                mParameters.put("videoCategoryId", "");
                mParameters.put("pageToken", mLastResponse.getNextPageToken());
                break;
            default:
                // mode key invalid
                mLastError = new IllegalArgumentException("Illegal mode key.");
                cancelLoad();
        }

        forceLoad();
    }


    @Override
    public VideoListResponse loadInBackground() {
        Log.v(TAG, "============= loadInBackground =============");    // test

        try {
            return getResponseFromApi();
        } catch (Exception e) {
            mLastError = e;
            cancelLoad();
            return null;
        }
    }


    /**
     * Handel the request to cancel loading
     * @return Returns false if the task could not be canceled,
     * typically because it has already completed normally,
     * or because startLoading() hasn't been called; returns true otherwise.
     * When true is returned, the task is still running and
     * the Loader.OnLoadCanceledListener will be called when the task completes.
     */
    @Override
    protected boolean onCancelLoad() {
        return super.onCancelLoad();
    }


    /**
     * Handel the request to reset the loader
     */
    @Override
    protected void onReset() {
        super.onReset();
    }


    public void setMode(int mode) {
        mModeKey = mode;
    }

    /**
     * Request a list response from YouTube API
     * @return VideoListResponse that contains useful data
     */
    private VideoListResponse getResponseFromApi() throws IOException {
        YouTube.Videos.List videosListMostPopularRequest
                = mService.videos().list(mParameters.get("part"));

        // max result
        videosListMostPopularRequest.setMaxResults(MAX_RESULT);

        if (mParameters.containsKey("chart") && !mParameters.get("chart").equals("")) {
            videosListMostPopularRequest.setChart(mParameters.get("chart"));
        }

        if (mParameters.containsKey("regionCode") && !mParameters.get("regionCode").equals("")) {
            videosListMostPopularRequest.setRegionCode(mParameters.get("regionCode"));
        }

        if (mParameters.containsKey("videoCategoryId") && !mParameters.get("videoCategoryId").equals("")) {
            videosListMostPopularRequest.setVideoCategoryId(mParameters.get("videoCategoryId"));
        }

        if (mParameters.containsKey("pageToken") && !mParameters.get("pageToken").equals("")) {
            videosListMostPopularRequest.setPageToken(mParameters.get("pageToken"));
        }

        VideoListResponse result = videosListMostPopularRequest.execute();

        return result;
    }


    /******************************************************************
     *                          Builder                               *
     ******************************************************************/

    public static class Builder {
        private Context mContext;


        public Builder(Context context) {
            mContext = context;
        }


        public VideoListRequestLoader buildMostPopularVideoListLoader() {
            return new VideoListRequestLoader(mContext, GET_POPULAR_VIDEO_LIST_KEY);
        }


        public VideoListRequestLoader buildNextPageLoader(@NonNull VideoListResponse lastResponse) {
            VideoListRequestLoader loader =
                    new VideoListRequestLoader(mContext, GET_POPULAR_VIDEO_LIST_NEXT_PAGE_KEY);
            loader.mLastResponse = lastResponse;
            return loader;
        }
    }
}
