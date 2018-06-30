package com.example.amitava.moviestalker.Utilities;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.amitava.moviestalker.R;

import java.util.ArrayList;

public class ReviewsAdapter extends ArrayAdapter<ListItem> {

    private Context context;
    private int layoutResourceId;
    private ArrayList<ListItem> mListData = new ArrayList<ListItem>();

    //Constructor
    public ReviewsAdapter(Context context, int layoutResourceId, ArrayList<ListItem> mListData){
        super(context, layoutResourceId, mListData);
        this.context = context;
        this.layoutResourceId = layoutResourceId;
        this.mListData = mListData;
    }

    public void setListData(ArrayList<ListItem> mListData) {
        this.mListData = mListData;
        notifyDataSetChanged();
    }

    static class ViewHolder{
        TextView mReview;
        TextView mUsername;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null){
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            convertView = inflater.inflate(layoutResourceId, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.mReview = (TextView) convertView.findViewById(R.id.tv_user_review);
            viewHolder.mUsername = (TextView) convertView.findViewById(R.id.tv_user);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        ListItem item = mListData.get(position);
        viewHolder.mReview.setText(item.getComment());
        viewHolder.mUsername.setText(item.getUsername());
        return convertView;
    }
}
