package com.starsearth.one.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

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
            if (isInTopTen(result)) {
                int index = indexToInsert(result);
                if (mAdapter != null && list != null) {
                    if (index == -1)  {
                        //if -1, insert at the end of the list
                        list.add(result);
                    }
                    else {
                        list.add(index,result);
                    }

                    if (list.size() > 10) {
                        //If the list is now more than 10 items, remove the lowest item
                        TypingTestResult lastItem = list.get(list.size()-1);
                        list.remove(lastItem);
                    }
                    if (index == -1) mAdapter.notifyItemInserted(list.size() - 1);
                    else mAdapter.notifyItemInserted(index);
                }
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

    private boolean isInTopTen(TypingTestResult result) {
        if (list.size() < 10) {
            return true;
        }

        int lowesstScore = list.get(list.size()-1).words_correct;
        if (result.words_correct > lowesstScore) {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_typing_test_result);

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
                Intent intent = new Intent(TypingTestResultActivity.this, TypingTestActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDatabase.removeEventListener(childEventListener);
    }
}
