package com.libo.libs;



import com.libo.libs.SlidingItemLayout.SlidingStatus;
import com.libo.libs.SlidingItemLayout.SlidingType;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

public class SlidingContentView extends LinearLayout {

	private ISlidingLayout slidingLayout;

	public void setSlidingLayout(ISlidingLayout slidingLayout) {
		this.slidingLayout = slidingLayout;
	}

	public SlidingContentView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SlidingContentView(Context context) {
		super(context);
	}

	// 拦截事件
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if (slidingLayout.getCurrentStaus() == SlidingStatus.Close) {
			// 不需要拦截
			return super.onInterceptTouchEvent(ev);
		}
		return true;
	}

	// 触摸事件传递
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (slidingLayout.getCurrentStaus() == SlidingStatus.Close) {
			return super.onTouchEvent(event);
		} else {
			if (event.getActionMasked() == MotionEvent.ACTION_UP) {
				slidingLayout.close();
			}
			return true;
		}
	}

}
