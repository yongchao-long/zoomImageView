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
	 * 值进行一次的图片缩放处理
	 */
	private boolean mOnce = false;

	/**
	 * 初始化时缩放的值
	 */
	private float mInitScale = 1.0f;
	/**
	 * 双击放大达到的值
	 */
	private float mMidScale = 2.0f; // 缩放的值在mInitScale和mMaxScale之间
	/**
	 * 放大的极限
	 */
	private float mMaxScale = 4.0f;
	
	//matrix的矩阵
	private final float[] matrixValues = new float[9];

	/**
	 * 控制缩放位移的矩阵
	 */
	private final Matrix mScaleMatrix = new Matrix();
	
	//获取用户多点触碰时的缩放比例
	private ScaleGestureDetector mScaleGestureDetector = null;
	
	private static final String TAG = ZoomImageView.class.getSimpleName();
	
	/*
	 * 自由移动所需要的全局变量
	 */
     //上一次触摸点的数量
	private int mlastPointerCount;
	//上一次中心点的位置
	private float mLastX;
	private float mLastY;
	//判断是否移动的标准值
	private int mTouchSlop;
	private boolean isCanDrag;
	//是否需要进行左右边界检测
	private boolean ischeckLeftAndRight;
	//是否需要进行进行上下边界检测
	private boolean ischeckTopAndBottom;
	
	/*
	 * 双击放大
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

		//缩放的目标值
		private float mTargetScale;
		//缩放的中心点
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
				//设置目标值
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
	 * 当onAttachedToWindow
	 */
	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		getViewTreeObserver().addOnGlobalLayoutListener(this);
	}

	/**
	 * 当onDetachedFromWindow
	 */
	@SuppressWarnings("deprecation")
	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		getViewTreeObserver().removeGlobalOnLayoutListener(this);
	}

	/**
	 * 全局的布局加载完成后，调用此方法 。 获取ImageView加载完成的图片,使图片居中缩放
	 */
	@Override
	public void onGlobalLayout() {
		if (!mOnce) {
			// 得到图片，以及宽和高
			Drawable d = getDrawable();
			if (d == null)
			   return;
			// 得到图片的宽高
			int width = getWidth();
			int height = getHeight();
			
			// 拉伸后的宽度.而不是真正图片的宽度
			int dw = d.getIntrinsicWidth();
			int dh = d.getIntrinsicHeight();


			float scale = 1.0f;// 默认缩放的值

			// 将图片的宽高和控件的宽高作对比，如果图片比较小，则将图片放大，反之亦然。
			// 如果图片的宽度大于控件的宽度,并且图片高度小于控件高度
			if (dw > width && dh < height) {
				scale = width * 1.0f / dw;// 图片太宽，宽度缩放
				Log.d("Debug", "图片宽大，高小");
			}

			if (dh > height && dw < width) {
				scale = height * 1.0f / dh;// 图片太高，高度缩放
				Log.d("Debug", "图片高大，宽小");
			}
			

			if (dw < width && dh < height || dw > width && dh > height) {
				scale = Math.min(width * 1.0f / dw, height * 1.0f / dh);
				Log.d("Debug", "图片宽高都大，或都小");
			}

			// 得到初始化缩放的比例
			mInitScale = scale;// 原大小

			// 将图片移动到控件的中心
			int dx = getWidth() / 2 - dw / 2;// 向x轴移动dx距离
			int dy = getHeight() / 2 - dh / 2;// 向y轴移动dx距离

			/**
			 * matrix: xScale xSkew xTrans 需要9个 ySkew yScale yTrans 0 0 0
			 */
			mScaleMatrix.postTranslate(dx, dy);// 平移
			mScaleMatrix.postScale(mInitScale, mInitScale, width / 2,
					height / 2);// 缩放,正常显示width/2,height/2中心
			setImageMatrix(mScaleMatrix);

			mOnce = true;
		}
	}

	//获取当前图片的缩放值
	public final float getscale(){
		
		mScaleMatrix.getValues(matrixValues);
		return matrixValues[Matrix.MSCALE_X];
		
	}
	
	//缩放区间initScale~MaxScale
	@SuppressLint("NewApi")
	@Override
	public boolean onScale(ScaleGestureDetector detector) {
		float scale = getscale();
		float scaleFactor =  detector.getScaleFactor();//多点触碰时缩小放大的值
		
		if(getDrawable() == null) 
			return true;
		
		//缩放范围的控制
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
	
	//RectF表示一个矩形，类型为float，获取图片放大缩小后的宽和高，以及左右上下4条边的坐标
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
	
	//在缩放时进行边界和居中控制
	private void checkBorderAndCenterWhenScale()
	{
		RectF rect = getMatrixRectF();
		float deltaX = 0;
		float deltaY = 0;

		int width = getWidth();
		int height = getHeight();

		// 缩放时进行边界检测，防止出现白边
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
		// 如果宽度或者高度小于控件的宽度或高度，让其居中
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
				//如果宽度小于控件宽度不允许横向移动
				if(rectf2.width() < getWidth()){
					ischeckLeftAndRight = false;
					dx = 0;
				}
				
				//如果高度小于控件高度不允许纵向移动
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

	//图片移动是进行边界判断，防止出现白边；
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

	//判断是否足以触发move
	private boolean isMove(float dx, float dy) {
		
		return Math.sqrt(dx * dx + dy * dy) > mTouchSlop;
	}
}
