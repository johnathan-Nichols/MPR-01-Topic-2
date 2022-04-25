package edu.hanu.tictactoewithfirebase;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

import edu.hanu.tictactoewithfirebase.Dialogs.CreateDialog;
import edu.hanu.tictactoewithfirebase.Dialogs.CreatePlayDialog;
import edu.hanu.tictactoewithfirebase.Dialogs.NewGameDialog;
import edu.hanu.tictactoewithfirebase.database.GameObject;

public class MainActivity extends AppCompatActivity {
    public static final String ACTIVE_ROOM="ACTIVE_ROOM";
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        //log out
        findViewById(R.id.btnExit).setOnClickListener(view->{
            new AlertDialog.Builder(view.getContext())
                .setIcon(R.drawable.ic_return)
                .setTitle("Please confirm")
                .setMessage("Are you sure that you want to log out?")
                .setPositiveButton("Yes", (dialogInterface, i) -> {
                    mAuth.signOut();
                    startActivity(new Intent(this, Login.class));
                })
                .setNegativeButton("No", null)
                .show();
        });

        String name = "Welcome "+ Objects.requireNonNull(Objects.requireNonNull(mAuth.getCurrentUser()).getEmail()).split("@")[0];
        ((TextView) findViewById(R.id.tvWel)).setText(name);

        findViewById(R.id.tvRan).setOnClickListener(view ->{
            Toast.makeText(this, "Join Random Room not set up yet", Toast.LENGTH_SHORT).show();
            //TODO: this
        });
        findViewById(R.id.tvCode).setOnClickListener(view ->{
            CreateDialog createDialog = new CreateDialog();
            createDialog.show(getSupportFragmentManager(), "GetRoomCode");
        });
        findViewById(R.id.tvBot).setOnClickListener(view -> {
            CreatePlayDialog createDialog = new CreatePlayDialog();
            createDialog.show(getSupportFragmentManager(), "GetRoomCode");
        });
        findViewById(R.id.tvCreateRoom).setOnClickListener(view -> {
            //see if we have a room first
            SharedPreferences sharedPreferences =this.getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
            String activeRoom = sharedPreferences.getString(ACTIVE_ROOM, null);

            if(activeRoom==null){
                NewGameDialog createDialog = new NewGameDialog();
                createDialog.show(getSupportFragmentManager(), "CreateRoom");
            }else{
                Intent intent = new Intent(getApplicationContext(), CreateRoom.class);

                intent.putExtra(ACTIVE_ROOM, activeRoom);

                startActivity(intent);
            }
        });
    }
}