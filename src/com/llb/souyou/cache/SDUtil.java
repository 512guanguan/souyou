package com.llb.souyou.cache;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Comparator;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

import com.llb.souyou.app.Constant;


/**
 * 主要是关于SD的操作方法
 * @author llb
 */
public class SDUtil {
	private static int MB = 1024*1024;// 1MB=1024KB
	private static double FREE_SD_SPACE_NEEDED_TO_CACHE = 10;//MB
	private static double IMG_CACHE_EXPIRE_TIME = 480*3600*1000;// 图片缓存过期时间480小时
	private static double DATA_CACHE_EXPIRE_TIME = 72*3600*1000;// 详情数据缓存过期时间72小时
	private static double LIST_CACHE_EXPIRE_TIME = 1*3600*1000;// 列表数据缓存过期时间1小时

	/**
	 * 检查SD卡是否存在
	 * @return boolean
	 */
	public static boolean hasSdcard() {
		String status = Environment.getExternalStorageState();
		if (status.equals(Environment.MEDIA_MOUNTED)) {
			return true;
		}
		return false;
	}
	/**
	 * 获取SD的可用容量
	 * @return int (MB)
	 */
	public static int getFreeSDSpace(){
		StatFs stat = new StatFs(Constant.BASE_PATH);
		//有多少块*一块有多少字节
		double sdFreeMB = ((double) stat.getAvailableBlocks() * (double) stat.getBlockSize())/MB;
		return (int) sdFreeMB;
	}
	/**
	 * 检查文件是否存在
	 * @param filePath
	 * @return boolean
	 */
	public static boolean exists(String filePath) {
		return new File(filePath).exists();
	}
	/**
	 * 保存图片到SD卡里面
	 * @param bitmap 要保存的图片
	 * @param fileName=AppUtil.md5(url);//这个文件名需要是经过MD5加密后的url
	 * @return boolean 保存是否成功
	 * @author llb
	 */
	public static boolean saveImage(Bitmap bitmap, String fileName) {
		// 判断sdcard上的空间
		if (bitmap == null||FREE_SD_SPACE_NEEDED_TO_CACHE > getFreeSDSpace()) {
			Log.w("llb", " trying to save null bitmap");
			Log.w("llb", "Low free space onsd, do not cache");
			return false;
		} 
		// 目录不存在则创建目录
		File dir = new File(Constant.IMG_BASE_PATH);
		Log.i("llb","dir path="+dir.getPath());
		if (!dir.exists()) {
			dir.mkdirs();
			if(!dir.isDirectory()){
				Log.i("llb","dir.isDirectory"+dir.isDirectory());
				return false;
			}
		}
		Log.i("llb","dir.isDirectory"+dir.isDirectory());
		// 保存图片
		try {
			//传进来的fileName=AppUtil.md5(fileName);//这个文件名需要是经过MD5加密后的url
			String realFileName =Constant.IMG_BASE_PATH+ fileName;
			if(SDUtil.exists(realFileName)){
				if(!SDUtil.removeExpiredCache(realFileName, 0)){//如果已经有图片缓存，那么先判断是否过期
					Log.i("llb","缓存还没过期呢！！");
					return true;//文件还没过期，不需要重新存了
				}
			}
			File file = new File(realFileName);
			file.createNewFile();//新建文件
			
			OutputStream outStream = new FileOutputStream(file);//写文件流
			//Some formats, like PNG which is lossless, will ignore the quality setting
			bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outStream);//不压缩     存为PNG格式
			outStream.flush();
			outStream.close();
			Log.i("llb", "Image saved to sd succeed");
			return true;
		} catch (FileNotFoundException e) {
			Log.w("llb", "FileNotFoundException");
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			Log.w("llb", "IOException");
			e.printStackTrace();
			return false;
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * 根据图片名称获取图片
	 * @param fileName
	 * @return Bitmap
	 * @author llb
	 */
	public static Bitmap getImage(String fileName) {
		//检查图片是否存在
		String realFileName = Constant.IMG_BASE_PATH + "/" + fileName;
		File file = new File(realFileName);
		if (!file.exists()) {
			return null;
		}
		//获取原始图片
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(realFileName, options);
	}
	
	/**
	 * 得到图片的Sample
	 * @param fileName 图片名字
	 * @return Bitmap
	 * @author llb
	 */
	public static Bitmap getSample(String fileName) {
		// check image file exists
		String realFileName = Constant.IMG_BASE_PATH+ fileName;//拼接路径
		File file = new File(realFileName);
		if (!file.exists()) {//检查文件是否存在
			return null;
		}
		//获取原始图片的尺寸
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		Bitmap bitmap = BitmapFactory.decodeFile(realFileName, options);
		int zoom = (int) (options.outHeight / (float) 50);
		if (zoom < 0) 
			zoom = 1;
		//得到resize后的bitmap图片
		options.inSampleSize = zoom;
		options.inJustDecodeBounds = false;
		bitmap = BitmapFactory.decodeFile(realFileName, options);
		return bitmap;
	}
	/**
	 * 设置文件最后更新的时间
	 * @param fileName 目标文件
	 * @author llb
	 */
	protected static void updateTime(String fileName) {
		File file = new File(fileName);
		long newModifiedTime = System.currentTimeMillis();
		file.setLastModified(newModifiedTime);
	}
//	/**
//	 * 移除已经失效的cache 
//	 * 时长大于CACHE_EXPIRE_TIME的就失效
//	 * @param dirPath 文件路径
//	 * @param filename 文件名
//	 * @param fileType 文件类型 0图片  1数据
//	 * @author llb
//	 */
//	public static void removeExpiredCache(String dirPath, String filename,int fileType) {
//		File file = new File(dirPath, filename);
//		switch (fileType) {
//		case 0://图片
//			if (System.currentTimeMillis() - file.lastModified() > IMG_CACHE_EXPIRE_TIME) {
//				Log.i("llb", "Clear some expiredcache Image ");
//				file.delete();//删除失效缓存
//			}
//			break;
//		case 1://数据
//			if (System.currentTimeMillis() - file.lastModified() > DATA_CACHE_EXPIRE_TIME) {
//				Log.i("llb", "Clear some expiredcache DATA ");
//				file.delete();//删除失效缓存
//			}
//			break;
//		}
//	}
	/**
	 * 移除已经失效的cache 
	 * 时长大于CACHE_EXPIRE_TIME的就失效
	 * @param fileName 文件名 完整路径
	 * @param fileType 文件类型  图片0    详情数据1        列表数据2       一般图片缓存时间长 ,详情次之，列表最短
	 * @author llb
	 * @return boolean  若文件过期并删除成功返回true
	 */
	public static boolean removeExpiredCache(String fileName,int fileType) {
		File file = new File(fileName);
		switch (fileType) {
		case 0://图片
			if (System.currentTimeMillis() - file.lastModified() > IMG_CACHE_EXPIRE_TIME) {
				Log.i("llb", "Clear some expiredcache Image ");
				file.delete();//删除失效缓存
				return true;//文件删除成功
			}else {
				//文件没过期
				return false;
			}
		case 1://详情数据
			if (System.currentTimeMillis() - file.lastModified() > DATA_CACHE_EXPIRE_TIME) {
				Log.i("llb", "Clear some expiredcache CONTENT DATA ");
				return file.delete();//删除失效缓存
				//文件删除成功
			}else {
				return false;
			}
		case 2://列表数据
			if (System.currentTimeMillis() - file.lastModified() > LIST_CACHE_EXPIRE_TIME) {
				Log.i("llb", "Clear some expiredcache LIST DATA ");
				return file.delete();//删除失效缓存
			}else {
				return false;
			}
		}
		return false;
	}
	/**
	 * 移除dirPath路径下的所有缓存
	 * @param dirPath
	 */
	public static void removeCache(String dirPath) {
		File dir = new File(dirPath);
		File[] files = dir.listFiles();//获取路径下所有的文件
		if (files == null) {
			return;
		}
		//如果需要的存储缓存的空间大于可用空间，那么将设定一个移除因子丢弃部分缓存数据
		if (FREE_SD_SPACE_NEEDED_TO_CACHE > getFreeSDSpace()) {
			int removeFactor = (int) ((0.5 * files.length) + 1);
			Arrays.sort(files, new FileLastModifSort());//先对缓存文件按最后修改时间排序
			Log.i("llb", "Clear some expiredcache files ");
			for (int i = 0; i < removeFactor; i++) {
				files[i].delete();
			}
		}
	}
	/**
	 * 实现了Compartor<File>接口函数compare(File arg0,File arg1)，用来比较两者最后更新的时间，
	 * arg0>arg1 return 1
	 * arg0>arg1 return 0
	 * else return -1
	 * @author llb
	 *
	 */
	private static class FileLastModifSort implements Comparator<File> {
		@Override
		public int compare(File arg0, File arg1) {
			if (arg0.lastModified() > arg1.lastModified()) {
				return 1;
			} else if (arg0.lastModified() == arg1.lastModified()) {
				return 0;
			} else {
				return -1;
			}
		}
	}
}
