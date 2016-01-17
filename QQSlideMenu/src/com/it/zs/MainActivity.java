package com.it.zs;

import java.util.Random;

import com.it.zs.DragLayout.OnDragLayoutStateChangeListener;
import com.it.zs.R;
import com.nineoldandroids.animation.FloatEvaluator;
import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;

import android.R.color;
import android.os.Bundle;
import android.app.Activity;
import android.graphics.Color;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.CycleInterpolator;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends Activity {

	private ListView menuListView;
	private ListView mainListView;
	private DragLayout mDragLayout;
	private ImageView mHead;
	private FloatEvaluator fe;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		fe = new FloatEvaluator();
		menuListView = (ListView) findViewById(R.id.menu_listview);
		mainListView = (ListView) findViewById(R.id.main_listview);
		mHead = (ImageView) findViewById(R.id.iv_head);
		mainListView.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, Constant.NAMES));
		menuListView.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, Constant.sCheeseStrings) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				TextView tv = (TextView) super.getView(position, convertView,
						parent);
				tv.setTextColor(Color.WHITE);// 给字体设置颜色
				return tv;
			}
		});

		mDragLayout = (DragLayout) findViewById(R.id.DragLayout);

		mDragLayout
				.setOnDragLayoutStateChangeListener(new OnDragLayoutStateChangeListener() {

					@Override
					public void onSliding(float percent) {
						Log.e("tag",
								"=================onSliding==================");
						ViewHelper.setAlpha(mHead,
								fe.evaluate(percent, 1.0f, 0));
					}

					@Override
					public void onOpen() {
						Log.e("tag",
								"=================onOpen==================");
					
						// 随即滚动到摸个位置
						menuListView.smoothScrollToPosition(new Random()
								.nextInt(menuListView.getCount()));
					}

					@Override
					public void onClose() {
						Log.e("tag",
								"=================onClose==================");
						ViewPropertyAnimator.animate(mHead).translationX(10.0f)
								.setInterpolator(new CycleInterpolator(6))
								.setDuration(1000).start();
					}
				});
	}

}
