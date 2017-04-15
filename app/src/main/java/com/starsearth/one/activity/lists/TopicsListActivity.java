package com.starsearth.one.activity.lists;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.starsearth.one.R;
import com.starsearth.one.activity.TopicActivity;
import com.starsearth.one.activity.forms.AddEditLessonActivity;
import com.starsearth.one.activity.forms.AddEditTopicActivity;
import com.starsearth.one.adapter.TopicsAdapter;
import com.starsearth.one.database.Firebase;
import com.starsearth.one.domain.Lesson;
import com.starsearth.one.domain.SENestedObject;
import com.starsearth.one.domain.Topic;

import java.util.ArrayList;
import java.util.Map;

public class TopicsListActivity extends ItemListActivity {

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
            addItemReferenceToParent(topicKey);

            if (adapter != null) {
                adapter.add(newTopic);
                adapter.notifyDataSetChanged();
            }

        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            Topic newTopic = dataSnapshot.getValue(Topic.class);
            String topicKey = dataSnapshot.getKey();
            updateItemChildInParent(newTopic);

            if (adapter != null) {
                ArrayList<Topic> list = adapter.getTopicList();
                for (int i = 0; i < list.size(); i++) {
                    Topic topic = list.get(i);
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
            removeItemFromParent(topicKey);

            if (adapter != null) {
                ArrayList<Topic> list = adapter.getTopicList();
                for (int i = 0; i < list.size(); i++) {
                    Topic topic = list.get(i);
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

    private void addItemReferenceToParent(String topicKey) {
        SENestedObject nestedObject = new SENestedObject(topicKey, "topics");
        parent.addTopic(nestedObject);
        mParentDatabase.setValue(parent);
    }

    private void updateItemChildInParent(Topic newTopic) {
        String topicKey = newTopic.getUid();
        Map<String, SENestedObject> exercises = newTopic.exercises;
        parent.topics.get(topicKey).children = exercises;
        mParentDatabase.setValue(parent);
    }

    private void removeItemFromParent(String topicKey) {
        parent.removeTopic(topicKey);
        mParentDatabase.setValue(parent);
    }

    private void deleteItem(final Topic deleteTopic) {
        new AlertDialog.Builder(TopicsListActivity.this)
                .setTitle(R.string.delete_topic)
                .setMessage(R.string.delete_topic_confirm_message)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Firebase firebase = new Firebase(REFERENCE);
                        firebase.removeTopic(deleteTopic);
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
        adapter = new TopicsAdapter(getApplicationContext(), 0, itemList);
        listView.setAdapter(adapter);

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
                Topic topic = adapter.getItem(position);
                Intent intent;
                Bundle bundle = new Bundle();
                bundle.putBoolean("admin", admin);
                if (admin) {
                    bundle.putParcelable("parent", topic);
                    intent = new Intent(TopicsListActivity.this, ExercisesListActivity.class);
                    intent.putExtras(bundle);
                    startActivityForResult(intent, position);
                }
                else {
                    bundle.putParcelable("topic", topic);
                    intent = new Intent(TopicsListActivity.this, TopicActivity.class);
                    intent.putExtras(bundle);
                    startActivityForResult(intent, position);
                }

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
        //listView.setEmptyView(btnAddItem);

        mDatabase = FirebaseDatabase.getInstance().getReference(REFERENCE);
        query = mDatabase.orderByChild("parentId").equalTo(parent.getUid());
        query.addChildEventListener(listener);
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
        if (menu.size() > 0) {
            menu.getItem(0).setTitle(R.string.edit_lesson);
            menu.getItem(1).setTitle(R.string.delete_lesson);
            menu.getItem(2).setTitle(R.string.add_topic);
        }


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
