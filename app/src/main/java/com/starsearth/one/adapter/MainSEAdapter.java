package com.starsearth.one.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.starsearth.one.R;
import com.starsearth.one.Utils;
import com.starsearth.one.activity.KeyboardActivity;
import com.starsearth.one.activity.GameResultActivity;
import com.starsearth.one.activity.profile.PhoneNumberActivity;
import com.starsearth.one.domain.Game;
import com.starsearth.one.domain.MainMenuItem;
import com.starsearth.one.domain.Result;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by faimac on 4/6/17.
 */

public class MainSEAdapter extends RecyclerView.Adapter<MainSEAdapter.ViewHolder> /*ArrayAdapter<String>*/ {

    private Context context;
    private FirebaseAnalytics firebaseAnalytics;
    private ArrayList<MainMenuItem> mDataset;

    public FirebaseAnalytics getFirebaseAnalytics() {
        return firebaseAnalytics;
    }

    public void setFirebaseAnalytics(FirebaseAnalytics firebaseAnalytics) {
        this.firebaseAnalytics = firebaseAnalytics;
    }

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

    public MainMenuItem getItem(int index) {
        return mDataset.get(index);
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

    private String formatLatTriedTime(List<Result> inputs) {
        String result = null;
        if (inputs != null && !inputs.isEmpty()) {
            Result input = inputs.get(0);
            long time = input.timestamp;
            String timeFormatted = Utils.formatDateTime(time);
            result = String.format(context.getString(R.string.last_tried), timeFormatted);
        }
        return result;
    }

    private String getGameTitle(Game game) {
        String result = null;
        if (game != null) {
            result = game.title;
        }
        return result;
    }


    @Override
    public void onBindViewHolder(MainSEAdapter.ViewHolder holder, final int position) {
        MainMenuItem object = null;
        if (position < mDataset.size()) object = mDataset.get(position);

        holder.mTextView1.setText(Utils.formatStringFirstLetterCapital(getGameTitle(object.game)));
        holder.mTextView2.setText(formatLatTriedTime(object.results));

        holder.mRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainMenuItem mainMenuItem = mDataset.get(position);
                Game game = mainMenuItem.game;

                if (game != null) sendAnalytics(game.id, game.title);
                Intent intent = new Intent(context, GameResultActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable("game", game);
                //bundle.putString("subject", mainMenuItem.subject);
                //bundle.putString("levelString", mainMenuItem.levelString);
                //bundle.putInt("game_id", (int) mainMenuItem.gameId.getValue());
                intent.putExtras(bundle);
                context.startActivity(intent);
            }
        });


    }

    public void sendAnalytics(int id, String name) {
        Bundle bundle = new Bundle();
        bundle.putInt(FirebaseAnalytics.Param.ITEM_ID, id);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, name);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "list_item");
        if (firebaseAnalytics != null) {
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        }
    }

    public void sendAnalytics(String name) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, name);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "list_item");
        if (firebaseAnalytics != null) {
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        }
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public void removeAt(int position) {
        mDataset.remove(position);
        //notifyItemRemoved(position);
        //notifyItemRangeChanged(position, mDataset.size());
    }

    public void addItem(MainMenuItem mainMenuItem) {
        int index = indexToInsert(mainMenuItem.lastTriedMillis);
        mDataset.add(index, mainMenuItem);
        //notifyDataSetChanged();
        //notifyItemInserted(index);
        //notifyItemRangeChanged(0, 1); //notifyItemRangeChanged(index, mDataset.size());
    }

    private int indexToInsert(long timestamp) {
        if (mDataset.isEmpty()) {
            return 0;
        }

        int index = binarySearh(timestamp, 0, mDataset.size() - 1);
        if (index > -1) {
            return index;
        }

      /*  for (int i = 0; i < mDataset.size(); i++) {
            long timestampAtIndex = mDataset.get(i).lastTriedMillis;
            if (timestamp >= timestampAtIndex) {
                return i;
            }
        }   */

        //It is less than all the existing time values. Put it at the end
        return mDataset.size();
    }

    private int binarySearh(long value, int startIndex, int endIndex) {
        if (startIndex <= endIndex) {
            return startIndex;
        }
        int result=-1;
        int middleIndex = (startIndex + endIndex)/2;
        if (value > mDataset.get(middleIndex).lastTriedMillis) {
            result = binarySearh(value, startIndex, middleIndex);
        }
        else if (value <= mDataset.get(middleIndex).lastTriedMillis) {
            result = binarySearh(value, middleIndex+1, endIndex);
        }
        return result;
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

    }
}
