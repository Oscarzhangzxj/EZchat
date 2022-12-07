package com.demo.firebasechat.ui;

import static android.text.TextUtils.isEmpty;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.demo.firebasechat.R;
import com.demo.firebasechat.models.User;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;


public class RegisterActivity extends AppCompatActivity implements
        View.OnClickListener {
    private static final String TAG = "RegisterActivity";
    private EditText mEmail, mPassword, mConfirmPassword;
    private ProgressBar mProgressBar;
    private FirebaseFirestore mDb;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mEmail = findViewById(R.id.input_email);
        mPassword = findViewById(R.id.input_password);
        mConfirmPassword = findViewById(R.id.input_confirm_password);
        mProgressBar = findViewById(R.id.progressBar);

        findViewById(R.id.btn_register).setOnClickListener(this);
        mDb = FirebaseFirestore.getInstance();
        hideSoftKeyboard();
    }

    public void registerNewEmail(final String email, String password) {
        showDialog();
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        User user = new User();
                        user.setEmail(email);
                        user.setUsername(email.substring(0, email.indexOf("@")));
                        user.setUser_id(FirebaseAuth.getInstance().getUid());

                        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                                .build();
                        mDb.setFirestoreSettings(settings);

                        DocumentReference newUserRef = mDb
                                .collection(getString(R.string.collection_users))
                                .document(FirebaseAuth.getInstance().getUid());

                        newUserRef.set(user).addOnCompleteListener(task1 -> {
                            hideDialog();
                            if (task1.isSuccessful()) {
                                redirectLoginScreen();
                            } else {
                                View parentLayout = findViewById(android.R.id.content);
                                Snackbar.make(parentLayout, task1.getException().getMessage(), Snackbar.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        View parentLayout = findViewById(android.R.id.content);
                        Snackbar.make(parentLayout, task.getException().getMessage(), Snackbar.LENGTH_SHORT).show();
                        hideDialog();
                    }
                });
    }

    private void redirectLoginScreen() {
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }


    private void showDialog() {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void hideDialog() {
        if (mProgressBar.getVisibility() == View.VISIBLE) {
            mProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void hideSoftKeyboard() {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_register: {
                if (!isEmpty(mEmail.getText().toString())
                        && !isEmpty(mPassword.getText().toString())
                        && !isEmpty(mConfirmPassword.getText().toString())) {

                    if (mPassword.getText().toString().equals(mConfirmPassword.getText().toString())) {
                        registerNewEmail(mEmail.getText().toString(), mPassword.getText().toString());
                    } else {
                        Toast.makeText(RegisterActivity.this, "Passwords do not Match", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(RegisterActivity.this, "You must fill out all the fields", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }
}
