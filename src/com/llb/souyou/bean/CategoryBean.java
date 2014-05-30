package com.llb.souyou.bean;


public class CategoryBean {
	private String imageUrl;
	private String title;//分类名称
	private String desc;//分类描述
	private int type;//标记类别，方便跳转
	private int parent_id;//父类别
	public CategoryBean(String imageUrl, String title, String desc,
			int type,int parent_id) {
		super();
		this.imageUrl = imageUrl;
		this.title = title;
		this.desc = desc;
		this.type = type;
		this.parent_id=parent_id;
	}
	/**
	 * 没有imageurl
	 * @param type
	 * @param parent_id
	 * @param title
	 * @param desc
	 */
	public CategoryBean(int type,int parent_id,String title, String desc) {
		super();
		this.type = type;
		this.parent_id=parent_id;
		this.title = title;
		this.desc = desc;
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
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public int getParent_id() {
		return parent_id;
	}
	public void setParent_id(int parent_id) {
		this.parent_id = parent_id;
	}
	
}
