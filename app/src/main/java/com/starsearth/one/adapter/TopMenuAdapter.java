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
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.DatabaseReference;
import com.starsearth.one.R;
import com.starsearth.one.activity.AssistantActivity;
import com.starsearth.one.activity.KeyboardActivity;
import com.starsearth.one.activity.profile.PhoneNumberActivity;
import com.starsearth.one.domain.Assistant;
import com.starsearth.one.domain.MoreOptionsMenuItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by faimac on 2/26/18.
 */

public class TopMenuAdapter extends RecyclerView.Adapter<TopMenuAdapter.ViewHolder> {

    private Context context;
    private FirebaseAnalytics mFirebaseAnalytics;
    private ArrayList<MoreOptionsMenuItem> mDataset;

    private List<Assistant> assistants = new ArrayList<>();

    public FirebaseAnalytics getFirebaseAnalytics() {
        return mFirebaseAnalytics;
    }

    public void setFirebaseAnalytics(FirebaseAnalytics firebaseAnalytics) {
        this.mFirebaseAnalytics = firebaseAnalytics;
    }

    public void sendAnalytics(String selected) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, selected);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

    public TopMenuAdapter(Context context, ArrayList<MoreOptionsMenuItem> list) {
        this.context = context;
        this.mDataset = list;
    }


    @Override
    public TopMenuAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        CardView cardView = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_more_options, parent, false);
        TopMenuAdapter.ViewHolder vh = new TopMenuAdapter.ViewHolder(cardView);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final MoreOptionsMenuItem item = mDataset.get(position);
        holder.mTextView1.setText(item.getText1());
        holder.mTextView2.setText(item.getText2());

        holder.mCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendAnalytics(item.getText1());
                Intent intent;
                Bundle bundle;
                if (position == 0) {
                    intent = new Intent(context, AssistantActivity.class);
                    bundle = new Bundle();
                    if (!assistants.isEmpty()) {
                        bundle.putParcelable("assistant", assistants.get(0));
                    }
                    intent.putExtras(bundle);
                    context.startActivity(intent);
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

    //add assistant at the end of the array list
    public void addAssistant(Assistant assistant) {
        if (assistants != null) {
            assistants.add(assistant);
        }
    }

    public void removeAssistant(Assistant assistant) {
        if (assistants != null) {
            assistants.remove(assistant);
        }
    }

    public void removeOldAssistantRecord(DatabaseReference assistantReference) {
        if (assistants != null && assistants.size() > 1) {
            Assistant firstItem = assistants.get(0);
            assistantReference.child(firstItem.uid).removeValue();
            assistants.remove(firstItem);
        }
    }

    private void setSEAssistantStatus(String status) {
        mDataset.get(0).setText2(status);
        notifyItemChanged(0);
    }

    /*
    This function updates the text based on the state
     */
    public void assistantStateChanged(Assistant mAssistant) {
        if (mAssistant == null) {
            return;
        }

        String assistantStatus;
        if (mAssistant.state > 9 && mAssistant.state < 13) {
            assistantStatus = context.getString(R.string.se_assistant_tap_here_to_continue);
        }
        else if (mAssistant.state == Assistant.State.KEYBOARD_TEST_COMPLETED_SUCCESS.getValue() ||
                mAssistant.state == Assistant.State.KEYBOARD_TEST_COMPLETED_FAIL.getValue()) {
            assistantStatus = context.getString(R.string.se_assistant_keyboard_test_completed);
        }
        else {
            assistantStatus = context.getString(R.string.se_assistant_no_update);
        }

        setSEAssistantStatus(assistantStatus);
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
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
