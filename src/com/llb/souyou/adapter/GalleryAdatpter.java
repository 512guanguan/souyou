package com.llb.souyou.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;

public class GalleryAdatpter extends BaseAdapter{
	private ArrayList<BitmapDrawable> images=new ArrayList<BitmapDrawable>(5);
	private Context mContext;
	public GalleryAdatpter(Context context,ArrayList<BitmapDrawable> images){
		mContext=context;
		this.images=images;
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return images.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return images.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ImageView view = (ImageView) convertView;
		if(view==null){
			view=new ImageView(mContext);
		}
		view.setImageDrawable(images.get(position));
		view.setAdjustViewBounds(true);  
		view.setLayoutParams(new Gallery.LayoutParams(320,480));
		//view.setScaleType(ImageView.ScaleType.FIT_XY); // 这个是维持图片原始大小 
//		LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));  
		return view; 

	}

}
