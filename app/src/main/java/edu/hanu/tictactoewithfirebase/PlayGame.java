package edu.hanu.tictactoewithfirebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import edu.hanu.tictactoewithfirebase.database.GameObject;

public class PlayGame extends AppCompatActivity {
    private static final String TAG = "PlayGame";

    GameObject mGameObject;

    String email=null;
    DatabaseReference myRef;

    private final int[] boardMarks = new int[9];

    //use this for now
    private boolean isO;

    //if false, do not allow move
    private boolean gameActive = true;

    public boolean usesAI;

    public boolean aiIsX = true;

    public int aiDifficultyLevel = 0;

    //easy mode: pick random number
    private final List<Integer> openLocations = new ArrayList<>();
    static final List<Integer> staticLocations = new ArrayList<>();

    //the numbers that we use to check for victory
    private static final int[][] checkNumbers ={
            {0, 1, 2},
            {3, 4, 5},
            {6, 7, 8},
            {0, 3, 6},
            {1, 4, 7},
            {2, 5, 8},
            {0, 4, 8},
            {2, 4, 6}
    };

    private int turn = 0;

    private int oFirstTurn = -1;

    ImageView[] boardImages=new ImageView[9];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.play_screen);

        //Go Back
        findViewById(R.id.btnOut).setOnClickListener(view -> startActivity(new Intent(getApplicationContext(), MainActivity.class)));

        //initialize openLocations
        for (int i = 0; i < 9; i++) {
            staticLocations.add(i);
        }
        openLocations.addAll(staticLocations);

        for(int i=0;i<boardImages.length;i++){
            boardImages[i] = findViewById(getResources().getIdentifier("boardLoc"+i, "id", getPackageName()));
            boardImages[i].setTag(i);
            boardImages[i].setOnClickListener(view -> ClickedLocation((int) view.getTag()));
        }

        //just loaded
        if(savedInstanceState == null){
            Bundle extras = getIntent().getExtras();

            //we set values
            if(extras != null){
                mGameObject = (GameObject) extras.getSerializable(MainActivity.GAME_OBJECT);
                if(mGameObject!=null){
                    //online game
                    email = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail();
                    myRef = FirebaseDatabase.getInstance("https://mpr01-topic2-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("games").child(mGameObject.roomId);

                    myRef.child("isSeekingPlayers").setValue(false);

                    myRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            mGameObject = snapshot.getValue(GameObject.class);
                            openLocations.clear();
                            openLocations.addAll(staticLocations);

                            for(int i = 0; i< Objects.requireNonNull(mGameObject).movesList.size(); i++){
                                int move =mGameObject.movesList.get(i);
                                if(move>0){

                                    boardMarks[i] = move;

                                    boardImages[i].setImageResource(move==2?R.drawable.ic_o:R.drawable.ic_x);

                                    openLocations.remove(openLocations.indexOf(i));

                                    turn++;

                                    CheckVictory();
                                }
                            }

                            isO = !mGameObject.isXTurn;
                            gameActive = mGameObject.isActive;
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }else {
                    //local game
                    usesAI = extras.getInt("USES_AI", 1)==1;
                    aiIsX = extras.getInt("AI_IS_X", 1)==1;
                    aiDifficultyLevel=extras.getInt("GAME_DIFF", 0);
                }
            }
        }

        if (mGameObject==null && aiIsX && usesAI) ChooseAIMove();
    }

    /**
     * Gets the location that was clicked
     * <p>
     * tries to place a mark there
     */
    public void ClickedLocation(int location) {
        //stops player from moving if not their turn and it is AI turn
        if (!gameActive || (usesAI && aiIsX && !isO)) return;

        //verify for online
        if(mGameObject!=null && (!(isO && mGameObject.playerOEmail.equals(email))) && (!(!isO && mGameObject.playerXEmail.equals(email)))) return;

        //place mark
        try {
            PlaceMark(location);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //tell AI to move
        if (usesAI) ChooseAIMove();
    }

    /**
     * Use Board marks to see if X or O are winners
     * <p>
     * 0,1,2
     * 3,4,5
     * 6,7,8
     * <p>
     * 0,3,6
     * 1,4,7
     * 2,5,8
     * <p>
     * 0,4,8
     * 2,4,6
     * <p>
     * if they do match and >0,
     * Log X or O
     */
    public void CheckVictory() {
        for (int i = 0; i < 8; i++) {
            if (CheckItemVictory(boardMarks[checkNumbers[i][0]], boardMarks[checkNumbers[i][1]],
                    boardMarks[checkNumbers[i][2]])) {
                gameActive = false;

                getSharedPreferences(getPackageName(), Context.MODE_PRIVATE).edit().remove(MainActivity.ACTIVE_ROOM).apply();

                new AlertDialog.Builder(this)
                        .setIcon(R.drawable.ic_return)
                        .setTitle("Winner is " + (boardMarks[checkNumbers[i][0]]==1?"X.":"O."))
                        .setMessage("Would you like to return to the home page?")
                        .setPositiveButton("Yes", (dialogInterface, i1) -> startActivity(new Intent(this, MainActivity.class)))
                        .setNegativeButton("No", null)
                        .show();
                return;
            }
        }

        if(openLocations.size()==0) {
            gameActive = false;
            getSharedPreferences(getPackageName(), Context.MODE_PRIVATE).edit().remove(MainActivity.ACTIVE_ROOM).apply();
            new AlertDialog.Builder(getBaseContext())
                    .setIcon(R.drawable.ic_return)
                    .setTitle("The game is tied.")
                    .setMessage("Would you like to return to the home page?")
                    .setPositiveButton("Yes", (dialogInterface, i1) -> startActivity(new Intent(getBaseContext(), MainActivity.class)))
                    .setNegativeButton("No", null)
                    .show();
        }
    }

    //#region AI Methods
    public boolean CheckItemVictory(int a, int b, int c) {
        return a > 0 && a == b && a == c;
    }

    int GrabRandom() {
        int rand = (int) (Math.random() * openLocations.size());
        return openLocations.get(rand);
    }

    /**
     * returns GrabRandom() if no strings
     * <p>
     * retuns location of string cap
     * <p>
     * Does not return strings with cap already
     */
    int CheckStringOf2(boolean onNegRetGrabRandom) {
        int savedInt = -1;

        for (int i = 0; i < 8; i++) {
            int a = boardMarks[checkNumbers[i][0]],
                    b = boardMarks[checkNumbers[i][1]],
                    c = boardMarks[checkNumbers[i][2]];

            if(a>0 && b>0 && c>0) continue;

            if (Check(a, b, c)) {
                int clearNumb, markNumb;

                if (a == 0) {
                    clearNumb = 0;
                    markNumb = 1;
                } else {
                    markNumb = 0;

                    if (b == 0) {
                        clearNumb = 1;
                    } else {
                        clearNumb = 2;
                    }
                }

                if (XOR(aiIsX, boardMarks[checkNumbers[i][markNumb]] == 1)) {
                    return checkNumbers[i][clearNumb];
                } else {
                    savedInt = checkNumbers[i][clearNumb];
                }
            }
        }

        return savedInt > -1 ? savedInt : (onNegRetGrabRandom ? GrabRandom() : -1);
    }

    public boolean Check(int a, int b, int c) {
        int clearCount = 0,
                xCount = 0,
                oCount = 0;

        if(a>0 && b>0 && c>0) return false;

        for (int i : new int[]{a, b, c}) {
            switch (i) {
                case 0:
                    clearCount++;
                    //too many clear spaces
                    if (clearCount > 1) return false;
                    break;
                case 1:
                    xCount++;
                    if (oCount > 0 || xCount > 2) return false;
                    break;
                case 2:
                    oCount++;
                    if (xCount > 0 || oCount > 2) return false;
                    break;
            }
        }

        return true;
    }

    boolean XOR(boolean aXOR, boolean bXOR) {
        return (aXOR && bXOR) || (!(aXOR || bXOR));
    }

    int XStrategy() {
        switch (turn) {
            case 0:
                return 4;
            case 2:
                // #region case 2
                //Did the other player play on a corner or a side?
                for (int i = 0; i < 4; i++) {
                    if (boardMarks[(i > 1 ? i + 1 : i) * 2] > 0) {
                        switch (i) {
                            case 0:
                                return 8;
                            case 1:
                                return 6;
                            case 2:
                                return 2;
                            case 3:
                                return 0;
                        }
                    }
                }

                //if we're still here, then they played on a side
                //1,3,5,7

                int[] choicesA = new int[]{0, 2, 3, 5, 6, 8},
                        choicesB = new int[]{0, 6, 1, 7, 2, 8};

                //find which side was played on
                if (boardMarks[1] > 0) {
                    //random play from {0, 2}, {3, 5}, {6, 8}
                    return choicesA[(int) (Math.random() * choicesA.length)];
                } else if (boardMarks[3] > 0) {
                    //random play from {0, 6}, {1, 7}, {2, 8}
                    return choicesB[(int) (Math.random() * choicesB.length)];
                } else if (boardMarks[5] > 0) {
                    //random play from {0, 6}, {1, 7}, {2, 8}
                    return choicesB[(int) (Math.random() * choicesB.length)];
                } else if (boardMarks[7] > 0) {
                    //random play from {0, 2}, {3, 5}, {6, 8}
                    return choicesA[(int) (Math.random() * choicesA.length)];
                }

                //something went wrong
                return CheckStringOf2(true);
            //  #endregion case 2
        }

        int locToUse = CheckStringOf2(false);

        if (locToUse == -1) locToUse = TestFor3Trap();

        return locToUse;
    }

    int OStrategy() {
        switch (turn) {
            case 1:
                // #region case 1
                //x is in the middle place in a corner
                if (boardMarks[4] > 0) {
                    //grab a random corner
                    int[] possibleChoices = new int[]{0, 2, 6, 8};

                    oFirstTurn = possibleChoices[new Random().nextInt(possibleChoices.length)];

                    return oFirstTurn;
                }
                //x is in corner or on side, place in middle
                return 4;
            // #endregion case 1
            case 3:
                // #region case 3
                //check string of 2
                int getNumb = CheckStringOf2(false);

                //x is middle
                if (boardMarks[4] > 0) {
                    //if get string of 2 is > -1 return that
                    if (getNumb > -1) return getNumb;

                    int cornerA = new int[]{2, 6}[new Random().nextInt(2)],
                            cornerB = new int[]{0, 8}[new Random().nextInt(2)];

                    //x placed across from us
                    switch (oFirstTurn) {
                        case 0:
                        case 8:
                            return cornerA;
                        case 2:
                        case 6:
                            return cornerB;
                    }
                }

                //we are in the middle
                if (getNumb > -1) return getNumb;

                //x is in a line around us
                if ((boardMarks[0] == 1 && boardMarks[8] == 1) || (boardMarks[2] == 1 && boardMarks[6] == 1)) {
                    return new int[]{1, 3, 5, 7}[new Random().nextInt(4)];
                }

                return GrabRandom();
            // #endregion case 3
        }

        return CheckStringOf2(true);
    }

    /**
     * Follows the strategy found in the game
     */
    int CheckStrategy() {
        return aiIsX ? XStrategy() : OStrategy();
    }

    /**
     * moves for the AI
     */
    private void ChooseAIMove() {
        if (!gameActive || turn > 8) return;
        //make easy first
        
        if(openLocations.size() ==1){
            try {
                PlaceMark(openLocations.get(0));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }

        int location;

        switch (aiDifficultyLevel) {
            //medium
            case 1:
                //if there is a string of 2, place 1 at the end
                location = CheckStringOf2(true);
                break;
            //hard
            case 2:
                location = CheckStrategy();
                break;
            //easy
            default:
                //pick random available number
                location = GrabRandom();
                break;
        }

        //place at that number
        try {
            PlaceMark(location);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * CheckStringOf2(false) should be called first
     */
    int TestFor3Trap() {
        // #region cross check && T check
        //4 is empty, then {1,3}, {3,7}, {7,5} or {5,1} are equal but nothing else
        int[][] numbsToCrossTCheck =
                {
                        //cross
                        {
                                1, 3, 5, 7, 4
                        },
                        {
                                3, 7, 5, 1, 4
                        },

                        //T
                        {
                                0, 2, 4, 7, 1
                        },
                        {
                                5, 4, 6, 0, 3
                        },
                        {
                                1, 4, 6, 8, 7
                        },
                        {
                                3, 4, 8, 2, 5
                        },

                        //L
                        {
                                0, 1, 5, 8, 2
                        },
                        {
                                2, 5, 7, 6, 8
                        },
                        {
                                8, 7, 3, 0, 6
                        },
                        {
                                6, 3, 1, 2, 0
                        },

                        //angle
                        {
                                2, 4, 7, 8, 6
                        },
                        {
                                0, 4, 7, 6, 8
                        },
                        {
                                0, 4, 5, 2, 8
                        },
                        {
                                6, 4, 5, 8, 2
                        },
                        {
                                6, 4, 1, 0, 2
                        },
                        {
                                8, 4, 1, 2, 0
                        },
                        {
                                8, 4, 3, 6, 0
                        },
                        {
                                2, 4, 0, 3, 6
                        }
                };

        for (int i = 0; i < 18; i++) {
            if (CrossTCheck(boardMarks[numbsToCrossTCheck[i][0]],
                    boardMarks[numbsToCrossTCheck[i][1]],
                    boardMarks[numbsToCrossTCheck[i][2]],
                    boardMarks[numbsToCrossTCheck[i][3]],
                    boardMarks[numbsToCrossTCheck[i][4]])) {
                return numbsToCrossTCheck[i][4];
            }
        }
        // #endregion cross check

        return GrabRandom();
    }

    boolean CrossTCheck(int a0, int a1, int b0, int b1, int middle) {
        //cound the numbs over 0
        int xCount = 0, oCount = 0;
        for (int i : new int[]{a0, a1, b0, b1}) {
            if (i == 1) {
                if (oCount > 0 || !aiIsX) return false;
                xCount++;
            }
            if (i == 2) {
                if (xCount > 0 || aiIsX) return false;
                oCount++;
            }
        }

        return (!(middle > 0 || (a0 > 0 && a1 > 0) || (b0 > 0 && b1 > 0))) && ((a0 > 0 || a1 > 0) && (b0 > 0 || b1 > 0));
    }
    //#endregion AI Methods

    private void PlaceMark(int location) throws Exception {
        if (location < 0 || location > 8) throw new Exception("Location is invalid.");

        //verify location
        if (boardMarks[location] > 0) {
            return;
        }

        int assignInt = isO ? 2 : 1;

        boardMarks[location] = assignInt;

        boardImages[location].setImageResource(isO?R.drawable.ic_o:R.drawable.ic_x);

        isO = !isO;

        CheckVictory();

        //remove location for AI
        openLocations.remove(openLocations.indexOf(location));

        if(openLocations.size()==0) {
            gameActive = false;
            getSharedPreferences(getPackageName(), Context.MODE_PRIVATE).edit().remove(MainActivity.ACTIVE_ROOM).apply();
            new AlertDialog.Builder(this)
                    .setIcon(R.drawable.ic_return)
                    .setTitle("The game is tied.")
                    .setMessage("Would you like to return to the home page?")
                    .setPositiveButton("Yes", (dialogInterface, i1) -> startActivity(new Intent(this, MainActivity.class)))
                    .setNegativeButton("No", null)
                    .show();
            return;
        }

        turn++;

        if(myRef!=null){
            mGameObject.movesList.clear();
            for(int i : boardMarks){
                mGameObject.movesList.add(i);
            }

            mGameObject.isXTurn = !isO;

            mGameObject.isActive=gameActive;

            myRef.setValue(mGameObject);
        }
    }


}