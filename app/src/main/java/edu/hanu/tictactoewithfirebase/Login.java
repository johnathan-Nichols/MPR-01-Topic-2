package edu.hanu.tictactoewithfirebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class Login extends AppCompatActivity {
    EditText mEmail, mPassword;
    Button mLoginButton;
    TextView mBtnRegister;
    FirebaseAuth mAuth;
    ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mEmail=findViewById(R.id.editTextEmail);
        mPassword=findViewById(R.id.editTextPassword);
        mLoginButton=findViewById(R.id.btnLogin);
        mBtnRegister=findViewById(R.id.textViewRegister);

        mAuth=FirebaseAuth.getInstance();
        mProgressBar=findViewById(R.id.progressBar2);

        mLoginButton.setOnClickListener(view -> {
            String email = mEmail.getText().toString().trim();
            String password = mPassword.getText().toString().trim();

            //region verify data
            //use this to throw error for all erroneous inputs before returning
            boolean shouldReturn=false;

            if(TextUtils.isEmpty(email)){
                mEmail.setError("An email is required.");
                shouldReturn=true;
            }

            if(TextUtils.isEmpty(password)){
                mPassword.setError("A password is required.");
                shouldReturn=true;
            }

            if(password.length()<6){
                mPassword.setError("The password must be at least 6 characters long.");
                shouldReturn=true;
            }

            if(shouldReturn) return;
            //endregion verify data

            mProgressBar.setVisibility(View.VISIBLE);

            //authenticate the user
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    Toast.makeText(this, "Successfully Logged In.", Toast.LENGTH_SHORT).show();

                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                }else{
                    Toast.makeText(this, "Error, "+ Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        mBtnRegister.setOnClickListener(view -> startActivity(new Intent(getApplicationContext(), Register.class)));
    }
}