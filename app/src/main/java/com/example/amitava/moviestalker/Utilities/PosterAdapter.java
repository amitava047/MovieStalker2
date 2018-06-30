package com.example.amitava.moviestalker.Utilities;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.amitava.moviestalker.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Amitava on 29-Nov-17.
 */

public class PosterAdapter extends ArrayAdapter<GridItem> {
    private Context context;
    private int layoutResourceId;
    private ArrayList<GridItem> mGridData = new ArrayList<GridItem>();

    public PosterAdapter(Context c, int layoutResourceId, ArrayList<GridItem> mGridData) {
        super(c,layoutResourceId,mGridData);
        context = c;
        this.layoutResourceId = layoutResourceId;
        this.mGridData = mGridData;
    }

    public void setGridData(ArrayList<GridItem> mGridData) {
        this.mGridData = mGridData;
        notifyDataSetChanged();
    }

    static class ViewHolder{
        ImageView mPoster;
        TextView mPosterTitle;
    }

    @Override
    public int getCount() {
        return mGridData.size();
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if(view == null){
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            view = inflater.inflate(layoutResourceId,viewGroup,false);
            viewHolder = new ViewHolder();
            viewHolder.mPosterTitle = (TextView) view.findViewById(R.id.poster_title);
            viewHolder.mPoster = (ImageView) view.findViewById(R.id.poster_image);
            view.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) view.getTag();
        }

        GridItem item = mGridData.get(position);
        Log.e("ITEM POSITION", "The item position is " + position);
        viewHolder.mPosterTitle.setText(item.getTitle());

        Picasso.with(context).load(item.getImage()).into(viewHolder.mPoster);
        return view;
    }
}
