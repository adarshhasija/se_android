package com.starsearth.one.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.starsearth.one.R;

import java.util.ArrayList;

public class BotHelpActivity extends AppCompatActivity {

    public static String LOG_TAG = "BotHelpActivity";

    private ListView lvMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        setTitle("What can I ask?");

        lvMain = (ListView) findViewById(R.id.lvMain);
        ArrayList<String> list = new ArrayList<String>();
        list.add("Indian Sign Language for");
        list.add("I want to know the value of this currency note");
        list.add("Am I standing at a bus stop?");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list);
        lvMain.setAdapter(adapter);

        lvMain.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
    }
}
