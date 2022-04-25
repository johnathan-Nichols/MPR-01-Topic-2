package edu.hanu.tictactoewithfirebase.Dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.appcompat.widget.SwitchCompat;

import edu.hanu.tictactoewithfirebase.PlayGame;
import edu.hanu.tictactoewithfirebase.R;

public class CreatePlayDialog extends AppCompatDialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_dialog_play_ai, null);

        builder.setView(view)
                .setTitle("Play Locally")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Play", (dialogInterface, i) -> {
                    Intent intent = new Intent(view.getContext(), PlayGame.class);

                    Bundle bundle = new Bundle();

                    SwitchCompat usesAI = view.findViewById(R.id.switch_usesAI);
                    String gameDiff = ((EditText) view.findViewById(R.id.start_game_diff)).getText().toString();
                    SwitchCompat aIisX = view.findViewById(R.id.switch_AIGoesFirst);

                    if(usesAI.isChecked()){
                        if(gameDiff.contains("[^210]") || gameDiff.length()!=1){
                            Toast.makeText(view.getContext(), "Invalid difficulty.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        int gameDifficulty = Integer.parseInt(gameDiff);

                        bundle.putInt("AI_IS_X", aIisX.isChecked()?1:0);
                        bundle.putInt("GAME_DIFF", gameDifficulty);

                        intent.putExtras(bundle);
                    }

                    startActivity(intent);
                });


        return  builder.create();
    }
}
