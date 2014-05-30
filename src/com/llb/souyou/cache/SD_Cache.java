package com.llb.souyou.cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import android.graphics.Bitmap;
import android.util.Log;

import com.llb.souyou.app.Constant;
import com.llb.souyou.util.AppUtil;

public class SD_Cache {
	/**
	 * 从缓存里面读取图片，使用时需要判断返回的Bitmap是否为null，若为null说明缓存中没有，需要去请求网络
	 * @param url 图片url
	 * @return Bitmap，若没找到则返回null
	 * @author llb
	 */
	public static Bitmap getCachedImage (String url) {
		String cacheKey = AppUtil.md5(url);//利用图片的MD5加密后的url作为缓存key
		Bitmap cachedImage = SDUtil.getImage(cacheKey);
		if (cachedImage != null) {
			Log.w("llb", "get cached image succeed");
			return cachedImage;
		} else {
			return null;
		}
	}
	/**
	 * 保存图片到SD缓存中
	 * @param bitmap 要保存的Bitmap
	 * @param fileName 利用MD5加密后的图片url
	 * @return
	 */
	public static boolean saveCachedImage(Bitmap bitmap, String fileName){
		if(SDUtil.hasSdcard()){
			return SDUtil.saveImage(bitmap, fileName);
		}else {
			Log.i("llb","没有SD卡");
			return false;
		}
	}
	/**
	 * 列表数据的缓存时间短
	 * 把列表数据缓存到SD中，一般在退出应用时进行
	 * @param list Object 要缓存的数据
	 * @param fileName String 文件名（完整的路径）
	 * @return boolean
	 */
	public static boolean saveCacheData(Object list,String fileName){
		File dir = new File(Constant.LIST_BASE_PATH);//检查路径是否存在
		Log.i("llb","dir path="+dir.getPath());
		if (!dir.exists()) {//目录不存在
			dir.mkdirs();
			if(!dir.isDirectory()){
				Log.i("llb","dir.isDirectory"+dir.isDirectory());
				return false;
			}
		}else {
			if (SDUtil.exists(fileName)) {//如果文件已经存在,检查缓存是否过期
				if(!SDUtil.removeExpiredCache(fileName, 2)){//如果已经有该数据的缓存，那么先判断是否过期
					Log.i("llb","缓存还没过期呢！！");
					return true;//文件还没过期，不需要重新存了
				}
			}
		}
		try {
			ObjectOutputStream oos=new ObjectOutputStream(new FileOutputStream(fileName));
			oos.writeObject(list);
			Log.i("llb", "列表缓存成功");
			oos.flush();
			oos.close();
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	/**
	 * item详情数据的缓存时间比列表长  类型1
	 * 把详情数据缓存到SD中，一般在请求到网络时进行
	 * @param detail Object 要缓存的数据
	 * @param fileName String 文件名（完整的路径）
	 * @return boolean
	 */
	public static boolean saveCacheDetailData(Object detail,String fileName){
		File dir = new File(Constant.DETAIL_BASE_PATH);//检查路径是否存在
		Log.i("llb","dir path="+dir.getPath());
		if (!dir.exists()) {//目录不存在
			dir.mkdirs();
			if(!dir.isDirectory()){
				Log.i("llb","dir.isDirectory"+dir.isDirectory());
				return false;
			}
		}else {
			if (SDUtil.exists(fileName)) {//如果文件已经存在,检查缓存是否过期
				if(!SDUtil.removeExpiredCache(fileName, 1)){//如果已经有该数据的缓存，那么先判断是否过期
					Log.i("llb","缓存还没过期呢！！");
					return true;//文件还没过期，不需要重新存了
				}
			}
		}
		try {
			ObjectOutputStream oos=new ObjectOutputStream(new FileOutputStream(fileName));
			oos.writeObject(detail);
			Log.i("llb", "列表缓存成功");
			oos.flush();
			oos.close();
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	/**
	 * 从SD中读取缓存的列表数据
	 * @param fileName String 要读取的文件绝对路径
	 * @return Object
	 */
	public static Object getCacheData(String fileName){
		if(!SDUtil.exists(fileName)){
			return null;//检查文件是否存在
		}
		try {
			ObjectInputStream ois=new ObjectInputStream(new FileInputStream(fileName));
			Object object=ois.readObject();
			Log.i("llb", "列表缓存获取成功");
			ois.close();
			return object;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}catch (ClassNotFoundException e) {
			// TODO: handle exception
			e.printStackTrace();
			return null;
		}
	}
	/**
	 * 删除单个缓存文件，暂时有点多余了
	 * @param fileName
	 * @return
	 */
	public static boolean clearCacheData(String fileName){
		Log.i("llb","清空文件"+fileName);
		File file=new File(fileName);
		if (file.exists()) {
			file.delete();
		}
		return true;
	}
}
