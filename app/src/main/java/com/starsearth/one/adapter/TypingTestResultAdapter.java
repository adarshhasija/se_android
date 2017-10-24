package com.starsearth.one.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.starsearth.one.R;
import com.starsearth.one.domain.TypingTestResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by faimac on 10/24/17.
 */

public class TypingTestResultAdapter extends RecyclerView.Adapter<TypingTestResultAdapter.ViewHolder> {

    private Context mContext;
    private ArrayList<TypingTestResult> mDataset;

    public TypingTestResultAdapter(Context context, ArrayList<TypingTestResult> myDataset) {
        mContext = context;
        mDataset = myDataset;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LinearLayout ll = (LinearLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_typing_test_result, parent, false);
        ViewHolder vh = new ViewHolder(ll);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        //Integer.toString(mDataset.get(position).score)
        int score = mDataset.get(position).score;
        int total = mDataset.get(position).total;
        holder.mScoreTextView.setText(String.format(mContext.getString(R.string.your_score), score, total));
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public LinearLayout mLinearLayout;
        public TextView mScoreTextView;
        public ViewHolder(LinearLayout ll) {
            super(ll);
            mLinearLayout = ll;
            mScoreTextView = (TextView) ll.findViewById(R.id.tv_score);
        }
    }
}
