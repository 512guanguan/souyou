package com.llb.souyou.bean;

import java.io.Serializable;

public class SoftwareItem1Bean implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String id;//应用的id
	private String imageUrl;
	private String title=null;
	private float starRating;//软件评级
	private String downloadTime;//下载次数
	private String size;//软件包大小
	private int status=-1;//下载状态status:-1=未有下载操作   0=failed 1=success 2=paused 3=loading
	private int progress=0;//下载进度，这个只有在下载那里用到了
	private String content;
	private String data_app_id;//这是资源在数据中心的id
	
	/**
	 * 带下载状态的构造函数，一般是用在了下载Activity里面
	 * @param id
	 * @param imageUrl
	 * @param title
	 * @param starRating
	 * @param downloadTime
	 * @param size
	 * @param int status 下载状态status:-1=未有下载操作   0=failed 1=success 2=paused 3=loading
	 * @param content
	 */
	public SoftwareItem1Bean(String id,String imageUrl, String title, float starRating,String downloadTime, String size,int status,int progress) {
		super();
		this.id=id;
		this.imageUrl = imageUrl;
		this.title = title;
		this.starRating = starRating;
		this.downloadTime=downloadTime;
		this.size = size;
		this.status = status;
		this.progress=progress;
	}
	/**
	 * 这个构造函数没有是否下载标记
	 * @param id
	 * @param imageUrl
	 * @param title
	 * @param starRating
	 * @param downloadTime
	 * @param size
	 * @param content
	 */
	public SoftwareItem1Bean(String id,String imageUrl, String title, float starRating,
			String downloadTime, String size, String content,String data_app_id) {
		super();
		this.id=id;
		this.imageUrl = imageUrl;
		this.title = title;
		this.starRating = starRating;
		this.downloadTime = downloadTime;
		this.size = size;
		this.content = content;
		this.data_app_id=data_app_id;
	}
	/**
	 * 这个构造函数没有content一项
	 * @param id
	 * @param imageUrl
	 * @param title
	 * @param starRating
	 * @param downloadTime
	 * @param size
	 * @param content
	 */
	public SoftwareItem1Bean(String id,String imageUrl, String title, float starRating,
			String downloadTime, String size,String data_app_id) {
		super();
		this.id=id;
		this.imageUrl = imageUrl;
		this.title = title;
		this.starRating = starRating;
		this.downloadTime = downloadTime;
		this.size = size;
		this.data_app_id=data_app_id;
	}
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getImageUrl() {
		return imageUrl;
	}
	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public float getStarRating() {
		return starRating;
	}
	public void setStarRating(float starRating) {
		this.starRating = starRating;
	}
	public String getDownloadTime() {
		return downloadTime;
	}
	public void setDownloadTime(String downloadTime) {
		this.downloadTime = downloadTime;
	}
	public String getSize() {
		return size;
	}
	public void setSize(String size) {
		this.size = size;
	}
	/**
	 * 下载状态status:-1=未有下载操作   0=failed 1=success但未安装  2=paused 3=loading
	 * @return
	 */
	public int getStatus() {
		return status;
	}
	/**
	 * 下载状态status:-1=未有下载操作   0=failed 1=success 2=paused 3=loading
	 * @param status
	 */
	public void setStatus(int status) {
		this.status = status;
	}
	/**
	 * 获取下载进度
	 * @return
	 */
	public int getProgress() {
		return progress;
	}
	public void setProgress(int progress) {
		this.progress = progress;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getData_app_id() {
		return data_app_id;
	}
	public void setData_app_id(String data_app_id) {
		this.data_app_id = data_app_id;
	}
	
	
}
