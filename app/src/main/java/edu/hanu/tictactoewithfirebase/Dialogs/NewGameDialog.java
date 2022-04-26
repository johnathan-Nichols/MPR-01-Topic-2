package edu.hanu.tictactoewithfirebase.Dialogs;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.appcompat.widget.SwitchCompat;

import edu.hanu.tictactoewithfirebase.CreateRoom;
import edu.hanu.tictactoewithfirebase.R;

public class NewGameDialog extends AppCompatDialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.new_game_dialog, null);

        builder.setView(view)
                .setTitle("Create a Room")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Create", (dialogInterface, i) -> {
                    Intent intent = new Intent(view.getContext(), CreateRoom.class);

                    intent.putExtra("SEEK_PLAYERS", ((SwitchCompat)view.findViewById(R.id.switch_SeekPlayers)).isChecked());
                    intent.putExtra("GO_FIRST", ((SwitchCompat)view.findViewById(R.id.switch_GoFirst)).isChecked());

                    startActivity(intent);
                });

        return  builder.create();
    }
}
