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
import android.widget.LinearLayout;
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
import java.util.List;

public class TopicsListActivity extends ItemListAdminActivity {

    private Lesson parent;
    private ArrayList<Topic> itemList;
    private TopicsAdapter adapter;

    private ValueEventListener parentListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            parent = null;
            parent = dataSnapshot.getValue(Lesson.class);

            if (parent != null) {
                tvParentLine1.setText(parent.getTitle());
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
            parent.addTopic(topicKey);
            mParentDatabase.setValue(parent);

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
            parent.removeTopic(topicKey);
            mParentDatabase.setValue(parent);

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

    private void deleteItem(final Topic topic) {
        new AlertDialog.Builder(TopicsListActivity.this)
                .setTitle(R.string.delete_topic)
                .setMessage(R.string.delete_topic_confirm_message)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mDatabase.child(topic.getUid()).removeValue();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_topics_list);
        setTitle(R.string.lesson_details);
        tvListViewHeader.setText(R.string.topics);
        btnAddItem.setText(R.string.add_topic);
        REFERENCE_PARENT = "/lessons/";
        REFERENCE = "topics";

        itemList = new ArrayList<>();

        Bundle bundle = getIntent().getExtras();
        parent = bundle.getParcelable("parent");

        if (parent != null) {
            mParentDatabase = FirebaseDatabase.getInstance().getReference(REFERENCE_PARENT + parent.getUid());
            mParentDatabase.addValueEventListener(parentListener);

            tvParentLine1.setText(parent.getTitle());
            llParent.setVisibility(View.VISIBLE);
        }
        else {
            llParent.setVisibility(View.GONE);
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
        btnAddItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TopicsListActivity.this, AddEditTopicActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt("totalItems", itemList.size());
                bundle.putString("parentId", parent.getUid());
                intent.putExtras(bundle);
                startActivityForResult(intent, -1);

            }
        });
        listView.setEmptyView(btnAddItem);

        mDatabase = FirebaseDatabase.getInstance().getReference(REFERENCE);
        Query query = mDatabase.child(REFERENCE);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<Topic> topicList = new ArrayList<>();
                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    Topic topic = snapshot.getValue(Topic.class);
                    topicList.add(topic);
                }
                adapter = new TopicsAdapter(getApplicationContext(), 0, topicList);
                listView.setAdapter(adapter);
                listView.setTag(topicList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        mDatabase.addChildEventListener(listener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mParentDatabase.removeEventListener(parentListener);
        mDatabase.removeEventListener(listener);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int index = info.position;

        Intent intent;
        Bundle bundle;

        switch (item.getItemId()) {
            case 0:
                intent = new Intent(this, AddEditTopicActivity.class);
                bundle = new Bundle();
                bundle.putParcelable("topic", itemList.get(index));
                intent.putExtras(bundle);
                startActivityForResult(intent, index);
                break;
            case 1:
                deleteItem(itemList.get(index));
                break;
            default: break;
        }

        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.getItem(0).setTitle(R.string.edit_lesson);
        menu.getItem(1).setTitle(R.string.delete_lesson);
        menu.getItem(2).setTitle(R.string.add_topic);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        Bundle bundle;

        switch (item.getItemId()) {
            case R.id.edit_parent:
                intent = new Intent(TopicsListActivity.this, AddEditLessonActivity.class);
                bundle = new Bundle();
                bundle.putParcelable("lesson", parent);
                intent.putExtras(bundle);
                startActivityForResult(intent, 100);
                return true;
            case R.id.delete_parent:
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
            case R.id.add_item:
                intent = new Intent(TopicsListActivity.this, AddEditTopicActivity.class);
                bundle = new Bundle();
                bundle.putInt("totalItems", itemList.size());
                bundle.putString("parentId", parent.getUid());
                intent.putExtras(bundle);
                startActivityForResult(intent, -1);
                return true;
            default: break;
        }


        return super.onOptionsItemSelected(item);
    }
}
