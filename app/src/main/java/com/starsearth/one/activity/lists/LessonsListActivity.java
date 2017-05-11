package com.starsearth.one.activity.lists;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.view.KeyEvent;
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
import com.starsearth.one.activity.MainSEActivity;
import com.starsearth.one.activity.domaindetail.CourseDetailActivity;
import com.starsearth.one.activity.forms.AddEditCourseActivity;
import com.starsearth.one.activity.forms.AddEditLessonActivity;
import com.starsearth.one.adapter.LessonsAdapter;
import com.starsearth.one.database.Firebase;
import com.starsearth.one.domain.Course;
import com.starsearth.one.domain.Lesson;
import com.starsearth.one.domain.SENestedObject;

import java.util.ArrayList;
import java.util.Map;

public class LessonsListActivity extends ItemListActivity {

    private Course parent;
    private ArrayList<Lesson> itemList;
    private LessonsAdapter adapter;

    private ValueEventListener parentListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            parent = null;
            parent = dataSnapshot.getValue(Course.class);

            if (parent != null) {
                tvParentLine1.setText(parent.getTitle());
                tvParentLine2.setText(parent.getDescription());
                tvParentLine2.setVisibility(View.VISIBLE);
            }
            else {
                //this means the parent was deleted from somewhere else
                //close the activity
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
            Lesson newLesson = dataSnapshot.getValue(Lesson.class);
            String lessonKey = dataSnapshot.getKey();
            addItemReferenceToParent(lessonKey);

            if (adapter != null) {
                adapter.add(newLesson);
                adapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            Lesson newLesson = dataSnapshot.getValue(Lesson.class);
            String lessonKey = dataSnapshot.getKey();
            updateItemChildInParent(newLesson);

            if (adapter != null) {
                ArrayList<Lesson> list = adapter.getLessonList();
                for (int i = 0; i < list.size(); i++) {
                    Lesson lesson = list.get(i);
                    if (lesson.getUid().equals(lessonKey)) {
                        adapter.remove(lesson);
                        adapter.insert(newLesson, i);
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            Lesson removedLesson = dataSnapshot.getValue(Lesson.class);
            String lessonKey = dataSnapshot.getKey();
            removeItemFromParent(lessonKey);

            if (adapter != null) {
                ArrayList<Lesson> list = adapter.getLessonList();
                for (int i = 0; i < list.size(); i++) {
                    Lesson lesson = list.get(i);
                    if (lesson.getUid().equals(lessonKey)) {
                        adapter.remove(lesson);
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

    private void addItemReferenceToParent(String lessonKey) {
        parent.addLesson(new SENestedObject(lessonKey, "lessons"));
        mParentDatabase.setValue(parent);
    }

    private void updateItemChildInParent(Lesson newLesson) {
        String lessonKey = newLesson.getUid();
        Map<String, SENestedObject> topics = newLesson.topics;
        parent.lessons.get(lessonKey).children = topics;
        mParentDatabase.setValue(parent);
    }

    private void removeItemFromParent(String lessonKey) {
        parent.removeLesson(lessonKey);
        mParentDatabase.setValue(parent);
    }

    private void deleteItem(final Lesson deleteLesson) {
        new AlertDialog.Builder(LessonsListActivity.this)
                .setTitle(R.string.delete_lesson)
                .setMessage(R.string.delete_lesson_confirm_message)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Firebase firebase = new Firebase(REFERENCE);
                        firebase.removeLesson(deleteLesson);
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

    private void setupParentDetailView() {
        Intent intent = new Intent(LessonsListActivity.this, CourseDetailActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable("item", parent);
        showParentDetailView(intent, bundle);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_lessons_list);
        tvListViewHeader.setText(R.string.lessons);
        btnAddItem.setText(R.string.add_lesson);
        REFERENCE_PARENT = "/courses/";
        REFERENCE = "lessons";

        itemList = new ArrayList<>();
        adapter = new LessonsAdapter(getApplicationContext(), 0, itemList);
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
            llParent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendAnalyticsParentOpenedFromTouch(parent.title);
                    setupParentDetailView();
                }
            });
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
                Lesson lesson = adapter.getItem(position);

                sendAnalytics(lesson.title);

                Bundle bundle = new Bundle();
                bundle.putParcelable("parent", lesson);
                bundle.putBoolean("admin", admin);
                Intent intent = new Intent(LessonsListActivity.this, TopicsListActivity.class);
                intent.putExtras(bundle);
                startActivityForResult(intent, position);
            }
        });
        listView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_F1 && event.getAction() == KeyEvent.ACTION_UP) {
                    sendAnalyticsParentOpenedFromKeyboard(parent.title);
                    setupParentDetailView();
                }
                return false;
            }
        });
        btnAddItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LessonsListActivity.this, AddEditLessonActivity.class);
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
        mParentDatabase.addValueEventListener(parentListener);

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
                intent = new Intent(this, AddEditLessonActivity.class);
                bundle = new Bundle();
                bundle.putParcelable("lesson", itemList.get(index));
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
            menu.getItem(0).setTitle(R.string.edit_course);
            menu.getItem(1).setTitle(R.string.delete_course);
            menu.getItem(2).setTitle(R.string.add_lesson);
        }


        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        Bundle bundle;

        switch (item.getItemId()) {
            case R.id.edit_parent:
                intent = new Intent(LessonsListActivity.this, AddEditCourseActivity.class);
                bundle = new Bundle();
                bundle.putParcelable("course", parent);
                intent.putExtras(bundle);
                startActivityForResult(intent, 100);
                return true;
            case R.id.delete_parent:
                new AlertDialog.Builder(LessonsListActivity.this)
                        .setTitle(R.string.delete_course)
                        .setMessage(R.string.delete_course_confirm_message)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                mParentDatabase.removeEventListener(parentListener);
                                mDatabase.removeEventListener(listener);
                                Firebase firebase = new Firebase(REFERENCE_PARENT);
                                firebase.removeCourse(parent);
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
                intent = new Intent(this, AddEditLessonActivity.class);
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
