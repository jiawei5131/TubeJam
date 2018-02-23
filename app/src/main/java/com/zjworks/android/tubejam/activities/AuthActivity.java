package com.zjworks.android.tubejam.activities;

import android.Manifest;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.zjworks.android.tubejam.R;
import com.zjworks.android.tubejam.utils.Authorizer;
import com.zjworks.android.tubejam.utils.TubeJamUtils;

import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * The launcher activity to get authentication from the user
 */
public class AuthActivity extends AppCompatActivity
                          implements View.OnClickListener,
                                     EasyPermissions.PermissionCallbacks {
    private TextView mSignInTextView;
    private GoogleAccountCredential mCredential;
    private Toast[] mToasts;

    static final int REQUEST_ACCOUNT_PICKER = TubeJamUtils.REQUEST_ACCOUNT_PICKER;
    static final int REQUEST_AUTHORIZATION = TubeJamUtils.REQUEST_AUTHORIZATION;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = TubeJamUtils.REQUEST_GOOGLE_PLAY_SERVICES;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = TubeJamUtils.REQUEST_PERMISSION_GET_ACCOUNTS;

    private static final String PREF_ACCOUNT_NAME = "accountName";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        // Hide the progress bar
        getSupportActionBar().hide();

        // Get views
        mSignInTextView = findViewById(R.id.tv_sign_in);
        mSignInTextView.setOnClickListener(this);

        // Initialize credentials
        Authorizer.initCredential(this);
        mCredential = Authorizer.getCredential(this);

        // Try to sign in automatically
        signInCheck();
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
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    TubeJamUtils.displayToastMessage(
                            this,
                            mToasts,
                            getResources().getString(R.string.google_service_not_available_error_message)
                    );
                } else {
                    signInCheck();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK
                        && data != null
                        && data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings =
                                getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);
                        signInCheck();
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    signInCheck();
                }
                break;
        }
    }


    /******************************************************************
     *                      View.OnClickListener                      *
     ******************************************************************/

    @Override
    public void onClick(View view) {
        int id = view.getId();

        switch (id) {
            case R.id.tv_sign_in:
                chooseAccount();
        }
    }


    /******************************************************************
     *                        Helper Functions                        *
     ******************************************************************/

    /**
     * Try to continue if an account has already chosen
     */
    private void signInCheck() {
        if (! TubeJamUtils.isGooglePlayServicesAvailable(this)) {
            TubeJamUtils.acquireGooglePlayServices(this);
        } else if (hasChosenAccount()) {
            startMainActivity();
        }
    }


    /**
     * Attempts to set the account used with the API credentials. If an account
     * name was previously saved it will use that one. Note that the setting the
     * account to use with the credentials object requires the app to have the
     * GET_ACCOUNTS permission, which is requested here if it is not already
     * present. The AfterPermissionGranted annotation indicates that this
     * function will be rerun automatically whenever the GET_ACCOUNTS permission
     * is granted.
     * @return true if an account has been chosen by the user; otherwise false
     */
    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private boolean hasChosenAccount() {
        if (EasyPermissions.hasPermissions(
                this, Manifest.permission.GET_ACCOUNTS)) {
            String accountName = getPreferences(Context.MODE_PRIVATE)
                    .getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                mCredential.setSelectedAccountName(accountName);
                return true;
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    getResources().getString(R.string.app_needs_google_service_message),
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }
        return false;
    }


    /**
     * Start the main activity
     */
    private void startMainActivity() {
        Intent startMainActivity = new Intent(this, MainActivity.class);
        startActivity(startMainActivity);
        finish();
    }


    /**
     * Start an account picker dialog to the user.
     */
    private void chooseAccount() {
        startActivityForResult(
                mCredential.newChooseAccountIntent(),
                REQUEST_ACCOUNT_PICKER);
    }


    /******************************************************************
     *               EasyPermissions.PermissionCallbacks              *
     ******************************************************************/

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
}
