package com.example.cal.calshop.ui.login;

import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cal.calshop.R;
import com.example.cal.calshop.model.User;
import com.example.cal.calshop.ui.BaseActivity;
import com.example.cal.calshop.ui.MainActivity;
import com.example.cal.calshop.utils.Constants;
import com.example.cal.calshop.utils.Utils;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static com.firebase.ui.auth.ui.AcquireEmailHelper.RC_SIGN_IN;

public class LoginActivity extends BaseActivity {
    private static final String LOG_TAG = LoginActivity.class.getSimpleName();
    /* A dialog that is presented until the Firebase authentication finished. */
    private ProgressDialog mAuthProgressDialog;
    private EditText mEditTextEmailInput, mEditTextPasswordInput;

    private FirebaseAuth mAuth;

    /**
     * Variables related to Google Login
     */
    /* A flag indicating that a PendingIntent is in progress and prevents us from starting further intents. */
    private boolean mGoogleIntentInProgress;
    /* Request code used to invoke sign in user interactions for Google+ */
    //public static final int RC_GOOGLE_LOGIN = 1;
    /* A Google account object that is populated if the user signs in with Google */
    GoogleSignInAccount mGoogleAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null && !intent.hasExtra(Intent.EXTRA_TEXT)) {
            Intent autoLogIntent = new Intent(LoginActivity.this, MainActivity.class);
            autoLogIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(autoLogIntent);
            finish();
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        /**
         * Link layout elements from XML and setup progress dialog
         */
        initializeScreen();
        if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
            String message = intent.getStringExtra(Intent.EXTRA_TEXT);
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }


        /**
         * Call signInPassword() when user taps "Done" keyboard action
         */
        mEditTextPasswordInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {

                if (actionId == EditorInfo.IME_ACTION_DONE || keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    signInPassword();
                }
                return true;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    /**
     * Override onCreateOptionsMenu to inflate nothing
     *
     * @param menu The menu with which nothing will happen
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }


    /**
     * Sign in with Password provider when user clicks sign in button
     */
    public void onSignInPressed(View view) {
        signInPassword();
    }

    /**
     * Open CreateAccountActivity when user taps on "Sign up" TextView
     */
    public void onSignUpPressed(View view) {
        Intent intent = new Intent(LoginActivity.this, CreateAccountActivity.class);
        startActivity(intent);
    }

    /**
     * Link layout elements from XML and setup the progress dialog
     */
    public void initializeScreen() {
        mEditTextEmailInput = (EditText) findViewById(R.id.edit_text_email);
        mEditTextPasswordInput = (EditText) findViewById(R.id.edit_text_password);
        LinearLayout linearLayoutLoginActivity = (LinearLayout) findViewById(R.id.linear_layout_login_activity);
        initializeBackground(linearLayoutLoginActivity);
        /* Setup the progress dialog that is displayed later when authenticating with Firebase */
        mAuthProgressDialog = new ProgressDialog(this);
        mAuthProgressDialog.setTitle(getString(R.string.progress_dialog_loading));
        mAuthProgressDialog.setMessage(getString(R.string.progress_dialog_authenticating_with_firebase));
        mAuthProgressDialog.setCancelable(false);
        /* Setup Google Sign In */
        setupGoogleSignIn();
    }

    /**
     * Sign in with Password provider (used when user taps "Done" action on keyboard)
     */
    public void signInPassword() {
        String email = mEditTextEmailInput.getText().toString();
        if (email.length() == 0) {
            showErrorToast(getString(R.string.error_cannot_be_empty));
            return;
        }
        final int minPasswordLength = 6;
        final String password = mEditTextPasswordInput.getText().toString();
        if (password.length() == 0) {
            showErrorToast(getString(R.string.error_cannot_be_empty));
            return;
        }

        mAuthProgressDialog.show();

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(
                new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (mAuthProgressDialog.isShowing()) mAuthProgressDialog.dismiss();

                        if (task.isSuccessful()) {
                            String encodedEmail = Utils.encodeEmail(mAuth.getCurrentUser().getEmail());
                            DatabaseReference ref = Constants.FIREBASE_LOCATION_ACTIVE_USERS.child(encodedEmail);
                            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    User user = dataSnapshot.getValue(User.class);
                                    setAuthenticatedUserPasswordProvider(mAuth.getCurrentUser(), user.getName());
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                }
                            });

                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            try {
                                throw task.getException();
                            } catch (FirebaseNetworkException e) {
                                showErrorToast(getString(R.string.error_message_failed_sign_in_no_network));
                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                if (e.getErrorCode().equals("ERROR_INVALID_EMAIL")) {
                                    showErrorToast(getString(R.string.error_message_email_issue));
                                }
                                if (e.getErrorCode().equals("ERROR_WRONG_PASSWORD")) {
                                    if (password.length() < minPasswordLength) {
                                        showErrorToast(getString(R.string.error_invalid_password_not_valid));
                                    } else {
                                        showErrorToast(getString(R.string.error_invalid_password_not_correct));
                                    }
                                }
                            } catch (FirebaseAuthInvalidUserException e) {
                                showErrorToast(getString(R.string.error_email_does_not_exist));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

        );
    }

    /**
     * Helper method that makes sure a user is created if the user
     * logs in with Firebase's email/password provider.
     * */

    private void setAuthenticatedUserPasswordProvider(FirebaseUser currentUser, String userName) {
        String email = currentUser.getEmail();
        String encodedEmail = Utils.encodeEmail(email);
        String provider = Constants.PREF_PROVIDER_PASSWORD;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Constants.KEY_PREF_USERNAME, userName);
        editor.putString(Constants.KEY_PREF_ENCODED_EMAIL, encodedEmail);
        editor.putString(Constants.KEY_PREF_PROVIDER, provider);
        editor.commit();
    }

    /**
     * Helper method that makes sure a user is created if the user
     * logs in with Firebase's Google login provider.
     */
    private void setAuthenticatedUserGoogle(FirebaseUser currentUser, String userName) {
        String email = currentUser.getEmail();
        String encodedEmail = Utils.encodeEmail(email);
        String provider = Constants.PREF_PROVIDER_GOOGLE;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Constants.KEY_PREF_USERNAME, userName);
        editor.putString(Constants.KEY_PREF_ENCODED_EMAIL, encodedEmail);
        editor.putString(Constants.KEY_PREF_PROVIDER, provider);
        editor.commit();
    }

    /**
     * Show error toast to users
     */
    private void showErrorToast(String message) {
        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
    }


    /**
     * Signs you into ShoppingList++ using the Google Login Provider
     */
    private void loginWithGoogle() {
        AuthCredential credential = GoogleAuthProvider.getCredential(mGoogleAccount.getIdToken(), null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(
                new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            final String userEmail = mAuth.getCurrentUser().getEmail().toLowerCase();
                            final String userName = mAuth.getCurrentUser().getDisplayName();
                            final String userEmailKey = userEmail.replaceAll("\\.", ",");

                            DatabaseReference ref = Constants.FIREBASE_LOCATION_ACTIVE_USERS
                                    .child(userEmailKey);
                            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    // If user doesn't exist in db, create one
                                    if (dataSnapshot.getValue() == null) {
                                        User newUser = new User(userEmail, userName);
                                        DatabaseReference newRef = Constants.FIREBASE_LOCATION_ACTIVE_USERS
                                                .child(userEmailKey);
                                        newRef.setValue(newUser);
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                            mAuthProgressDialog.dismiss();
                            setAuthenticatedUserGoogle(mAuth.getCurrentUser(), userName);
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            try {
                                throw task.getException();
                            } catch (FirebaseNetworkException e) {
                                showErrorToast(getString(R.string.error_message_failed_sign_in_no_network));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
        );
    }

    /**
     * GOOGLE SIGN IN CODE
     * <p>
     * This code is mostly boiler plate from
     * https://developers.google.com/identity/sign-in/android/start-integrating
     * and
     * https://github.com/googlesamples/google-services/blob/master/android/signin/app/src/main/java/com/google/samples/quickstart/signin/SignInActivity.java
     * <p>
     * The big picture steps are:
     * 1. User clicks the sign in with Google button
     * 2. An intent is started for sign in.
     * - If the connection fails it is caught in the onConnectionFailed callback
     * - If it finishes, onActivityResult is called with the correct request code.
     * 3. If the sign in was successful, set the mGoogleAccount to the current account and
     * then call get GoogleOAuthTokenAndLogin
     * 4. firebaseAuthWithGoogle launches an AsyncTask to get an OAuth2 token from Google.
     * 5. Once this token is retrieved it is available to you in the onPostExecute method of
     * the AsyncTask. **This is the token required by Firebase**
     */


    /* Sets up the Google Sign In Button : https://developers.google.com/android/reference/com/google/android/gms/common/SignInButton */
    private void setupGoogleSignIn() {
        SignInButton signInButton = (SignInButton) findViewById(R.id.login_with_google);
        signInButton.setSize(SignInButton.SIZE_WIDE);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSignInGooglePressed(v);
            }
        });
    }

    /**
     * Sign in with Google plus when user clicks "Sign in with Google" textView (button)
     */
    public void onSignInGooglePressed(View view) {

        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
        mAuthProgressDialog.show();

    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        /**
         * An unresolvable error has occurred and Google APIs (including Sign-In) will not
         * be available.
         */
        mAuthProgressDialog.dismiss();
        showErrorToast(result.toString());
    }


    /**
     * This callback is triggered when any startActivityForResult finishes. The requestCode maps to
     * the value passed into startActivityForResult.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /* Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...); */
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }

    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(LOG_TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            /* Signed in successfully, get the OAuth token */
            mGoogleAccount = result.getSignInAccount();
            loginWithGoogle();


        } else {
            if (result.getStatus().getStatusCode() == GoogleSignInStatusCodes.SIGN_IN_CANCELLED) {
                showErrorToast("The sign in was cancelled. Make sure you're connected to the internet and try again.");
            } else {
                showErrorToast("Error handling the sign in: " + result.getStatus().getStatusMessage());
            }
            mAuthProgressDialog.dismiss();
        }
    }

    /**
     * Gets the GoogleAuthToken and logs in.
     */
//    private void firebaseAuthWithGoogle() {
//        /* Get OAuth token in Background */
//        AsyncTask<Void, Void, AuthCredential> task = new AsyncTask<Void, Void, AuthCredential>() {
//            String mErrorMessage = null;
//
//            @Override
//            protected AuthCredential doInBackground(Void... params) {
//                //String token = null;
//                Log.v(LOG_TAG, mGoogleAccount.toString());
//                Log.v(LOG_TAG, "idToken is null? " + ((Boolean)(mGoogleAccount.getIdToken() == null)).toString());
//                AuthCredential credential = GoogleAuthProvider.getCredential(mGoogleAccount.getIdToken(), null);
//
////                try {
////                    String scope = String.format(getString(R.string.oauth2_format), new Scope(Scopes.PROFILE)) + " email";
////
////                    token = GoogleAuthUtil.getToken(LoginActivity.this, mGoogleAccount.getEmail(), scope);
////                } catch (IOException transientEx) {
////                    /* Network or server error */
////                    Log.e(LOG_TAG, getString(R.string.google_error_auth_with_google) + transientEx);
////                    mErrorMessage = getString(R.string.google_error_network_error) + transientEx.getMessage();
////                } catch (UserRecoverableAuthException e) {
////                    Log.w(LOG_TAG, getString(R.string.google_error_recoverable_oauth_error) + e.toString());
////
////                    /* We probably need to ask for permissions, so start the intent if there is none pending */
////                    if (!mGoogleIntentInProgress) {
////                        mGoogleIntentInProgress = true;
////                        Intent recover = e.getIntent();
////                        startActivityForResult(recover, RC_GOOGLE_LOGIN);
////                    }
////                } catch (GoogleAuthException authEx) {
////                    /* The call is not ever expected to succeed assuming you have already verified that
////                     * Google Play services is installed. */
////                    Log.e(LOG_TAG, " " + authEx.getMessage(), authEx);
////                    mErrorMessage = getString(R.string.google_error_auth_with_google) + authEx.getMessage();
////                }
//                return credential;
//            }
//
//            @Override
//            protected void onPostExecute(AuthCredential credential) {
//                mAuthProgressDialog.dismiss();
//                if (credential != null) {
//                    /* Successfully got OAuth token, now login with Google */
//                    loginWithGoogle(credential);
//                } else if (mErrorMessage != null) {
//                    showErrorToast(mErrorMessage);
//                }
//            }
//        };
//
//        task.execute();
//    }
}
