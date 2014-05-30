package com.llb.souyou;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.llb.souyou.adapter.DownListviewAdapter;
import com.llb.souyou.app.Constant;
import com.llb.souyou.bean.SoftwareItem1Bean;
import com.llb.souyou.db.DBHelper;
import com.llb.souyou.util.AppUtil;
import com.llb.souyou.util.DownloadThread;

public class DownloadActivity extends Activity implements OnClickListener{
//	private ImageView icon;
//	private TextView title,tv_progress;
//	private Button bt_status;
//	private ProgressBar progressBar;
	private SoftwareItem1Bean app;
	private ExecutorService pool;
	private String filePath;
	
	private DBHelper helper;
	private ArrayList<SoftwareItem1Bean> loadingList=new ArrayList<SoftwareItem1Bean>(5);//正在下载的列表
	private HashMap<Integer, Integer> places=new HashMap<Integer, Integer>(10);//存在每个应用的位置信息 id-位置
	HashMap<Integer, Long> sizes=new HashMap<Integer, Long>(10);//反映每个应用的大小变化

	
	private ArrayList<SoftwareItem1Bean> finishList=new ArrayList<SoftwareItem1Bean>(5);//下载完成却没有安装的列表
	private DownListviewAdapter adapter;
	private ListView listView;
	
	public Handler handler=new Handler(){
		long size=0;
//		HashMap<Integer, Long> sizes=new HashMap<Integer, Long>(5);//反映每个应用的大小变化
		long total=0;
		public void handleMessage(android.os.Message msg) {
			int id=msg.arg1;
//			HashMap<Integer, Object> message=new HashMap<Integer, Object>(1);
			
//			message.putAll((HashMap<Integer, Object>) msg.obj);//获取返回的信息
//			((HashMap<Integer, Object>)msg.obj).clear();
//			Iterator iterator= message.entrySet().iterator();
//			int id=(Integer) message.keySet().toArray()[0];//获取应用id号
//			if(iterator.hasNext()){
//				id=(Integer) iterator.next();//获取应用id号
//			}
			switch (msg.what) {
			case 0://正在下载
				Long size=sizes.get(id)+(Long)msg.obj; //message.get(id);//原来的大小+新下载的大小
				sizes.put(id,size);//更新已下载的应用大小
//				Log.i("llb", "更新后的下载总数"+sizes.get(id));
				int progre=(int)((size*100)/total);//下载进度
				loadingList.get(places.get(id)).setProgress(progre);//设置下载进度
				adapter.notifyDataSetChanged();
//				progressBar.setProgress(progre);
//				tv_progress.setText(progre+"%");
				break;
			case 1://下载成功了
//				progressBar.setProgress(100);
//				bt_status.setText("成功");
//				tv_progress.setText("100%");
				Log.i("llb", "下载成功");
				loadingList.get(places.get(id)).setProgress(100);//设置下载进度
				adapter.notifyDataSetChanged();
				helper=new DBHelper(getBaseContext());
				helper.ExecSQL("update download set status=1,progress="+100+" where app_id="+String.valueOf(id));
				helper.db.close();
				AppUtil.openFile(DownloadActivity.this, filePath);
				break;
			case 2://下载失败了
//				bt_status.setText("失败");
				Log.i("llb", "下载="+msg.obj);
				//修改数据库中的状态
				helper=new DBHelper(getBaseContext());
				helper.ExecSQL("update download set status=0,progress="+loadingList.get(places.get(id)).getProgress()+" where app_id="+String.valueOf(id));
				helper.db.close();
				Toast.makeText(getApplicationContext(), "不好意思，有个子线程下载失败了", 1).show();
				break;
			case 3://下载过程中返回应用总大小
				total=(Long)msg.obj;
				Log.i("llb", "总数="+total);
				break;
			case 4://返回一些提示信息
				Toast.makeText(getApplicationContext(),(CharSequence) msg.obj,1).show();//(CharSequence) message.get(id), 1).show();
				break;
			}
//			message.clear();
		};
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_download);
		
		listView=(ListView) findViewById(R.id.lv_loading);
//		progressBar=(ProgressBar) findViewById(R.id.probar);
////		progressBar.setProgressDrawable(d);
//		title=(TextView)findViewById(R.id.tv_appname);
//		tv_progress=(TextView)findViewById(R.id.tv_progress);
//		bt_status=(Button)findViewById(R.id.bt_down);
		pool=Executors.newFixedThreadPool(2);//最多同时下载2个软件
		
		helper=new DBHelper(this);
		Cursor cursor=helper.rawQuery("select * from download", null);
		cursor.moveToFirst();
		//下载状态表 download(app_id,imageurl,title,rate,app_down,size,status);
		//下载状态status:-1=未有下载操作   0=failed 1=success 2=paused 3=loading
		for(int i=0;i<cursor.getCount();i++){
			if(cursor.getInt(cursor.getColumnIndex("status"))==1){//下载完成的应用列表
				finishList.add(new SoftwareItem1Bean(cursor.getString(0),
						cursor.getString(1), cursor.getString(2), 
						cursor.getFloat(3), cursor.getString(4), 
						cursor.getString(5), cursor.getInt(6),cursor.getInt(7)));
//				places.put(cursor.getInt(0), finishList.size()-1);//存放app_id对应的位置   0起始
//				sizes.put(cursor.getInt(0), 0L);
			} else {//下载未完成的应用列表
				loadingList.add(new SoftwareItem1Bean(cursor.getString(0),
						cursor.getString(1), cursor.getString(2), 
						cursor.getFloat(3), cursor.getString(4), 
						cursor.getString(5), cursor.getInt(6),cursor.getInt(7)));
				places.put(cursor.getInt(0), loadingList.size()-1);//存放app_id对应的位置   0起始
				sizes.put(cursor.getInt(0), 0L);
			}
			cursor.moveToNext();
		}
		cursor.close();
		
		
		
		Intent intent=this.getIntent();
		if(intent.getIntExtra("src", 1)>0){//不是从Actionbar直接点击来的
			app=(SoftwareItem1Bean) intent.getSerializableExtra("app");
			app.setStatus(3);//设置状态是下载中
			//检查是否已经在数据库了
//			Cursor cursor2=helper.rawQuery("select app_id from download where app_id=?", new String[]{app.getId()});
//			if(cursor2.getCount()>0){
//				cursor2.close();//已经存在这条记录了
//				Toast.makeText(this, "已经在下载列表了", 0).show();
//			}else {
				
			if(!places.containsKey(Integer.parseInt(app.getId()))){//检查是否已经在列表中了
				places.put(Integer.parseInt(app.getId()),places.size());//保存新的位置信息
				sizes.put(Integer.parseInt(app.getId()),0L);
				loadingList.add(app);//把新条目存进来
				
	//			title.setText(app.getTitle());
				String urlPath=Constant.DOWNLOAD_URL+app.getId();
				filePath=Constant.APP_BASE_PATH+app.getTitle()+".apk";
				Log.i("llb","urlPath="+urlPath);
				
				//开启线程去下载
				pool.execute(new DownloadThread(urlPath, app.getTitle(), handler,Integer.parseInt(app.getId())));//开始下载
				
				//保存到数据库中
				//下载状态表 download(id,app_id,imageurl,title,rate,app_down,size,status);
				ContentValues values=new ContentValues();
				values.put("app_id", app.getId());
				values.put("imageurl", app.getImageUrl());
				values.put("title", app.getTitle());
				values.put("rate", app.getStarRating());
				values.put("app_down", app.getDownloadTime());
				values.put("size", app.getSize());
				values.put("status", -1);//3-loading
				values.put("progress", app.getProgress());
	//			helper.insert("download", values);//插入一条下载记录到数据库表
				helper.replace("download", values);
				values.clear();
			}
		}
		helper.db.close();//关闭数据库对象
		
		adapter=new DownListviewAdapter(this,loadingList);
		listView.setAdapter(adapter);
//		bt_status.setOnClickListener(this);
		
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		Toast.makeText(this, "客观到外面点去，我还木有穿好衣服", 1).show();
	}
}
