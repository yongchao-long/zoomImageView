package com.example.zoomimage;

import android.annotation.SuppressLint;
import android.support.v4.view.ViewPager;
import android.view.View;

public class RotatePageDownTransformer implements ViewPager.PageTransformer{

	private static final float MAX_ROTATE = 20f;
	private float mRot;
	@SuppressLint("NewApi") 
    public void transformPage(View view, float position) {  
        int pageWidth = view.getWidth();  
  
        if (position < -1) { // [-Infinity,-1)  
            // This page is way off-screen to the left.  
            view.setRotation(0); 
  
        } else if (position <= 0) { // [-1,0]  
            // Use the default slide transition when moving to the left page  
            mRot = MAX_ROTATE * position;
            view.setPivotX(pageWidth/2);
            view.setPivotY(view.getMeasuredHeight());
            view.setRotation(mRot);
  
         // a页滑动至b页 ; a页position从 0.0 ~ -1 ;b页position从1 ~ 0.0  
        } else if (position <= 1) { // (0,1]  
        	mRot = MAX_ROTATE * position;
            view.setPivotX(pageWidth/2);
            view.setPivotY(view.getMeasuredHeight());
            view.setRotation(mRot);
  
        } else { // (1,+Infinity]  
            // This page is way off-screen to the right.  
            view.setRotation(0); 
        }  
    }  

}
