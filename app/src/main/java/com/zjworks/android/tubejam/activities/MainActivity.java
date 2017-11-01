package com.zjworks.android.tubejam.activities;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeScopes;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import com.zjworks.android.tubejam.R;
import com.zjworks.android.tubejam.fragments.MasterVideoListFragment;
import com.zjworks.android.tubejam.utils.Authorizer;
import com.zjworks.android.tubejam.utils.TubeJamUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity
                          implements EasyPermissions.PermissionCallbacks {
    private GoogleAccountCredential mCredential;
    private TextView mErrorMessageTextView;
    private ProgressBar mProgressBar;
    private FragmentManager mFragmentManager;
    private MasterVideoListFragment mMasterVideoListFragment;


    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = { YouTubeScopes.YOUTUBE_READONLY };

    private final Context context = this;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:

                    // replace with a list of videos
                    mFragmentManager.beginTransaction()
                            .replace(R.id.master_video_list_container, mMasterVideoListFragment)
                            .commit();
                    return true;
                case R.id.navigation_dashboard:
                    mErrorMessageTextView.setText(R.string.title_dashboard);
                    return true;
                case R.id.navigation_notifications:
                    mErrorMessageTextView.setText(R.string.title_notifications);
                    return true;
            }
            return false;
        }

    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find views
        mErrorMessageTextView = findViewById(R.id.tv_error_message);
        mProgressBar = findViewById(R.id.progress_bar);

        // Bind bottom navigation menu
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        // Initialize credentials and service object.
        Authorizer.initCredential(this);
        mCredential = Authorizer.getCredential();

        initMostPopularFragment();
    }


    /**
     * Init the master videos list for most popular videos
     */
    private void initMostPopularFragment() {
        // Initialize a master video list fragment
        // and display a list of videos
        mMasterVideoListFragment = new MasterVideoListFragment();
        mFragmentManager = getSupportFragmentManager();

        // init a list of videos
        mFragmentManager.beginTransaction()
                .add(R.id.master_video_list_container, mMasterVideoListFragment)
                .commit();
    }


    /**
     * Respond to requests for permissions at runtime for API 23 and above.
     * @param requestCode The request code passed in
     *     requestPermissions(android.app.Activity, String, int, String[])
     * @param permissions The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *     which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);
    }

    /**
     * Callback for when a permission is granted using the EasyPermissions
     * library.
     * @param requestCode The request code associated with the requested
     *         permission
     * @param list The requested permission list. Never null.
     */
    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        // Do nothing.
    }

    /**
     * Callback for when a permission is denied using the EasyPermissions
     * library.
     * @param requestCode The request code associated with the requested
     *         permission
     * @param list The requested permission list. Never null.
     */
    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        // Do nothing.
    }


    /**
     * An asynchronous task that handles the YouTube Data API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    private class MakeRequestTask extends AsyncTask<Void, Void, List<String>> {
        private com.google.api.services.youtube.YouTube mService = null;
        private Exception mLastError = null;

        MakeRequestTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.youtube.YouTube.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("YouTube Data API Android Quickstart")
                    .build();
        }

        /**
         * Background task to call YouTube Data API.
         * @param params no parameters needed for this task.
         */
        @Override
        protected List<String> doInBackground(Void... params) {
            try {
                return getDataFromApi();
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        /**
         * Fetch information about the "GoogleDevelopers" YouTube channel.
         * @return List of Strings containing information about the channel.
         * @throws IOException
         */
        private List<String> getDataFromApi() throws IOException {
            List<String> videosInfo = new ArrayList<String>();
            try {
                HashMap<String, String> parameters = new HashMap<>();
                parameters.put("part", "snippet,contentDetails,statistics");
                parameters.put("chart", "mostPopular");
                parameters.put("regionCode", "US");
                parameters.put("videoCategoryId", "");

                YouTube.Videos.List videosListMostPopularRequest = mService.videos().list(parameters.get("part").toString());
                if (parameters.containsKey("chart") && parameters.get("chart") != "") {
                    videosListMostPopularRequest.setChart(parameters.get("chart").toString());
                }

                if (parameters.containsKey("regionCode") && parameters.get("regionCode") != "") {
                    videosListMostPopularRequest.setRegionCode(parameters.get("regionCode").toString());
                }

                if (parameters.containsKey("videoCategoryId") && parameters.get("videoCategoryId") != "") {
                    videosListMostPopularRequest.setVideoCategoryId(parameters.get("videoCategoryId").toString());
                }

                VideoListResponse response = videosListMostPopularRequest.execute();
                List<Video> results = response.getItems();

                if (results != null) {
                    for (Video video : results) {
                        System.out.println(video.toString());
                        videosInfo.add(video.toString());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return videosInfo;
        }


        @Override
        protected void onPreExecute() {
            mErrorMessageTextView.setText("");
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(List<String> output) {
            mProgressBar.setVisibility(View.INVISIBLE);
            if (output == null || output.size() == 0) {
                mErrorMessageTextView.setText("No results returned.");
            } else {
                output.add(0, "Data retrieved using the YouTube Data API:");
                mErrorMessageTextView.setText(TextUtils.join("\n", output));
            }
        }

        @Override
        protected void onCancelled() {
            mProgressBar.setVisibility(View.INVISIBLE);
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    TubeJamUtils.showGooglePlayServicesAvailabilityErrorDialog(
                            (Activity) context,
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            MainActivity.REQUEST_AUTHORIZATION);
                } else {
                    mErrorMessageTextView.setText("The following error occurred:\n"
                            + mLastError.getMessage());
                }
            } else {
                mErrorMessageTextView.setText("Request cancelled.");
            }
        }
    }
}
