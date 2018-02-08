package com.starsearth.one.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.starsearth.one.R;
import com.starsearth.one.Utils;
import com.starsearth.one.domain.Result;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by faimac on 10/24/17.
 */

public class ResultAdapter extends RecyclerView.Adapter<ResultAdapter.ViewHolder> {

    private Context mContext;
    private ArrayList<Result> mDataset;
    private String mType = null;

    public ResultAdapter(Context context, ArrayList<Result> myDataset) {
        mContext = context;
        mDataset = myDataset;
    }

    public ResultAdapter(Context context, ArrayList<Result> myDataset, String type) {
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

    public int getAccuracy(int wordsCorrect, int wordsTotalFinished) {
        double accuracy = (double) wordsCorrect/wordsTotalFinished;
        double accuracyPercentage = Math.ceil(accuracy*100);
        int accuracyPercentageInt = (int) accuracyPercentage;
        return accuracyPercentageInt;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Result result = mDataset.get(position);
        int wordsCorrect = result.words_correct;
        int wordsTotalFinished = result.words_total_finished;
        int accuracyPercentageInt = getAccuracy(wordsCorrect, wordsTotalFinished);
        long timeTakenMillis = result.timeTakenMillis;
        holder.mScoreTextView.setText(mContext.getResources().getString(R.string.words_correct) + ": " + wordsCorrect + " out of " + wordsTotalFinished);
        holder.mAccuracyTextView.setText(mContext.getResources().getString(R.string.accuracy) + ": " + accuracyPercentageInt + "%");

        if (timeTakenMillis/1000 < 10) {
            holder.mTimeTakenTextView.setText(mContext.getResources().getString(R.string.time_taken) +
                                            ": " + (timeTakenMillis/1000)/60 + "m 0" + timeTakenMillis / 1000 +"s");
        }
        else {
            int mins = (int) (timeTakenMillis/1000)/60;
            int seconds = (int) (timeTakenMillis/1000) % 60;
            //holder.mTimeTakenTextView.setText(mins + ":" + ((seconds == 0)? "00" : seconds)); //If seconds are 0, print double 0, else print seconds
        }

       /* if (position == 0) {
            holder.mWpmHighScore.setText(Integer.toString(wordsCorrect));
            holder.mWpmHighScore.setVisibility(View.VISIBLE);
            holder.mMainLabel.setText(R.string.high_score);
        } else if (position == 1) {
            holder.mWpmLastScore.setText(Integer.toString(wordsCorrect));
            holder.mWpmLastScore.setVisibility(View.VISIBLE);
            holder.mMainLabel.setText(String.format(mContext.getString(R.string.last_tried), Utils.formatDateTime(result.timestamp)));
        }   */
        holder.mWpmLastScore.setText(Integer.toString(wordsCorrect));
        holder.mWpmLastScore.setVisibility(View.VISIBLE);
        holder.mMainLabel.setText(String.format(mContext.getString(R.string.last_tried), Utils.formatDateTime(result.timestamp)));

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
        public TextView mMainLabel;
        public TextView mWpmHighScore;
        public TextView mWpmLastScore;
        public TextView mAccuracyTextView;
        public ViewHolder(LinearLayout ll) {
            super(ll);
            mLinearLayout = ll;
            mMainLabel = (TextView) ll.findViewById(R.id.tv_label_main);
            mScoreTextView = (TextView) ll.findViewById(R.id.tv_score);
            mTimeTakenTextView = (TextView) ll.findViewById(R.id.tv_time_taken);
            mWpmHighScore = (TextView) ll.findViewById(R.id.tv_wpm_high_score);
            mWpmLastScore = (TextView) ll.findViewById(R.id.tv_wpm_last_score);
            mAccuracyTextView = (TextView) ll.findViewById(R.id.tv_accuracy);
        }
    }
}
