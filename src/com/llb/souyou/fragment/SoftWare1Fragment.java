package com.llb.souyou.fragment;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.http.HttpEntity;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ViewSwitcher.ViewFactory;

import com.llb.souyou.ContentActivity;
import com.llb.souyou.R;
import com.llb.souyou.adapter.Software1ListViewAdapter;
import com.llb.souyou.app.Constant;
import com.llb.souyou.app.MyHttpClient;
import com.llb.souyou.bean.SoftwareItem1Bean;
import com.llb.souyou.cache.SD_Cache;
import com.llb.souyou.util.AppUtil;
import com.llb.souyou.util.GetIconImage;
import com.llb.souyou.util.JsonDecodeUtil;
import com.llb.souyou.util.MyImageSwitcher;
import com.llb.souyou.util.Rotate3D;

public class SoftWare1Fragment extends Fragment implements OnClickListener,OnItemClickListener,OnScrollListener{
	private View view=null;
	private ArrayList<SoftwareItem1Bean> list;
	private ArrayList<SoftwareItem1Bean> freshData=new ArrayList<SoftwareItem1Bean>(20);
	private ListView listView;
	private Software1ListViewAdapter adapter;
	private boolean refreshable=true;//底部刷新
	private boolean firstLoad=true;//初次加载数据
	private Button bt_loadmore;
	private static ExecutorService LIMITED_TASK_EXECUTOR;//线程池，在3.0以后AsyncTask有了不少新特性
	private static Future future=null;//用来保证加载更多操作时刻有线程可用
	private ArrayList<String> imageURLs=new ArrayList<String>(16);
	
	private MyImageSwitcher imageSwitcher;
	private float halfWidth,halfHeight;//imageswitcher的一半尺寸
	private long duration=600;//旋转动画持续时间
	private int index=0;//轮播到第几张图片
	private GestureDetector gestureDetector;//手势监听
	private ImageAsynTask imageAsynTask;
	
	//交互实现
	private LinearLayout detail;//详情页
	private LinearLayout download;//下载按钮
	
	private int[] imageList={R.drawable.a,R.drawable.b,R.drawable.c,R.drawable.d};//ArrayList<String> imageList;//存储轮播图片url
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		list=new ArrayList<SoftwareItem1Bean>(32);
		//最忙的时候：轮播+2条图标+新数据
		LIMITED_TASK_EXECUTOR=Executors.newCachedThreadPool();//按需新建线程会好点吧 
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if(view==null){
			view=inflater.inflate(R.layout.software1_fragment,null);
			listView=(ListView) view.findViewById(R.id.lv_software1);
			bt_loadmore=(Button)view.findViewById(R.id.bt_loadmore);
			detail=(LinearLayout) view.findViewById(R.id.linear);
			download=(LinearLayout) view.findViewById(R.id.linearright);
			initList();
			
			imageSwitcher=(MyImageSwitcher) view.findViewById(R.id.imageswitcher);
			imageSwitcher.setViewPager((ViewPager)getActivity().findViewById(R.id.pager));
//			halfHeight=imageSwitcher.getHeight()/2.0f;
//			halfWidth=imageSwitcher.getWidth()/2.0f;
//			Log.i("llb","尺寸"+halfWidth+"*"+halfHeight);
			gestureDetector=new GestureDetector(view.getContext(), new MyOnGestureListener());
			imageSwitcher.setFactory(new ViewFactory() {
				@Override
				public View makeView() {
					//生成一个显示页面，一张图片，要是复杂的可以利用xml设计
					ImageView imageView=new ImageView(view.getContext());
//					imageView.setBackgroundColor(color);
					imageView.setScaleType(ImageView.ScaleType.FIT_XY);
					imageView.setLayoutParams(new ImageSwitcher.LayoutParams(
							LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
					return imageView;
				}
			});
//			imageAsynTask=new ImageAsynTask();
//			imageAsynTask.execute(index);
		}
		ViewGroup parent = (ViewGroup) view.getParent();
		if(parent!=null){
			parent.removeView(view);//先移除
		}
		
		imageSwitcher.setImageResource(imageList[index]);
//		imageSwitcher.setImageURI(uri);
		imageSwitcher.setTag("imageswitcher1");
		imageSwitcher.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				halfHeight=imageSwitcher.getHeight()/2.0f;
				halfWidth=imageSwitcher.getWidth()/2.0f;
//				Log.i("llb","尺寸"+halfWidth+"*"+halfHeight);
				Log.i("llb","imageswitcher检测到了事件");
				boolean a=gestureDetector.onTouchEvent(event);
				Log.i("llb","a="+a);
				return true;
			}
		});
		
		imageAsynTask=new ImageAsynTask();
//		imageAsynTask.execute(index);
		imageAsynTask.executeOnExecutor(LIMITED_TASK_EXECUTOR,index);
		
		
		adapter=new Software1ListViewAdapter(view.getContext(),list);
		bt_loadmore.setOnClickListener(this);

		listView.setAdapter(adapter);
		listView.setOnScrollListener(this);
		listView.setOnItemClickListener(this);
		
//		detail.setOnClickListener(this);
//		download.setOnClickListener(this);
		
		
		return view;
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.bt_loadmore:
			Log.i("llb","loadmore被点击");
			int totalItem=listView.getChildCount();
			if(future!=null){
				Log.i("llb", "future.isDone()"+future.isDone());
				if(!future.isDone()){
					future.cancel(true);//如果这个图标下载线程还没有结束，直接取消执行
					Log.i("llb", "future.isDone()"+future.isDone());
				}
			}
			loadData(String.valueOf(list.size()),list.get(list.size()-1).getDownloadTime());
			Log.i("llb","最后一个是："+totalItem+" "+list.get(list.size()-1).getTitle());
			break;
		}
	}
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
//		Toast.makeText(getActivity(), "详情还么有实现哟", 0).show();
		Intent intent=new Intent();
		Bundle bundle=new Bundle();
		bundle.putSerializable("item", list.get(position));//把当前的信息传递过去
		intent.putExtras(bundle);
		intent.putExtra("src", "1");//从榜单来的
		intent.setClass(this.getActivity(), ContentActivity.class);
		startActivity(intent);
	}
	private void  initList(){
		//首先请求网络得到最新的数据
		//先查缓存有没有数据
		Object object=SD_Cache.getCacheData(Constant.TOP_LIST_FILENAME);
		if(object==null){
			list.clear();
			Log.i("llb","缓存没数据,此时list.size="+list.size());
			loadData("0","-1");//请求数据
		}else {
			list=(ArrayList<SoftwareItem1Bean>)object;//目前的逻辑是有问题的，内容无法更新，另外加个刷新按钮吧
			Log.i("llb","缓存有数据,此时list.size="+list.size());
			loadData("0", list.get(0).getDownloadTime());//这种如何区别底部加载
		}
	}
	
	
	/**
	 * 监听手指滑动顶部轮播图的动作
	 * @author llb
	 *
	 */
	private class MyOnGestureListener implements GestureDetector.OnGestureListener{
		@Override
		public boolean onDown(MotionEvent e) {
			Log.i("llb","imageswitcher onDown");
			return true;
		}
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			if(velocityX>0){//x正方向有速度 即向右滑
				Log.i("llb","velocityX>0");
				Rotate3D rotateIn=new Rotate3D(-90, 0, halfWidth, halfHeight);//新图进来 从-90°到0°，
				rotateIn.setDuration(duration);    
				rotateIn.setFillAfter(true);//运动保持
		        imageSwitcher.setInAnimation(rotateIn);   
		        Rotate3D rotateOut = new Rotate3D(0,90,halfWidth,halfHeight);//旧图退出去
		        rotateOut.setDuration(duration);    
		        rotateOut.setFillAfter(true);
		        imageSwitcher.setOutAnimation(rotateOut); 
		        
		        index-=1;//序号降一
		        index=(index==-1)?(imageList.length-1):index;//只有等于0时需要调整
		        
		        imageSwitcher.setImageResource(imageList[index]);//设置新图片
			}else if(velocityX<0){//手指左滑，沿X轴负向
				Rotate3D rotateIn=new Rotate3D(90, 0, halfWidth, halfHeight);//新图进来
				rotateIn.setDuration(duration);    
				rotateIn.setFillAfter(true);
		        imageSwitcher.setInAnimation(rotateIn);   
		        Rotate3D rotateOut = new Rotate3D(0,-90,halfWidth,halfHeight);//旧图出去
		        rotateOut.setDuration(duration);    
		        rotateOut.setFillAfter(true);
		        imageSwitcher.setOutAnimation(rotateOut); 
		        
		        index+=1;//序号加一
		        index=(index==imageList.length)?0:index;//只有等于0时需要调整
		        
		        imageSwitcher.setImageResource(imageList[index]);//设置新图片
			}
			Log.i("Llb","super.onFling()");
			return true;
		}
		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			Log.i("llb","imageswitcher onSingleTapUp=");
			return true;
		}
		@Override
		public void onShowPress(MotionEvent e) {
			// TODO Auto-generated method stub
			Log.i("llb","imageswitcher onShowPress");
		}
		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			Log.i("llb","imageswitcher onScroll");
			return true;
		}
		@Override
		public void onLongPress(MotionEvent e) {
			// TODO Auto-generated method stub
			Log.i("llb","imageswitcher onLongPress");
		}
	}
	/**
	 * 利用异步来实现图片的不停翻转
	 * @author llb
	 *
	 */
	class ImageAsynTask extends AsyncTask{
		@Override
		protected Object doInBackground(Object... params) {
			// TODO Auto-generated method stub
//			Log.i("llb","输入的数据："+params[0]);
			int index=(Integer)params[0];
			for(int i=0;i<500;i++){
				try {
				Thread.sleep(5000);
//				index=(index==0)?3:(index-1);//逆序
				publishProgress(index);//反馈
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			return null;
		}
		@Override
		protected void onProgressUpdate(Object... values) {
			super.onProgressUpdate(values);
//			Log.i("llb","得到的数据："+values[0]);
			halfHeight=imageSwitcher.getHeight()/2.0f;
			halfWidth=imageSwitcher.getWidth()/2.0f;
			
			Rotate3D rotateIn=new Rotate3D(-90, 0, halfWidth, halfHeight);//新图进来 从-90°到0°，
			rotateIn.setDuration(duration);    
			rotateIn.setFillAfter(true);//运动保持
	        imageSwitcher.setInAnimation(rotateIn);   
	        Rotate3D rotateOut = new Rotate3D(0,90,halfWidth,halfHeight);//旧图退出去
	        rotateOut.setDuration(duration);    
	        rotateOut.setFillAfter(true);
	        imageSwitcher.setOutAnimation(rotateOut); 
	        
	        index=(index==0)?3:(index-1);//在外面更改序号更大程度上可以避免冲突
			imageSwitcher.setImageResource(imageList[index]);//imageList[(Integer)values[0]]);
	        
		}
	}
	/**
	 * AsyncTask的代码简洁，但是相较于Handler+Thread的方式，更耗资源，灵活度稍差。一般在数据量不大的情况下使用
	 * 这个程序中，所有关于列表刷新的部分都采用AsyncTask，而内容详情相关的操作采用Handler+Thread
	 * @author llb
	 *
	 */
	class ListAsynctask extends AsyncTask<BasicNameValuePair, Void, String>{
		public  ArrayList<SoftwareItem1Bean> listReturn;//用来返回取得的列表数据
		@Override
		protected String doInBackground(BasicNameValuePair... params) {
			//postByHttpClient(String url,NameValuePair...pairs)
			Log.i("llb", "我在doInBackground");
			HttpEntity entity=null;
			try {
				entity = MyHttpClient.getByHttpClient(params[0].getValue(),params[1],params[2],params[3]);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				Log.i("llb", "doinBackground出错了哟");
				e1.printStackTrace();
				return null;
			}
			//get方法请求数据
			String result="";
			try {
				result = AppUtil.entityToJsonString(entity);
				Log.i("llb", "result="+result);
			} catch (Exception e) {
				e.printStackTrace();
				Log.i("zgr","doInBackgroup wrong!");
				return null;
			}
			return result;
		}
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			Log.i("llb", "我在onpostExecute");
//			refreshable=true;//可以底部刷新
			 bt_loadmore.setVisibility(View.GONE);//让底部按钮不可见
			int code=0;//返回的操作功能号
			if(null!=result && result!=""){
				try {
					JSONObject jsonObject=new JSONObject(result);//先把返回的数据转成一个json对象，方便解析
					JsonDecodeUtil jsonDecodeUtil=new JsonDecodeUtil();
					code=Integer.parseInt(jsonObject.getString("code"));
					switch (code) {
					case 100://列表信息请求成功
						listReturn=jsonDecodeUtil.decodeListJson(jsonObject);//解析里面的result数据
						Log.i("zgr","onPostExecute="+result);
						freshData.clear();//先清空之前的数据
						freshData=listReturn;//把得到的数据传进来
						refreshListView(freshData,code);//刷新页面
						imageURLs.clear();//先清除之前的数据
						for(SoftwareItem1Bean item:freshData){
							imageURLs.add(item.getImageUrl());//获取所有请求得到的item的图标链接
						}
						//开启线程去加载应用图标图片
						if(imageURLs.size()>5){
							Log.i("llb","要开始两个线程加快加载速度"+imageURLs.size());
							int mid=(int) (imageURLs.size()*0.5);
							future=LIMITED_TASK_EXECUTOR.submit(new Thread(new GetIconImage(
									new ArrayList<String>(imageURLs.subList(0, mid)))));
//							LIMITED_TASK_EXECUTOR.execute(new Thread(new GetIconImage(
//									new ArrayList<String>(imageURLs.subList(0, mid)))));
							LIMITED_TASK_EXECUTOR.execute(new Thread(new GetIconImage(
									new ArrayList<String>(imageURLs.subList(mid, imageURLs.size())))));
						} else {
							LIMITED_TASK_EXECUTOR.execute(new Thread(new GetIconImage(imageURLs)));
						}
						break;
					case 0://表明取得数据
//						Toast.makeText(ActivityFragment, "没有新数据", 0).show();//???????
						Toast.makeText(getActivity(), "没有更多数据了", 0).show();//???????
						bt_loadmore.setVisibility(View.GONE);//隐藏加载更多
						freshData.clear();//清空，表面没有新数据
						refreshListView(freshData, code);
						break;
					}
				} catch (Exception e) {
					e.printStackTrace();
					freshData.clear();//清空，表面没有新数据
					refreshListView(freshData, code);
				}
			}else {
				Log.i("zgr","应该是网络之类的问题，待处理");
				refreshListView(freshData,code);//刷新页面
				//应该是网络异常之类的，不然至少有功能好，不会为空
			}
		}
	}
	private void refreshListView(ArrayList<SoftwareItem1Bean> newData,int code){
		switch (code) {
		case 100://把数据挂在最前面
			if(firstLoad){//初次加载数据而且
				if(newData.size()>15){//新数据大于15
					list.clear();//清除缓存数据
					list=newData;
				}else {
					list.addAll(0, newData);
				}
			}else {
				list.addAll(newData);
			}
			break;
		case 0://没渠道数据
			break;
		}
		Log.i("llb", "新数据一共有"+newData.size()+"合并后一共有"+list.size());
		firstLoad=false;
		adapter.notifyDataSetChanged();
	}
	/**
	 * 下载数据
	 * @param item 当前最大item的位置
	 * @param app_down 当前item的下载量
	 */
	public void loadData(String item,String app_down){
		BasicNameValuePair pair1=new BasicNameValuePair("url",Constant.BASE_URL);
		BasicNameValuePair pair2=new BasicNameValuePair("type","100");//榜单列表刷新
		BasicNameValuePair pair3=new BasicNameValuePair("item",item);//当前最大item的位置
		BasicNameValuePair pair4=new BasicNameValuePair("down",app_down);
//		new ListAsynctask().execute(pair1,pair2,pair3);
		new ListAsynctask().executeOnExecutor(LIMITED_TASK_EXECUTOR, pair1,pair2,pair3,pair4);
	}
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		// TODO Auto-generated method stub
		if( firstVisibleItem+visibleItemCount>=totalItemCount && totalItemCount>0 ){
//			Log.i("llb", "totalItemCount="+totalItemCount);
//			 if (refreshable) {  
//				 refreshable = false;//正在加载中，失能  
//				 loading.setVisibility(View.VISIBLE);
//				 String item=String.valueOf(totalItemCount);
				 //loadData(item);//请求数据
				 bt_loadmore.setVisibility(View.VISIBLE);//让底部按钮可见
//			 }
		}else {
			bt_loadmore.setVisibility(View.GONE);//让底部按钮可见
		}
	}
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// TODO Auto-generated method stub
	}
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		//榜单列表缓存名称为：toplist 退出程序前缓存数据
		Log.i("llb", "list.size()"+list.size());
		if (list.size()>60) {//切断缓存数据的长度list.subList(0, 60)返回值没有实现序列化接口
			SD_Cache.saveCacheData(new ArrayList<SoftwareItem1Bean>(list.subList(0, 60)), Constant.TOP_LIST_FILENAME);
		}else if(list.size()==0){//没数据的直接删除缓存文件
			Log.i("llb", "清空缓存文件");
			SD_Cache.clearCacheData(Constant.TOP_LIST_FILENAME);
		}else {
			SD_Cache.saveCacheData(list, Constant.TOP_LIST_FILENAME);
		}
//		imageAsynTask.cancel(true);
		LIMITED_TASK_EXECUTOR.shutdownNow();
		super.onDestroy();
	}
	
}
