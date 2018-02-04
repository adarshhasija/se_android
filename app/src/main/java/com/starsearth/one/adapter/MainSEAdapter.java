package com.starsearth.one.adapter;

import android.content.Context;
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
import com.starsearth.one.domain.Course;
import com.starsearth.one.domain.Game;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by faimac on 4/6/17.
 */

public class MainSEAdapter extends RecyclerView.Adapter<MainSEAdapter.ViewHolder> /*ArrayAdapter<String>*/ {

    private Context context;
    private ArrayList<String> mDataset;
    private ArrayList<String> mTimes = new ArrayList<>();

    public MainSEAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull ArrayList<String> mDataset) {
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

    public ArrayList<String> getObjectList() {
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
    public void onBindViewHolder(MainSEAdapter.ViewHolder holder, int position) {
        String object = null;
        if (position < mDataset.size()) object = mDataset.get(position);
        holder.mTextView1.setText(object);
        String lastTriedTime = null;
        if (position < mTimes.size()) lastTriedTime = mTimes.get(position);
        if (lastTriedTime != null) {
            String lastTried = String.format(context.getString(R.string.last_tried), lastTriedTime);
            holder.mTextView2.setText(lastTried);
            holder.setContentDescription(object + " " + lastTried);
        }

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

    public void addItem(String textLine1, String textLine2) {
        mDataset.add(0, textLine1);
        mTimes.add(0, textLine2);
        notifyItemInserted(0);
        notifyItemRangeChanged(0, mDataset.size());
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
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


        @Override
        public void onClick(View view) {

        }

    }
}
