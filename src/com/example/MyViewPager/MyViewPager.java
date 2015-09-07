package com.example.MyViewPager;

import java.util.HashMap;
import java.util.Map;

import com.example.zoomimage.MainActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

public class MyViewPager extends ViewPager {
	private View mLeft;
	private View mRight;
	private static final float MIN_SCALE = 0.6F;
	private float mScale;
	private float mTrans;
	private int mLength;
	private Map<Integer, View> mChildren = new HashMap<Integer, View>();
	
	public void setViewFromPosition(int position, View view){
		
		mChildren.put(position, view);
	}
	
	public void removeViewFromPOsition(int position){
		
		mChildren.remove(position);
	}
	
	public void getImageViewsLength(ImageView[] imageView){
		mLength = imageView.length;
	}

	public MyViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
		
	}
	
	
	public MyViewPager(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onPageScrolled(int position, float offset, int offsetPixels) {
		
		mLeft = mChildren.get(position%mLength);
		mRight = mChildren.get((position + 1)%mLength);
		
		animStack(mLeft,mRight,offset,offsetPixels);
		super.onPageScrolled(position, offset, offsetPixels);
	}

	@SuppressLint("NewApi") private void animStack(View left, View right, float offset,
			int offsetPixels) {
		
		if(right != null){
			//��0~1ҳ��offset��0~1
			mScale = (1 - MIN_SCALE)*offset + MIN_SCALE;
			//offsetPixels��0~���
			mTrans = -getWidth() - getPageMargin() + offsetPixels;
			
			right.setScaleX(mScale);
			right.setScaleY(mScale);
			right.setTranslationX(mTrans);
		}
		
		if(left != null){
			left.bringToFront();
		}
		
	}

}
