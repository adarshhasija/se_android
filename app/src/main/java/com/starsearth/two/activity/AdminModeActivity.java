package com.starsearth.two.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.starsearth.two.R;

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
             /*   Intent intent = new Intent(AdminModeActivity.this, CoursesListActivity.class);
                Bundle bundle = new Bundle();
                bundle.putBoolean("admin", true);
                intent.putExtras(bundle);
                startActivity(intent);  */
            }
        });
    }
}
