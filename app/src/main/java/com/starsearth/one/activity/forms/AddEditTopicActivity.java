package com.starsearth.one.activity.forms;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.starsearth.one.R;
import com.starsearth.one.database.Firebase;
import com.starsearth.one.domain.Topic;

import java.util.ArrayList;

public class AddEditTopicActivity extends AppCompatActivity {

    private String DATABASE_REFERENCE = "topics";
    private String UID;
    private String parentId;
    private Topic topic;
    private int selectedIndex = 0;

    //UI
    private EditText etTopicTitle;
    private EditText etTopicDescription;
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

        etTopicTitle = (EditText) findViewById(R.id.et_topic_title);
        etTopicDescription = (EditText) findViewById(R.id.et_topic_description);
        if (topic != null) {
            etTopicTitle.setText(topic.getTitle());
            etTopicDescription.setText(topic.getDescription());
        }
        btnDone = (Button) findViewById(R.id.btn_done);
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String topicTitle = etTopicTitle.getText().toString();
                String topicDescription = etTopicDescription.getText().toString();

                if (topicTitle == null || topicTitle.length() < 1) {
                    Toast.makeText(AddEditTopicActivity.this, R.string.topic_title_blank, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (topicDescription == null || topicDescription.length() < 1) {
                    Toast.makeText(AddEditTopicActivity.this, R.string.topic_description_blank, Toast.LENGTH_SHORT).show();
                    return;
                }

                Firebase firebase = new Firebase(DATABASE_REFERENCE);
                if (UID != null) {
                    topic.setTitle(topicTitle);
                    topic.setDescription(topicDescription);
                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    topic.setUpdatedBy(currentUser.getUid());
                    firebase.updateExistingTopic(UID, topic);
                }
                else {
                    UID = firebase.writeNewTopic(selectedIndex, topicTitle, topicDescription, parentId);
                }

                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putString("uid", UID);
                bundle.putString("title", topicTitle);
                bundle.putString("description", topicDescription);
                intent.putExtras(bundle);
                setResult(RESULT_OK, intent);
                finish();

            }
        });
    }
}
