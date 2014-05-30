package com.llb.souyou.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.llb.souyou.R;
import com.llb.souyou.bean.CategoryBean;

public class CategoryGridViewAdapter extends BaseAdapter{
	private Context context;
	private ArrayList<CategoryBean> list;
	private ImageView imageView;
	private TextView title,desc;
	
	public CategoryGridViewAdapter(Context context,
			ArrayList<CategoryBean> list) {
		super();
		this.context = context;
		this.list = list;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView==null){
			convertView=LayoutInflater.from(context).inflate(R.layout.category1_item, null);
		}
		imageView=(ImageView) convertView.findViewById(R.id.icon);
		title=(TextView) convertView.findViewById(R.id.tv_title);
		desc=(TextView) convertView.findViewById(R.id.tv_content);
		
		//imageView设置图标省略
		title.setText(list.get(position).getTitle());
		desc.setText(list.get(position).getDesc());
		return convertView;
	}

}
