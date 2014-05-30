package com.llb.souyou.app;

import java.io.File;

import android.os.Environment;

import com.llb.souyou.util.AppUtil;

public class Constant {
	/**站点地址**/
	public static final String HOST_URL="http://bobapp.sinaapp.com";
//	public static final String HOST_URL="http://192.168.0.104/BobApp";
	/**接口地址  **/
	public static final String BASE_URL=HOST_URL+"/mobile/mobile.php";
//	public static final String BASE_URL=HOST_URL+"/mobile/mobile.php";
	/**下载地址**/
	public static final String DOWNLOAD_URL=HOST_URL+"/mobile/mobile.php?type=400&id=";
	//这个id是历史列表里面的appcms_history_id，其实最新版是跟应用表里面的data_app_id是一致的
	
	/**缓存地址**/
	public static final	String BASE_PATH=Environment.getExternalStorageDirectory().getPath();
	public static final String IMG_BASE_PATH= BASE_PATH+File.separator+"souyou"+File.separator+"img"+File.separator;//应用图标缓存
	public static final String LIST_BASE_PATH= BASE_PATH+File.separator+"souyou"+File.separator+"list"+File.separator;//应用列表缓存
	public static final String DETAIL_BASE_PATH= BASE_PATH+File.separator+"souyou"+File.separator+"detail"+File.separator;//应用详情缓存
	public static final String TOP_LIST_FILENAME=LIST_BASE_PATH+AppUtil.md5("toplist");
	public static final String NEW_LIST_FILENAME=LIST_BASE_PATH+AppUtil.md5("newlist");
	
	/**应用下载后保存地址**/
	public static final String APP_BASE_PATH= BASE_PATH+File.separator+"souyou"+File.separator+"download"+File.separator;//应用详情缓存
		
}
