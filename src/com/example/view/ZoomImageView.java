package com.example.view;



import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;
import android.view.View.OnTouchListener;

public class ZoomImageView extends ImageView implements OnGlobalLayoutListener, OnScaleGestureListener,OnTouchListener{

	/**
	 * ֵ����һ�ε�ͼƬ���Ŵ���
	 */
	private boolean mOnce = false;

	/**
	 * ��ʼ��ʱ���ŵ�ֵ
	 */
	private float mInitScale = 1.0f;
	/**
	 * ˫���Ŵ�ﵽ��ֵ
	 */
	private float mMidScale = 2.0f; // ���ŵ�ֵ��mInitScale��mMaxScale֮��
	/**
	 * �Ŵ�ļ���
	 */
	private float mMaxScale = 4.0f;
	
	//matrix�ľ���
	private final float[] matrixValues = new float[9];

	/**
	 * ��������λ�Ƶľ���
	 */
	private final Matrix mScaleMatrix = new Matrix();
	
	//��ȡ�û���㴥��ʱ�����ű���
	private ScaleGestureDetector mScaleGestureDetector = null;
	
	private static final String TAG = ZoomImageView.class.getSimpleName();
	
	/*
	 * �����ƶ�����Ҫ��ȫ�ֱ���
	 */
     //��һ�δ����������
	private int mlastPointerCount;
	//��һ�����ĵ��λ��
	private float mLastX;
	private float mLastY;
	//�ж��Ƿ��ƶ��ı�׼ֵ
	private int mTouchSlop;
	private boolean isCanDrag;
	//�Ƿ���Ҫ�������ұ߽���
	private boolean ischeckLeftAndRight;
	//�Ƿ���Ҫ���н������±߽���
	private boolean ischeckTopAndBottom;
	
	/*
	 * ˫���Ŵ�
	 */
	private GestureDetector mGestureDetector;
	private boolean isAutoScale;
	
	public ZoomImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setScaleType(ScaleType.MATRIX);
		mScaleGestureDetector = new ScaleGestureDetector(context, this);
		this.setOnTouchListener(this);
		mTouchSlop = ViewConfiguration.get(context).getTouchSlop();
		mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener()
		{
			@Override
			public boolean onDoubleTap(MotionEvent e) {
				if(isAutoScale)
					return true;
				float x = e.getX();
				float y = e.getY();
				
				if(getscale() < mMidScale){
					
					postDelayed(new AutoScaleRunnable(mMidScale, x, y), 16);
					isAutoScale = true;
				}else{
					postDelayed(new AutoScaleRunnable(mInitScale, x, y), 16);
					isAutoScale = true;
				}
				return true;
			}
		});
	}
	public class AutoScaleRunnable implements Runnable{

		//���ŵ�Ŀ��ֵ
		private float mTargetScale;
		//���ŵ����ĵ�
		private float x;
		private float y;
		
		private final float BIGGER = 1.07f;
		private final float SMALL = 0.93f;
		
		private float tmpScale;
		
		
		public AutoScaleRunnable(float mTargetScale, float x, float y) {
			super();
			this.mTargetScale = mTargetScale;
			this.x = x;
			this.y = y;
			
			if(getscale() < mTargetScale){
				tmpScale = BIGGER;
			}
			if(getscale() > mTargetScale){
				tmpScale = SMALL;
			}
		}


		@Override
		public void run() {
			// TODO Auto-generated method stub
			mScaleMatrix.postScale(tmpScale, tmpScale, x, y);
			checkBorderAndCenterWhenScale();
			setImageMatrix(mScaleMatrix);
			
			float currentScale = getscale();
			
			if((tmpScale > 1.0f && currentScale < mTargetScale) 
				|| (tmpScale < 1.0f && currentScale > mTargetScale)){
				
				postDelayed(this, 16);
			}else{
				//����Ŀ��ֵ
				float scale = mTargetScale / currentScale;
				mScaleMatrix.postScale(scale, scale, x, y);
				checkBorderAndCenterWhenScale();
				setImageMatrix(mScaleMatrix);
				isAutoScale = false;
			}
		}

	}

	public ZoomImageView(Context context) {
		this(context,null);
		
	}

	/**
	 * ��onAttachedToWindow
	 */
	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		getViewTreeObserver().addOnGlobalLayoutListener(this);
	}

	/**
	 * ��onDetachedFromWindow
	 */
	@SuppressWarnings("deprecation")
	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		getViewTreeObserver().removeGlobalOnLayoutListener(this);
	}

	/**
	 * ȫ�ֵĲ��ּ�����ɺ󣬵��ô˷��� �� ��ȡImageView������ɵ�ͼƬ,ʹͼƬ��������
	 */
	@Override
	public void onGlobalLayout() {
		if (!mOnce) {
			// �õ�ͼƬ���Լ���͸�
			Drawable d = getDrawable();
			if (d == null)
			   return;
			// �õ�ͼƬ�Ŀ��
			int width = getWidth();
			int height = getHeight();
			
			// �����Ŀ��.����������ͼƬ�Ŀ��
			int dw = d.getIntrinsicWidth();
			int dh = d.getIntrinsicHeight();


			float scale = 1.0f;// Ĭ�����ŵ�ֵ

			// ��ͼƬ�Ŀ�ߺͿؼ��Ŀ�����Աȣ����ͼƬ�Ƚ�С����ͼƬ�Ŵ󣬷�֮��Ȼ��
			// ���ͼƬ�Ŀ�ȴ��ڿؼ��Ŀ��,����ͼƬ�߶�С�ڿؼ��߶�
			if (dw > width && dh < height) {
				scale = width * 1.0f / dw;// ͼƬ̫���������
				Log.d("Debug", "ͼƬ��󣬸�С");
			}

			if (dh > height && dw < width) {
				scale = height * 1.0f / dh;// ͼƬ̫�ߣ��߶�����
				Log.d("Debug", "ͼƬ�ߴ󣬿�С");
			}
			

			if (dw < width && dh < height || dw > width && dh > height) {
				scale = Math.min(width * 1.0f / dw, height * 1.0f / dh);
				Log.d("Debug", "ͼƬ��߶��󣬻�С");
			}

			// �õ���ʼ�����ŵı���
			mInitScale = scale;// ԭ��С

			// ��ͼƬ�ƶ����ؼ�������
			int dx = getWidth() / 2 - dw / 2;// ��x���ƶ�dx����
			int dy = getHeight() / 2 - dh / 2;// ��y���ƶ�dx����

			/**
			 * matrix: xScale xSkew xTrans ��Ҫ9�� ySkew yScale yTrans 0 0 0
			 */
			mScaleMatrix.postTranslate(dx, dy);// ƽ��
			mScaleMatrix.postScale(mInitScale, mInitScale, width / 2,
					height / 2);// ����,������ʾwidth/2,height/2����
			setImageMatrix(mScaleMatrix);

			mOnce = true;
		}
	}

	//��ȡ��ǰͼƬ������ֵ
	public final float getscale(){
		
		mScaleMatrix.getValues(matrixValues);
		return matrixValues[Matrix.MSCALE_X];
		
	}
	
	//��������initScale~MaxScale
	@SuppressLint("NewApi")
	@Override
	public boolean onScale(ScaleGestureDetector detector) {
		float scale = getscale();
		float scaleFactor =  detector.getScaleFactor();//��㴥��ʱ��С�Ŵ��ֵ
		
		if(getDrawable() == null) 
			return true;
		
		//���ŷ�Χ�Ŀ���
		if((scale < mMaxScale && scaleFactor > 1.0f) 
				|| (scale > mInitScale && scaleFactor < 1.0f)){
			
			if(scale * scaleFactor < mInitScale){
				scaleFactor = mInitScale / scale;
			}
			if(scale * scaleFactor > mMaxScale){
				scaleFactor = mMaxScale / scale;
			}
			
			mScaleMatrix.postScale(scaleFactor, scaleFactor,detector.getFocusX(),detector.getFocusY());
			checkBorderAndCenterWhenScale();
		    setImageMatrix(mScaleMatrix);
		}
		
		return true;
	}
	
	//RectF��ʾһ�����Σ�����Ϊfloat����ȡͼƬ�Ŵ���С��Ŀ�͸ߣ��Լ���������4���ߵ�����
	private RectF getMatrixRectF()  
    {  
        Matrix matrix = mScaleMatrix;  
        RectF rect = new RectF();  
        Drawable d = getDrawable();  
        if (null != d)  
        {  
            rect.set(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());  
            matrix.mapRect(rect);  
        }  
        return rect;  
    }  
	
	//������ʱ���б߽�;��п���
	private void checkBorderAndCenterWhenScale()
	{
		RectF rect = getMatrixRectF();
		float deltaX = 0;
		float deltaY = 0;

		int width = getWidth();
		int height = getHeight();

		// ����ʱ���б߽��⣬��ֹ���ְױ�
		if (rect.width() >= width)
		{
			if (rect.left > 0)
			{
				deltaX = -rect.left;
			}
			if (rect.right < width)
			{
				deltaX = width - rect.right;
			}
		}
		if (rect.height() >= height)
		{
			if (rect.top > 0)
			{
				deltaY = -rect.top;
			}
			if (rect.bottom < height)
			{
				deltaY = height - rect.bottom;
			}
		}
		// �����Ȼ��߸߶�С�ڿؼ��Ŀ�Ȼ�߶ȣ��������
		if (rect.width() < width)
		{
			deltaX = width * 0.5f - rect.right + 0.5f * rect.width();
		}
		if (rect.height() < height)
		{
			deltaY = height * 0.5f - rect.bottom + 0.5f * rect.height();
		}
		Log.e(TAG, "deltaX = " + deltaX + " , deltaY = " + deltaY);
		mScaleMatrix.postTranslate(deltaX, deltaY);

	}
	
	

	@Override
	public boolean onScaleBegin(ScaleGestureDetector arg0) {
		
		return true;
	}

	@Override
	public void onScaleEnd(ScaleGestureDetector arg0) {
		
		
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if(mGestureDetector.onTouchEvent(event))
			return true;
		mScaleGestureDetector.onTouchEvent(event);
		
		float x = 0;
		float y = 0;
		
		int pointerCount = event.getPointerCount();
		
		for(int i = 0;i < pointerCount;i++){
			x += event.getX(i);
			y += event.getY(i);
		}
		
		x /= pointerCount;
		y /= pointerCount;
		
		if(mlastPointerCount != pointerCount){
			isCanDrag = false;
			mLastX = x;
			mLastY = y;
		}
		
		mlastPointerCount = pointerCount;
		RectF rectf = getMatrixRectF();
		switch(event.getAction()){
		case MotionEvent.ACTION_DOWN:
			if(rectf.width() > getWidth()+0.01 || rectf.height() > getHeight()){
				if(getParent() instanceof ViewPager)
				getParent().requestDisallowInterceptTouchEvent(true);
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if((rectf.width() > getWidth()+0.01) || (rectf.height() > getHeight())){
				if(getParent() instanceof ViewPager)
				getParent().requestDisallowInterceptTouchEvent(true);
			}
			float dx = x - mLastX;
			float dy = y - mLastY;
			
			if(!isCanDrag){
				isCanDrag = isMove(dx, dy);
			}
			
			if(isCanDrag){
				
				RectF rectf2 = getMatrixRectF();
				
				if(getDrawable() != null){
					ischeckLeftAndRight = ischeckTopAndBottom = true;
				//������С�ڿؼ���Ȳ���������ƶ�
				if(rectf2.width() < getWidth()){
					ischeckLeftAndRight = false;
					dx = 0;
				}
				
				//����߶�С�ڿؼ��߶Ȳ����������ƶ�
				if(rectf2.height() < getHeight()){
					ischeckTopAndBottom = false;
					dy = 0;
				}
				
				mScaleMatrix.postTranslate(dx, dy);
				checkBroderWhenTranslate();
				setImageMatrix(mScaleMatrix);
			}
			}
			mLastX = x;
			mLastY = y;
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			mlastPointerCount = 0;
			break;
		}
		
		
		return true;
	}

	//ͼƬ�ƶ��ǽ��б߽��жϣ���ֹ���ְױߣ�
	private void checkBroderWhenTranslate() {
		RectF rectf = getMatrixRectF();
		
		float delatX = 0;
		float delatY = 0;
		
		int width = getWidth();
		int height = getHeight();
		
		if(rectf.top > 0 && ischeckTopAndBottom){
			delatY = -rectf.top;
		}
		
		if(rectf.bottom < height && ischeckTopAndBottom){
			delatY = height - rectf.bottom;
		}
		
		if(rectf.left > 0 && ischeckLeftAndRight){
			delatX = -rectf.left;
		}
		
		if(rectf.right < width && ischeckLeftAndRight){
			delatX = width - rectf.right;
		}
		
		mScaleMatrix.postTranslate(delatX, delatY);
		
		
	}

	//�ж��Ƿ����Դ���move
	private boolean isMove(float dx, float dy) {
		
		return Math.sqrt(dx * dx + dy * dy) > mTouchSlop;
	}
}
