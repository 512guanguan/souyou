package com.llb.souyou;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.llb.souyou.adapter.GalleryAdatpter;
import com.llb.souyou.app.Constant;
import com.llb.souyou.bean.SoftwareItem1Bean;
import com.llb.souyou.cache.SD_Cache;
import com.llb.souyou.util.AppUtil;
import com.llb.souyou.util.JsonDecodeUtil;
import com.llb.souyou.util.NetworkUtil;

public class ContentActivity extends Activity implements OnItemClickListener{
	private SoftwareItem1Bean item;
	private TextView  app_title,content,app_down,app_size;
	private ImageView icon;
	private LinearLayout down1;//顶部下载按钮
	private Button bt_down2;//底部下载按钮
	private Gallery gallery;//图片切换
	private GalleryAdatpter adatpter;
	private ArrayList<BitmapDrawable> imageList=new ArrayList<BitmapDrawable>(5);
	//网络请求
	static final int IMAGE=2;//图片请求结果
	static final int SUCCESS=1;//标记请求结果
	static final int FAILED=0;
	private Handler mHandler=new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case SUCCESS:
				IdentityHashMap<String,String> hashMap=(IdentityHashMap<String, String>) msg.obj;
				content.setText(Html.fromHtml(hashMap.get("app_desc")));
				break;
			case IMAGE://每得到一张图片响应一次
				imageList.add((BitmapDrawable)msg.obj);
				adatpter.notifyDataSetChanged();
				break;
			case FAILED:
				break;
			}
		};
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_content);
		icon=(ImageView) findViewById(R.id.icon);
		app_title=(TextView) findViewById(R.id.tv_title);
		content=(TextView) findViewById(R.id.tv_desc_detail);
		app_down=(TextView) findViewById(R.id.tv_downloadtimes);
		app_size=(TextView) findViewById(R.id.tv_size);
		
		down1=(LinearLayout) findViewById(R.id.linearright);
		bt_down2=(Button) findViewById(R.id.bt_download);
		gallery=(Gallery) findViewById(R.id.gallery);
		
		adatpter=new GalleryAdatpter(this,imageList);
		gallery.setAdapter(adatpter);
		gallery.setOnItemClickListener(this);
		
		Intent intent=this.getIntent();
		item=(SoftwareItem1Bean) intent.getExtras().get("item");
				
		//开启一个线程 请求数据
		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				BasicNameValuePair pair1=new BasicNameValuePair("type","101");//code=101表示请求详情
				BasicNameValuePair pair2=new BasicNameValuePair("id", item.getId());
				String url=NetworkUtil.createURL(pair1,pair2);//拼接url
				String jsonString=NetworkUtil.checkCacheData(Constant.DETAIL_BASE_PATH, AppUtil.md5(url));
				if(jsonString==null){//说明没有缓存数据
					//向网络请求详情数据
					jsonString=NetworkUtil.getHttpEntity( Constant.DETAIL_BASE_PATH,url);
				}
				
				try {
					JSONObject jsonObject=new JSONObject(jsonString);
					String code=jsonObject.getString("code");
					Log.i("llb", "code="+code);
					//普通的HashMap键值无法重复，而IdentityHashMap当key1==key2时才认为是重复键值
					IdentityHashMap<String, String> msgMap=new JsonDecodeUtil().decodeDetailJson(jsonObject);
					//缓存数据
					SD_Cache.saveCacheDetailData(msgMap,Constant.DETAIL_BASE_PATH+AppUtil.md5(url));
					Log.i("llb", "masMap="+msgMap.toString());
					if(code.equals("101")){//请求成功
						mHandler.obtainMessage(SUCCESS, msgMap).sendToTarget();//返回数据
						//去除非图片url元素   文字内容
//						msgMap.remove("app_desc");//如果执行这个操作就跪了，只存了一份呀！！
						
						Log.i("llb", "继续请求网络图片");
						Iterator<Map.Entry<String, String>> iterator=msgMap.entrySet().iterator();
						String imageUrl=null;
						
						while (iterator.hasNext()) {
							Map.Entry<String, String> itm=iterator.next();
							Log.i("llb",itm.getKey());
							if (itm.getKey()!="app_desc") {
								imageUrl=itm.getValue();//获得图片url
								//在这里应该先判断一下是否在缓存了图片    再考虑是否请求网络
								Bitmap bitmap=SD_Cache.getCachedImage(imageUrl);
								if(bitmap!=null){//缓存中有资源
									Log.i("llb","小样，缓存有了呀");
									mHandler.obtainMessage(IMAGE,new BitmapDrawable(bitmap)).sendToTarget();
								}else {//没有缓存，那么请求网络
									BitmapDrawable drawable=NetworkUtil.getDrawable(imageUrl);//获取了一张图片（可能是缓存读取的）
									Log.i("llb","缓存了一张图片");
									mHandler.obtainMessage(IMAGE,drawable).sendToTarget();
								}
							}
						}
					}else {
						mHandler.obtainMessage(FAILED).sendToTarget();
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
		app_title.setText(item.getTitle());
		app_size.setText(item.getSize());
		app_down.setText(item.getDownloadTime()+"次下载");
		//从SD卡中寻找有没有
		Bitmap bitmap=SD_Cache.getCachedImage(item.getImageUrl());
		if(null!=bitmap){
			icon.setImageBitmap(bitmap);
		}
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if(event.getKeyCode()==KeyEvent.KEYCODE_BACK){
			this.finish();//结束
		}
		return super.onKeyDown(keyCode, event);
	}
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		Toast.makeText ( this ,"客官轻点",0).show(); 
	}
	
	
}
