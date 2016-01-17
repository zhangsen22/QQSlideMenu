package com.it.zs;

import com.nineoldandroids.animation.FloatEvaluator;
import com.nineoldandroids.view.ViewHelper;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public class DragLayout extends FrameLayout {

	private ViewDragHelper mDragHelper;
	private View mainView;
	private View menuView;
	private int dragRange;

	public DragLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initView();
	}

	public DragLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView();
	}

	public DragLayout(Context context) {
		super(context);
		initView();
	}

	private void initView() {
		// 1：实例化一个ViewDragHelper对象
		mDragHelper = ViewDragHelper.create(this, callback);
		fe = new FloatEvaluator();
	}

	// 2：将处理触摸事件的方法，交给ViewDragHelper处理
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		boolean result = mDragHelper.shouldInterceptTouchEvent(ev);
		return result;
	};

	public boolean onTouchEvent(MotionEvent event) {
		mDragHelper.processTouchEvent(event);
		return true;
	};

	// 3:创建一个监听器，注册给ViewDragHelper
	ViewDragHelper.Callback callback = new ViewDragHelper.Callback() {

		@Override
		public boolean tryCaptureView(View child, int pointerId) {

			return child == mainView || child == menuView;
		}

		// 控制View的水平方向移动
		public int clampViewPositionHorizontal(View child, int left, int dx) {
			if (child == mainView) {
				if (left < 0) {
					left = 0;
				} else if (left > dragRange) {
					left = (int) dragRange;
				}
			}

			return left;
		}

		// 控制子View的触摸事件
		// 比如：mianView控件中listview，如果我们要触摸listview实现滑动的效果，就调用此方法，返回值大于零即可
		@Override
		public int getViewHorizontalDragRange(View child) {

			return (int) dragRange;
		}

		public void onViewPositionChanged(View changedView, int left, int top,
				int dx, int dy) {

			if (changedView == menuView) {
				// 让menuView触摸不滑动,就调此方法
				menuView.layout(0, 0, menuView.getMeasuredWidth(),
						menuView.getMeasuredHeight());

				// 让mainView滑动
				int newLeft = mainView.getLeft() + dx;
				if (newLeft < 0) {
					newLeft = 0;
				} else if (newLeft > dragRange) {
					newLeft = (int) dragRange;
					System.out.println("newLeft:"+newLeft);
				}
				mainView.layout(newLeft, mainView.getTop() + dy,
						mainView.getLeft() + dx + mainView.getMeasuredWidth(),
						mainView.getTop() + dy + mainView.getMeasuredHeight());
			}

			float percent = mainView.getLeft() * 1.0f / dragRange;
			
			System.out.println("mainView.getLeft():"+mainView.getLeft());
			
			executAnimation(percent);
			
			if(mainView.getLeft() == 0){
				//关闭
				if(mListener != null){
					mListener.onClose();
					currentState = EnumState.CLOSE;
				}
			}else if(mainView.getLeft() == dragRange){
				
				//打开状态
				if(mListener != null){
					mListener.onOpen();
					currentState = EnumState.OPEN;
				}
				
			}else{
				//滑动状态
				if(mListener != null){
					mListener.onSliding(percent);
					currentState = EnumState.SLIDING;
				}
			}
			
		}

		// 手指抬起触摸调用此方法,就是手指离开子View时调用
		@Override
		public void onViewReleased(View releasedChild, float xvel, float yvel) {

			if (mainView.getLeft() <= dragRange/2) {
				// 缓缓向左移动
				mDragHelper.smoothSlideViewTo(mainView, 0, mainView.getTop());
				ViewCompat.postInvalidateOnAnimation(DragLayout.this);
			} else if (mainView.getLeft() > dragRange/2) {
				// 缓缓向右移动
				mDragHelper.smoothSlideViewTo(mainView, (int) dragRange,
						mainView.getTop());
				ViewCompat.postInvalidateOnAnimation(DragLayout.this);
			}
		}

	};
	private FloatEvaluator fe;

	public void computeScroll() {
		if (mDragHelper.continueSettling(true)) {
			ViewCompat.postInvalidateOnAnimation(DragLayout.this);
		}
	};

	// 动画
	protected void executAnimation(float percent) {

		ViewHelper.setScaleX(mainView, fe.evaluate(percent, 1.0f, 0.7f));
		ViewHelper.setScaleY(mainView, fe.evaluate(percent, 1.0f, 0.7f));
		ViewHelper.setTranslationX(menuView,
				fe.evaluate(percent, -menuView.getMeasuredWidth() / 2, 0));
		ViewHelper.setAlpha(menuView, fe.evaluate(percent, 0, 1.0f));
		ViewHelper.setScaleX(menuView, fe.evaluate(percent, 0.7f, 1.0f));
		ViewHelper.setScaleY(menuView, fe.evaluate(percent, 0.7f, 1.0f));
	}

	// 当Onmeasured方法执行完毕后就执行此方法
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {

		dragRange = (int) (this.getMeasuredWidth() * 0.6f);
		System.out.println("dragRange:"+dragRange);
	};

	protected void onFinishInflate() {

		mainView = getChildAt(1);
		menuView = getChildAt(0);
	};
	
	/**
	 * DragLayout的打开，关闭，滑动状态的监听
	 */
	public interface OnDragLayoutStateChangeListener{
		public void onOpen();
		public void onClose();
		public void onSliding(float percent);
	}
	//初始状态关闭
	private OnDragLayoutStateChangeListener mListener;
	private EnumState currentState = EnumState.CLOSE;
	
	/**
	 * 获得当前SlideMenu的状态
	 * @return
	 */
	public EnumState getCurrentState() {
		return currentState;
	}

	public void setOnDragLayoutStateChangeListener(OnDragLayoutStateChangeListener listener){
		
		this.mListener = listener;
	}
	
	public enum EnumState{
		OPEN,CLOSE,SLIDING;
	}
}
