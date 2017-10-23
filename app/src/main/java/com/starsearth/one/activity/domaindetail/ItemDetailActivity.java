package com.starsearth.one.activity.domaindetail;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.starsearth.one.R;

public class ItemDetailActivity extends AppCompatActivity {

    protected TextView tvTitle;
    protected TextView tvDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);

        tvTitle = (TextView) findViewById(R.id.tv_title);
        tvDescription = (TextView) findViewById(R.id.tv_description);

    }
}
