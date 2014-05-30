package com.llb.souyou.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.http.HttpEntity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.util.Log;
/**
 * 主要是程序通用的工具方法
 * @author llb
 *
 */
public class AppUtil {
	public static final String BASEURL_STRING="http://192.168.1.104/collegepy/index.php";//网址入口
	/**
	 * 把httpclient返回的entity解析出里面的json格式String
	 * @param entity
	 * @return josn规范的String
	 * @throws Exception IO&IllLegalState
	 */
	public static String entityToJsonString(HttpEntity entity) throws Exception{
		StringBuilder sBuilder=new StringBuilder();//干脆重新声明一个变量
		BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(entity.getContent()));
		String s=null;
		while((s=bufferedReader.readLine())!=null){
			sBuilder.append(s);//逐行读取数据
			s=null;//清空
		}
		bufferedReader.close();//别忘记关闭
		Log.i("zgr","resEntity="+sBuilder.toString());
		return sBuilder.toString();
	}
	/**
	 * MD5加密
	 * @param str 要加密的数据
	 * @return 加密后的String
	 */
	static public String md5 (String str) {
		MessageDigest algorithm = null;
		try {
			algorithm = MessageDigest.getInstance("MD5");//采用MD5算法
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		if (algorithm != null) {
			algorithm.reset();
			algorithm.update(str.getBytes());
			byte[] bytes = algorithm.digest();
			StringBuilder hexString = new StringBuilder();
			for (byte b : bytes) {
				hexString.append(Integer.toHexString(0xFF & b));//转换成16进制
			}
			return hexString.toString();
		}
		return "";
		
	}
	/** 获取耗费内存**/
	public static long getUsedMemory () {
		long total = Runtime.getRuntime().totalMemory();
		long free = Runtime.getRuntime().freeMemory();
		return total - free;
	}
	/**
	 * 想获取屏幕大小
	 * @param context
	 * @return
	 */
	public static DisplayMetrics getDisplayMetrics(Context context){
//		DisplayMetrics metrics = new DisplayMetrics();
//		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		DisplayMetrics metrics=context.getResources().getDisplayMetrics();
		return metrics;
	}
	/**
	 * //打开APK程序代码 安装应用
	 * @param context
	 * @param filePath
	 */
	public static void openFile(Context context,String filePath) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//启动新的task
        intent.setAction(android.content.Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File(filePath)),
                        "application/vnd.android.package-archive");
        context.startActivity(intent);
	}
}
