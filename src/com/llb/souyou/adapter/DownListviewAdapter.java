package com.llb.souyou.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.llb.souyou.R;
import com.llb.souyou.bean.SoftwareItem1Bean;
import com.llb.souyou.cache.SD_Cache;

public class DownListviewAdapter extends BaseAdapter{
	private ArrayList<SoftwareItem1Bean> list;//存储的是几个item需要的信息
	private Context mContext;
	private ImageView imageView;
	private TextView title;//应用名称
	private TextView tv_progress;//下载百分比显示
	private ProgressBar progressBar;//进度条显示
	private Button bt_status;//下载状态按钮
	private int status;//下载状态status:-1=未有下载操作   0=failed 1=success 2=paused 3=loading
	public DownListviewAdapter( Context mContext,ArrayList<SoftwareItem1Bean> list){
		this.mContext=mContext;
		this.list=list;
	}
	public DownListviewAdapter(Context mContext){
		this.mContext=mContext;
	}
	@Override
	public int getCount() {
		return list.size();
	}
	@Override
	public Object getItem(int index) {
		return list.get(index);
	}
	@Override
	public long getItemId(int position) {
		return position;
	}
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		if(convertView==null){
			convertView=LayoutInflater.from(mContext).inflate(R.layout.download_item, null);
		}
//		View view=LayoutInflater.from(mContext).inflate(R.layout.left_listview_item, null);
		imageView=(ImageView) convertView.findViewById(R.id.icon);
		title=(TextView) convertView.findViewById(R.id.tv_appname);
		tv_progress=(TextView) convertView.findViewById(R.id.tv_progress);
		progressBar=(ProgressBar) convertView.findViewById(R.id.probar);
		bt_status=(Button) convertView.findViewById(R.id.bt_down);
		
		imageView.setImageResource(R.drawable.appicon2);
//		Log.i("llb","position="+position+"  list.tostring()="+list.toString());
		title.setText(list.get(position).getTitle());//应用名称
		tv_progress.setText(list.get(position).getProgress()+"%");//进度显示
		progressBar.setProgress(list.get(position).getProgress());//进度条
		
		//从SD卡中寻找有没有
		Bitmap bitmap=SD_Cache.getCachedImage(list.get(position).getImageUrl());
		if(null!=bitmap){
			imageView.setImageBitmap(bitmap);
		}
		switch(list.get(position).getStatus()){
		case -1://未下载
			break;
		case 3://loading
			bt_status.setText("下载中");
			break;
		case 0:
			bt_status.setText("失败");
			break;
		case 1:
			bt_status.setText("安装");
			progressBar.setVisibility(View.GONE);//隐藏进度条
			tv_progress.setVisibility(View.GONE);
		}
		
		bt_status.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				switch(list.get(position).getStatus()){
				case -1://未下载
					break;
				case 3://loading 正在下载》》暂停
					break;
				case 0://下载失败》》开始下载
					break;
				case 1: //下载完成》》执行安装
					break;
				}
			}
		});
		return convertView;
	}

}
