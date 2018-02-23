package com.zjworks.android.tubejam.modules.youtube_api_loaders.comment_threads;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.CommentThreadListResponse;
import com.google.api.services.youtube.model.SearchListResponse;
import com.zjworks.android.tubejam.utils.Authorizer;
import com.zjworks.android.tubejam.utils.TubeJamUtils;

import java.io.IOException;
import java.util.HashMap;

/**
 * Created by nemay5131 on 2018-02-19.
 *
 * Retrieve comment threads (a bulk of comments) under a given video from YouTube API.
 */

public class CommentThreadsListRequestLoader extends AsyncTaskLoader<CommentThreadsListRequestLoader.CommentThreadsListRequestResult> {
    private static final int LOAD_BY_VIDEO_ID_MODE_KEY = 1;

    private String mVideoId;
    private YouTube mService;
    private int mModeKey;

    private CommentThreadListResponse mResponse;
    private Exception mLastError;
    private HashMap<String, String> mParameters;

    private CommentThreadsListRequestLoader(Context context,
                                           int mode) {
        super(context);

        // get YouTube service
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        mService = new YouTube
                .Builder(transport, jsonFactory, Authorizer.getCredential(context))
                .setApplicationName(TubeJamUtils.getApplicationName(context))
                .build();

        mParameters = new HashMap<>();
        mModeKey = mode;
    }


    @Override
    protected void onStartLoading() {
        mParameters.put("part", "snippet");

        switch (mModeKey) {
            case LOAD_BY_VIDEO_ID_MODE_KEY: {
                if (mVideoId != null) {
                    mParameters.put("videoId", mVideoId);
                } else {
                    mLastError = new IllegalArgumentException("must give a video id");
                    cancelLoad();
                }
                break;
            }

            default: {
                // mode key invalid
                mLastError = new IllegalArgumentException("Illegal mode key.");
                cancelLoad();
            }
        }

        forceLoad();
    }


    @Override
    public CommentThreadsListRequestResult loadInBackground() {
        try {
            mResponse = getResponseFromApi();
            mLastError = null;
        } catch (IOException e) {
            mResponse = null;
            mLastError = e;
        }

        return new CommentThreadsListRequestResult(mResponse, mLastError);
    }


    /**
     * Execute the loader to retrieve data from YouTube API
     * @return  The results returned from API
     * @throws IOException
     */
    private CommentThreadListResponse getResponseFromApi() throws IOException {
        YouTube.CommentThreads.List commentThreadsListRequest
                = mService.commentThreads().list(mParameters.get("part"));

        if (mParameters.containsKey("videoId") && !mParameters.get("videoId").equals("")) {
            commentThreadsListRequest.setVideoId(mParameters.get("videoId"));
        }

        CommentThreadListResponse result = commentThreadsListRequest.execute();
        Log.v(this.getClass().toString(), result.toString());

        return result;
    }


    /**
     *
     * @param videoId
     */
    private void setVideoId(String videoId) {
        mVideoId = videoId;
    }

    /******************************************************************
     *                          Builder                               *
     *                  Builder of the loader                         *
     ******************************************************************/

    public static class Builder {
        private Context mContext;

        public Builder(Context context) {
            mContext = context;
        }


        public CommentThreadsListRequestLoader buildCommentThreadsListByVideoIdRequestLoader(String videoId) {
            CommentThreadsListRequestLoader loader = new CommentThreadsListRequestLoader(
                    mContext, LOAD_BY_VIDEO_ID_MODE_KEY);
            loader.setVideoId(videoId);

            return loader;
        }
    }

    /******************************************************************
     *                CommentThreadsListRequestResult                 *
     *        Result structure that returned by this loader           *
     ******************************************************************/

    public class CommentThreadsListRequestResult {
        private CommentThreadListResponse mResultResponse;
        private Exception mLastError;

        public CommentThreadsListRequestResult(CommentThreadListResponse response,
                                               Exception error) {
            mResultResponse = response;
            mLastError = error;
        }


        public CommentThreadListResponse getResultResponse() {
            return mResultResponse;
        }

        public Exception getError() {
            return mLastError;
        }
    }
}
