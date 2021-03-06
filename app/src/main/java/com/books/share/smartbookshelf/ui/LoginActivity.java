package com.books.share.smartbookshelf.ui;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.*;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.books.share.smartbookshelf.R;
import com.books.share.smartbookshelf.lib.conf.Conf;
import com.books.share.smartbookshelf.lib.trans.api.itf.APIClient;
import com.books.share.smartbookshelf.lib.trans.api.ServiceGenerator;
import com.books.share.smartbookshelf.lib.trans.api.itf.BookshelfApiClient;
import com.books.share.smartbookshelf.lib.trans.api.object.AccessToken;
import com.books.share.smartbookshelf.lib.trans.api.object.FcmToken;
import com.books.share.smartbookshelf.lib.trans.api.object.MyShelf;
import com.books.share.smartbookshelf.ui.fragment.Myshelf;
import com.google.firebase.messaging.RemoteMessage;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {
    private static final int REQUEST_READ_CONTACTS = 0;
    private static final int REQUEST_ACCESS_FINE_LOCATION = 1;

    private UserLoginTask mAuthTask = null;

    private static final String API_OAUTH_CLIENTID = Conf.API_OAUTH_CLIENTID;
    private static final String API_OAUTH_CLIENTSECRET = Conf.API_OAUTH_CLIENTSECRET;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;


    public static final int MULTIPLE_PERMISSIONS = 10;

    String[] permissions = new String[]{
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_login);

        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);

        checkPermissions();

        SharedPreferences prefs = getApplicationContext().getSharedPreferences(Conf.APPLICATION_ID, Context.MODE_PRIVATE);

        if (prefs.getBoolean("oauth.loggedin", false)) {
            RefreshToken refreshToken = new RefreshToken();
            refreshToken.execute();
        }

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }


    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mEmailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }


    private boolean checkPermissions() {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p : permissions) {
            result = ContextCompat.checkSelfPermission(getApplicationContext(), p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MULTIPLE_PERMISSIONS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permissions granted.
                } else {
                    String permission = "";
                    for (String per : permissions) {
                        permission += "\n" + per;
                    }
                    Log.d("login", permission);
                    // permissions list of don't granted permission
                }
                return;
            }
        }
    }
//    /**
//     * Callback received when a permissions request has been completed.
//     */
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
//                                           @NonNull int[] grantResults) {
//        if (requestCode == REQUEST_READ_CONTACTS) {
//            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                populateAutoComplete();
//            }
//        } else if(requestCode == REQUEST_ACCESS_FINE_LOCATION) {
//            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                getLocation();
//            }
//        }
//    }

    private void getLocation() {
        LocationManager lm = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        List<String> providers = lm.getProviders(true);

        Location bestLocation = null;

        for (String provider : providers) {
            Location location = lm.getLastKnownLocation(provider);
            if (location == null) {
                continue;
            }

            if (bestLocation == null || location.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = location;
                SaveLocation saveLocation = new SaveLocation(String.valueOf(bestLocation.getLatitude()), String.valueOf(bestLocation.getLongitude()));
                saveLocation.execute();
                Log.d("login", bestLocation.getLongitude() + " " + bestLocation.getLatitude());
            }
        }

        if (bestLocation == null) {
            Log.d("login", "is NULL!");
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);

            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    public boolean checkResponse(Response<AccessToken> response, SharedPreferences prefs) {
        int statusCode = response.code();
        if (statusCode == 200) {
            AccessToken token = response.body();
            prefs.edit().putBoolean("oauth.loggedin", true).apply();
            prefs.edit().putString("oauth.accesstoken", token.getAccess_token()).apply();
            prefs.edit().putString("oauth.refreshtoken", token.getRefresh_token()).apply();
            prefs.edit().putString("oauth.tokentype", token.getToken_type()).apply();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            final SharedPreferences prefs = getApplicationContext().getSharedPreferences(
                    Conf.APPLICATION_ID, Context.MODE_PRIVATE);

            APIClient client = ServiceGenerator.createService(APIClient.class);
            Call<AccessToken> call = client.getNewAccessToken(mEmailView.getText().toString(), mPasswordView.getText().toString(), API_OAUTH_CLIENTID,
                    API_OAUTH_CLIENTSECRET, "password");

            try {
                return checkResponse(call.execute(), prefs);
            } catch (IOException e) {
                Log.e("act", "error", e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                getLocation();
                Intent intent = new Intent(LoginActivity.this, SmartBookshelfMainActivity.class);
                startActivity(intent);
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class RefreshToken extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            final SharedPreferences prefs = getApplicationContext().getSharedPreferences(
                    Conf.APPLICATION_ID, Context.MODE_PRIVATE);

            APIClient client = ServiceGenerator.createService(APIClient.class);
            Call<AccessToken> call = client.getRefreshAccessToken(prefs.getString("oauth.refreshtoken", ""), API_OAUTH_CLIENTID, API_OAUTH_CLIENTSECRET,
                    "refresh_token");

            try {
                return checkResponse(call.execute(), prefs);
            } catch (IOException e) {
                Log.e("act", "error", e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                getLocation();
                Intent intent = new Intent(LoginActivity.this, SmartBookshelfMainActivity.class);
                startActivity(intent);
            } else {

            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    public class SaveLocation extends AsyncTask<Void, Void, Boolean> {

        private final String lat_r;
        private final String lng_r;

        SaveLocation(String lat, String lng) {
            lat_r = lat;
            lng_r = lng;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            final SharedPreferences prefs = getApplicationContext().getSharedPreferences(
                    Conf.APPLICATION_ID, Context.MODE_PRIVATE);
            AccessToken accessToken = new AccessToken();
            accessToken.setAccess_token(prefs.getString("oauth.accesstoken", ""));
            accessToken.setRefresh_token(prefs.getString("oauth.refreshtoken", ""));
            accessToken.setToken_type(prefs.getString("oauth.tokentype", ""));

            BookshelfApiClient client = ServiceGenerator.createService(BookshelfApiClient.class, accessToken, getApplicationContext());
            Call<Object> call = client.setLocation(lat_r, lng_r);
            Call<FcmToken> call_token = client.saveFcmToken(1, prefs.getString("push_token", ""));
            Call<MyShelf> shelf = client.getShelf();
            try {
                call.execute();
                call_token.execute();
                MyShelf myShelf = shelf.execute().body();
                prefs.edit().putInt("shelf", myShelf.getId()).apply();
                prefs.edit().putInt("user", myShelf.getUser()).apply();
                return true;
            } catch (IOException e) {
                Log.e("act", "error", e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

}

