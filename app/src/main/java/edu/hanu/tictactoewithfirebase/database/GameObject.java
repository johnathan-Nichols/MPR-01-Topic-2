package edu.hanu.tictactoewithfirebase.database;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/*
 * Don't test connection anymore, let players play whenever.
 *
 * Get key on load, do not store with the object?
 *
 * TODO: Save game id in preferences and on app load, try to join room.
 *  remove from preferences if the game !isActive
 *  on create room, if has a saved game that has a null x or o, load that
 *  game instead of creating a new game.
 *
 * */
public class GameObject {
    //the date that this was created
    public Date creationDate;

    //if null and is looking set here
    //else, will be filled when a second player enters the Room ID
    public String playerXEmail;
    public String playerOEmail;

    //if true, look for a player who is looking to join a random room
    public boolean isSeekingPlayers;

    //populate with 0=Empty, 1=X, 2=O
    public List<Integer> movesList = new ArrayList<>();

    //used to determine if the game is won or not
    public boolean isActive;

    //used to decide who's turn it is
    public boolean isXTurn;

    public GameObject(Date creationDate, String playerXEmail, String playerOEmail, boolean isSeekingPlayers, int[] movesList, boolean isActive, boolean isXTurn) {
        this.creationDate = creationDate;
        this.playerXEmail = playerXEmail;
        this.playerOEmail = playerOEmail;
        this.isSeekingPlayers = isSeekingPlayers;
        for(int move : movesList){
            this.movesList.add(move);
        }
        this.isActive = isActive;
        this.isXTurn = isXTurn;
    }

    public GameObject(boolean isSeekingPlayers, boolean playerIsX) {
        this.creationDate = Calendar.getInstance().getTime();

        String email = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail();;

        if(playerIsX){
            this.playerXEmail = email;
            this.playerOEmail = "";
        }else {
            this.playerXEmail = "";
            this.playerOEmail = email;
        }

        for(int i=0;i<9;i++){
            movesList.add(0);
        }

        this.isSeekingPlayers = isSeekingPlayers;
        this.isActive = true;
        this.isXTurn = true;
    }
}
