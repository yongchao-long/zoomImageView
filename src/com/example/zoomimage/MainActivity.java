package com.example.zoomimage;

import com.example.MyViewPager.MyViewPager;
import com.example.view.ZoomImageView;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


public class MainActivity extends ActionBarActivity {

	private MyViewPager mViewPager;
	private int[] mImage = new int[]{R.drawable.tbug,R.drawable.ttt,R.drawable.xx};
	private ImageView[] mImageViews = new ImageView[mImage.length];
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vp);
        
        mViewPager = (MyViewPager) findViewById(R.id.viewpager);
        /*
         * �������Զ���
         */
        //mViewPager.setPageTransformer(true, new DepthPageTransformer());
        //mViewPager.setPageTransformer(true, new ZoomOutPageTransformer());
        //mViewPager.setPageTransformer(true, new RotatePageDownTransformer());
        mViewPager.setAdapter(new PagerAdapter() {
			
        	@Override
        	public Object instantiateItem(ViewGroup container, int position) {
        		ZoomImageView imageView = new ZoomImageView(getApplicationContext());
        		position %= mImageViews.length;
        		imageView.setImageResource(mImage[position]);
        		container.addView(imageView);
        		mImageViews[position] = imageView;
        		//MyViwePager�ı�Ҫ����
        		mViewPager.setViewFromPosition(position, imageView);
        		mViewPager.getImageViewsLength(mImageViews);
        		return imageView;
        	}
        	
        	@Override
        	public void destroyItem(ViewGroup container, int position,
        			Object object) {
        		
        		//container.removeView(mImageViews[position]);
        		
        		//mViewPager.removeViewFromPOsition(position);
        	}
        	
			@Override
			public boolean isViewFromObject(View arg0, Object arg1) {
				
				return arg0 == arg1;
			}
			
			@Override
			public int getCount() {
				//return mImageViews.length;
				return Integer.MAX_VALUE;
			}
		});
    }


   
}
