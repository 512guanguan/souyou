package com.llb.souyou.adapter;

import java.util.ArrayList;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
/**
 * 这个adapter里面有Fragment数组
 * @author llb
 *
 */
public class mViewPagerAdapter extends FragmentPagerAdapter {
	private ArrayList<Fragment> fragments=new ArrayList<Fragment>(3);
	public mViewPagerAdapter(FragmentManager fm) {
		super(fm);
	}
	public mViewPagerAdapter(FragmentManager fm,ArrayList<Fragment> fragments) {
		super(fm);
		this.fragments=fragments;
	}
	@Override
	public Fragment getItem(int arg0) {
		// TODO Auto-generated method stub
		return fragments.get(arg0);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return fragments.size();
	}
	
}
