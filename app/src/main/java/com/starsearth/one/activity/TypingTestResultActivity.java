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
import android.widget.RelativeLayout;
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
import com.starsearth.one.adapter.TypingTestResultAdapter;
import com.starsearth.one.domain.TypingTestResult;

import java.util.ArrayList;
import java.util.List;

public class TypingTestResultActivity extends AppCompatActivity {

    public static int MAX_NUMBER_IN_LIST = 1;

    private FirebaseAnalytics mFirebaseAnalytics;

    private ArrayList<TypingTestResult> list = new ArrayList<>();
    private DatabaseReference mDatabase;

    private Button btnStart;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private ChildEventListener childEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            TypingTestResult result = dataSnapshot.getValue(TypingTestResult.class);
            int index = indexToInsert(result);
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
                    TypingTestResult lastItem = list.get(list.size()-1);
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

    private boolean isTopResult(int words_correct) {
        if (list.size() < MAX_NUMBER_IN_LIST) {
            return true;
        }

        int lowestScore = list.get(list.size()-1).words_correct;
        if (words_correct > lowestScore) {
            return true;
        }

        return false;
    }

    /**
     *
     * @param result
     * @return index of list. -1 if need to insert at the end of the list
     */
    private int indexToInsert(TypingTestResult result) {
        if (list.isEmpty()) {
            return 0;
        }

        for (int i = 0; i < list.size(); i++) {
            TypingTestResult listItem = list.get(i);
            if (result.words_correct > listItem.words_correct) {
                return i;
            }
        }
        //If the score is smaller than all current scores, add it in the end
        return -1;
    }

    private void alertScore(int words_correct, boolean highScore) {
        if (highScore) {
            Toast.makeText(getApplicationContext(), R.string.high_score, Toast.LENGTH_LONG).show();
        }

    }

    private AlertDialog.Builder createAlertDialog() {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(TypingTestResultActivity.this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(TypingTestResultActivity.this);
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

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        mRecyclerView = (RecyclerView) findViewById(R.id.rv);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        mAdapter = new TypingTestResultAdapter(getApplicationContext(), list);
        mRecyclerView.setAdapter(mAdapter);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference("typing_game_results");
        mDatabase.keepSynced(true);
        Query query = mDatabase.orderByChild("userId").equalTo(currentUser.getUid());
        query.addChildEventListener(childEventListener);

        btnStart = (Button) findViewById(R.id.btn_start);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Button typing test start");
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                Intent intent = new Intent(TypingTestResultActivity.this, TypingTestActivity.class);
                startActivityForResult(intent, 0);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0 && resultCode == RESULT_CANCELED) {
            AlertDialog.Builder builder = createAlertDialog();
            builder.setMessage(R.string.typing_game_cancelled)
                    .show();
        }
        else if (requestCode == 0 && resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            if (bundle != null) {
                int wordsCorrect = bundle.getInt("words_correct");
                //This should not be in onChildAdded as it should only be shown once we return from completing a game
                if (isTopResult(wordsCorrect)) {
                    alertScore(wordsCorrect, true);
                }
                else {
                    alertScore(wordsCorrect, false);
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDatabase.removeEventListener(childEventListener);
    }
}
