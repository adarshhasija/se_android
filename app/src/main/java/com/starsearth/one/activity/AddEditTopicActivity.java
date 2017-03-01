package com.starsearth.one.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.starsearth.one.R;
import com.starsearth.one.database.Firebase;
import com.starsearth.one.domain.Lesson;
import com.starsearth.one.domain.Topic;

import java.util.ArrayList;

public class AddEditTopicActivity extends AppCompatActivity {

    private String DATABASE_REFERENCE = "topics";
    private String UID;
    private String parentId;
    private Topic topic;
    private int selectedIndex = 0;

    //UI
    private EditText etTopicName;
    private EditText etTopicContent;
    private Button btnDone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_topic);
        setTitle(R.string.enter_edit_topic_details);

        Bundle extras = getIntent().getExtras();
        int totalTopics = 0;
        if (extras != null) {
            totalTopics = extras.getInt("totalItems");
            parentId = extras.getString("parentId");
            topic = extras.getParcelable("topic");
            if (topic != null )UID = topic.getUid();
        }
        ArrayList<Integer> list = new ArrayList<Integer>();
        for (int i = 0; i < (totalTopics + 1); i++) {
            list.add(i+1);
        }
        selectedIndex = list.get(list.size()-1) - 1;

        etTopicName = (EditText) findViewById(R.id.et_topic_name);
        etTopicContent = (EditText) findViewById(R.id.et_topic_content);
        if (topic != null) {
            etTopicName.setText(topic.getName());
            etTopicContent.setText(topic.getContent());
        }
        btnDone = (Button) findViewById(R.id.btn_done);
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String topicName = etTopicName.getText().toString();
                String topicContent = etTopicContent.getText().toString();

                if (topicName == null || topicName.length() < 1) {
                    Toast.makeText(AddEditTopicActivity.this, R.string.topic_name_blank, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (topicContent == null || topicContent.length() < 1) {
                    Toast.makeText(AddEditTopicActivity.this, R.string.topic_content_blank, Toast.LENGTH_SHORT).show();
                    return;
                }

                Firebase firebase = new Firebase(DATABASE_REFERENCE);
                if (UID != null) {
                    topic.setName(topicName);
                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    topic.setUpdatedBy(currentUser.getUid());
                    firebase.updateExistingTopic(UID, topic);
                }
                else {
                    UID = firebase.writeNewTopic(selectedIndex, topicName, topicContent, parentId);
                }

                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putString("uid", UID);
                bundle.putString("name", topicName);
                bundle.putString("content", topicContent);
                intent.putExtras(bundle);
                setResult(RESULT_OK, intent);
                finish();

            }
        });
    }
}
