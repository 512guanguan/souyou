package com.llb.souyou.fragment;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.HttpEntity;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.llb.souyou.ContentActivity;
import com.llb.souyou.R;
import com.llb.souyou.adapter.Software3ListViewAdapter;
import com.llb.souyou.app.Constant;
import com.llb.souyou.app.MyHttpClient;
import com.llb.souyou.bean.SoftwareItem1Bean;
import com.llb.souyou.cache.SD_Cache;
import com.llb.souyou.util.AppUtil;
import com.llb.souyou.util.GetIconImage;
import com.llb.souyou.util.JsonDecodeUtil;

public class SoftWare3Fragment extends Fragment implements OnScrollListener,OnClickListener,OnItemClickListener{
	private View view=null;
	private ArrayList<SoftwareItem1Bean> list;
	private ListView listView;
	private Software3ListViewAdapter adapter;
	private Button bt_loadmore;
	private static ExecutorService LIMITED_TASK_EXECUTOR;//线程池
	private ArrayList<SoftwareItem1Bean> freshData=new ArrayList<SoftwareItem1Bean>(20);
	private ArrayList<String> imageURLs=new ArrayList<String>(16);//用来放请求得到的图片信息
	private boolean firstLoad=true;//第一次刷新，数据加载缓存数据前面
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		list=new ArrayList<SoftwareItem1Bean>(32);
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if(view==null){
			view=inflater.inflate(R.layout.software3_fragment,null);
			listView=(ListView) view.findViewById(R.id.lv_software3);
			bt_loadmore=(Button) view.findViewById(R.id.bt_loadmore);
			//最忙的时候：2条图标+1数据
			LIMITED_TASK_EXECUTOR=Executors.newCachedThreadPool();//线程池
			initList();
		}
		ViewGroup parent = (ViewGroup) view.getParent();
		if(parent!=null){
			parent.removeView(view);//先移除
		}
		
		adapter=new Software3ListViewAdapter(view.getContext(),list);
		listView.setAdapter(adapter);
		listView.setOnScrollListener(this);
		listView.setOnItemClickListener(this);
		bt_loadmore.setOnClickListener(this);
		
		return view;
	}
	private void  initList(){
		//先取得缓存数据，看最新的是多少
		Object object=SD_Cache.getCacheData(Constant.NEW_LIST_FILENAME);
		Log.i("llb", "object="+String.valueOf(object));
		if (object==null) {//这个判断条件无效[null,null,null,...]
			loadData("0","-1");//把最新的id传过去
		}else {
			list=(ArrayList<SoftwareItem1Bean>)object;
			loadData("0",list.get(0).getId());//把最新的id传过去
		}
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.bt_loadmore:
			Log.i("llb","loadmore被点击");
			loadData(String.valueOf(list.size()),list.get(list.size()-1).getId());
			break;

		default:
			break;
		}
	}
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		Log.i("llb","点击了");
		Intent intent=new Intent();
		Bundle bundle=new Bundle();
		bundle.putSerializable("item", list.get(position));//把当前的信息传递过去
		intent.putExtras(bundle);
		intent.putExtra("src", "3");//从最新应用来的
		intent.setClass(this.getActivity(), ContentActivity.class);
		startActivity(intent);
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
			Log.i("llb", "我在onpaostExecute");
//			refreshable=true;//可以底部刷新
			 bt_loadmore.setVisibility(View.GONE);//让底部按钮可见
			int code=0;//返回的操作功能号
			if(null!=result && result!=""){
				try {
					JSONObject jsonObject=new JSONObject(result);//先把返回的数据转成一个json对象，方便解析
					JsonDecodeUtil jsonDecodeUtil=new JsonDecodeUtil();
					code=Integer.parseInt(jsonObject.getString("code"));
					switch (code) {
					case 300://列表信息请求成功
						listReturn=jsonDecodeUtil.decodeNewListJson(jsonObject);//解析里面的result数据
						Log.i("zgr","onPostExecute="+result);
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
							LIMITED_TASK_EXECUTOR.execute(new Thread(new GetIconImage(
									new ArrayList<String>(imageURLs.subList(0, mid)))));
							LIMITED_TASK_EXECUTOR.execute(new Thread(new GetIconImage(
									new ArrayList<String>(imageURLs.subList(mid, imageURLs.size())))));
						} else {
							LIMITED_TASK_EXECUTOR.execute(new Thread(new GetIconImage(imageURLs)));
						}
						break;
					case 0://表明取得数据
						Toast.makeText(getActivity(), "没有更多数据了", 0).show();//???????
						bt_loadmore.setVisibility(View.GONE);//隐藏加载更多
						Log.i("zgr","返回0 没有数据");
						freshData.clear();
						refreshListView(freshData,code);//刷新页面
						break;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}else {
				Log.i("zgr","应该是网络之类的问题，待处理");
//				list=(ArrayList<SoftwareItem3Bean>)SD_Cache.getCacheData(Constant.LIST_BASE_PATH);
				refreshListView(freshData,code);//刷新页面
				//应该是网络异常之类的，不然至少有功能好，不会为空
			}
		}
	}
	private void refreshListView(ArrayList<SoftwareItem1Bean> newData,int code){
		switch (code) {
		case 300://把数据挂在最前面
			if(firstLoad && newData!=null){
				if (newData.size()>20) {
					list.clear();//如果新数据超过20条，去掉缓存数据
				}
				list.addAll(0, newData);//第一次加载放缓存数据之前
			}else {
				list.addAll(newData);
			}
			break;
		case 0://没渠道数据
			break;
		}
		firstLoad=false;
		adapter.notifyDataSetChanged();
	}
	/**
	 * 下载数据
	 * @param item 当前最大item的位置
	 * @param id 当前最小的id，底部加载
	 */
	public void loadData(String item,String id){
		BasicNameValuePair pair1=new BasicNameValuePair("url",Constant.BASE_URL);
		BasicNameValuePair pair2=new BasicNameValuePair("type","300");//榜单列表刷新
		BasicNameValuePair pair3=new BasicNameValuePair("item",item);//当前最新item的id 越新的条目id会越大
		BasicNameValuePair pair4=new BasicNameValuePair("id",id);
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
		//榜单列表缓存名称为：newlist 退出程序前缓存数据
		
		if (list.size()>60) {//切断缓存数据的长度
			SD_Cache.saveCacheData(new ArrayList<SoftwareItem1Bean>(list.subList(0, 60)), Constant.NEW_LIST_FILENAME);
		}else if(list.size()==0){//没数据的直接删除缓存文件
			Log.i("llb", "清空缓存文件");
			SD_Cache.clearCacheData(Constant.NEW_LIST_FILENAME);
		}else {
			SD_Cache.saveCacheData(list, Constant.NEW_LIST_FILENAME);
		}
		LIMITED_TASK_EXECUTOR.shutdownNow();
		super.onDestroy();
	}
}
