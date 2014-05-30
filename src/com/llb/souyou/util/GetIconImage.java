package com.llb.souyou.util;

import java.util.ArrayList;
import java.util.Iterator;

public class GetIconImage implements Runnable{
	ArrayList<String> imageURL=new ArrayList<String>();
	public GetIconImage(ArrayList<String> imageURL){
		this.imageURL=imageURL;
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		Iterator<String> iterator=imageURL.iterator();
		while(iterator.hasNext()){
			NetworkUtil.getIconDrawable(iterator.next());
		}
	}
}
