package com.zjworks.android.tubejam.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerFragment;
import com.google.api.services.youtube.model.Video;
import com.zjworks.android.tubejam.R;
import com.zjworks.android.tubejam.data.TubeJamVideo;
import com.zjworks.android.tubejam.utils.Config;

/**
 * Created by nemay5131 on 2017-11-21.
 */

// TODO: play youtube video here
public class VideoPlayerFragment extends Fragment {
    private YouTubePlayerFragment mYouTubePlayerFragment;
    private Context mContext;
    private Activity mActivity;
    private TubeJamVideo mVideoModel;

    public VideoPlayerFragment() {}

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        mActivity = (Activity) context;

        try {
            mVideoModel =
                    (TubeJamVideo) mActivity
                        .getIntent()
                        .getSerializableExtra(TubeJamVideo.TUBEJAM_VIDEO_OBJECT_KEY);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_video_player, container, false);

        mYouTubePlayerFragment = (YouTubePlayerFragment) mActivity.getFragmentManager()
                .findFragmentById(R.id.fg_youtube_player);

        return rootView;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (mVideoModel != null) {
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
     *                  Initialization  Helper                        *
     ******************************************************************/

}
