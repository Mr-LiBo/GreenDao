package com.libo.libs;


//9、自定义内容视图
//9.1 提供接口
//目的：该接口给我们的ContentView使用，回调到SlidingItemLayout
public interface ISlidingLayout {
	// 当前视图状态
	public SlidingItemLayout.SlidingStatus getCurrentStaus();

	// 关闭
	public void close();

	// 打开
	public void open();
}
