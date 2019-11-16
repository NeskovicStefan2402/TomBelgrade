package com.example.tombelgradeapp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.LinkedList;

public class MyAdapter extends BaseAdapter {
    Context context;
    private final LinkedList<String> index;
    private final LinkedList<String> values;
    private final LinkedList<String> onoff;

    public MyAdapter(Context context, LinkedList<String> index, LinkedList<String> values,LinkedList<String> onoff){
        //super(context, R.layout.single_list_app_item, utilsArrayList);
        this.context = context;
        this.values = values;
        this.index = index;
        this.onoff=onoff;
    }

    @Override
    public int getCount() {
        return values.size();
    }

    @Override
    public Object getItem(int i) {
        return i;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {


        ViewHolder viewHolder;

        final View result;

        if (convertView == null) {

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.stavka, parent, false);
            viewHolder.txtName = (TextView) convertView.findViewById(R.id.textViewValue);
            viewHolder.txtIndex = (TextView) convertView.findViewById(R.id.textViewIndex);
            viewHolder.txtONOFF = (TextView) convertView.findViewById(R.id.textViewOnOff);
            result=convertView;
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result=convertView;
        }
        viewHolder.txtName.setText(values.get(position));
        viewHolder.txtIndex.setText(index.get(position));
        viewHolder.txtONOFF.setText(onoff.get(position));
        return convertView;
    }

    private static class ViewHolder {

        TextView txtName,txtIndex,txtONOFF;


    }


}