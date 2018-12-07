package jp.co.mo.tictactoyonline;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

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

    private int activePlayer = NO_PLAYER; // 1- for first, 2 for second
    private List<Integer> player1; // hold player 1 data
    private List<Integer> player2; // hold player 2 data

    private Switch autoPlayStatusToggle;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_main);
        initElement();
    }

    private void initElement() {
        activePlayer = PLAYER_1_ID; // 1- for first, 2 for second
        player1 = new ArrayList<>();
        player2 = new ArrayList<>();
        autoPlayStatusToggle = findViewById(R.id.autoPlayStatusToggle);
    }

    public void btnClick(View view) {
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
        playGame(cellId, selectedBtn);
    }

    private void playGame(int cellId, Button selectedBtn) {
        Log.d(TAG, "cell id: " + String.valueOf(cellId));

        if (activePlayer == PLAYER_1_ID) {
            selectedBtn.setText("x");
            selectedBtn.setBackgroundColor(Color.GREEN);
            player1.add(cellId);
            activePlayer = PLAYER_2_ID;
        } else if (activePlayer == PLAYER_2_ID) {
            selectedBtn.setText("0");
            selectedBtn.setBackgroundColor(Color.BLUE);
            player2.add(cellId);
            activePlayer = PLAYER_1_ID;
        }
        selectedBtn.setEnabled(false);
        if (!isFinishedGame()) {
            // auto playをさせるのはplayer2だけ
            // auto play modeの場合はちょっとまってからプレイさせる
            if (activePlayer == PLAYER_2_ID && autoPlayStatusToggle != null && autoPlayStatusToggle.isChecked()) {
                ProgressDialog progressDialog = new ProgressDialog(this);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.setMessage("オートプレイを実行しています");
                progressDialog.setCancelable(true);
                progressDialog.show();

                new AutoPlay(this, progressDialog).execute();
            }
        }
    }

    private boolean isFinishedGame() {
        int winner = NO_PLAYER;
        winner = checkWinner(player1, PLAYER_1_ID);
        if (winner == NO_PLAYER) {
            winner = checkWinner(player2, PLAYER_2_ID);
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

    private void autoPlay() {
        // auto play modeの場合はちょっとまってからプレイさせる
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        List<Integer> emptyCells = new ArrayList<>(); // all unselected cell

        // Find empty cells;
        for (int cellId = 1; cellId < 10; cellId++) {
            if (!(player1.contains(cellId) || player2.contains(cellId))) {
                emptyCells.add(cellId);
            }
        }

        Random r = new Random();
        int randomIndex = r.nextInt(emptyCells.size() - 0) + 0; // if size =3, select (0,1,2)
        int cellId = emptyCells.get(randomIndex);
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

    private static class AutoPlay extends AsyncTask<Void, Void, String> {
        private WeakReference<MainActivity> activityReference;
        private ProgressDialog mProgressDialog;

        public AutoPlay(MainActivity context, ProgressDialog progressDialog) {
            this.activityReference = new WeakReference<>(context);
            this.mProgressDialog = progressDialog;
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                // just wait for auto play
                Thread.sleep(1000);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            activityReference.get().autoPlay();
            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
                mProgressDialog = null;
            }
        }
    }

}