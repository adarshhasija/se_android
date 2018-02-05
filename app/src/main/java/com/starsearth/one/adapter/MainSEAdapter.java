package com.starsearth.one.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.starsearth.one.R;
import com.starsearth.one.activity.KeyboardActivity;
import com.starsearth.one.activity.TypingTestResultActivity;
import com.starsearth.one.activity.profile.PhoneNumberActivity;
import com.starsearth.one.domain.Course;
import com.starsearth.one.domain.Game;
import com.starsearth.one.domain.MainMenuItem;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by faimac on 4/6/17.
 */

public class MainSEAdapter extends RecyclerView.Adapter<MainSEAdapter.ViewHolder> /*ArrayAdapter<String>*/ {

    private Context context;
    private ArrayList<MainMenuItem> mDataset;

    public MainSEAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull ArrayList<MainMenuItem> mDataset) {
        //super(context, resource, objects);
        this.context = context;
        this.mDataset = mDataset;
    }

    //@Override
    //public int getCount() {
        //return objectList.size();
    //}

   /* @Nullable
    @Override
    public String getItem(int position) {
        return objectList.get(position);
    }   */

    public ArrayList<MainMenuItem> getObjectList() {
        return mDataset;
    }

  /*  @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //convertView = inflater.inflate(R.layout.courses_list_item, null);
        convertView = inflater.inflate(R.layout.list_item_main, null);

        String object = getItem(position);
        TextView tv1 = (TextView) convertView.findViewById(R.id.text1);
        tv1.setText(object);

        return convertView;
    }   */


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RelativeLayout rl = (RelativeLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_main, parent, false);
        ViewHolder vh = new ViewHolder(rl);
        return vh;
    }

    @Override
    public void onBindViewHolder(MainSEAdapter.ViewHolder holder, final int position) {
        MainMenuItem object = null;
        if (position < mDataset.size()) object = mDataset.get(position);
        if (object.subject != null) {
            holder.mTextView1.setText(formatStringFirstLetterCapital(object.subject) + " - " + object.levelString);
        }
        else {
            holder.mTextView1.setText(formatStringFirstLetterCapital(object.other));
        }
        String lastTriedTime = null;
        long lastTriedMillis = mDataset.get(position).lastTriedMillis;
        if (position < mDataset.size() && lastTriedMillis > 0) lastTriedTime = formatDateTime(mDataset.get(position).lastTriedMillis);
        if (lastTriedTime != null) {
            String lastTried = String.format(context.getString(R.string.last_tried), lastTriedTime);
            holder.mTextView2.setText(lastTried);
            holder.setContentDescription(object + " " + lastTried);
        }

        holder.mRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainMenuItem mainMenuItem = mDataset.get(position);
                Intent intent=null;
                Bundle bundle;
                if (mainMenuItem.other != null) {
                    if (mainMenuItem.other.equalsIgnoreCase("Keyboard Test")) {
                        intent = new Intent(context, KeyboardActivity.class);
                        context.startActivity(intent);
                    }
                    if (mainMenuItem.other.equalsIgnoreCase("Phone Number")) {
                        intent = new Intent(context, PhoneNumberActivity.class);
                        context.startActivity(intent);
                    }
                }
                else if (mainMenuItem.subject.equalsIgnoreCase("typing")) {
                    intent = new Intent(context, TypingTestResultActivity.class);
                    bundle = new Bundle();
                    bundle.putString("subject", mainMenuItem.subject);
                    bundle.putString("levelString", mainMenuItem.levelString);
                    intent.putExtras(bundle);
                    context.startActivity(intent);
                }

            }
        });


    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public void removeAt(int position) {
        mDataset.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, mDataset.size());
    }

    public void addItem(MainMenuItem mainMenuItem) {
        int index = indexToInsert(mainMenuItem.lastTriedMillis);
        mDataset.add(index, mainMenuItem);
        notifyItemInserted(index);
        notifyItemRangeChanged(index, mDataset.size());
    }

    private int indexToInsert(long timestamp) {
        if (mDataset.isEmpty()) {
            return 0;
        }

        for (int i = 0; i < mDataset.size(); i++) {
            long timestampAtIndex = mDataset.get(i).lastTriedMillis;
            if (timestamp >= timestampAtIndex) {
                return i;
            }
        }

        //It is less than all the existing time values. Put it at the end
        return mDataset.size();
    }

    /*
        Returns date in local time zone
     */
    private String formatDateTime(long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date(timestamp));
        int offsetFromUTC = getOffsetFromUTC(cal);
        cal.add(Calendar.MILLISECOND, offsetFromUTC);
        Date date = cal.getTime(); //For debugging
        String monthString = String.format(Locale.US,"%tB",cal);
        monthString = formatStringFirstLetterCapital(monthString);
        String finalString = cal.get(Calendar.DATE) + " " + monthString + " " + cal.get(Calendar.YEAR);
        return finalString;
    }

    private String formatStringFirstLetterCapital(String s) {
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    /*
        This function returns the offset from GMT for the current timezone
        Returns: offset in millis
     */
    private int getOffsetFromUTC(Calendar cal) {
        Date date = cal.getTime();
        TimeZone tz = cal.getTimeZone();
        //Returns the number of milliseconds since January 1, 1970, 00:00:00 GMT
        long msFromEpochGmt = date.getTime();
        //gives you the current offset in ms from GMT at the current date
        int offsetFromUTC = tz.getOffset(msFromEpochGmt);
        return offsetFromUTC;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public RelativeLayout mRelativeLayout;
        public LinearLayout mLinearLayout;
        public TextView mTextView1;
        public TextView mTextView2;
        public ViewHolder(RelativeLayout rl) {
            super(rl);
            mRelativeLayout = rl;
            mLinearLayout = (LinearLayout) rl.findViewById(R.id.ll);
            mTextView1 = (TextView) mLinearLayout.findViewById(R.id.text1);
            mTextView2 = (TextView) mLinearLayout.findViewById(R.id.text2);
        }

        public void setContentDescription(String text) {
            mRelativeLayout.setContentDescription(text);
        }

    }
}
