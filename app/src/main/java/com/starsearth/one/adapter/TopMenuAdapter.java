package com.starsearth.one.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.starsearth.one.R;
import com.starsearth.one.activity.KeyboardActivity;
import com.starsearth.one.activity.profile.PhoneNumberActivity;

/**
 * Created by faimac on 2/26/18.
 */

public class TopMenuAdapter extends RecyclerView.Adapter<TopMenuAdapter.ViewHolder> {

    private Context context;
    private FirebaseAnalytics firebaseAnalytics;

    private TextView seAssistantStatus;

    public FirebaseAnalytics getFirebaseAnalytics() {
        return firebaseAnalytics;
    }

    public void setFirebaseAnalytics(FirebaseAnalytics firebaseAnalytics) {
        this.firebaseAnalytics = firebaseAnalytics;
    }

    public TopMenuAdapter(Context context) {
        this.context = context;
    }


    @Override
    public TopMenuAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        CardView cardView = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_top_menu, parent, false);
        TopMenuAdapter.ViewHolder vh = new TopMenuAdapter.ViewHolder(cardView);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        String text1=null;
        String text2=null;
        if (position == 0) {
            text1 = context.getResources().getString(R.string.se_assistant);
            text2 = context.getResources().getString(R.string.se_assistant_tap_here_to_begin);
            seAssistantStatus = holder.mTextView2; //This needs to be saved so we can keep updating the status
        }
        else if (position == 1) {
            text1 = context.getResources().getString(R.string.keyboard_test);
        }
        else if (position == 2) {
            text1 = context.getResources().getString(R.string.phone_number);
        }
        holder.mTextView1.setText(text1);
        holder.mTextView2.setText(text2);

        holder.mCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=null;
                Bundle bundle;
                if (position == 0) {

                }
                else if (position == 1) {
                    intent = new Intent(context, KeyboardActivity.class);
                    context.startActivity(intent);
                }
                else if (position == 2) {
                    intent = new Intent(context, PhoneNumberActivity.class);
                    context.startActivity(intent);
                }
            }
        });
    }

    public void setSEAssistantStatus(String status) {
        if (seAssistantStatus != null) {
            seAssistantStatus.setText(status);
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public CardView mCardView;
        public LinearLayout mLinearLayout;
        public TextView mTextView1;
        public TextView mTextView2;
        public ViewHolder(CardView cardView) {
            super(cardView);
            mCardView = cardView;
            mLinearLayout = (LinearLayout) mCardView.findViewById(R.id.ll);
            mTextView1 = (TextView) mLinearLayout.findViewById(R.id.text1);
            mTextView2 = (TextView) mLinearLayout.findViewById(R.id.text2);
        }

    }
}
