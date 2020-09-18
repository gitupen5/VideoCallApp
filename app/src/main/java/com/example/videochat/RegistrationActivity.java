package com.example.videochat;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.hbb20.CountryCodePicker;

public class RegistrationActivity extends AppCompatActivity {

    private CountryCodePicker ccp;
    private EditText phoneText;
    private EditText codeText; //the code user will receive.
    private Button continueAndNextBtn;
    private String checker = "", phoneNumber = "";
    private RelativeLayout relativeLayout;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    private FirebaseAuth mAuth;
    private String mVarificationId;

    private PhoneAuthProvider.ForceResendingToken mResendToken;

    private ProgressDialog loadingBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        mAuth = FirebaseAuth.getInstance();
        // put RegistrationActivity.this if this doesn't work.
        loadingBar = new ProgressDialog(this);

        //Finding specific views
        phoneText = findViewById(R.id.phoneText);
        codeText = findViewById(R.id.codeText);
        continueAndNextBtn = (Button) findViewById(R.id.continueNextButton);
        relativeLayout = findViewById(R.id.phoneAuth);

        ccp = (CountryCodePicker) findViewById(R.id.ccp);

        //registering ccp with ph number editText.
        ccp.registerCarrierNumberEditText(phoneText);

        //When User Click on Continue button.
        continueAndNextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (continueAndNextBtn.getText().equals("Submit") || checker.equals("Code Sent")) {
                    String verificationCode = codeText.getText().toString();

                    if (verificationCode.equals("")) {
                        Toast.makeText(RegistrationActivity.this, "Please enter verification code first!", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        loadingBar.setTitle("Code Verification");
                        loadingBar.setMessage("Please Wait while we're verifying your code.");
                        loadingBar.setCanceledOnTouchOutside(false);
                        loadingBar.show();

                        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVarificationId, verificationCode);
                        signInWithPhoneAuthCredential(credential);
                    }
                }

                else {
                    phoneNumber = ccp.getFullNumberWithPlus();
                    if (!phoneNumber.equals("")) {
                        loadingBar.setTitle("Phone Number Verification");
                        loadingBar.setMessage("Please Wait while we're verifying your phone number..");
                        loadingBar.setCanceledOnTouchOutside(false);
                        loadingBar.show();

                        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                                phoneNumber, //Phone no. to verify
                                60, //Timeout duration
                                TimeUnit.SECONDS, //Unit of timeout
                                RegistrationActivity.this,  //Activity (for callbacks binding)
                                mCallbacks);    //OnVerificationStateChangedCallbacks
                    }
                    else {
                        Toast.makeText(RegistrationActivity.this, "Please enter valid phone number.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        //Verification States (eg. Verification completed or verification failed)
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            //When Verification Completed
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential)
            {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            //When Verification Failed
            @Override
            public void onVerificationFailed(FirebaseException e)
            {
                Toast.makeText(RegistrationActivity.this, "Invalid Phone Number!", Toast.LENGTH_SHORT).show();

                loadingBar.dismiss();
                relativeLayout.setVisibility(View.VISIBLE);

                continueAndNextBtn.setText("Continue");
                codeText.setVisibility(View.GONE);
            }

            //When SimCard is in another phone.
            @Override
            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);

                mVarificationId = s;
                mResendToken = forceResendingToken;

                relativeLayout.setVisibility(View.GONE);
                checker = "Code Sent";

                continueAndNextBtn.setText("Submit");
                codeText.setVisibility(View.VISIBLE);

                loadingBar.dismiss();
                Toast.makeText(RegistrationActivity.this, "Code has been sent.", Toast.LENGTH_SHORT).show();
            }
        };


    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        //User is already logged in
        if(firebaseUser != null){
            Intent homeIntent = new Intent(RegistrationActivity.this, MainActivity.class);
            startActivity(homeIntent);
            finish();
        }
    }

    //Sign in the user with PhoneAuth Credential.
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //Sign in Successful
                            loadingBar.dismiss();
                            Toast.makeText(RegistrationActivity.this, "You're logged in successfully", Toast.LENGTH_SHORT).show();
                            sendUserToMainActivity();
                        }
                        else {
                                loadingBar.dismiss();
                                String e = task.getException().toString();
                                Toast.makeText(RegistrationActivity.this, "Error: " + e, Toast.LENGTH_SHORT).show();
                        }
                     }
                });
    }

    //Sending user to another activity.
    private void sendUserToMainActivity()
    {
        Intent intent = new Intent(RegistrationActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }


}