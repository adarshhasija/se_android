package com.starsearth.one.activity.lists;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.starsearth.one.R;
import com.starsearth.one.activity.MainSEActivity;
import com.starsearth.one.activity.domaindetail.CourseDetailActivity;

public class ItemListActivity extends AppCompatActivity {

    public String ANALYTICS_ADMIN = "";
    public String ANALYTICS_PARENT_F1 = "parent_opened_keyboard_f1";
    public String ANALYTICS_PARENT_TOUCH = "parent_opened_touch";

    protected String REFERENCE_PARENT;
    protected String REFERENCE;

    protected DatabaseReference mParentDatabase;
    protected DatabaseReference mDatabase;
    protected FirebaseAnalytics mFirebaseAnalytics;
    protected Query query;
    protected boolean admin = false;

    //UI
    protected LinearLayout llParent;
    protected TextView tvParentLine1;
    protected TextView tvParentLine2;
    protected RelativeLayout rlListViewHeader;
    protected TextView tvListViewHeader;
    protected ListView listView;
    protected Button btnAddItem;


    protected void showParentDetailView(Intent intent, Bundle bundle) {
        if (intent != null && bundle != null) {
            intent.putExtras(bundle);
            startActivity(intent);
        }
    }

    protected void sendAnalyticsParentOpenedFromTouch(String parent) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, ANALYTICS_PARENT_TOUCH + parent);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

    protected void sendAnalyticsParentOpenedFromKeyboard(String parent) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, ANALYTICS_PARENT_F1 + parent);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

    protected void sendAnalytics(String selected) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, selected+ANALYTICS_ADMIN);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        llParent = (LinearLayout) findViewById(R.id.ll_parent);
        tvParentLine1 = (TextView) findViewById(R.id.tv_parent_line_1);
        tvParentLine2 = (TextView) findViewById(R.id.tv_parent_line_2);
        rlListViewHeader = (RelativeLayout) findViewById(R.id.rl_listview_header);
        tvListViewHeader = (TextView) findViewById(R.id.tv_listview_header);
        listView = (ListView) findViewById(R.id.listView);
        registerForContextMenu(listView);
        btnAddItem = (Button) findViewById(R.id.btn_add_item);


        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.getBoolean("admin")) {
            admin = true;
            ANALYTICS_ADMIN = "_admin";
            ActionBar actionBar = getSupportActionBar();
            actionBar.setBackgroundDrawable(new ColorDrawable(Color.RED));
        }

        if (admin) {
            rlListViewHeader.setVisibility(View.VISIBLE);
            btnAddItem.setVisibility(View.VISIBLE);
            listView.setEmptyView(btnAddItem);
        }
        else {
            rlListViewHeader.setVisibility(View.GONE);
        }

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (admin) {
            menu.add(0, 0, 0, R.string.edit);
            menu.add(0, 1, 1, R.string.delete);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (admin) {
            getMenuInflater().inflate(R.menu.activity_item_list_admin, menu);
        }


        return super.onCreateOptionsMenu(menu);
    }
}
