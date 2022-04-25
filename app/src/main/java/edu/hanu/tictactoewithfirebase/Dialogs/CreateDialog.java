package edu.hanu.tictactoewithfirebase.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

import edu.hanu.tictactoewithfirebase.MainActivity;
import edu.hanu.tictactoewithfirebase.PlayGame;
import edu.hanu.tictactoewithfirebase.R;
import edu.hanu.tictactoewithfirebase.database.GameObject;

public class CreateDialog extends AppCompatDialogFragment {
    private static final String TAG = "CreateDialog";

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_dialog, null);

        builder.setView(view)
            .setTitle("Join By Code")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Join", (dialogInterface, i) -> {
                EditText mEditTextRoomCode = view.findViewById(R.id.edit_room_code);
                DatabaseReference myRef = FirebaseDatabase.getInstance("https://mpr01-topic2-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("games").child(mEditTextRoomCode.getText().toString());

                myRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        GameObject gameObject = snapshot.getValue(GameObject.class);
                        assert gameObject != null;

                        //verify GameObject
                        if(gameObject.playerXEmail.length()<1){
                            gameObject.playerXEmail = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail();
                        }
                        else if(gameObject.playerOEmail.length()<1){
                            gameObject.playerOEmail = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail();
                        }

                        myRef.setValue(gameObject);

                        assert gameObject.playerXEmail != null;
                        assert gameObject.playerOEmail != null;
                        if(gameObject.playerXEmail.length() > 0 && gameObject.playerOEmail.length()>0) {
                            view.getContext().getSharedPreferences(view.getContext().getPackageName(), Context.MODE_PRIVATE).edit().putString(MainActivity.ACTIVE_ROOM, gameObject.roomId).apply();

                            Intent intent = new Intent(view.getContext(), PlayGame.class);

                            Bundle bundle = new Bundle();

                            bundle.putSerializable(MainActivity.GAME_OBJECT, gameObject);

                            intent.putExtras(bundle);

                            view.getContext().startActivity(intent);

                            myRef.child(gameObject.roomId).removeEventListener(this);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            });

        return  builder.create();
    }
}
