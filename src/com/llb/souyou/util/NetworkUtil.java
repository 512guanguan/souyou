package com.llb.souyou.util;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;

import android.graphics.drawable.BitmapDrawable;
import android.util.Log;

import com.llb.souyou.app.Constant;
import com.llb.souyou.cache.SD_Cache;


public class NetworkUtil {
	/**
	 * 拼装生成想要的get请求方式的turl
	 * @param pairs
	 * @return String
	 */
	public static String createURL(NameValuePair... pairs){
		//拼接访问URL
		StringBuilder sBuilder=new StringBuilder();
		sBuilder.append(Constant.BASE_URL);
		if(null!=pairs && pairs.length>0){
			sBuilder.append("?");
			for(int i=0;i<pairs.length;i++){
				if(i>0){
					sBuilder.append("&");
				}
				sBuilder.append(String.format("%s=%s",pairs[i].getName(),pairs[i].getValue()));//拼参数
			}
		}
		return sBuilder.toString();
	}
	/**
	 * 检查是否有普通数据的缓存
	 * @param cacheDir 缓存文件夹
	 * @param url 文件名是md5(url)
	 * @return String 缓存的字符串
	 */
	public static String checkCacheData(String cacheDir,String url){
		//首先先在缓存里面寻找看是否存在文件
		String jsonString=(String) SD_Cache.getCacheData(cacheDir+AppUtil.md5(url));
		if(jsonString!=null){
			Log.i("llb","缓存数据获取成功");
			return jsonString;
		}
		return null;
	}
	/**
	 * 详情内容请求
	 * 传递一系列NameValuePair进来，封装成url采用get方式取回数据
	 * @param pairs NameValuePair
	 * @return HttpEntity
	 */
	public static String getHttpEntity(String cacheDir,String url){
//		//拼接访问URL
//		StringBuilder sBuilder=new StringBuilder();
//		sBuilder.append(Constant.BASE_URL);
//		if(null!=pairs && pairs.length>0){
//			sBuilder.append("?");
//			for(int i=0;i<pairs.length;i++){
//				if(i>0){
//					sBuilder.append("&");
//				}
//				sBuilder.append(String.format("%s=%s",pairs[i].getName(),pairs[i].getValue()));//拼参数
//			}
//		}
//		String url=sBuilder.toString();
		//存储得到json格式String
		//首先先在缓存里面寻找看是否存在文件
//		String jsonString=(String) SD_Cache.getCacheData(cacheDir+AppUtil.md5(url));
//		if(jsonString!=null){
//			Log.i("llb","缓存数据获取成功");
//			return jsonString;
//		}
		//没有缓存那么就请求网络
		HttpClient httpClient=new DefaultHttpClient();
		HttpUriRequest request=new HttpGet(url);
//		Log.i("llb","url="+sBuilder.toString());
		try {
			HttpResponse response=httpClient.execute(request);
			if (response.getStatusLine().getStatusCode()!=HttpStatus.SC_OK) {
				throw new RuntimeException("数据请求失败");
			}else {
				HttpEntity entity=response.getEntity();
				//先把entity转成json格式字符串，方便序列化缓存
				String jsonString = AppUtil.entityToJsonString(entity);
				//先缓存数据
//				SD_Cache.saveCacheDetailData(jsonString,cacheDir+AppUtil.md5(url));
				return jsonString;
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * get the Drawable form URL
	 * 暂时主要指的就是请求详情页里面的图片，一般都是大图
	 * @param url
	 * @author llb
	 * @return Drawable
	 */
	public static BitmapDrawable getDrawable(String url){
		Log.i("llb", "图片地址url="+url);
		try {
			//SD缓存没有那么开始请求网络
//			InputStream inputStream=(InputStream) new URL(url).getContent();//或者是用httpclient
			InputStream inputStream=new DefaultHttpClient().execute(new HttpGet(url)).getEntity().getContent();
			BitmapDrawable bitmapDrawable=(BitmapDrawable) BitmapDrawable.createFromStream(inputStream, "图名");
			//得到原图片后先在SD卡里面缓存一份
			SD_Cache.saveCachedImage(bitmapDrawable.getBitmap(), AppUtil.md5(url));
			bitmapDrawable.setBounds(0, 0, 300, 300);//瞎设置的大小
			return bitmapDrawable;
		} catch (Exception e) {
			e.printStackTrace();
			Log.i("llb","图片读取失败了呀");
			return null;
		}
	}
	/**
	 * 请求应用图标资源
	 * @param url
	 * @author llb
	 * @return Drawable
	 */
	public static BitmapDrawable getIconDrawable(String url){
		Log.i("llb", "图标地址url="+url);
		try {
			//SD缓存没有那么开始请求网络
//			InputStream inputStream=(InputStream) new URL(url).getContent();//或者是用httpclient
			InputStream inputStream=new DefaultHttpClient().execute(new HttpGet(url)).getEntity().getContent();
			BitmapDrawable bitmapDrawable=(BitmapDrawable) BitmapDrawable.createFromStream(inputStream, "图名");
			//得到原图片后先在SD卡里面缓存一份
			SD_Cache.saveCachedImage(bitmapDrawable.getBitmap(), AppUtil.md5(url));
			bitmapDrawable.setBounds(0, 0, 40, 40);//瞎设置的大小
			return bitmapDrawable;
		} catch (Exception e) {
			e.printStackTrace();
			Log.i("llb","图片读取失败了呀");
			return null;
		}
	}
	
//	/**
//	 * 这个方法是为了适应屏幕
//	 * @param BitmapDrawable drawable
//	 * @return Drawable
//	 */
//	public Drawable adaptScreen(BitmapDrawable drawable){
//		DisplayMetrics metrics=AppUtil.getDisplayMetrics(this.context);
//		int bitmapW=drawable.getBitmap().getWidth();
//		int bitmapH=drawable.getBitmap().getHeight();
//		//按照屏幕比例缩放程序
////		if(metrics.heightPixels<drawable.getIntrinsicHeight()||metrics.widthPixels<drawable.getIntrinsicWidth()){
//		if(metrics.heightPixels<bitmapH||metrics.widthPixels<bitmapW){
//			drawable.setBounds(0, 0, metrics.widthPixels, metrics.widthPixels*(bitmapH/bitmapW));
//		}else {
//			Log.i("llb","没超");
//			drawable.setBounds(0, 0,bitmapW,bitmapH);
//		}
//		return drawable;
//		
//	}
}
