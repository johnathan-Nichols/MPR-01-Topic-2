package edu.hanu.tictactoewithfirebase.Dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import edu.hanu.tictactoewithfirebase.R;

public class CreateDialog extends AppCompatDialogFragment {
    EditText mEditTextRoomCode;

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
                    Toast.makeText(getActivity(), "Join room by code has not been implemented "+mEditTextRoomCode.getText().toString(), Toast.LENGTH_SHORT).show();
                });

        mEditTextRoomCode = view.findViewById(R.id.edit_room_code);

        return  builder.create();
    }
}
