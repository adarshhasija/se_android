package com.starsearth.one.activity.lists;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
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
import com.starsearth.one.activity.forms.AddEditExerciseActivity;
import com.starsearth.one.activity.forms.AddEditLessonActivity;
import com.starsearth.one.activity.forms.AddEditTopicActivity;
import com.starsearth.one.adapter.ExercisesAdapter;
import com.starsearth.one.database.Firebase;
import com.starsearth.one.domain.Exercise;
import com.starsearth.one.domain.SENestedObject;
import com.starsearth.one.domain.Topic;

import java.util.ArrayList;
import java.util.Map;

public class ExercisesListActivity extends ItemListActivity {

    private Topic parent;
    private ArrayList<Exercise> itemList;
    private ExercisesAdapter adapter;

    private ValueEventListener parentListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            parent = null;
            parent = dataSnapshot.getValue(Topic.class);

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
            Exercise newExercise = dataSnapshot.getValue(Exercise.class);
            String exerciseKey = dataSnapshot.getKey();
            //SENestedObject nestedObject = new SENestedObject(exerciseKey, "exercises");
            //parent.addExercise(nestedObject);
            //mParentDatabase.setValue(parent);
            addItemReferenceToParent(exerciseKey);

            if (adapter != null) {
                adapter.add(newExercise);
                adapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            Exercise newExercise = dataSnapshot.getValue(Exercise.class);
            String exerciseKey = dataSnapshot.getKey();
            //Map<String, SENestedObject> questions = newExercise.questions;
            //parent.exercises.get(exerciseKey).children = questions;
            //mParentDatabase.setValue(parent);
            updateItemChildInParent(newExercise);

            if (adapter != null) {
                ArrayList<Exercise> list = adapter.getExerciseList();
                for (int i = 0; i < list.size(); i++) {
                    Exercise exercise = list.get(i);
                    if (exercise.getUid().equals(exerciseKey)) {
                        adapter.remove(exercise);
                        adapter.insert(newExercise, i);
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            Exercise removedExercise = dataSnapshot.getValue(Exercise.class);
            String exerciseKey = dataSnapshot.getKey();
            //parent.removeExercise(exerciseKey);
            //mParentDatabase.setValue(parent);
            removeItemFromParent(exerciseKey);

            if (adapter != null) {
                ArrayList<Exercise> list = adapter.getExerciseList();
                for (int i = 0; i < list.size(); i++) {
                    Exercise exercise = list.get(i);
                    if (exercise.getUid().equals(exerciseKey)) {
                        adapter.remove(exercise);
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

    private void addItemReferenceToParent(String exerciseKey) {
        SENestedObject nestedObject = new SENestedObject(exerciseKey, "exercises");
        //parent.addExercise(nestedObject);
        mParentDatabase.setValue(parent);
    }

    private void updateItemChildInParent(Exercise newExercise) {
        String exerciseKey = newExercise.getUid();
        Map<String, SENestedObject> questions = newExercise.questions;
        //parent.exercises.get(exerciseKey).children = questions;
        mParentDatabase.setValue(parent);
    }

    private void removeItemFromParent(String exerciseKey) {
        //parent.removeExercise(exerciseKey);
        mParentDatabase.setValue(parent);
    }

    private void deleteItem(final Exercise deleteExercise) {
        new AlertDialog.Builder(ExercisesListActivity.this)
                .setTitle(R.string.delete_exercise)
                .setMessage(R.string.delete_exercise_confirm_message)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Firebase firebase = new Firebase(REFERENCE);
                        firebase.removeExercise(deleteExercise);
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
        //setContentView(R.layout.activity_exercises_list);
        tvListViewHeader.setText(R.string.exercises);
        btnAddItem.setText(R.string.add_exercise);
        REFERENCE_PARENT = "/topics/";
        REFERENCE = "exercises";

        itemList = new ArrayList<>();
        adapter = new ExercisesAdapter(getApplicationContext(), 0, itemList);
        listView.setAdapter(adapter);

        Bundle bundle = getIntent().getExtras();
        parent = bundle.getParcelable("parent");
        boolean parentPresent = false;
        if (parent != null) {
            parentPresent = true;
            mParentDatabase = FirebaseDatabase.getInstance().getReference(REFERENCE_PARENT + parent.getUid());
            mParentDatabase.addValueEventListener(parentListener);

            setTitle(parent.getTitle());
            tvParentLine1.setText(parent.getTitle());
        }

        if (admin && parentPresent) {
            llParent.setVisibility(View.VISIBLE);
        }
        else {
            llParent.setVisibility(View.GONE);
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Exercise exercise = adapter.getItem(position);

                sendAnalytics(exercise.title);

                Bundle bundle = new Bundle();
                bundle.putParcelable("parent", exercise);
                bundle.putBoolean("admin", admin);
                Intent intent = new Intent(ExercisesListActivity.this, QuestionsListActivity.class);
                intent.putExtras(bundle);
                startActivityForResult(intent, position);
            }
        });

        btnAddItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ExercisesListActivity.this, AddEditExerciseActivity.class);
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
                intent = new Intent(this, AddEditExerciseActivity.class);
                bundle = new Bundle();
                bundle.putParcelable("exercise", itemList.get(index));
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
            menu.getItem(0).setTitle(R.string.edit_topic);
            menu.getItem(1).setTitle(R.string.delete_topic);
            menu.getItem(2).setTitle(R.string.add_exercise);
        }


        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        Bundle bundle;

        switch (item.getItemId()) {
            case R.id.edit_parent:
                intent = new Intent(ExercisesListActivity.this, AddEditTopicActivity.class);
                bundle = new Bundle();
                bundle.putParcelable("topic", parent);
                intent.putExtras(bundle);
                startActivityForResult(intent, 100);
                return true;
            case R.id.delete_parent:
                new AlertDialog.Builder(ExercisesListActivity.this)
                        .setTitle(R.string.delete_topic)
                        .setMessage(R.string.delete_topic_confirm_message)
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
                intent = new Intent(ExercisesListActivity.this, AddEditExerciseActivity.class);
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
