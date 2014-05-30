package com.llb.souyou.fragment;

import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.Toast;

import com.llb.souyou.CateInfoActivity;
import com.llb.souyou.R;
import com.llb.souyou.adapter.CategoryGridViewAdapter;
import com.llb.souyou.app.Constant;
import com.llb.souyou.app.MyHttpClient;
import com.llb.souyou.bean.CategoryBean;
import com.llb.souyou.db.DBHelper;
import com.llb.souyou.util.AppUtil;
import com.llb.souyou.util.JsonDecodeUtil;

public class SoftWare2Fragment extends Fragment implements OnItemClickListener{
	private View view=null;
	private ArrayList<CategoryBean> list=new ArrayList<CategoryBean>(20);
	private CategoryGridViewAdapter adapter;
	private GridView gridView;
	private DBHelper helper; 
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		list=new ArrayList<CategoryBean>();
		helper =new DBHelper(this.getActivity());
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if(view==null){
			view=inflater.inflate(R.layout.webapp2_fragment,null);
			gridView=(GridView) view.findViewById(R.id.gridview);
			initList();
			adapter=new CategoryGridViewAdapter(view.getContext(), list);
		}
		ViewGroup parent = (ViewGroup) view.getParent();
		if(parent!=null){
			parent.removeView(view);//先移除
		}
		
		gridView.setAdapter(adapter);
		gridView.setOnItemClickListener(this);
		return view;
	}
	private void initList() {
		// TODO Auto-generated method stub
//		for(int i=0;i<8;i++){
//			list.add(new CategoryBean("图片url","游戏", "暗黑世界、神魔仙界、植物大战僵尸", 0,1));
//		}
		Cursor cursor=helper.rawQuery("select * from category;", null);//查询数据库
		//数据库缓存超时时间是2小时
		long time=0;
		if(cursor.getCount()>0){
			cursor.moveToFirst();
			time=System.currentTimeMillis()-cursor.getLong(6);
			Log.i("llb", "time="+System.currentTimeMillis()+"  db_time="+cursor.getLong(6));
		}else {
			time=3600000*3;//总之是不能进入下一个if
		}
		boolean hasnext=true;
		Log.i("llb","分类缓存数据库未过期");
		while(hasnext){
			list.add(new CategoryBean(cursor.getInt(1), cursor.getInt(2), cursor.getString(3), cursor.getString(5)));
			hasnext=cursor.moveToNext();
		}
		cursor.close();
		helper.db.close();
		if(time>(3600000*2)){//说明分类缓存已经过期了，重新请求网络
			Log.i("llb", "分类要请求网络");
			BasicNameValuePair pair0=new BasicNameValuePair("url", Constant.BASE_URL);
			BasicNameValuePair pair=new BasicNameValuePair("type", "200");
			new CategoryAsync().execute(pair0,pair);
		}
     }
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		Toast.makeText(this.getActivity(), "客官轻点，还没准备好", 1).show();
		Intent intent=new Intent(this.getActivity(),CateInfoActivity.class);
		intent.putExtra("cate_id",list.get(position).getType());//传过去分类id号
		intent.putExtra("title",list.get(position).getTitle());//传递分类名称
		startActivity(intent);
	}
	class CategoryAsync extends AsyncTask<NameValuePair, Void,String>{
		@Override
		protected String doInBackground(NameValuePair... params) {
			// TODO Auto-generated method stub
			HttpEntity entity=null;
			try {
				Log.i("llb","我在中间的doinbackground");
				entity = MyHttpClient.getByHttpClient(params[0].getValue(),params[1]);
			} catch (Exception e1) {
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
				Log.i("zgr","category doInBackgroup wrong!");
				return null;
			}
			return result;
		}
		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			if(result!="" && result!=null){
				try {
					JSONObject jsonObject=new JSONObject(result);
					JsonDecodeUtil jsonDecodeUtil=new JsonDecodeUtil();
					int code=Integer.parseInt(jsonObject.getString("code"));
					switch (code) {
					case 200://列表信息请求成功
						try {
							list.clear();//有了新数据再把旧的清除
							list.addAll(jsonDecodeUtil.decodeCategoryJson(jsonObject));
							Log.i("llb","list.tostring="+list.toString());
							adapter.notifyDataSetChanged();
							//保存数据到数据库
							long time=System.currentTimeMillis();
							ContentValues values = new ContentValues();  
							for(int i=0;i<list.size();i++){
								values.put("id", i); 
								values.put("cate_id", list.get(i).getType()); 
						        values.put("parent_id", list.get(i).getParent_id());
						        values.put("title", list.get(i).getTitle());
						        values.put("imageurl", list.get(i).getImageUrl());  
						        values.put("desc", list.get(i).getDesc()); 
						        values.put("time",time); //插入当前时间
//								helper.update("category", values, "cate_id=?",
//										new String[] {String.valueOf(bean.getType()) });
						        helper.replace("category", values);
								values.clear();
							}
							helper.db.close();
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}//解析里面的result数据
						break;
					case 0://没有数据
						adapter.notifyDataSetChanged();
						break;
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					adapter.notifyDataSetChanged();
				}
			}else {
				adapter.notifyDataSetChanged();
				Log.i("llb","没有取到任何数据");
			}
			
		}
	}
}
