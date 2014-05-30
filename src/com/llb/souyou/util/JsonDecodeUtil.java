package com.llb.souyou.util;

import java.util.ArrayList;
import java.util.IdentityHashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.llb.souyou.bean.CategoryBean;
import com.llb.souyou.bean.SoftwareItem1Bean;

/**
 * 主要用来解析服务器接收的json数据
 * @author llb
 *
 */
public class JsonDecodeUtil {
	/**
	 * 榜单数据的列表解析
	 * 取出json里面的result部分内容，封装在一个ArrayList里面
	 * @param response 待解析的json数据
	 * @return ArrayList<ActivityMainItemBean>
	 * @throws JSONException
	 */
	public ArrayList<SoftwareItem1Bean> decodeListJson(JSONObject response) throws JSONException{
		JSONArray messageJsonArray=response.getJSONArray("message");//返回的列表信息
		JSONObject itemJsonObject;
		ArrayList<SoftwareItem1Bean> message = new ArrayList<SoftwareItem1Bean>();//用来存储result信息
		SoftwareItem1Bean itemBean;//存储每一个item信息
		for(int index=0;index<messageJsonArray.length();index++){//逐个解析jsonarray里面的item对象
			itemJsonObject=messageJsonArray.getJSONObject(index);
			itemBean = new SoftwareItem1Bean(
					itemJsonObject.getString("app_id"),
					itemJsonObject.getString("app_logo"),
					itemJsonObject.getString("app_title"),
					Float.parseFloat(itemJsonObject.getString("app_recomment")),
					itemJsonObject.getString("app_down"), itemJsonObject
							.getString("app_size"), itemJsonObject
							.getString("substring(app_desc,14,50)"),itemJsonObject.getString("data_app_id"));
			message.add(itemBean);//
		}
		return message;
	}
	/**
	 * 最新应用的列表数据解析
	 * 取出json里面的result部分内容，封装在一个ArrayList里面
	 * @param response 待解析的json数据
	 * @return ArrayList<ActivityMainItemBean>
	 * @throws JSONException
	 */
	public ArrayList<SoftwareItem1Bean> decodeNewListJson(JSONObject response) throws JSONException{
		JSONArray messageJsonArray=response.getJSONArray("message");//返回的列表信息
		JSONObject itemJsonObject;
		ArrayList<SoftwareItem1Bean> message = new ArrayList<SoftwareItem1Bean>();//用来存储result信息
		SoftwareItem1Bean itemBean;//存储每一个item信息
		for(int index=0;index<messageJsonArray.length();index++){//逐个解析jsonarray里面的item对象
			itemJsonObject=messageJsonArray.getJSONObject(index);
			itemBean = new SoftwareItem1Bean(
					itemJsonObject.getString("app_id"),
					itemJsonObject.getString("app_logo"),
					itemJsonObject.getString("app_title"),
					Float.parseFloat(itemJsonObject.getString("app_recomment")),
					itemJsonObject.getString("app_down"), itemJsonObject
							.getString("app_size"),itemJsonObject.getString("data_app_id"));
			message.add(itemBean);//
		}
		return message;
	}
	/**
	 * 应用详情的数据解析
	 * 取出json里面的result部分内容，封装在一个ArrayList<Map<String,String>>里面
	 * @param response 待解析的json数据
	 * @return ArrayList<ActivityMainItemBean>
	 * @throws JSONException
	 */
	public IdentityHashMap<String, String> decodeDetailJson(JSONObject response) throws JSONException{
		JSONArray messageJsonArray=response.getJSONArray("message");//返回的列表信息
		JSONObject itemJsonObject;
		IdentityHashMap<String, String> hashMap = new IdentityHashMap<String, String>();//用来存储result信息
		for(int index=0;index<messageJsonArray.length();index++){//逐个解析jsonarray里面的item对象
			itemJsonObject=messageJsonArray.getJSONObject(index);
			if(index==0){
				hashMap.put("app_desc", itemJsonObject.getString("app_desc"));
			}else {
				String key=new String("resource_url");
				hashMap.put(key, itemJsonObject.getString("resource_url"));
				//下面这种是不行的，不会新建一个键值，覆盖了
//				hashMap.put("resource_url", itemJsonObject.getString("resource_url"));
			}
		}
		return hashMap;
	}
	/**
	 * 解析包含Category分类的json
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ArrayList<CategoryBean> decodeCategoryJson(JSONObject response)throws Exception{
		JSONArray messageJsonArray=response.getJSONArray("message");//返回的列表信息
		JSONObject itemJsonObject;
		ArrayList<CategoryBean> message = new ArrayList<CategoryBean>();//用来存储result信息
		CategoryBean itemBean;//存储每一个item信息
		for(int index=0;index<messageJsonArray.length();index++){//逐个解析jsonarray里面的item对象
			itemJsonObject=messageJsonArray.getJSONObject(index);
			itemBean = new CategoryBean(
					Integer.parseInt(itemJsonObject.getString("cate_id")),
					Integer.parseInt(itemJsonObject.getString("parent_id")),
					itemJsonObject.getString("cname"),
					itemJsonObject.getString("cdesc"));
			message.add(itemBean);//
		}
		return message;
	}
	/**
	 * 某一分类中最新应用的列表数据解析
	 * 取出json里面的result部分内容，封装在一个ArrayList里面
	 * @param response 待解析的json数据
	 * @return ArrayList<ActivityMainItemBean>
	 * @throws JSONException
	 */
//	public ArrayList<SoftwareItem1Bean> decodeCateInfoJson(JSONObject response) throws JSONException{
//		JSONArray messageJsonArray=response.getJSONArray("message");//返回的列表信息
//		JSONObject itemJsonObject;
//		ArrayList<SoftwareItem1Bean> message = new ArrayList<SoftwareItem1Bean>();//用来存储result信息
//		SoftwareItem1Bean itemBean;//存储每一个item信息
//		for(int index=0;index<messageJsonArray.length();index++){//逐个解析jsonarray里面的item对象
//			itemJsonObject=messageJsonArray.getJSONObject(index);
//			itemBean = new SoftwareItem1Bean(
//					itemJsonObject.getString("app_id"),
//					itemJsonObject.getString("app_logo"),
//					itemJsonObject.getString("app_title"),
//					Float.parseFloat(itemJsonObject.getString("app_recomment")),
//					itemJsonObject.getString("app_down"), itemJsonObject
//							.getString("app_size"),itemJsonObject.getString("data_app_id"));
//			message.add(itemBean);//
//		}
//		return message;
//	}
}
