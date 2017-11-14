package com.libo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.ListView;

import com.libo.db.DaoSession;
import com.libo.db.UserDao;
import com.libo.libs.SlideListAdapter;
import com.libo.R;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
	private SlideListAdapter adapter;
	DaoSession daoSession;
	private List<User> list;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		initData();
		initMainContent();


	}

	private void initData() {
		MyApplication myApplication = (MyApplication) getApplication();
		daoSession = myApplication.getDaoSession();

		UserDao dao = daoSession.getUserDao();
		List<User> userList = new ArrayList<>();
		for (int i =0 ;i<5;i++)
		{
			User user = new User(i+"id",i+"name",i+"age",i+"sex",i+"salary");
			userList.add(user);
		}
		dao.insertOrReplaceInTx(userList);

		list = dao.queryBuilder().listLazy();
	}

	private void initMainContent() {
		// 15、第二个问题处理：当滑动视图处于打开状态，滑动列表控件，那么之前已打开的滑动视图需要关闭？
		// 解决方案：需要给列表视图添加OnScrollListener监听
		ListView lv_sliding = (ListView) findViewById(R.id.lv_sliding);
		adapter = new SlideListAdapter(MainActivity.this);
		lv_sliding.setAdapter(adapter);
		lv_sliding.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				//正在滑动，立马将之前的已打开的视图关闭
				if (scrollState == OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
					adapter.getSlideManager().closeAllLayout();
				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
			}
		});
		adapter.setAdapterData(list);
	}

	@Override
	protected void onResume() {
		super.onResume();

	}
}
