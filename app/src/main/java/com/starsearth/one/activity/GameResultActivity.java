package com.starsearth.one.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.starsearth.one.R;
import com.starsearth.one.Utils;
import com.starsearth.one.adapter.ResultAdapter;
import com.starsearth.one.domain.TypingGame;
import com.starsearth.one.domain.Result;

import java.util.ArrayList;

public class GameResultActivity extends AppCompatActivity {

    public static int MAX_NUMBER_IN_LIST = 1;

    private FirebaseAnalytics mFirebaseAnalytics;

    private ArrayList<Result> list = new ArrayList<>();
    private DatabaseReference mDatabase;

    private Button btnStart;
    private TextView tvInstruction;
    private RecyclerView mRecyclerView;
    private ResultAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private ArrayList<String> content = new ArrayList<>();

    private ChildEventListener childEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Result result = dataSnapshot.getValue(Result.class);

            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                //if it is not the same game type, return
                if (extras.getInt("game_id") != result.game_id) {
                    return;
                }
            }

          /*  if (mAdapter != null && list != null) {
                if (!list.isEmpty()) {
                    Result highScore = list.get(0);
                    if (isTopResult(result.words_correct, result.words_total_finished)) {
                        highScore = list.get(0);
                        mDatabase.child(highScore.uid).removeValue();
                        list.remove(highScore);
                        list.add(0, result); //this is the new highscore
                    }
                    if (list.size() > 1) {
                        //last tried row exists
                        //remove last tried
                        //replace with new value
                        Result lastItem = list.get(list.size()-1);
                        list.remove(lastItem);
                        if (!highScore.uid.equals(lastItem.uid)) {
                            //delete it from the cloud ONLY if it is not the highscore
                            mDatabase.child(lastItem.uid).removeValue();
                        }

                    }
                    list.add(result);

                } else {
                    //if list is empty, add it twice
                    //once as highscore
                    //once as last attempt
                    list.add(result);
                    list.add(result);
                }
                mAdapter.notifyDataSetChanged();
            }   */

            int index = 0; //indexToInsert(result);
            if (mAdapter != null && list != null) {
                if (index == -1)  {
                    //if -1, insert at the end of the list
                    list.add(result);
                }
                else {
                    list.add(index,result);
                }

                if (list.size() > MAX_NUMBER_IN_LIST) {
                    //If the list is now more than MAX_NUMBER_IN_LIST items, remove the lowest item
                    Result lastItem = list.get(list.size()-1);
                    mDatabase.child(lastItem.uid).removeValue(); //delete from the database
                    list.remove(lastItem);
                }
                //mAdapter.notifyItemChanged(index);
                mAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private boolean isTopResult(int correct, int totalFinished) {
        //if (list.size() < MAX_NUMBER_IN_LIST) {
        //    return true;
        //}
        boolean result = false;
        if (!list.isEmpty()) {
            Result highScore = list.get(0);
            if (correct > highScore.words_correct
                    //&& mAdapter.getAccuracy(correct, totalFinished) > mAdapter.getAccuracy(highScore.words_correct, highScore.words_total_finished)
                    ) {
                result = true;
            }
        }
        else {
            result = true;
        }

        return result;
    }

    /**
     *
     * @param result
     * @return index of list. -1 if need to insert at the end of the list
     */
    private int indexToInsert(Result result) {
        if (list.isEmpty()) {
            return 0;
        }

        for (int i = 0; i < list.size(); i++) {
            Result listItem = list.get(i);
            if (result.words_correct > listItem.words_correct) {
                return i;
            }
        }
        //If the score is smaller than all current scores, add it in the end
        return -1;
    }

    private void alertScore(int words_correct, int words_total_finished, boolean highScore) {
      /*  if (highScore) {
            Toast.makeText(getApplicationContext(), getString(R.string.high_score) + " " + words_correct, Toast.LENGTH_LONG).show();
        }
        else {
            Toast.makeText(getApplicationContext(), getString(R.string.your_score) + " " + words_correct, Toast.LENGTH_LONG).show();
        }   */
        Toast.makeText(getApplicationContext(), words_correct + " " + getString(R.string.words_per_minute) + " " + getString(R.string.accuracy) + " " + mAdapter.getAccuracy(words_correct, words_total_finished) + "%", Toast.LENGTH_LONG).show();

    }

    private void setInstructionTextAndContent() {
        Bundle extras = getIntent().getExtras();
        String levelString;
        int game_id=0;
        if (extras != null) {
            levelString = extras.getString("levelString");
            game_id = extras.getInt("game_id");
        }
        else {
            return;
        }
        TypingGame game = new TypingGame();
        TypingGame.Id id = TypingGame.Id.fromInt(game_id);
        switch (id) {
            case ONE_WORD:
                content.add(game.words.get(0));
                tvInstruction.setText(String.format(getString(R.string.typing_game_instructions_1_word), content.get(0)));
                break;
            case MANY_WORDS:
                for (String word : game.words) {
                    content.add(word);
                }
                tvInstruction.setText(getString(R.string.typing_game_instructions_many_words));
                break;
            case ONE_SENTENCE:
                content.add(game.sentences.get(0));
                tvInstruction.setText(String.format(getString(R.string.typing_game_instructions_1_sentence), content.get(0)));
                break;
            case MANY_SENTENCES:
                for (String sentence : game.sentences) {
                    content.add(sentence);
                }
                tvInstruction.setText(getString(R.string.typing_game_instructions_many_sentences));
                break;
            case LETTERS_LOWER_CASE:
                tvInstruction.setText(getString(R.string.typing_game_instructions_letters_small));
                break;
            case LETTERS_UPPER_CASE:
                tvInstruction.setText(getString(R.string.typing_game_instructions_letters_capital));
                break;
            default:
                break;

        }
    }

    private AlertDialog.Builder createAlertDialog() {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(GameResultActivity.this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(GameResultActivity.this);
        }

        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        return builder;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_typing_test_result);

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            setTitle(Utils.formatStringFirstLetterCapital(extras.getString("subject")) + " - " + extras.getString("levelString"));
        }

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        mRecyclerView = (RecyclerView) findViewById(R.id.rv);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        mAdapter = new ResultAdapter(getApplicationContext(), list);
        mRecyclerView.setAdapter(mAdapter);

        tvInstruction = (TextView) findViewById(R.id.tv_instruction);
        setInstructionTextAndContent();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        mDatabase = FirebaseDatabase.getInstance().getReference("results");
        mDatabase.keepSynced(true);
        Query query = mDatabase.orderByChild("userId").equalTo(currentUser.getUid());
        query.addChildEventListener(childEventListener);

        btnStart = (Button) findViewById(R.id.btn_start);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                String subject = extras.getString("subject");
                String levelString = extras.getString("levelString");
                int id = extras.getInt("game_id");
                bundle.putInt(FirebaseAnalytics.Param.ITEM_ID, id);
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, subject + " " + levelString);
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "button start game");
                //bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Button start game: " + subject + " " + levelString);
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                Intent intent = new Intent(GameResultActivity.this, GameActivity.class);
                bundle.putStringArrayList("content", content);
                Bundle extras = getIntent().getExtras();
                if (extras != null) {
                    bundle.putString("subject", extras.getString("subject"));
                    bundle.putString("levelString", extras.getString("levelString"));
                    bundle.putInt("game_id", extras.getInt("game_id"));
                    bundle.putInt("level", extras.getInt("level"));
                }
                intent.putExtras(bundle);
                startActivityForResult(intent, 0);
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0 && resultCode == RESULT_CANCELED) {
            Toast.makeText(getApplicationContext(), R.string.typing_game_cancelled, Toast.LENGTH_LONG).show();
        }
        else if (requestCode == 0 && resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            if (bundle != null) {
                int wordsCorrect = bundle.getInt("words_correct");
                int wordsTotalFinished = bundle.getInt("words_total_finished");
                //This should not be in onChildAdded as it should only be shown once we return from completing a game
              /*  if (isTopResult(wordsCorrect, wordsTotalFinished)) {
                    alertScore(wordsCorrect, wordsTotalFinished, true);
                }
                else {
                    alertScore(wordsCorrect, wordsTotalFinished, false);
                }   */
              alertScore(wordsCorrect, wordsTotalFinished, true);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDatabase.removeEventListener(childEventListener);
    }
}
