package com.starsearth.one.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.starsearth.one.R;
import com.starsearth.one.adapter.LessonsAdapter;
import com.starsearth.one.adapter.TopicsAdapter;
import com.starsearth.one.domain.Course;
import com.starsearth.one.domain.Lesson;
import com.starsearth.one.domain.Topic;

import java.util.ArrayList;

public class TopicsListActivity extends AppCompatActivity {

    private String REFERENCE_PARENT = "/lessons/";
    private String REFERENCE = "topics";

    private Lesson lesson;
    private ArrayList<Topic> topicList;
    private DatabaseReference mParentDatabase;
    private DatabaseReference mDatabase; //Lessons list DB
    private TopicsAdapter adapter;

    //UI
    private TextView tvParentName;
    private ListView listView;
    private Button btnAddTopic;

    private ValueEventListener parentListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            lesson = null;
            lesson = dataSnapshot.getValue(Lesson.class);
            String lessonKey = dataSnapshot.getKey();

            if (lesson != null) {
                tvParentName.setText(lesson.getName());
            }
            else {
                //This means the parent was deleted from somewhere else
                //close activity
                finish();
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Toast.makeText(getApplicationContext(),databaseError.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    };

    private ChildEventListener listener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Topic newTopic = dataSnapshot.getValue(Topic.class);
            String topicKey = dataSnapshot.getKey();
            lesson.addTopic(topicKey);
            mParentDatabase.setValue(lesson);

            if (adapter != null) {
                adapter.add(newTopic);
                adapter.notifyDataSetChanged();
            }

        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            Topic newTopic = dataSnapshot.getValue(Topic.class);
            String topicKey = dataSnapshot.getKey();

            if (adapter != null) {
                ArrayList<Topic> list = adapter.getTopicList();
                for (int i = 0; i < list.size(); i++) {
                    Topic topic = list.get(0);
                    if (topic.getUid().equals(topicKey)) {
                        adapter.remove(topic);
                        adapter.insert(newTopic, i);
                        adapter.notifyDataSetChanged();
                    }
                }
            }

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            Topic removedTopic = dataSnapshot.getValue(Topic.class);
            String topicKey = dataSnapshot.getKey();
            lesson.removeTopic(topicKey);
            mParentDatabase.setValue(lesson);

            if (adapter != null) {
                ArrayList<Topic> list = adapter.getTopicList();
                for (int i = 0; i < list.size(); i++) {
                    Topic topic = list.get(0);
                    if (topic.getUid().equals(topicKey)) {
                        adapter.remove(topic);
                        adapter.notifyDataSetChanged();
                    }
                }
            }

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topics_list);

        topicList = new ArrayList<>();

        setTitle(R.string.lesson_details);
        Bundle bundle = getIntent().getExtras();
        lesson = bundle.getParcelable("lesson");

        tvParentName = (TextView) findViewById(R.id.tv_parent_name);
        tvParentName.setText(lesson.getName());
        listView = (ListView) findViewById(R.id.listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
        btnAddTopic = (Button) findViewById(R.id.btn_add_topic);
        btnAddTopic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TopicsListActivity.this, AddEditTopicActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt("totalItems", topicList.size());
                bundle.putString("parentId", lesson.getUid());
                intent.putExtras(bundle);
                startActivityForResult(intent, -1);
            }
        });
        listView.setEmptyView(btnAddTopic);

        mParentDatabase = FirebaseDatabase.getInstance().getReference(REFERENCE_PARENT + lesson.getUid());

        mDatabase = FirebaseDatabase.getInstance().getReference(REFERENCE);
        Query query = mDatabase.child(REFERENCE);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    Topic topic = snapshot.getValue(Topic.class);
                    topicList.add(topic);
                }
                adapter = new TopicsAdapter(getApplicationContext(), 0, topicList);
                listView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mParentDatabase.addValueEventListener(parentListener);
        mDatabase.addChildEventListener(listener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mParentDatabase.removeEventListener(parentListener);
        mDatabase.removeEventListener(listener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_topics_list, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        Bundle bundle;

        switch (item.getItemId()) {
            case R.id.edit_lesson:
                intent = new Intent(TopicsListActivity.this, AddEditLessonActivity.class);
                bundle = new Bundle();
                bundle.putParcelable("lesson", lesson);
                intent.putExtras(bundle);
                startActivityForResult(intent, 100);
                return true;
            case R.id.delete_lesson:
                new AlertDialog.Builder(TopicsListActivity.this)
                        .setTitle(R.string.delete_lesson)
                        .setMessage(R.string.delete_lesson_confirm_message)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                mParentDatabase.removeEventListener(parentListener);
                                mParentDatabase.removeValue();
                                finish();
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                return true;
            case R.id.add_topic:
                intent = new Intent(TopicsListActivity.this, AddEditTopicActivity.class);
                bundle = new Bundle();
                bundle.putInt("totalItems", topicList.size());
                bundle.putString("parentId", lesson.getUid());
                intent.putExtras(bundle);
                startActivityForResult(intent, -1);
                return true;
            default: break;
        }


        return super.onOptionsItemSelected(item);
    }
}
