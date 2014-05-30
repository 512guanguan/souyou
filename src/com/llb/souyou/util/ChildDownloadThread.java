package com.llb.souyou.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CountDownLatch;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.llb.souyou.app.Constant;

/**
 * 这个线程的作用：
 * 给一个url和存储文件的filePath，负责把数据从服务器取回来保存，其他的不用理
 * 说白了就是负责一个下载操作中的某一块数据的下载任务
 * @author llb
 *
 */
public class ChildDownloadThread implements Runnable{
	private String urlPath;
	private Handler handler;
	private Message msg;
	private int app_id;
	private String filePath;
	private long startp=0;
	private long endp=0;
	private CountDownLatch latch;
	public boolean status=false;//标记运行结果 0失败 1成功
	/**
	 * 子线程，负责下载某一块
	 * @param urlPath 请求地址
	 * @param filePath 文件保存路径
	 * @param latch 负责线程同步
	 * @param startp 开始读取位
	 * @param endp 结束读取位
	 */
	public ChildDownloadThread(String urlPath,String filePath,CountDownLatch latch,long startp,long endp,Handler handler,int app_id){
		this.urlPath=urlPath;
		this.filePath=filePath;
		this.startp=startp;
		this.latch=latch;
		this.endp=endp;
		this.handler=handler;
		this.app_id=app_id;
		Log.i("llb","startp="+startp+" endp="+endp);
	}
	@Override
	public void run() {
		URL url;
		try {
			url = new URL(urlPath);
			HttpURLConnection connData = (HttpURLConnection) url.openConnection();
			if (null == connData) {
				return;
			}
			// 读取超时时间 毫秒级
			connData.setReadTimeout(10000);
			connData.setConnectTimeout(9000);
			connData.setRequestMethod("GET");
			connData.setRequestProperty("Range", "bytes="+startp+"-"+endp); // 设置获取数据的范围  
			connData.setDoInput(true);
			connData.connect();
			
			int rescode=connData.getResponseCode();
			Log.i("llb","getResponseCode:"+rescode);
			if (rescode== HttpURLConnection.HTTP_OK||rescode==HttpURLConnection.HTTP_PARTIAL) {//200||206
				InputStream is = connData.getInputStream();
				FileOutputStream fos = new FileOutputStream(filePath);
				byte[] buffer = new byte[4*1024];
				int i = 0;//统计读取了多少
				//统计累积读取字节数
				long downFileSize = 0;//(int) hasdown;
				while ((i = is.read(buffer)) != -1) {
					downFileSize = downFileSize + i;
					fos.write(buffer, 0, i);
					//每读满4096个byte（一个内存页），往磁盘上flush一下  
					if(downFileSize % 4096 == 0){  
						fos.flush();  
						
						handler.obtainMessage(0,app_id,0,downFileSize).sendToTarget();//返回新下载值
						downFileSize=0;//置空
					}  
//					Log.i("llb","已经下载了:"+downFileSize);
				}
				fos.flush();
				fos.close();
				is.close();
				connData.disconnect();
				status=true;
				latch.countDown();
			}
		} catch (Exception e) {
			Log.i("llb","子线程出错了");
			status=false;
			latch.countDown();
			e.printStackTrace();
		}
	}
	/**
	 * 应用下载的具体方法
	 * @param urlpath 下载URL
	 * @param saveFilePath 保存文件路径
	 * @return ture:下载成功
	 */
	public boolean downloadApp(String urlpath,String filePath) {
		long hasdown=0;//上次下载的大小
		//检查文件夹是否存在
		File dir = new File(Constant.APP_BASE_PATH);
		if (!dir.exists()) {
			dir.mkdirs();
			if(!dir.isDirectory()){
				return false;
			}
		}
		//检查文件是否已经存在
		File saveFilePath=new File(filePath);
		if(!saveFilePath.exists()){
			try {
				saveFilePath.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Log.i("llb", "创建文件失败");
				e.printStackTrace();
			}
		}else {
			hasdown=saveFilePath.length();//获取已下载文件大小
			Log.i("llb", "文件已存在"+hasdown);
		}
		int fileSize = -1;
		boolean result = false;
		int progress = 0;
		try {
			URL url = new URL(urlpath);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			if (null == conn) {
				return false;
			}
			// 读取超时时间 毫秒级
			conn.setReadTimeout(5000);
			conn.setRequestMethod("GET");
			
			conn.setInstanceFollowRedirects(false);//拦截重定向信息
//			conn.setRequestProperty("Range", "bytes=" + 0+"-"+4096); // 设置获取数据的范围  
			conn.setDoInput(true);
			conn.connect();
			Log.i("llb","getResponseCode:"+conn.getResponseCode());
			Log.i("llb","location"+conn.getHeaderField("location"));
			
			String location=null;
			// MovedPermanently 指示请求的信息已移到 Location 头中指定的 URI
			// 处。接收到此状态时的默认操作为遵循与响应关联的 Location 头。
			if(conn.getResponseCode()==HttpURLConnection.HTTP_MOVED_PERM){
				location=conn.getHeaderField("location");//获取重定向地址 状态码301
				conn.disconnect();//关闭连接
			}
			HttpURLConnection connData = (HttpURLConnection) url.openConnection();
			if (null == connData) {
				return false;
			}
			// 读取超时时间 毫秒级
			connData.setReadTimeout(9000);
			connData.setRequestMethod("GET");
			connData.setRequestProperty("Range", "bytes=" + 0+"-"+4096); // 设置获取数据的范围  
			connData.setDoInput(true);
			connData.connect();
			
			
			int rescode=connData.getResponseCode();
			if (rescode== HttpURLConnection.HTTP_OK||rescode==HttpURLConnection.HTTP_PARTIAL) {//200||206
				fileSize = connData.getContentLength();
				InputStream is = connData.getInputStream();
				Log.i("llb","ETag"+connData.getHeaderField("ETag"));
				Log.i("llb","Accept-Ranges"+connData.getHeaderField(("Accept-Ranges")));
				FileOutputStream fos = new FileOutputStream(saveFilePath);
				byte[] buffer = new byte[4*1024];
				int i = 0;//统计读取了多少
				//统计累积读取字节数
				int downFileSize = 0;//(int) hasdown;
				int tempProgress = -1;
				while ((i = is.read(buffer)) != -1) {
					downFileSize = downFileSize + i;
					// 下载进度
					progress = (int) ((downFileSize*100.0) / fileSize);
					Log.i("llb","progress="+ progress);
					fos.write(buffer, 0, i);
					synchronized (this) {
						if (downFileSize == fileSize) {
							// 下载完成
							handler.obtainMessage(1,app_id,0, "success").sendToTarget();
						} else if (tempProgress<progress-5) {//别通知太频繁
							// 下载进度发生改变，则发送Message
							tempProgress = progress;
							//更改进度
							handler.obtainMessage(0,app_id,0,tempProgress).sendToTarget();
						}
					}
					//每读满4096个byte（一个内存页），往磁盘上flush一下  
					if(downFileSize % 4096 == 0){  
						fos.flush();  
					}  

				}
				fos.flush();
				fos.close();
				is.close();
				connData.disconnect();
				result = true;
			} else {
				result = false;
				Log.e("llb", "downloadFile failed");
			}
		} catch (Exception e) {
			result = false;
			Log.e("llb", "downloadFile catch Exception:", e);
		}
		return result;
	}
}
