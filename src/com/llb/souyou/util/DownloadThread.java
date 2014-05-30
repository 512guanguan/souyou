package com.llb.souyou.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import android.os.Handler;
import android.util.Log;

import com.llb.souyou.app.Constant;

/**
 * 这个线程负责某一个任务的下载任务，这里面需要统筹断点下载的事情
 * @author llb
 */
public class DownloadThread implements Runnable{
	private String logicURL=null;//业务服务器的请求地址
	private String dataUrl=null;//拦截重定向后获得的数据服务器地址
	private String appName=null;//下载后文件在SD卡中的文件名，不包括.apk后缀
	private int app_id=0;//应用在下载列表里面的顺序，方便更换下载状态
//	private HashMap<Integer, Object> message=new HashMap<Integer, Object>(1);//返回给主线程的消息
//	private Message message=new Message();
	
	private Handler handler=null;//通知UI线程
	private ExecutorService pool;//多线程下载时的线程池
	private long appSize=0;//查询得到文件的大小 bytes
	private long[] startPositions;//用来存放每一个线程开始现在的字节号
	private long blockSize=0;//每一块的大小，最后一块不定
	private String[] filePaths;//存储所有临时文件名
	private CountDownLatch countDownLatch;//线程同步用
	private int threadNum;//要开启的线程数
	private ChildDownloadThread[] childThreads;//下载子线程
	
	
	/**
	 * 构造函数
	 * @param logicURL 业务服务器请求url
	 * @param fileName 该应用的名字
	 * @param handler UI线程的Handler
	 * @param place int 应用在下载列表的位置，从0起始
	 */
	public DownloadThread(String logicURL,String appName,Handler handler,int app_id){
		this.logicURL=logicURL;
		this.appName=appName;
		this.handler=handler;
		this.app_id=app_id;//
//		message.arg1=app_id;
		pool=Executors.newCachedThreadPool();
	}

	@Override
	public void run() {
		//检查任务曾下载过，有缓存数据
		//请求业务服务器获取重定向后的Location信息
		//请求dataUrl获取整个应用的大小信息
		//根据应用大小和已经下载的数据情况新建合适个数的子线程进行多线程下载
		//待所有的子线程执行完毕，若成功开始合并临时文件
		int status=checkFileStatus(appName);
		if(status==2){
			//已经下载过了，不用重复下载
			Log.i("llb", "已经下载过了哟");
//			Message message0=new Message();
//			message0.arg1=app_id;
//			message0.obj="已经下载过了哟";
//			message.put(app_id, "已经下载过了哟");
			handler.obtainMessage(4,app_id,0,"已经下载过了哟").sendToTarget();//返回程序总大小
//			message.clear();
//			handler.obtainMessage(4, "已经下载过了哟").sendToTarget();//返回程序总大小
			return;
		}
		dataUrl=getDataURL(logicURL);//获取到了数据服务器的地址
		if(null==dataUrl){
			Log.i("llb", "请求dataUrl出错了");
			return;
		}
		Log.i("llb", "请求dataUrl="+dataUrl);
		appSize=getAppSize(dataUrl);//获取应用大小
//		message.put(app_id,appSize);
//		Message message7=new Message();
//		message7.arg1=app_id;
//		message7.obj=appSize;
		
		handler.obtainMessage(3,app_id,0,appSize).sendToTarget();//返回程序总大小
//		message.clear();
//		handler.obtainMessage(3, appSize).sendToTarget();//返回程序总大小
		//确定下载的线程数
		if(appSize<10*1024*1024){
			threadNum=4;//4个线程同时下载
		}else if(appSize<20*1024*1024){
			threadNum=8;
		}else if(appSize<30*1024*1024){
			threadNum=10;
		}else {
			threadNum=12;//最多12个线程同时下载
		}
		//声明数组
		startPositions=new long[threadNum];
		blockSize=appSize/threadNum;
		filePaths=new String[threadNum];
		countDownLatch=new CountDownLatch(threadNum);
		childThreads=new ChildDownloadThread[threadNum];
		
		for(int i=0;i<threadNum;i++){//初始化文件名
			filePaths[i]=Constant.APP_BASE_PATH+appName+"_"+i+".tmp";
		}
		switch (status) {
		case 0://说明有临时文件，下载未完成
			Log.i("llb", "SD卡有些临时文件哟");
//			message.put(app_id, "SD卡有些临时文件，可以断点续传哟");
//			Message message1=new Message();
//			message1.arg1=app_id;
//			message1.obj="SD卡有些临时文件，可以断点续传哟";
			handler.obtainMessage(4,app_id,0,"SD卡有些临时文件，可以断点续传哟").sendToTarget();//返回程序总大小
//			message.clear();
//			handler.obtainMessage(4, "SD卡有些临时文件，可以断点续传哟").sendToTarget();//返回程序总大小
			File file;
			long hasDown=0;
			for(int i=0;i<threadNum;i++){//初始化文件起始位
				file=new File(filePaths[i]);
				if(file.exists()){
					startPositions[i]=i*blockSize+file.length();
					hasDown+=file.length();//计算已下载值
				}
			}
//			message.put(app_id,hasDown);
//			Message message2=new Message();
//			message2.arg1=app_id;
//			message2.obj=hasDown;
			handler.obtainMessage(0,app_id,0,hasDown).sendToTarget();//返回已下载大小
//			message.clear();
//			handler.obtainMessage(0,hasDown).sendToTarget();//返回已下载大小
			Log.i("llb", "startposition="+startPositions.toString());
			break;
		case 1://第一次下载
//			message.put(app_id,0L);
//			Message message3=new Message();
//			message3.arg1=app_id;
//			message3.obj=0L;
			handler.obtainMessage(0,app_id,0,0L).sendToTarget();//第一次下载，返回已下载大小
//			message.clear();
//			handler.obtainMessage(0,0L).sendToTarget();
			Log.i("llb", "第一次下载,threadNum="+threadNum);
			for(int i=0;i<threadNum;i++){//临时文件的名字appName_i.tmp
				try {
					new File(filePaths[i]).createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			//初始化起始位数组
			for(int i=0;i<threadNum;i++){
				startPositions[i]=i*blockSize+0;
			}
			break;
		}
		//接下来就是开启子线程去下载了
		long endposition=0;
		for (int i = 0; i < threadNum; i++) {
			if(i==(threadNum-1)){
				endposition=appSize-1;
			}else {
				endposition=(i+1)*blockSize-1;
			}
			childThreads[i] = new ChildDownloadThread(dataUrl, filePaths[i],
					countDownLatch, startPositions[i],endposition,handler,app_id);
			pool.execute(childThreads[i]);
		}
		try {
			//等待所有的子线程结束执行
			countDownLatch.await(20, TimeUnit.MINUTES);//最长20分钟
//			message.put(app_id,"子线程都执行完毕了");
//			Message message4=new Message();
//			message4.arg1=app_id;
//			message4.obj="子线程都执行完毕了";
			handler.obtainMessage(4,app_id,0,"子线程都执行完毕了").sendToTarget();
//			message.clear();
//			handler.obtainMessage(2,"子线程都执行完毕了").sendToTarget();
			pool.shutdownNow();
			//接下来是合并所有的文件
			tempToAPKFile(childThreads);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	/**
	 * 检查SD卡中这个文件的存储状态
	 * @param fileName
	 * @return
	 */
	private int checkFileStatus(String appName){
		//检查文件夹是否存在
		File dir = new File(Constant.APP_BASE_PATH);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		//检查文件是否已经存在 filePath_i其中i是线程编号 1 2 3 4。。。。
		File tempFile=new File(Constant.APP_BASE_PATH+appName+"_4");//如果已经下载过
		File file=new File(Constant.APP_BASE_PATH+appName+".apk");//如果已经下载完
		if(tempFile.exists()){
			return 0;//说明有临时文件，下载未完成
		}else if (file.exists()) {
			return 2;//说明已经下载完了，不用重复下载
		}else {
			return 1;//第一次下载
		}
	}
	/**
	 * 负责向业务服务器请求数据服务器地址
	 * @param logicURL 业务服务器请求地址
	 * @return 返回重定向的Location
	 */
	private String getDataURL(String logicURL){
		String location=null;//重定向地址
		URL url=null;
		try {
			url = new URL(logicURL);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			if (null == conn) {
				return null;
			}
			// 读取超时时间 毫秒级
			conn.setReadTimeout(5000);
			conn.setConnectTimeout(5000);
			conn.setRequestMethod("GET");
			conn.setInstanceFollowRedirects(false);//拦截重定向信息
			conn.setDoInput(true);
			conn.connect();
			Log.i("llb","getResponseCode:"+conn.getResponseCode());
			Log.i("llb","location"+conn.getHeaderField("location"));
			// MovedPermanently 指示请求的信息已移到 Location 头中指定的 URI
			// 处。接收到此状态时的默认操作为遵循与响应关联的 Location 头。
			if(conn.getResponseCode()==HttpURLConnection.HTTP_MOVED_PERM){
				location=conn.getHeaderField("location");//获取重定向地址 状态码301
				conn.disconnect();//关闭连接
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return location;
	}
	/**
	 * 获取应用的大小
	 * @param dataurl 数据服务器的请求链接
	 * @return long 应用大小bytes
	 */
	private long getAppSize(String dataurl){
		URL url=null;
		HttpURLConnection connData=null;
		try {
			url=new URL(dataurl);
			connData = (HttpURLConnection) url.openConnection();
//			connData.setRequestProperty("Accept-Encoding", "identity");//设置不要压缩 
			Log.i("llb","appSize="+ connData.getContentLength());
			return connData.getContentLength();//获取文件大小
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0;
	}
	private void tempToAPKFile(ChildDownloadThread[] childThreads) {
		try {
			BufferedOutputStream outputStream = new BufferedOutputStream(
					new FileOutputStream(Constant.APP_BASE_PATH+appName+".apk"));
			// 遍历所有子线程创建的临时文件，按顺序把下载内容写入目标文件中
			//前提是所有请求都没有出错，这个需要细化再改
			for (int i = 0; i < threadNum; i++) {
				//逐个线程检查是否都下载成功了，暂时空置	
				if(childThreads[i].status){
					BufferedInputStream inputStream = new BufferedInputStream(
							new FileInputStream(new File(filePaths[i])));
					System.out.println("Now is file " + childThreads[i]);
					int len = 0;
					long count = 0;
					byte[] buffer = new byte[2048];
					while ((len = inputStream.read(buffer)) != -1) {
						count += len;
						outputStream.write(buffer, 0, len);
						if ((count % 4096) == 0) {
							outputStream.flush();
						}
					}
					inputStream.close();
					// 删除临时文件
					new File(filePaths[i]).delete();
					if(i==threadNum-1){
//						message.put(app_id,"下载成功了");
//						Message message5=new Message();
//						message5.arg1=app_id;
//						message5.obj="下载成功了";
						handler.obtainMessage(1,app_id,0,"下载成功了").sendToTarget();
//						message.clear();
//						handler.obtainMessage(1, "下载成功了").sendToTarget();
					}
				}else {
					// 删除合并的错误文件
					new File(Constant.APP_BASE_PATH+appName+".apk").delete();
//					message.put(app_id,"下载失败了");
//					Message message6=new Message();
//					message6.arg1=app_id;
//					message6.obj="下载失败了";
					handler.obtainMessage(2,app_id,0,"下载失败了").sendToTarget();
//					message.clear();
//					handler.obtainMessage(2, "下载失败了").sendToTarget();
				}
			}
			outputStream.flush();
			outputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
