package com.starsearth.one.adapter;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.starsearth.one.R;
import com.starsearth.one.domain.Question;

import java.util.List;

/**
 * Created by faimac on 3/7/17.
 */

public class QuestionsAdapter extends ArrayAdapter<Question> {

    private Context context;
    private List<Question> questionList;


    public QuestionsAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<Question> questionList) {
        super(context, resource, questionList);
        this.context = context;
        this.questionList = questionList;
    }

    @Override
    public int getCount() {
        return questionList.size();
    }

    @Nullable
    @Override
    public Question getItem(int position) {
        return questionList.get(position);
    }

    public List<Question> getQuestionList() {
        return questionList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.list_item_domain, null);

        Question question = getItem(position);
        String title = question.getTitle();
        TextView tv1 = (TextView) convertView.findViewById(R.id.text1);
        tv1.setText(title);

        return convertView;
    }
}
