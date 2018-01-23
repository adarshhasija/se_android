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
    private String mType = null;

    public TypingTestResultAdapter(Context context, ArrayList<TypingTestResult> myDataset) {
        mContext = context;
        mDataset = myDataset;
    }

    public TypingTestResultAdapter(Context context, ArrayList<TypingTestResult> myDataset, String type) {
        mContext = context;
        mDataset = myDataset;
        mType = type;
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
        TypingTestResult result = mDataset.get(position);
        int wordsCorrect = result.words_correct;
        int wordsTotalFinished = result.words_total_finished;
        double accuracy = (double) wordsCorrect/wordsTotalFinished;
        double accuracyPercentage = Math.ceil(accuracy*100);
        long timeTakenMillis = result.timeTakenMillis;
        holder.mScoreTextView.setText(mContext.getResources().getString(R.string.words_correct) + ": " + wordsCorrect + " out of " + wordsTotalFinished);

        if (timeTakenMillis/1000 < 10) {
            holder.mTimeTakenTextView.setText(mContext.getResources().getString(R.string.time_taken) +
                                            ": " + (timeTakenMillis/1000)/60 + "m 0" + timeTakenMillis / 1000 +"s");
        }
        else {
            int mins = (int) (timeTakenMillis/1000)/60;
            int seconds = (int) (timeTakenMillis/1000) % 60;
            //holder.mTimeTakenTextView.setText(mins + ":" + ((seconds == 0)? "00" : seconds)); //If seconds are 0, print double 0, else print seconds
        }
        holder.mWpm.setText(Integer.toString(wordsCorrect));
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public LinearLayout mLinearLayout;
        public TextView mScoreTextView;
        public TextView mTimeTakenTextView;
        public TextView mWpm;
        public ViewHolder(LinearLayout ll) {
            super(ll);
            mLinearLayout = ll;
            mScoreTextView = (TextView) ll.findViewById(R.id.tv_score);
            mTimeTakenTextView = (TextView) ll.findViewById(R.id.tv_time_taken);
            mWpm = (TextView) ll.findViewById(R.id.tv_wpm);
        }
    }
}
