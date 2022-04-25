package edu.hanu.tictactoewithfirebase;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import android.content.ClipboardManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

import edu.hanu.tictactoewithfirebase.database.GameObject;

public class CreateRoom extends AppCompatActivity {
    private static final String TAG = "CreateRoom";
    String roomID="";
    TextView roomIDText;
    DatabaseReference myRef;
    GameObject gameObject;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);

        findViewById(R.id.btnBack).setOnClickListener(view-> startActivity(new Intent(getBaseContext(), MainActivity.class)));

        myRef = FirebaseDatabase.getInstance("https://mpr01-topic2-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("games");

        roomIDText=findViewById(R.id.tvRoomId);

        //allow invite random
        findViewById(R.id.tvInviRan).setOnClickListener(view -> {
            if(roomID==null || roomID.length()<1) return;

            Toast.makeText(this, "Waiting for a random player to join.", Toast.LENGTH_SHORT).show();

            myRef.child(roomID).child("isSeekingPlayers").setValue(true);
        });

        //invite friend
        findViewById(R.id.tvInvite).setOnClickListener(view->{
            if(roomID==null || roomID.length()<1) return;

            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("RoomId", roomID);
            clipboard.setPrimaryClip(clip);

            Toast.makeText(this, "Waiting for players to join.\nRoomId has been copied to clipboard.", Toast.LENGTH_SHORT).show();

            myRef.child(roomID).child("isSeekingPlayers").setValue(false);
        });

        if(savedInstanceState == null){
            Bundle extras = getIntent().getExtras();

            //we set values
            if(extras != null){
                String room = extras.getString(MainActivity.ACTIVE_ROOM, null);
                Log.d(TAG, "onCreate: "+room);
                if(room !=null){
                    LoadRoom(room);
                }else{
                    boolean playerGoesFirst = extras.getBoolean("GO_FIRST", true);
                    boolean seekPlayers = extras.getBoolean("SEEK_PLAYERS", true);
                    CreateNewRoom(seekPlayers, playerGoesFirst);
                }
            }
        }
    }

    void CreateNewRoom(boolean seekPlayers, boolean playerGoesFirst){
        gameObject = new GameObject(seekPlayers, playerGoesFirst);

        //region Firebase create/read
        roomID = myRef.push().getKey();

        this.getSharedPreferences(getPackageName(), Context.MODE_PRIVATE).edit().putString(MainActivity.ACTIVE_ROOM, roomID).apply();

        gameObject.roomId=roomID;

        assert roomID != null;
        myRef.child(roomID).setValue(gameObject);
        roomIDText.setText(roomID);

        WaitForJoin();
    }

    void LoadRoom(String room){
        roomID = room;

        //loadRoom
        myRef.child(roomID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue()==null) {
                    getSharedPreferences(getPackageName(), Context.MODE_PRIVATE).edit().remove(MainActivity.ACTIVE_ROOM).apply();
                    startActivity(new Intent(getBaseContext(), MainActivity.class));
                    myRef.child(roomID).removeEventListener(this);
                    return;
                }

                gameObject = snapshot.getValue(GameObject.class);

                assert gameObject != null;
                if(gameObject.playerXEmail.length()>0 && gameObject.playerOEmail.length()>0){
                    Intent intent = new Intent(getBaseContext(), PlayGame.class);

                    Bundle bundle = new Bundle();

                    bundle.putSerializable(MainActivity.GAME_OBJECT, gameObject);

                    intent.putExtras(bundle);

                    startActivity(intent);

                    myRef.child(roomID).removeEventListener(this);
                }

                myRef.child(roomID).removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        WaitForJoin();
    }

    void WaitForJoin(){
        myRef.child(roomID).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Log.d(TAG, "onChildChanged: "+snapshot);

                switch (Objects.requireNonNull(snapshot.getKey())){
                    case "playerOEmail":
                        gameObject.playerOEmail=snapshot.getValue(String.class);
                        break;
                    case "playerXEmail":
                        gameObject.playerXEmail=snapshot.getValue(String.class);
                        break;
                }

                assert gameObject.playerXEmail != null;
                assert gameObject.playerOEmail != null;
                if(gameObject.playerXEmail.length()>0 && gameObject.playerOEmail.length()>0) {
                    Intent intent = new Intent(getBaseContext(), PlayGame.class);

                    Bundle bundle = new Bundle();

                    bundle.putSerializable(MainActivity.GAME_OBJECT, gameObject);

                    intent.putExtras(bundle);

                    startActivity(intent);

                    myRef.child(roomID).removeEventListener(this);
                }
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
    }
}
