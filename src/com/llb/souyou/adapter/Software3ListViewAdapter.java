package com.llb.souyou.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.llb.souyou.DownloadActivity;
import com.llb.souyou.R;
import com.llb.souyou.bean.SoftwareItem1Bean;
import com.llb.souyou.cache.SD_Cache;

public class Software3ListViewAdapter extends BaseAdapter{
	private ArrayList<SoftwareItem1Bean> list;//存储的是几个item需要的信息
	private Context mContext;
	private ImageView imageView;
	private TextView title,downloadTime,size;
	private RatingBar starRating;
	private int status;//下载状态status:-1=未有下载操作   0=failed 1=success 2=paused 3=loading
	private LinearLayout downLinear;//下载按钮区域响应
	public Software3ListViewAdapter( Context mContext,ArrayList<SoftwareItem1Bean> list){
		this.mContext=mContext;
		this.list=list;
	}
	public Software3ListViewAdapter(Context mContext){
		this.mContext=mContext;
		SoftwareItem1Bean item=null;//=new LeftViewItemBean(R.drawable.leftview, "热门帖子");
		list.add(item);
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return list.size();
	}
	@Override
	public Object getItem(int index) {
		// TODO Auto-generated method stub
		return list.get(index);
	}
	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		if(convertView==null){
			convertView=LayoutInflater.from(mContext).inflate(R.layout.software_item3, null);
		}
//		View view=LayoutInflater.from(mContext).inflate(R.layout.left_listview_item, null);
		imageView=(ImageView) convertView.findViewById(R.id.icon);
		title=(TextView) convertView.findViewById(R.id.tv_title);
		starRating=(RatingBar) convertView.findViewById(R.id.ratingbar);
		downloadTime=(TextView) convertView.findViewById(R.id.tv_downloadtimes);
		size=(TextView) convertView.findViewById(R.id.tv_size);
		downLinear=(LinearLayout) convertView.findViewById(R.id.linearright);
		
		imageView.setImageResource(R.drawable.appicon2);
		title.setText(list.get(position).getTitle());
		starRating.setRating(list.get(position).getStarRating());
		downloadTime.setText(list.get(position).getDownloadTime()+"次下载");
		size.setText(list.get(position).getSize());
		
		//从SD卡中寻找有没有
		Bitmap bitmap=SD_Cache.getCachedImage(list.get(position).getImageUrl());
		if(null!=bitmap){
			imageView.setImageBitmap(bitmap);
		}
		
		downLinear.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
//				Toast.makeText(mContext, "下载还么有实现哟", 0).show();
				Intent intent=new Intent(mContext,DownloadActivity.class);
				intent.putExtra("app", list.get(position));
				mContext.startActivity(intent);
			}
		});
		
		if(list.get(position).getStatus()>0){
			//设置下载的图标和文字
		}else {
			//设置打开的图标文字
		}
		return convertView;
	}
}
