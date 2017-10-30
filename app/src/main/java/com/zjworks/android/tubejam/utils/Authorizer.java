package com.zjworks.android.tubejam.utils;

import android.content.Context;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.youtube.YouTubeScopes;
import com.zjworks.android.tubejam.activities.MainActivity;

import java.util.Arrays;

/**
 * Created by nemay5131 on 2017-10-26.
 *
 * This class will serve as an authority that each class try to get the credential from.
 */

public final class Authorizer {
    private static final String[] SCOPES = { YouTubeScopes.YOUTUBE_READONLY };

    private static GoogleAccountCredential mCredential;

    private Authorizer() { }

    public static void initCredential(Context context) {
        if (mCredential == null) {
            mCredential = GoogleAccountCredential.usingOAuth2(
                    context.getApplicationContext(), Arrays.asList(SCOPES))
                    .setBackOff(new ExponentialBackOff());
        }
    }

    public static GoogleAccountCredential getCredential() {
        return mCredential;
    }
}
