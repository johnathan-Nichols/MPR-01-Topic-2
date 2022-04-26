package edu.hanu.tictactoewithfirebase;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class Register extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        //check if already logged in
        if(mAuth.getCurrentUser()!=null) startActivity(new Intent(getApplicationContext(), MainActivity.class));

        findViewById(R.id.imgbtnBackToLogin).setOnClickListener(view -> startActivity(new Intent(this, Login.class)));

        EditText mEmail = findViewById(R.id.txtInputEditTextCreateUsername);
        EditText mPassword = findViewById(R.id.txtInputEditTextCreatePassword);
        EditText mVerifyPassword = findViewById(R.id.txtInputEditTextConfirmPassword);

        findViewById(R.id.btnRegister).setOnClickListener(view->{
            String email = mEmail.getText().toString().trim();
            String password = mPassword.getText().toString().trim();
            String verifiedPassword = mVerifyPassword.getText().toString().trim();

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

            if(TextUtils.isEmpty(verifiedPassword)){
                mVerifyPassword.setError("A verified password is required.");
                shouldReturn=true;
            }

            if(password.length()<6){
                mPassword.setError("The password must be at least 6 characters long.");
                shouldReturn=true;
            }

            if(!password.equals(verifiedPassword)){
                mVerifyPassword.setError("The passwords do not match.");
                shouldReturn=true;
            }

            if(shouldReturn) return;
            //endregion verify data

            //register with firebase
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    Toast.makeText(this, "Successfully registered.", Toast.LENGTH_SHORT).show();

                    if(FirebaseAuth.getInstance().getCurrentUser()!=null){
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    }else{
                        Toast.makeText(this, "An unexpected has occurred, please log in.", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(this, "Error, "+ Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}