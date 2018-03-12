package com.starsearth.one.adapter;

import android.content.Context;
import android.view.View;

import com.starsearth.one.R;
import com.starsearth.one.Utils;
import com.starsearth.one.domain.Result;
import com.starsearth.one.domain.ResultTyping;

import java.util.ArrayList;

/**
 * Created by faimac on 3/9/18.
 */

public class ResultTypingAdapter extends ResultAdapter {

    protected ArrayList<ResultTyping> mDataset;

    public ResultTypingAdapter(Context context, ArrayList<ResultTyping> myDataset) {
        super(context);
        mDataset = myDataset;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ResultTyping result = mDataset.get(position);
        holder.mWpmLastScore.setText(Integer.toString(result.getScore()));
        holder.mWpmLastScore.setVisibility(View.VISIBLE);
        holder.mMainLabel.setText(String.format(mContext.getString(R.string.last_tried), Utils.formatDateTime(result.timestamp)));
    }

    public int getItemCount() {
        return mDataset.size();
    }

    public int getAccuracy(int wordsCorrect, int wordsTotalFinished) {
        double accuracy = (double) wordsCorrect/wordsTotalFinished;
        double accuracyPercentage = Math.ceil(accuracy*100);
        int accuracyPercentageInt = (int) accuracyPercentage;
        return accuracyPercentageInt;
    }
}
