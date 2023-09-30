package com.example.smsphishing;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignUpActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private EditText signUpEmail, signUpPassword, signUpConfirmPassword;
    private Button signUpButton;
    private TextView signInTextButton;
    boolean passwordVisible;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        auth = FirebaseAuth.getInstance();
        signUpEmail = findViewById(R.id.sign_up_email);
        signUpPassword = findViewById(R.id.sign_up_password);
        signUpConfirmPassword = findViewById(R.id.confirm_password);
        signUpButton = findViewById(R.id.sign_up_button);
        signInTextButton = findViewById(R.id.tv_sign_in_text2);

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = signUpEmail.getText().toString().trim();
                String pass = signUpPassword.getText().toString().trim();
                String pass2 = signUpConfirmPassword.getText().toString().trim();

                if (email.isEmpty()) {
                    Toast.makeText(SignUpActivity.this, "Please fill the details",
                            Toast.LENGTH_SHORT).show();
                    signUpEmail.setError("Email should not be empty");
                    signUpEmail.requestFocus();
                }
                if (pass.isEmpty()) {
                    Toast.makeText(SignUpActivity.this, "Please fill the details",
                            Toast.LENGTH_SHORT).show();
                    signUpPassword.setError("Password should not be empty");
                    signUpPassword.requestFocus();
                }
                if (pass2.isEmpty()) {
                    Toast.makeText(SignUpActivity.this,
                            "Please fill the details", Toast.LENGTH_SHORT).show();
                    signUpConfirmPassword.setError("Confirm Password should not be empty");
                    signUpConfirmPassword.requestFocus();
                } else if (!pass.equals(pass2)) {
                    Toast.makeText(SignUpActivity.this,
                            "Please enter same password", Toast.LENGTH_SHORT).show();
                } else {
                    auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(SignUpActivity.this,
                                        "SignUp successful", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(SignUpActivity.this, SignInActivity.class));
                            } else {
                                Toast.makeText(SignUpActivity.this,
                                        "SignUp failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

        signUpPassword.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int right = 2;
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= signUpPassword.getRight() - signUpPassword
                            .getCompoundDrawables()[right].getBounds().width()) {
                        int selection = signUpPassword.getSelectionEnd();
                        if (passwordVisible) {
                            signUpPassword.setCompoundDrawablesRelativeWithIntrinsicBounds(
                                    0, 0, R.drawable.baseline_visibility_off_24, 0
                            );
                            signUpPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                            passwordVisible = false;
                        } else {
                            signUpPassword.setCompoundDrawablesRelativeWithIntrinsicBounds(
                                    0, 0, R.drawable.baseline_visibility_24, 0
                            );
                            signUpPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                            passwordVisible = true;
                        }
                        signUpPassword.setSelection(selection);
                        return true;
                    }
                }
                return false;
            }
        });

        signUpConfirmPassword.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int right = 2;
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= signUpConfirmPassword.getRight() - signUpConfirmPassword
                            .getCompoundDrawables()[right].getBounds().width()) {
                        int selection = signUpConfirmPassword.getSelectionEnd();
                        if (passwordVisible) {
                            signUpConfirmPassword.setCompoundDrawablesRelativeWithIntrinsicBounds(
                                    0, 0, R.drawable.baseline_visibility_off_24, 0
                            );
                            signUpConfirmPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                            passwordVisible = false;
                        } else {
                            signUpConfirmPassword.setCompoundDrawablesRelativeWithIntrinsicBounds(
                                    0, 0, R.drawable.baseline_visibility_24, 0
                            );
                            signUpConfirmPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                            passwordVisible = true;
                        }
                        signUpConfirmPassword.setSelection(selection);
                        return true;
                    }
                }
                return false;
            }
        });

        signInTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignUpActivity.this, SignInActivity.class));
            }
        });

    }
}