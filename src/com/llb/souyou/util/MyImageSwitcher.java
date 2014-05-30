package com.llb.souyou.util;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageSwitcher;

public class MyImageSwitcher extends ImageSwitcher{
	private ViewPager viewPager;
	public MyImageSwitcher(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	public MyImageSwitcher(Context context, AttributeSet attrs) {
		super(context, attrs);		
	}
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		viewPager.requestDisallowInterceptTouchEvent(true);
		return super.onInterceptTouchEvent(ev);
	}
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		viewPager.requestDisallowInterceptTouchEvent(true);
		return super.dispatchTouchEvent(ev);
	}
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		if(MotionEvent.ACTION_UP == event.getAction())
            viewPager.requestDisallowInterceptTouchEvent(false);
		else
            viewPager.requestDisallowInterceptTouchEvent(true);
		return super.onTouchEvent(event);
	}
	public ViewPager getViewPager() {
		return viewPager;
	}
	public void setViewPager(ViewPager viewPager) {
		this.viewPager = viewPager;
	}
	
}
