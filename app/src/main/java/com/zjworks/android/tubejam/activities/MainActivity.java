package com.zjworks.android.tubejam.activities;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.api.services.youtube.model.Video;
import com.zjworks.android.tubejam.R;
import com.zjworks.android.tubejam.data.TubeJamVideo;
import com.zjworks.android.tubejam.fragments.MasterVideoListFragment;
import com.zjworks.android.tubejam.modules.videos.VideoListAdapter;
import com.zjworks.android.tubejam.utils.TubeJamUtils;

public class MainActivity extends AppCompatActivity
                          implements VideoListAdapter.OnVideoListItemClickedListener {
    private TextView mErrorMessageTextView;
    private FragmentManager mFragmentManager;
    private MasterVideoListFragment mMasterVideoListFragment;

    static final int REQUEST_AUTHORIZATION = TubeJamUtils.REQUEST_AUTHORIZATION;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:

                    // replace with a list of videos
                    if (isServiceChecked()) {
                        mFragmentManager.beginTransaction()
                                .replace(R.id.master_video_list_container, mMasterVideoListFragment)
                                .commit();
                    }
                    return true;
                case R.id.navigation_dashboard:
                    return true;
                case R.id.navigation_notifications:
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

        // Bind bottom navigation menu
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        initMostPopularFragment();
    }


    /**
     * Check required services
     */
    private boolean isServiceChecked() {
        if (! isDeviceOnline()) {
            mErrorMessageTextView.setText(getResources().getString(R.string.offline_message));
            return false;
        }
        return true;
    }


    /**
     * Checks whether the device currently has a network connection.
     * @return true if the device has a network connection, false otherwise.
     */
    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }


    /**
     * Called when an activity launched here (specifically, AccountPicker
     * and authorization) exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode code indicating the result of the incoming
     *     activity result.
     * @param data Intent (containing result data) returned by incoming
     *     activity result.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    initMostPopularFragment();
                }
                break;
        }
    }



    /******************************************************************
     *                  Initialize Fragments Helper                   *
     ******************************************************************/

    /**
     * Init the master videos list for most popular videos
     */
    private void initMostPopularFragment() {
        // Initialize a master video list fragment
        // and display a list of videos
        mMasterVideoListFragment = new MasterVideoListFragment();
        mFragmentManager = getSupportFragmentManager();

        if (isServiceChecked()) {
            // init a list of videos
            mFragmentManager.beginTransaction()
                    .add(R.id.master_video_list_container, mMasterVideoListFragment)
                    .commit();
        }
    }


    /******************************************************************
     *        VideoListAdapter.OnVideoListItemClickedListener         *
     *  Listen to the click on the video list item on the master list *
     ******************************************************************/
    // TODO: Start a new activity to play youtube video
    /**
     * Start a new activity or add a fragment to display the YouTube video
     * player based on the information given in the video.
     * @param item  a YouTube Video Model that we want to display
     */
    @Override
    public void onVideoListItemClicked(Video item) {
        // pass the information of the video as serializables
        TubeJamVideo video = new TubeJamVideo(item);

        // start a new PlayVideoActivity
        Intent startPlayVideoActivity = new Intent(this, PlayVideoActivity.class);
        startPlayVideoActivity.putExtra(TubeJamVideo.TUBEJAM_VIDEO_OBJECT_KEY, video);
        startActivity(startPlayVideoActivity);
    }
}
