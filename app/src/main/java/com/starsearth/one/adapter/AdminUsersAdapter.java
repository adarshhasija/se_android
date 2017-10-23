package com.starsearth.one.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.starsearth.one.R;
import com.starsearth.one.domain.Course;
import com.starsearth.one.domain.User;

import java.util.ArrayList;

/**
 * Created by faimac on 4/15/17.
 */

public class AdminUsersAdapter extends ArrayAdapter<User> {

    private Context context;
    private ArrayList<User> userList;

    public AdminUsersAdapter(Context context, int resource, ArrayList<User> userList) {
        super(context, resource, userList);
        this.context = context;
        this.userList = userList;
    }

    @Override
    public int getCount() {
        return userList.size();
    }

    @Nullable
    @Override
    public User getItem(int position) {
        return userList.get(position);
    }

    public ArrayList<User> getUserList() {
        return userList;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.list_item_admin_users, null);
        //convertView = inflater.inflate(android.R.layout.simple_list_item_multiple_choice, null);

        User user = getItem(position);
        String email = user.email;
        TextView tv1 = (TextView) convertView.findViewById(R.id.text1);
        tv1.setText(email);

        return convertView;
    }
}
