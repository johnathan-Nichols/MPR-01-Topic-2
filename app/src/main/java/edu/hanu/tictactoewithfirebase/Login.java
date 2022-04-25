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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        //check if already logged in
        if(mAuth.getCurrentUser()!=null) startActivity(new Intent(getApplicationContext(), MainActivity.class));

        EditText mEmail=findViewById(R.id.tvEmail);
        EditText mPassword=findViewById(R.id.tvPass);

        //go to register
        findViewById(R.id.btnRegister).setOnClickListener(view -> startActivity(new Intent(this, Register.class)));

        //reset pass
        findViewById(R.id.tvForget).setOnClickListener(view -> mAuth.sendPasswordResetEmail(mEmail.getText().toString()).addOnCompleteListener(task->{
            String message;
            if(task.isSuccessful()){
                message = "Password reset email sent to "+mEmail.getText().toString()+".";
            }else{
                message = "Error, "+ Objects.requireNonNull(task.getException()).getMessage();
            }
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }));

        //attempt to login
         findViewById(R.id.btnLogin).setOnClickListener(view -> {
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

                    //authenticate the user
                    mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                        if(task.isSuccessful()){
                            Toast.makeText(this, "Successfully Logged In.", Toast.LENGTH_SHORT).show();

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