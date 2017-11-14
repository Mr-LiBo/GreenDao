package com.libo.libs;

/**
 * 10 定义回调监听
 * 
 * @author Dream
 * 
 */
public interface OnSlideItemListener {
	
	//关闭
	public void onClose(SlidingItemLayout swipeLayout);

	//打开
	public void onOpen(SlidingItemLayout swipeLayout);

	//开始关闭
	public void onStartClose(SlidingItemLayout swipeLayout);

	//开始打开
	public void onStartOpen(SlidingItemLayout swipeLayout);
}
