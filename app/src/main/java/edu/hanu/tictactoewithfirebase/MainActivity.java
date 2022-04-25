package edu.hanu.tictactoewithfirebase;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

import edu.hanu.tictactoewithfirebase.Dialogs.CreateDialog;
import edu.hanu.tictactoewithfirebase.Dialogs.CreatePlayDialog;
import edu.hanu.tictactoewithfirebase.Dialogs.NewGameDialog;
import edu.hanu.tictactoewithfirebase.database.GameObject;

public class MainActivity extends AppCompatActivity {
    public static final String ACTIVE_ROOM="ACTIVE_ROOM";
    public static final String GAME_OBJECT="GAME_OBJECT";
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        DatabaseReference myRef = FirebaseDatabase.getInstance("https://mpr01-topic2-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("games");

        //log out
        findViewById(R.id.btnExit).setOnClickListener(view-> new AlertDialog.Builder(view.getContext())
            .setIcon(R.drawable.ic_return)
            .setTitle("Please confirm")
            .setMessage("Are you sure that you want to log out?")
            .setPositiveButton("Yes", (dialogInterface, i) -> {
                mAuth.signOut();
                startActivity(new Intent(this, Login.class));
            })
            .setNegativeButton("No", null)
            .show());

        String name = "Welcome "+ Objects.requireNonNull(Objects.requireNonNull(mAuth.getCurrentUser()).getEmail()).split("@")[0];
        ((TextView) findViewById(R.id.tvWel)).setText(name);

        //join random
        findViewById(R.id.tvRan).setOnClickListener(view ->{
            final boolean[] hasFoundUser = {false};

            //Search for room with isSeekingPlayers
            myRef.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    GameObject value = snapshot.getValue(GameObject.class);
                    assert value != null;
                    if(value.isSeekingPlayers && (value.playerXEmail.length()<1 || value.playerOEmail.length()<1)){
                        getSharedPreferences(getPackageName(), Context.MODE_PRIVATE).edit().putString(MainActivity.ACTIVE_ROOM, value.roomId).apply();

                        if(value.playerXEmail.length()<1){
                            value.playerXEmail = mAuth.getCurrentUser().getEmail();
                        }else {
                            value.playerOEmail = mAuth.getCurrentUser().getEmail();
                        }

                        myRef.child(value.roomId).setValue(value);

                        Intent intent = new Intent(getBaseContext(), PlayGame.class);

                        Bundle bundle = new Bundle();

                        bundle.putSerializable(GAME_OBJECT, value);

                        intent.putExtras(bundle);

                        startActivity(intent);

                        myRef.removeEventListener(this);

                        hasFoundUser[0] = true;
                    }
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            if(!hasFoundUser[0]) Toast.makeText(this, "Waiting for room.", Toast.LENGTH_SHORT).show();
        });

        //join by code
        findViewById(R.id.tvCode).setOnClickListener(view ->{
            CreateDialog createDialog = new CreateDialog();
            createDialog.show(getSupportFragmentManager(), "GetRoomCode");
        });

        //play with AI
        findViewById(R.id.tvBot).setOnClickListener(view -> {
            CreatePlayDialog createDialog = new CreatePlayDialog();
            createDialog.show(getSupportFragmentManager(), "GetRoomCode");
        });

        //onCreate room
        findViewById(R.id.tvCreateRoom).setOnClickListener(view -> {
            //see if we have a room first
            SharedPreferences sharedPreferences =this.getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
            String activeRoom = sharedPreferences.getString(ACTIVE_ROOM, null);
            Log.d(TAG, "onCreate: "+activeRoom);

            if(activeRoom==null){
                NewGameDialog createDialog = new NewGameDialog();
                createDialog.show(getSupportFragmentManager(), "CreateRoom");
            }else{
                //send the GameObject
                Intent intent = new Intent(getApplicationContext(), CreateRoom.class);

                intent.putExtra(ACTIVE_ROOM, activeRoom);

                startActivity(intent);

            }
        });
    }
}