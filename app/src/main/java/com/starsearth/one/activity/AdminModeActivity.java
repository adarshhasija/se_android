package com.starsearth.one.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.starsearth.one.R;
import com.starsearth.one.activity.lists.CoursesListActivity;

public class AdminModeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_console);
        setTitle(R.string.admin);

        Button btnViewData = (Button) findViewById(R.id.btn_view_data);
        btnViewData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Open admin mode
                Intent intent = new Intent(AdminModeActivity.this, CoursesListActivity.class);
                Bundle bundle = new Bundle();
                bundle.putBoolean("admin", true);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
    }
}
