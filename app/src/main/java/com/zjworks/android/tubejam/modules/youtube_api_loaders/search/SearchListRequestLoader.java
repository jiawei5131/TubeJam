package com.zjworks.android.tubejam.modules.youtube_api_loaders.search;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.zjworks.android.tubejam.utils.Authorizer;
import com.zjworks.android.tubejam.utils.TubeJamUtils;

import java.io.IOException;
import java.util.HashMap;

/**
 * Created by nemay5131 on 2018-01-24.
 */

public class SearchListRequestLoader extends AsyncTaskLoader<SearchListRequestLoader.SearchListRequestResult> {
    public static final int SEARCH_BY_KEY_WORDS_MODE_KEY = 40;
    public static final int SEARCH_RELATED_VIDEOS_MODE_KEY = 41;

    public static final long MAX_RESULT = 15;

    private YouTube mService;
    private SearchListResponse mResponse;
    private Exception mLastError;
    private HashMap<String, String> mParameters;
    private String mSearchKeyWords, mRelatedVideoId;
    private int mModeKey;

    private SearchListRequestLoader(Context context, int mode) {
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
        // set the parameters for the request
        mParameters.put("part", "snippet");

        switch (mModeKey) {
            case SEARCH_RELATED_VIDEOS_MODE_KEY: {
                if (mRelatedVideoId != null) {
                    mParameters.put("relatedToVideoId", mRelatedVideoId);
                    mParameters.put("type", "video");
                } else {
                    mLastError = new IllegalArgumentException("must give a related video id.");
                    cancelLoad();
                }
                break;
            }

            case SEARCH_BY_KEY_WORDS_MODE_KEY: {
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
    public SearchListRequestResult loadInBackground() {
        try {
            mResponse = getResponseFromApi();
            mLastError = null;
        } catch (Exception e) {
            mResponse = null;
            mLastError = e;
        }
        return new SearchListRequestResult(mResponse, mLastError);
    }


    /**
     * Request a list response from YouTube API
     * @return VideoListResponse that contains useful data
     */
    private SearchListResponse getResponseFromApi() throws IOException {
        YouTube.Search.List searchListRequest
                = mService.search().list(mParameters.get("part"));

        // max result
        searchListRequest.setMaxResults(MAX_RESULT);

        if (mParameters.containsKey("regionCode") && !mParameters.get("regionCode").equals("")) {
            searchListRequest.setRegionCode(mParameters.get("regionCode"));
        }

        if (mParameters.containsKey("pageToken") && !mParameters.get("pageToken").equals("")) {
            searchListRequest.setPageToken(mParameters.get("pageToken"));
        }

        if (mParameters.containsKey("type") && !mParameters.get("type").equals("")) {
            searchListRequest.setType(mParameters.get("type"));
        }

        if (mModeKey == SEARCH_RELATED_VIDEOS_MODE_KEY) {
            if (mParameters.containsKey("relatedToVideoId")
                    && !mParameters.get("relatedToVideoId").equals("")) {
                searchListRequest.setRelatedToVideoId(mParameters.get("relatedToVideoId"));
            }
        }

        SearchListResponse result = searchListRequest.execute();

        return result;
    }

    private void setSearchKeyWords(String searchKeyWords) {
        mSearchKeyWords = searchKeyWords;
    }


    private void setSearchRelatedVideoId(String relatedVideoId) {
        mRelatedVideoId = relatedVideoId;
    }

    /******************************************************************
     *                          Builder                               *
     *                  Builder of the loader                         *
     ******************************************************************/
    public static class Builder {
        private Context context;

        public Builder(Context context) {
            this.context = context;
        }

        public SearchListRequestLoader buildSearchRelatedVideoLoader(String relatedToVideoId) {
            SearchListRequestLoader loader = new SearchListRequestLoader(context, SEARCH_RELATED_VIDEOS_MODE_KEY);
            loader.setSearchRelatedVideoId(relatedToVideoId);
            return loader;
        }


        public SearchListRequestLoader buildSearchByKeyWordsLoader(String keyWords) {
            SearchListRequestLoader loader = new SearchListRequestLoader(context, SEARCH_BY_KEY_WORDS_MODE_KEY);
            loader.setSearchKeyWords(keyWords);
            return loader;
        }
    }


    /******************************************************************
     *                    SearchListRequestResult                     *
     *        Result structure that returned by this loader           *
     ******************************************************************/

    public class SearchListRequestResult {
        private SearchListResponse mResultResponse;
        private Exception mLastError;

        public SearchListRequestResult(SearchListResponse result, Exception error) {
            mResultResponse = result;
            mLastError = error;
        }

        public Exception getError() {
            return mLastError;
        }

        public SearchListResponse getResultResponse() {
            return mResultResponse;
        }
    }
}
