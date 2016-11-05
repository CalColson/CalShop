package com.example.cal.calshop.ui.login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Patterns;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.cal.calshop.R;
import com.example.cal.calshop.model.User;
import com.example.cal.calshop.ui.BaseActivity;
import com.example.cal.calshop.utils.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class CreateAccountActivity extends BaseActivity {
    private static final String LOG_TAG = CreateAccountActivity.class.getSimpleName();
    private ProgressDialog mAuthProgressDialog;
    private EditText mEditTextUsernameCreate, mEditTextEmailCreate, mEditTextPasswordCreate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        /**
         * Link layout elements from XML and setup the progress dialog
         */
        initializeScreen();
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
     * Link layout elements from XML and setup the progress dialog
     */
    public void initializeScreen() {
        mEditTextUsernameCreate = (EditText) findViewById(R.id.edit_text_username_create);
        mEditTextEmailCreate = (EditText) findViewById(R.id.edit_text_email_create);
        mEditTextPasswordCreate = (EditText) findViewById(R.id.edit_text_password_create);
        LinearLayout linearLayoutCreateAccountActivity = (LinearLayout) findViewById(R.id.linear_layout_create_account_activity);
        initializeBackground(linearLayoutCreateAccountActivity);

        /* Setup the progress dialog that is displayed later when authenticating with Firebase */
        mAuthProgressDialog = new ProgressDialog(this);
        mAuthProgressDialog.setTitle(getResources().getString(R.string.progress_dialog_loading));
        mAuthProgressDialog.setMessage(getResources().getString(R.string.progress_dialog_creating_user_with_firebase));
        mAuthProgressDialog.setCancelable(false);
    }

    /**
     * Open LoginActivity when user taps on "Sign in" textView
     */
    public void onSignInPressed(View view) {
        Intent intent = new Intent(CreateAccountActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Create new account using Firebase email/password provider
     */
    public void onCreateAccountPressed(View view) {
        final String userEmail = mEditTextEmailCreate.getText().toString();
        final String userEmailKey = userEmail.replaceAll("\\.", ",");
        final String userPassword = mEditTextPasswordCreate.getText().toString();
        final String userName = mEditTextUsernameCreate.getText().toString();

        if (!isUserNameValid(userName)) {
            mEditTextEmailCreate.setError(getString(R.string.error_cannot_be_empty));

            showErrorToast(getString(R.string.error_cannot_be_empty));

            return;
        }

        if (!isEmailValid(userEmail)) {
            mEditTextEmailCreate.setError(getString(R.string.error_invalid_email_not_valid,
                    mEditTextEmailCreate.getText().toString()));

            showErrorToast(getString(R.string.error_invalid_email_not_valid,
                    mEditTextEmailCreate.getText().toString()));

            return;
        }

        if (!isPasswordValid(userPassword)) {
            mEditTextPasswordCreate.setError(getString(R.string.error_invalid_password_not_valid));

            showErrorToast(getString(R.string.error_invalid_password_not_valid));

            return;
        }



        FirebaseAuth auth = FirebaseAuth.getInstance();

        auth.createUserWithEmailAndPassword(userEmail, userPassword)
        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.v(LOG_TAG, "Successfully created user account: " + task.getResult().getUser().getEmail());

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

                }
                else {
                    try {
                        throw task.getException();
                    }
                    catch (FirebaseAuthUserCollisionException e) {
                        showErrorToast(getString(R.string.error_email_taken));
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                        showErrorToast(getString(R.string.dialog_title_error));
                    }
                }
                if (mAuthProgressDialog.isShowing()) mAuthProgressDialog.dismiss();
            }
        });
        mAuthProgressDialog.show();
    }

    /**
     * Creates a new user in Firebase from the Java POJO
     */
    private void createUserInFirebaseHelper(final String encodedEmail) {
    }

    private boolean isEmailValid(String email) {
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return false;
        }

        return true;
    }

    private boolean isUserNameValid(String userName) {
        if (userName.length() == 0) return false;
        return true;
    }

    private boolean isPasswordValid(String password) {
        int minPasswordLength = 6;
        if (password.length() < minPasswordLength) {
            return false;
        }
        return true;
    }

    /**
     * Show error toast to users
     */
    private void showErrorToast(String message) {
        Toast.makeText(CreateAccountActivity.this, message, Toast.LENGTH_LONG).show();
    }
}
