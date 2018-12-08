package jp.co.mo.tictactoyonline;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String DATA_KEY_UESRS = "users";
    private static final String DATA_KEY_REQUEST = "request";
    private static final String DATA_KEY_PLAYING = "playing";
    private static final String DATA_KEY_CELLID = "cellId";

    private static final int MAX_ROW_NUMBER = 3;

    private static final int PLAYER_1_ID = 1;
    private static final int PLAYER_2_ID = PLAYER_1_ID + 1;
    private static final int NO_PLAYER = -1;

    private static final int BTN_ID_1 = 1;
    private static final int BTN_ID_2 = BTN_ID_1 + 1;
    private static final int BTN_ID_3 = BTN_ID_2 + +1;
    private static final int BTN_ID_4 = BTN_ID_3 + +1;
    private static final int BTN_ID_5 = BTN_ID_4 + +1;
    private static final int BTN_ID_6 = BTN_ID_5 + +1;
    private static final int BTN_ID_7 = BTN_ID_6 + +1;
    private static final int BTN_ID_8 = BTN_ID_7 + +1;
    private static final int BTN_ID_9 = BTN_ID_8 + +1;

    private int mActivePlayer = NO_PLAYER; // 1- for first, 2 for second
    private List<Integer> mPlayer1; // hold player 1 data
    private List<Integer> mPlayer2; // hold player 2 data
    private String mPlayerSession = "";


    private ProgressDialog mProgressDialog;

    @BindView(R.id.autoPlayStatusToggle)
    Switch mAutoPlayStatusToggle;
    @BindView(R.id.inviteUserNameText)
    EditText mInviteUserNameText;
    @BindView(R.id.inviteUserBtn)
    Button mInviteUserBtn;
    @BindView(R.id.acceptInvitationBtn)
    Button mAcceptInvitationBtn;
    @BindView(R.id.loginUserText)
    EditText mLoginUserText;
    @BindView(R.id.loginBtn)
    Button mloginBtn;
    @BindView(R.id.logoutBtn)
    Button mLogoutBtn;
    @BindView(R.id.registerBtn)
    Button mRegisterBtn;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference myRef = database.getReference();
    private String mUid;
    private String mUserEmail;

    private String mFirstPlayUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initElement();
        mAuth = FirebaseAuth.getInstance();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    mUserEmail = user.getEmail();
                    mUid = user.getUid();
                    if (!TextUtils.isEmpty(mUserEmail)) {

                        myRef.child(DATA_KEY_UESRS)
                                .child(beforeAt(mUserEmail))
                                .child(DATA_KEY_REQUEST)
                                .setValue(mUid)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "onAuthState Changed: sign_in");
                                        mLoginUserText.setText(mUserEmail);
                                        setElementStatus(true);
                                        checkInvitation();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG, "onAuthState Changed: sign_in failed");
                                        Log.d(TAG, e.getMessage());
                                        mLoginUserText.setText("");
                                        setElementStatus(false);
                                    }
                                });
                    }
                } else {
                    Log.d(TAG, "onAuthState Changed: sign_out");
                    mLoginUserText.setText("");
                    setElementStatus(false);
                }
            }
        };

    }

    private void initElement() {
        mActivePlayer = PLAYER_1_ID; // 1- for first, 2 for second
        mPlayer1 = new ArrayList<>();
        mPlayer2 = new ArrayList<>();
    }

    private void setElementStatus(boolean isLogin) {
        mLoginUserText.setEnabled(!isLogin);
        mRegisterBtn.setEnabled(!isLogin);
        mRegisterBtn.setVisibility(isLogin ? View.GONE : View.VISIBLE);
        mloginBtn.setEnabled(!isLogin);
        mloginBtn.setVisibility(isLogin ? View.GONE : View.VISIBLE);
        mLogoutBtn.setEnabled(isLogin);
        mLogoutBtn.setVisibility(isLogin ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthStateListener);
    }

    private String beforeAt(String email) {
        String[] str = email.split("@");
        return str[0];
    }

    private void createUserAuthentiation(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
//                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(getApplicationContext(), "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
//                            updateUI(null);
                        }

                        // ...
                    }
                });
    }

    private void signInUserAuthentication(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
//                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(getApplicationContext(), "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
//                            updateUI(null);
                        }

                        // ...
                    }
                });
    }

    private void signOut() {
        mAuth.signOut();
    }

    private void checkInvitation() {
        myRef.child(DATA_KEY_UESRS)
                .child(beforeAt(mUserEmail))
                .child(DATA_KEY_REQUEST)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        try {
                            Map<String, Object> data = (HashMap<String, Object>) dataSnapshot.getValue();
                            if (data != null) {
                                String value;
                                for (String key : data.keySet()) {
                                    if (!TextUtils.isEmpty(key)) {
                                        value = (String) data.get(key);
                                        mInviteUserNameText.setText(value);
                                        myRef.child(DATA_KEY_UESRS)
                                                .child(beforeAt(mUserEmail))
                                                .child(DATA_KEY_REQUEST)
                                                .setValue(mUid)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        invitationColor();
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {

                                                    }
                                                });
                                    }

                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void invitationColor() {
        mInviteUserNameText.setBackgroundColor(Color.RED);
    }

    @OnClick(R.id.inviteUserBtn)
    public void onClickInviteUserBtn(View view) {
        Toast.makeText(this, "Click Invite UserBtn", Toast.LENGTH_SHORT).show();
        final String inviteUser = mInviteUserNameText.getText().toString();

        if (!TextUtils.isEmpty(inviteUser)) {
            mInviteUserBtn.setEnabled(false);
            myRef.child(DATA_KEY_UESRS)
                    .child(beforeAt(beforeAt(inviteUser)))
                    .child(DATA_KEY_REQUEST)
                    .push()
                    .setValue(mUserEmail)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mInviteUserBtn.setEnabled(false);
                            startGame(beforeAt(inviteUser) + ":" + beforeAt(mUserEmail));
                            mFirstPlayUserName = beforeAt(mUserEmail);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            mInviteUserBtn.setEnabled(true);
                        }
                    });
        }
    }

    @OnClick(R.id.acceptInvitationBtn)
    public void onClickAcceptInvitationBtn(View view) {
        Toast.makeText(this, "Click Accept InvitationBtn", Toast.LENGTH_SHORT).show();
        final String inviteUser = mInviteUserNameText.getText().toString();
        if (!TextUtils.isEmpty(inviteUser)) {
            myRef.child(DATA_KEY_UESRS)
                    .child(beforeAt(beforeAt(inviteUser)))
                    .child(DATA_KEY_REQUEST)
                    .push()
                    .setValue(mUserEmail)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mInviteUserBtn.setEnabled(false);
                            startGame(beforeAt(mUserEmail) + ":" + beforeAt(inviteUser));
                            mFirstPlayUserName = beforeAt(inviteUser);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            mInviteUserBtn.setEnabled(true);
                        }
                    });
        }
    }

    private void startGame(String playGameId) {
        mPlayerSession = playGameId;
        myRef.child(DATA_KEY_PLAYING)
                .child(playGameId)
                .removeValue();

        myRef.child(DATA_KEY_PLAYING)
                .child(playGameId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        try {
                            mPlayer1.clear();
                            mPlayer2.clear();
                            mActivePlayer = PLAYER_2_ID;

                            Map<String, Object> data = (HashMap<String, Object>) dataSnapshot.getValue();
                            if (data != null) {

                                String value;
                                String firstPlayer = beforeAt(mUserEmail);

                                for (String key : data.keySet()) {
                                    if (!TextUtils.isEmpty(key)) {
                                        value = (String) data.get(key);
                                        if (!value.equals(firstPlayer)) {
                                            mActivePlayer = mUserEmail.equals(mFirstPlayUserName) ? PLAYER_1_ID : PLAYER_2_ID;
                                        } else {
                                            mActivePlayer = mUserEmail.equals(mFirstPlayUserName) ? PLAYER_2_ID : PLAYER_1_ID;
                                        }
                                        firstPlayer = value;
                                        String[] splicatId = key.split(":");
                                        autoPlay(Integer.parseInt(splicatId[1]));
                                    }

                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @OnClick(R.id.registerBtn)
    public void onClickRegisterBtn() {
        createUserAuthentiation(mLoginUserText.getText().toString(), "gaogaomango");
    }

    @OnClick(R.id.loginBtn)
    public void onClickLoginBtn(View view) {
        signInUserAuthentication(mLoginUserText.getText().toString(), "gaogaomango");
    }

    @OnClick(R.id.logoutBtn)
    public void onClickLogoutBtn(View view) {
        signOut();
    }

    public void btnClick(View view) {
        // game is not started;
        if (mPlayerSession.length() <= 0) {
            Log.e(TAG, "btnClick mPlayerSession.length() <= 0");
            return;
        }
        Button selectedBtn = (Button) view;
        int cellId = -1;
        switch (selectedBtn.getId()) {
            case R.id.btn1:
                cellId = BTN_ID_1;
                break;
            case R.id.btn2:
                cellId = BTN_ID_2;
                break;
            case R.id.btn3:
                cellId = BTN_ID_3;
                break;
            case R.id.btn4:
                cellId = BTN_ID_4;
                break;
            case R.id.btn5:
                cellId = BTN_ID_5;
                break;
            case R.id.btn6:
                cellId = BTN_ID_6;
                break;
            case R.id.btn7:
                cellId = BTN_ID_7;
                break;
            case R.id.btn8:
                cellId = BTN_ID_8;
                break;
            case R.id.btn9:
                cellId = BTN_ID_9;
                break;
        }
        myRef.child(DATA_KEY_PLAYING).child(mPlayerSession).child(DATA_KEY_CELLID + ":" + cellId).setValue(beforeAt(mUserEmail));
//        playGame(cellId, selectedBtn);
    }

    private void playGame(int cellId, Button selectedBtn) {
        Log.d(TAG, "cell id: " + String.valueOf(cellId));

        if (mActivePlayer == PLAYER_1_ID) {
            selectedBtn.setText("x");
            selectedBtn.setBackgroundColor(Color.GREEN);
            mPlayer1.add(cellId);
        } else if (mActivePlayer == PLAYER_2_ID) {
            selectedBtn.setText("0");
            selectedBtn.setBackgroundColor(Color.BLUE);
            mPlayer2.add(cellId);
        }
        selectedBtn.setEnabled(false);
        isFinishedGame();
    }

    private boolean isFinishedGame() {
        int winner = NO_PLAYER;
        winner = checkWinner(mPlayer1, PLAYER_1_ID);
        if (winner == NO_PLAYER) {
            winner = checkWinner(mPlayer2, PLAYER_2_ID);
        }

        if (winner == NO_PLAYER) {
            return false;
        }

        if (winner == PLAYER_1_ID) {
            showWinnerDialog("Player 1 is winner!");
        } else if (winner == PLAYER_2_ID) {
            showWinnerDialog("Player 2 is winner!");
        }
        return true;
    }

    private void showWinnerDialog(String message) {
        new AlertDialog.Builder(this)
                .setTitle("WINNER")
                .setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        refleshActivity();
                    }
                })
                .show();
    }

    public void restartClick(View view) {
        new AlertDialog.Builder(view.getContext())
                .setTitle("ALERT")
                .setMessage("Do you want to clear this game?")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        refleshActivity();
                    }
                })
                .setNegativeButton("CANCLE", null)
                .show();
    }

    private void refleshActivity() {
        finish();
        startActivity(getIntent());
    }

    private int checkWinner(List<Integer> player, int playerId) {
        int winnerId = NO_PLAYER;
        for (int i = 1; i <= MAX_ROW_NUMBER; i++) {
            if (player.contains(i) && player.contains(i + 1) && player.contains(i + 2)) {
                winnerId = playerId;
                break;
            } else if (player.contains(i) && player.contains(i + MAX_ROW_NUMBER) && player.contains(i + MAX_ROW_NUMBER * 2)) {
                winnerId = playerId;
                break;
            }
        }
        if ((player.contains(1) && player.contains(5) && player.contains(9))
                || (player.contains(3) && player.contains(5) && player.contains(7))) {
            winnerId = playerId;
        }
        return winnerId;
    }

    private void autoPlay(int cellId) {
        Log.e(TAG, "autoPlay cellId: " + String.valueOf(cellId));
        Button btnSelected = null;
        switch (cellId) {
            case BTN_ID_1:
                btnSelected = findViewById(R.id.btn1);
                break;
            case BTN_ID_2:
                btnSelected = findViewById(R.id.btn2);
                break;
            case BTN_ID_3:
                btnSelected = findViewById(R.id.btn3);
                break;
            case BTN_ID_4:
                btnSelected = findViewById(R.id.btn4);
                break;
            case BTN_ID_5:
                btnSelected = findViewById(R.id.btn5);
                break;
            case BTN_ID_6:
                btnSelected = findViewById(R.id.btn6);
                break;
            case BTN_ID_7:
                btnSelected = findViewById(R.id.btn7);
                break;
            case BTN_ID_8:
                btnSelected = findViewById(R.id.btn8);
                break;
            case BTN_ID_9:
                btnSelected = findViewById(R.id.btn9);
                break;
            default:
                btnSelected = findViewById(R.id.btn1);
                break;
        }

        playGame(cellId, btnSelected);
    }

}