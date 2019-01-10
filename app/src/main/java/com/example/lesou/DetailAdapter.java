package com.example.lesou;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class DetailAdapter extends ArrayAdapter {

    private final int resourceId;

    public DetailAdapter(Context context, int textViewResourceId, List<DetailMenuItem> objects) {
        super(context, textViewResourceId, objects);
        resourceId = textViewResourceId;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        DetailMenuItem detail = (DetailMenuItem) getItem(position); // 获取当前项的Fruit实例
        View view = LayoutInflater.from(getContext()).inflate(resourceId, null);//实例化一个对象
        ImageView dtailImage = (ImageView) view.findViewById(R.id.detail_image);//获取该布局内的图片视图
        TextView detailName = (TextView) view.findViewById(R.id.detail_name);//获取该布局内的文本视图
        dtailImage.setImageResource(detail.getImageId());//为图片视图设置图片资源
        detailName.setText(detail.getName());//为文本视图设置文本内容
        return view;
    }
}
