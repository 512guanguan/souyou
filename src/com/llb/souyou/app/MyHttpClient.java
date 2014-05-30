package com.llb.souyou.app;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.util.Log;

public class MyHttpClient{
	private static HttpClient httpClient=null;//整个App共享这一个
//	@Override
//	public void onCreate() {//在Activity Service等创建之前就执行了
//		super.onCreate();
//		httpClient=MyApp.createHttpClient();
//	}
	/**
	 * 创建HttpClient实例，主要是设置http client的各项连接参数和连接管理器
	 * @return 返回一个线程安全的HttpClient实例
	 */
	private static HttpClient createHttpClient() {
		HttpParams params=new BasicHttpParams();
		//设置http连接的基本参数
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);//协议版本号
		HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);//设置编码方式
		//向服务器发送数据的时候，需要先向服务器发起一个请求看服务器是否愿意接受客户端将要发送的数据
		HttpProtocolParams.setUseExpectContinue(params, true);//这个参数的设置要小心点
		HttpProtocolParams.setUserAgent(params,"Mozilla/5.0 (Linux;U;Android 2.2.1;en-us;Nexus One Build/FRG83)"+
		" AppleWebKit/533.1(KHTML, like Gecko) Version/4.0 Mobile Safari/533.1" );  
		 
		ConnManagerParams.setTimeout(params, 2000);//从连接池中取连接的超时时间
		int connectionTimeOut = 8000;//连接超时时间
//		if (!HttpUtils.isWifiDataEnable(context)) {
//			connectionTimeOut = 10000;
//		}
		HttpConnectionParams.setConnectionTimeout(params, connectionTimeOut);
		HttpConnectionParams.setSoTimeout(params, 8000);//设置Socket请求超时
		
		SchemeRegistry sRegistry=new SchemeRegistry();//设置HttpClient支持HTTP和HTTPS两种模式
		sRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		sRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
		
		ThreadSafeClientConnManager threadSafeCCM=new ThreadSafeClientConnManager(params, sRegistry);
		return new DefaultHttpClient(threadSafeCCM, params);
	}
	/**
	 * 对外提供获取httpclient实例的方法
	 * @return
	 */
	public static HttpClient getHttpClient() {
		if(null==httpClient){
			httpClient=createHttpClient();
		}
		return httpClient;
	}
	/**
	 * 通过post方式请求数据
	 * @param url 请求接口地址
	 * @param pairs NameValuePair数据
	 * @return String格式的请求结果
	 */
	public static String postByHttpClient(String url,NameValuePair...pairs){	
		//封装需要post的数据
		List<NameValuePair> nameParams=new ArrayList<NameValuePair>();
		for(NameValuePair np:pairs){//保存传递进来的各项参数
			nameParams.add(np);
		}
		
//		JSONObject json = new JSONObject();
//        Object email = null;
//        json.put("email", email);
//        Object pwd = null;
//        json.put("password", pwd);
//        StringEntity se = new StringEntity( "JSON: " + json.toString());
//        se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
//        post.setEntity(se);
		
		UrlEncodedFormEntity entity=null;
		try {
			entity = new UrlEncodedFormEntity(nameParams, HTTP.UTF_8);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			Log.i("zgr","数据封装编码出错了");
			return null;
		}
		//创建post请求
		HttpPost postRequest=new HttpPost(url);
		postRequest.setEntity(entity);
		//发送post请求
		HttpClient hClient=getHttpClient();
		HttpResponse response=null;
		try {
			response=hClient.execute(postRequest);
		}catch(IOException e) {
			e.printStackTrace();
			return null;
		}
		//检查请求状态码
		if(response.getStatusLine().getStatusCode()!=HttpStatus.SC_OK){
			throw new RuntimeException("请求数据失败！");
		}
		//检查并返回请求的数据
		HttpEntity responseEntity=response.getEntity();
		try {
			return (null==responseEntity) ? null:EntityUtils.toString(responseEntity);//, HTTP.UTF_8);
		} catch (ParseException e) {
			e.printStackTrace();
			Log.i("zgr","数据转换失败");
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			Log.i("zgr","IOException");
			return null;
		}
	}
	/**
	 * 通过get方式请求网络数据
	 * @param url 请求地址
	 * @param pairs 请求需要的各个参数
	 * @return HttpEntity 请求结果数据
	 */
	public static HttpEntity getByHttpClient(String url,NameValuePair...pairs){
		StringBuilder sBuilder=new StringBuilder();
		sBuilder.append(url);
		if(null!=pairs && pairs.length>0){
			sBuilder.append("?");
			for(int i=0;i<pairs.length;i++){
				if(i>0){
					sBuilder.append("&");
				}
				sBuilder.append(String.format("%s=%s",pairs[i].getName(),pairs[i].getValue()));//拼参数
			}
		}
		String path=sBuilder.toString();
		Log.i("llb",path);
		HttpGet getRequest=new HttpGet(path);
		try {//getHttpClient() new DefaultHttpClient()
			HttpResponse response=getHttpClient().execute(getRequest);
			if(response.getStatusLine().getStatusCode()!=HttpStatus.SC_OK){//检查状态码
				throw new RuntimeException("数据请求失败");
			}
			HttpEntity resEntity=response.getEntity();//获得传回来的信息
			return resEntity;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
//			sBuilder=null;
//			sBuilder=new StringBuilder();//干脆重新声明一个变量
////			sBuilder.delete(0, sBuilder.length());//清空之前的变量
//			
//			BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(resEntity.getContent()));
//			String s=null;
//			while((s=bufferedReader.readLine())!=null){
//				sBuilder.append(s);//逐行读取数据
//				s=null;//清空
//			}
//			
//			Log.i("zgr","resEntity="+sBuilder.toString());
////			return (null==resEntity)? null :resEntity.toString();//返回数据
//			return sBuilder.toString();
//		}
	}
}
