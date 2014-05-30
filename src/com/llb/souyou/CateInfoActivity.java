package com.llb.souyou;

import java.util.ArrayList;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;

import com.llb.souyou.adapter.mViewPagerAdapter;
import com.llb.souyou.fragment.CateInfo1Fragment;

public class CateInfoActivity extends FragmentActivity{
	private ActionBar actionBar;
	private PagerAdapter pagerAdapter;
	private ViewPager viewPager;
	private int cate_id;
	private String title="分类";
	private ArrayList<Fragment> fragments=new ArrayList<Fragment>(3);//用来存储Fragment
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cate);//暂时先用着最新应用的布局，将来这一块要重新细化设计
		Intent intent=this.getIntent();
		cate_id=intent.getIntExtra("cate_id", 1);//得到分类id
		title=intent.getStringExtra("title");
		initActionBar();
		initViewPager();
	}
	private void initActionBar(){
		actionBar=getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true); 
		//actionBar.setDisplayShowTitleEnabled(true);//默认是true
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE, ActionBar.DISPLAY_SHOW_TITLE);
		actionBar.setTitle(title);//设置新的标题
	}
	private void initViewPager(){
		viewPager=(ViewPager) findViewById(R.id.pager);
		
		//从Activity给Fragment传递参数
		Fragment cate1Frament=new CateInfo1Fragment();
		Bundle bundle=new Bundle();
		bundle.putInt("cate_id", cate_id);
		cate1Frament.setArguments(bundle);
		
		fragments.add(cate1Frament);
		pagerAdapter=new mViewPagerAdapter(getSupportFragmentManager(), fragments);
		viewPager.setAdapter(pagerAdapter);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		    case android.R.id.home://ActionBar的向上导航
		        Intent intent = new Intent(this, MainActivity.class);
		        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		        startActivity(intent);
		    	return true;
		    default:
		        return super.onOptionsItemSelected(item);
		}
	}
}
