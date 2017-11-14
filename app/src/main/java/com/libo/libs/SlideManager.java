package com.libo.libs;

import java.util.HashSet;

//统一管理回调监听
public class SlideManager {

	private HashSet<SlidingItemLayout> mUnClosedLayouts = new HashSet<SlidingItemLayout>();

	private OnSlideItemListener onSlideItemListener = new OnSlideItemListener() {
		@Override
		public void onOpen(SlidingItemLayout swipeLayout) {
			mUnClosedLayouts.add(swipeLayout);
		}

		@Override
		public void onClose(SlidingItemLayout swipeLayout) {
			mUnClosedLayouts.remove(swipeLayout);
		}

		@Override
		public void onStartClose(SlidingItemLayout swipeLayout) {
		}

		@Override
		public void onStartOpen(SlidingItemLayout swipeLayout) {
			closeAllLayout();
			mUnClosedLayouts.add(swipeLayout);
		}
	};

	public OnSlideItemListener getOnSlideItemListener() {
		return onSlideItemListener;
	}

	public int getUnClosedCount() {
		return mUnClosedLayouts.size();
	}

	/**
	 * 关闭所有的已打开视图
	 */
	public void closeAllLayout() {
		if (mUnClosedLayouts.size() == 0)
			return;

		for (SlidingItemLayout l : mUnClosedLayouts) {
			l.closeSlidingLayout(true, false);
		}
		mUnClosedLayouts.clear();
	}
}
