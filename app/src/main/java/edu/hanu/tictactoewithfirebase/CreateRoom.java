package edu.hanu.tictactoewithfirebase;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import edu.hanu.tictactoewithfirebase.Dialogs.CreateDialog;
import edu.hanu.tictactoewithfirebase.Dialogs.NewGameDialog;
import edu.hanu.tictactoewithfirebase.database.GameObject;

public class CreateRoom extends AppCompatActivity {
    private static final String TAG = "CreateRoom";
    String roomID="";
    TextView roomIDText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);

        roomIDText=findViewById(R.id.tvRoomId);

        if(savedInstanceState == null){
            Bundle extras = getIntent().getExtras();

            //we set values
            if(extras != null){
                String activeRoom = extras.getString(MainActivity.ACTIVE_ROOM, null);
                if(activeRoom !=null){

                    LoadRoom(activeRoom);
                }else{
                    Log.d(TAG, "onCreate: new room");
                    boolean playerGoesFirst = extras.getBoolean("GO_FIRST", true);
                    boolean seekPlayers = extras.getBoolean("SEEK_PLAYERS", true);
                    CreateNewRoom(seekPlayers, playerGoesFirst);
                }
            }
        }
    }

    void CreateNewRoom(boolean seekPlayers, boolean playerGoesFirst){
        //roomIDText.setText(roomID);

        GameObject gameObject = new GameObject(seekPlayers, playerGoesFirst);

        //region Firebase create/read
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://mpr01-topic2-default-rtdb.asia-southeast1.firebasedatabase.app/");
        DatabaseReference myRef = database.getReference("games");
        myRef.push().setValue(gameObject).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                Toast.makeText(this, "Created Room", Toast.LENGTH_SHORT).show();
            }
        });

//        writeButton.setOnClickListener(view -> {
//            Log.d(TAG, "onCreate: write");
//            myRef.setValue("Hello, World!");
//        });
//
//        readButton.setOnClickListener(view -> {
//            Log.d(TAG, "onCreate: Read");
//
//            myRef.addValueEventListener(new ValueEventListener() {
//                @Override
//                public void onDataChange(DataSnapshot dataSnapshot) {
//                    // This method is called once with the initial value and again
//                    // whenever data at this location is updated.
//                    String value = dataSnapshot.getValue(String.class);
//                    Log.d(TAG, "Value is: " + value);
//                }
//
//                @Override
//                public void onCancelled(DatabaseError error) {
//                    // Failed to read value
//                    Log.w(TAG, "Failed to read value.", error.toException());
//                }
//            });
//        });
        //endregion Firebase create/read
    }

    void LoadRoom(String activeRoom){
        Toast.makeText(this, "Loading "+activeRoom, Toast.LENGTH_SHORT).show();
    }
}
