package com.libo.libs;

import android.content.Context;
import android.graphics.Rect;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

//1、定义组件
public class SlidingItemLayout extends FrameLayout implements ISlidingLayout {

    // 2、定义滑动视图摆放方向（采用枚举定义）
    public enum SlidingType {
        Left, Right;
    }

    // 3、定义滑动视图滑动状态（采用枚举）
    public enum SlidingStatus {
        Close, Open, Sliding;
    }

    // 4、初始化当前SlidingItemLayout条目子视图-内容视图
    private View contentView;
    // 4、初始化当前SlidingItemLayout条目子视图-功能视图
    private View functionView;
    private int horizontalDX;

    // 6.1.1 计算布局摆放的位置(矩形：left top right buttom)
    private SlidingType slidingType = SlidingType.Right;
    // 6.1.1 计算布局摆放的位置(矩形：left top right buttom)
    private SlidingStatus slidingStatus = SlidingStatus.Close;

    // 8.1 设置手势方向（水平方向：左、右）
    private GestureDetectorCompat detectorCompat;

    // 8.2 添加手势拖拽与视图之间回调接口
    private ViewDragHelper viewDragHelper;

    // 10、解决listView显示问题
    private OnSlideItemListener onSlideItemListener;

    public void setOnSlideItemListener(OnSlideItemListener onSlideItemListener) {
        this.onSlideItemListener = onSlideItemListener;
    }

    public OnSlideItemListener getOnSlideItemListener() {
        return onSlideItemListener;
    }

    public View getContentView() {
        return contentView;
    }

    public SlidingItemLayout(Context context) {
        super(context);
    }

    public SlidingItemLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        initView();

        initGesture();
    }

    // 4、初始化当前SlidingItemLayout条目子视图
    private void initView() {
        if (getChildCount() != 2) {
            throw new IllegalArgumentException("你的子视图只允许有两个");
        }
        contentView = getChildAt(0);
        functionView = getChildAt(1);

        initContentView();
    }

    // 5、滑动视图测量
    // 目的：计算滑动视图－滑动偏移量
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // 普及：测量有很多策略......mode
        // 默认：我的FunctionView有多宽，那么我的偏移量就多大
        // 以下是我的规范
        horizontalDX = functionView.getMeasuredWidth();
        contentViewWidth = contentView.getMeasuredWidth();
    }

    // 6、滑动视图摆放（onlayout方法）
    // 注意：滑动视图处于关闭状态
    // 6.1 摆放内容视图
    // 6.2 摆放功能视图
    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
                            int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        layoutView(false);
    }

    // 6、滑动视图摆放（onlayout方法）
    // 注意：滑动视图处于关闭状态
    // 6.1 摆放内容视图
    // 6.2 摆放功能视图
    // isOpen（true:代表默认打开 false：默认关闭）
    private void layoutView(boolean isOpen) {
        // 6.1 摆放内容视图
        // 6.1.1 计算布局摆放的位置
        Rect contentRect = layoutContentView(isOpen);
        // 6.1.2 摆放内容视图
        contentView.layout(contentRect.left, contentRect.top,
                contentRect.right, contentRect.bottom);

        // 6.2 摆放功能视图
        // 6.2.1 计算功能视图的位置
        Rect functionRect = layoutFunctionView(contentRect, isOpen);
        // 6.2.2 摆放功能视图
        functionView.layout(functionRect.left, functionRect.top,
                functionRect.right, functionRect.bottom);
    }

    // 6.1.1 计算布局摆放的位置(矩形：left top right buttom)
    private Rect layoutContentView(boolean isOpen) {
        int left = 0;
        // 处理true状态
        if (isOpen) {
            if (slidingType == SlidingType.Left) {
                // 功能视图摆放方向---左边
                left = horizontalDX;
            } else if (slidingType == SlidingType.Right) {
                // 功能视图摆放右边
                left = -horizontalDX;
            }
        }
        // 首先摆放默认情况--false状态
        return new Rect(left, 0, left + getMeasuredWidth(), getMeasuredHeight());
    }

    // 6.2 摆放功能视图
    // 6.2.1 计算功能视图的位置
    // isOpen:是否打开（代表打开功能视图）
    private Rect layoutFunctionView(Rect rect, boolean isOpen) {
        int left = 0;

        if (isOpen) {
            // 打开状态
            // 根据类型摆放
            if (slidingType == SlidingType.Right) {
                // 功能视图摆放在右边
                left = getMeasuredWidth() - horizontalDX;
            } else if (slidingType == SlidingType.Left) {
                // 功能视图摆放在左边
                left = 0;
            }
        } else {
            // 这个判断目的：关闭状态
            // 根据类型摆放
            if (slidingType == SlidingType.Right) {
                // 功能视图摆放在右边
                left = rect.right;
            } else if (slidingType == SlidingType.Left) {
                // 功能视图摆放在左边
                left = -horizontalDX;
            }
        }

        return new Rect(left, 0, left + horizontalDX,
                functionView.getMeasuredHeight());
    }

    // 8、手势处理
    private void initGesture() {
        // 8.1 设置手势方向（水平方向：左、右）
        detectorCompat = new GestureDetectorCompat(getContext(),
                onGestureListener);

        // 8.2 添加手势拖拽与视图之间回调接口
        viewDragHelper = ViewDragHelper.create(this, callback);
    }

    // 8.1 设置手势方向（水平方向：左、右）
    // 注意：将来你们写接口的时候，记得要给一个默认适配接口类
    private OnGestureListener onGestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                float distanceX, float distanceY) {
            // 处理方向
            // distanceX：代表x方向偏移量
            // distanceY：代表y方向偏移量
            // 注意：要取绝对值
            // 返回true：代表横向滑动
            // 返回false：代表纵向滑动
            return Math.abs(distanceX) >= Math.abs(distanceY);
        }
    };

    // 8.2 添加手势拖拽与视图之间回调接口
    private ViewDragHelper.Callback callback = new ViewDragHelper.Callback() {

        // 8.2.1 重写tryCaptureView方法
        // 目的：绑定拖拽视图
        // 记得：组件有两个视图（一个内容视图，一个是功能视图）
        // 内容视图：contentView
        // 功能视图: functionView
        @Override
        public boolean tryCaptureView(View view, int pointerId) {
            // view：当前拖拽的View
            // pointerId(扩展知识): 当前单点触控手指ID
            return view == contentView || view == functionView;
        }

        // 8.2.2 重写getViewHorizontalDragRange方法
        // 目的：设置滑动偏移量（不可能无限滑动）
        @Override
        public int getViewHorizontalDragRange(View child) {
            return horizontalDX;
        }

        ;

        // 8.2.3 重写clampViewPositionHorizontal方法
        // 目的：控制滚动的范围
        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            // childView：拖拽视图
            // left:距离父容器左边的距离
            // dx:当前拖拽视图偏移量
            int newLeft = left;

            // 从我们效果来看：contentView和FunctionView都可以滑动
            // 所以说我们要分开处理各自滑动范围
            // 8.2.3.1 控制contentView滑动视图范围
            if (child == contentView) {
                // 是不是又分为了左边和右边
                switch (slidingType) {
                    case Left:
                        // 左边－（代表是FunctionView摆放在左边）
                        // ContentView滑动的范围（0～horizontalDX）
                        if (newLeft < 0) {
                            newLeft = 0;
                        } else if (newLeft > horizontalDX) {
                            newLeft = horizontalDX;
                        }
                        break;
                    case Right:
                        // 右边-（代表是FunctionView摆放在左边）
                        // //ContentView滑动的范围（-horizontalDX～0）
                        if (newLeft < -horizontalDX) {
                            newLeft = -horizontalDX;
                        } else if (newLeft > 0) {
                            newLeft = 0;
                        }
                        break;
                }
            } else if (child == functionView) {
                // 当前拖拽是functionView
                switch (slidingType) {
                    case Left:
                        // 左边－（代表是FunctionView摆放在左边）
                        // 范围：-horizontalDX~0
                        if (newLeft < -horizontalDX) {
                            newLeft = -horizontalDX;
                        } else if (newLeft > 0) {
                            newLeft = 0;
                        }
                        break;
                    case Right:
                        // 右边-（代表是FunctionView摆放在左边）
                        // 范围：屏幕宽度-horizontalDX至屏幕宽度
                        if (newLeft < contentViewWidth - horizontalDX) {
                            newLeft = contentViewWidth - horizontalDX;
                        } else if (newLeft > contentViewWidth) {
                            newLeft = contentViewWidth;
                        }
                        break;
                }
            }
            return newLeft;
        }

        ;

        // 8.2.4 重写onViewPositionChanged方法
        // 目的：拖拽视图的时候，希望能够同时干一些其他事
        // 说白了拖拽ContentView的时候，希望FunctionView也要跟着动）
        @Override
        public void onViewPositionChanged(View changedView, int left, int top,
                                          int dx, int dy) {
            // changedView：代表当前拖拽视图
            // left:当前拖拽视图距离父容器左边距离
            // top:当前拖拽视图距离父容器顶部距离
            // dx:当前拖拽视图x方向偏移量（X方向）
            // dy:当前拖拽视图y方向偏移量（Y方向）
            // 两种情况
            // 8.2.4.1 第一种情况：拖拽ContentVeiw，FunctionView跟着动
            if (changedView == contentView) {
                // 移动FunctionView
                functionView.offsetLeftAndRight(dx);
            }

            // 8.2.4.2 第二种情况：拖拽FunctionView,ContentView跟着动
            if (changedView == functionView) {
                contentView.offsetLeftAndRight(dx);
            }

            // 8.2.4.3 随时随刻更新滑动视图状态
            updateSlidingStatus();

            // 8.2.4.4 更新视图
            invalidate();
        }

        ;

        // 8.2.5 重写onViewReleased方法
        // 目的：当我们拖拽手势弹起，我们需要做一些逻辑处理
        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            // releasedChild：代表你当前拖拽释放的视图
            // xvel:拖拽x方向速度
            // yvel:拖拽Y方向速度
            // 分为两种情况
            // 8.2.5.1 第一种情况：拖拽ContentView，释放之后要对contentView和functionView做处理
            if (releasedChild == contentView) {
                onContentViewReleased(xvel, yvel);
            }
            // 8.2.5.2 第二种情况：拖拽functionView，释放之后要对functionView和contentView做处理
            if (releasedChild == functionView) {
                onFunctionViewReleased(xvel, yvel);
            }
            // 更新视图
            invalidate();
        }

        ;

        @Override
        public void onViewDragStateChanged(int state) {
            // 视图状态发生改变回调
        }

        ;

    };

    // 8.2.4.3 随时随刻更新滑动视图状态
    private void updateSlidingStatus() {
        updateSlidingStatus(true);
    }

    // 8.2.4.3 随时随刻更新滑动视图状态
    // isNotify:是否更新(状态可控，灵活)
    private void updateSlidingStatus(boolean isNotify) {
        SlidingStatus status = getCurrentSlidingStatus();
        // slidingStatus:原始状态
        if (status != slidingStatus) {
            if (!isNotify && onSlideItemListener == null) {
                return;
            }
            // 这个里面你可以做一些外部回调
            if (status == SlidingStatus.Open) {
                this.onSlideItemListener.onOpen(this);
            } else if (status == SlidingStatus.Close) {
                this.onSlideItemListener.onClose(this);
            } else if (status == SlidingStatus.Sliding) {
                if (slidingStatus == SlidingStatus.Close) {
                    this.onSlideItemListener.onStartOpen(this);
                } else if (slidingStatus == SlidingStatus.Open) {
                    this.onSlideItemListener.onStartClose(this);
                }
            }
        }
        slidingStatus = status;
    }

    // 8.2.4.3 随时随刻更新滑动视图状态--获取当前视图状态
    // 注意：通过滑动偏移量控制（也可以通过left判断获取）
    private SlidingStatus getCurrentSlidingStatus() {
        int left = contentView.getLeft();
        if (left == 0) {
            return SlidingStatus.Close;
        }
        // left == horizontalDX(FunctionView摆放在左边)
        // left == -horizontalDX(FunctionView摆放在右边)
        if (left == horizontalDX || left == -horizontalDX) {
            return SlidingStatus.Open;
        }
        return SlidingStatus.Sliding;
    }

    // 8.2.5.1 第一种情况：拖拽ContentView，释放之后要对contentView和functionView做处理
    private void onContentViewReleased(float xvel, float yvel) {
        // 第一步：判断摆放方向
        switch (slidingType) {
            case Left:// FunctionView摆放在左边
                // 根据速度取判断
                if (xvel == 0) {
                    // 当前拖拽停下来了(需要判断拖拽停止之后，偏移量范围)
                    // 有一个拖拽范围（假设horizontalDX＝100）
                    // 如果你只拖拽<=20距离回弹，超过了20，去到指定100
                    // 说明：(horizontalDX * 0.5f) 这个值可以自己定义（只要合理即可）
                    if (contentView.getLeft() > horizontalDX * 0.5f) {
                        // 打开状态---打开滑动视图
                        openSlidingLayout(true);
                    } else {
                        closeSlidingLayout(true);
                    }
                } else if (xvel > 0) {
                    openSlidingLayout(true);
                } else {
                    closeSlidingLayout(true);
                }
                break;
            case Right:// FunctionView摆放在右边
                if (xvel == 0) {
                    // 右边的偏移量怎么计算（和左边相反）
                    if (contentView.getLeft() < -horizontalDX * 0.5f) {
                        openSlidingLayout(true);
                    } else {
                        closeSlidingLayout(true);
                    }
                } else if (xvel < 0) {
                    openSlidingLayout(true);
                } else {
                    closeSlidingLayout(true);
                }
                break;
        }
    }

    private int contentViewWidth;

    // 8.2.5.1 第一种情况：拖拽ContentView，释放之后要对contentView和functionView做处理
    // 打开滑动视图
    // isSmooth:是否在滑动的时候有动画
    public void openSlidingLayout(boolean isSmooth) {
        openSlidingLayout(isSmooth, true);
    }

    // 8.2.5.1 第一种情况：拖拽ContentView，释放之后要对contentView和functionView做处理
    // 打开滑动视图
    // isSmooth:是否在滑动的时候有动画
    // isNotify:是否更新视图状态
    // 注意：当我们的手势弹起的时候，要更新状态
    public void openSlidingLayout(boolean isSmooth, boolean isNotify) {
        if (isSmooth) {
            // 计算contentView left、right值
            // 目标位置
            Rect contentRect = layoutContentView(true);
            // 注意：smoothSlideViewTo帮助我们自动滚动视图
            // child：需要滚动的视图
            // finalLeft: X方向目标位置
            // finalTop: Y方向目标位置
            // 返回值:代表是否滚动完成 true：滚动到了目的地 false正在滚动
            if (viewDragHelper.smoothSlideViewTo(contentView, contentRect.left,
                    contentRect.top)) {
                // invalidate－－－刷新视图
                // 扩展知识（系统版本兼容）
                // 一般情况调用invalidate
                // 另外一种情况：ViewCompat.postInvalidateOnAnimation(this);
                // 区别：版本兼容
                // 问题：如果版本小于16是怎么处理？如果大于等于16是怎么处理？
                // 答案：低于16版本（源码分析得出结论：view.invalidate();）
                // 大于等于16（源码分析得出结论：view.postInvalidateOnAnimation();）
                ViewCompat.postInvalidateOnAnimation(this);
            }
        } else {
            // 重写摆放(关闭视图)
            layoutView(false);
            // 更新状态
            updateSlidingStatus(isNotify);
        }
    }

    // 8.2.5.1 第一种情况：拖拽ContentView，释放之后要对contentView和functionView做处理
    // 打开滑动视图
    // isSmooth:是否在滑动的时候有动画
    public void closeSlidingLayout(boolean isSmooth) {
        closeSlidingLayout(isSmooth, true);
    }

    // 8.2.5.1 第一种情况：拖拽ContentView，释放之后要对contentView和functionView做处理
    // 关闭滑动视图
    // isSmooth:是否在滑动的时候有动画
    // isNotify:是否更新视图状态
    // 注意：当我们的手势弹起的时候，要更新状态
    public void closeSlidingLayout(boolean isSmooth, boolean isNotify) {
        if (isSmooth) {
            // 计算contentView left、right值
            // 目标位置
            Rect contentRect = layoutContentView(false);
            // 注意：smoothSlideViewTo帮助我们自动滚动视图
            // child：需要滚动的视图
            // finalLeft: X方向目标位置
            // finalTop: Y方向目标位置
            // 返回值:代表是否滚动完成 true：滚动到了目的地 false正在滚动
            if (viewDragHelper.smoothSlideViewTo(contentView, contentRect.left,
                    contentRect.top)) {
                // invalidate－－－刷新视图
                // 扩展知识（系统版本兼容）
                // 一般情况调用invalidate
                // 另外一种情况：ViewCompat.postInvalidateOnAnimation(this);
                // 区别：版本兼容
                // 问题：如果版本小于16是怎么处理？如果大于等于16是怎么处理？
                // 答案：低于16版本（源码分析得出结论：view.invalidate();）
                // 大于等于16（源码分析得出结论：view.postInvalidateOnAnimation();）
                ViewCompat.postInvalidateOnAnimation(this);
            }
        } else {
            // 重写摆放
            layoutView(false);
            // 更新状态
            updateSlidingStatus(isNotify);
        }
    }

    // 8.2.5.2 第二种情况：拖拽functionView，释放之后要对functionView和contentView做处理
    private void onFunctionViewReleased(float xvel, float yvel) {
        // 第一步：判断摆放方向
        switch (slidingType) {
            case Left:// FunctionView摆放在左边
                // 根据速度取判断
                if (xvel == 0) {
                    // 当前拖拽停下来了(需要判断拖拽停止之后，偏移量范围)
                    // 有一个拖拽范围（contentViewWidth - horizontalDX 至 contentView.width）
                    // 偏移多少我就打开菜单（常量 contentViewWidth-horizontalDX * 0.5f）
                    if (functionView.getLeft() > (-horizontalDX * 0.5f)) {
                        openSlidingLayout(true);
                    } else {
                        closeSlidingLayout(true);
                    }
                } else if (xvel > 0) {
                    // 方向X轴正方向
                    openSlidingLayout(true);
                } else {
                    closeSlidingLayout(true);
                }
                break;
            case Right:// FunctionView摆放在右边
                // 根据速度取判断
                if (xvel == 0) {
                    // 当前拖拽停下来了(需要判断拖拽停止之后，偏移量范围)
                    // 有一个拖拽范围（contentViewWidth - horizontalDX 至 contentView.width）
                    // 偏移多少我就打开菜单（常量 contentViewWidth-horizontalDX * 0.5f）
                    if (functionView.getLeft() < (contentViewWidth - horizontalDX * 0.5f)) {
                        openSlidingLayout(true);
                    } else {
                        closeSlidingLayout(true);
                    }
                } else if (xvel < 0) {
                    // 方向X轴正方向
                    openSlidingLayout(true);
                } else {
                    closeSlidingLayout(true);
                }
                break;
        }
    }

    // 9.2 初始化内容视图，绑定监听，SlidingItemLayout实现该接口
    private void initContentView() {
        if (contentView instanceof SlidingContentView) {
            SlidingContentView slidingContentView = (SlidingContentView) contentView;
            slidingContentView.setSlidingLayout(this);
        }
    }

    @Override
    public SlidingStatus getCurrentStaus() {
        return getCurrentSlidingStatus();
    }

    @Override
    public void close() {
        closeSlidingLayout(true);
    }

    @Override
    public void open() {
        openSlidingLayout(true);
    }

    // 9.3 事件分发给我们的ContentView
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return viewDragHelper.shouldInterceptTouchEvent(ev)
                & detectorCompat.onTouchEvent(ev);
    }

    // 9.3 事件分发给我们的ContentView（触摸事件）
    private float downX;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 处理按下，移动，弹起
        switch (MotionEventCompat.getActionMasked(event)) {
            case MotionEvent.ACTION_DOWN:
                downX = event.getRawX();
                break;
            case MotionEvent.ACTION_MOVE:
                float x = event.getRawX() - downX;
                // 什么时候拦截？
                // 这个getTouchSlop是默认滑动最小距离
                // 通过源码分析得知，系统默认最小值：8
                // private static final int TOUCH_SLOP = 8;
                if (x > viewDragHelper.getTouchSlop()) {
                    // 父容器不要拦截我的事件，我要处理
                    requestDisallowInterceptTouchEvent(true);
                }
                break;
            case MotionEvent.ACTION_UP:
                downX = 0;
                break;
        }

        // 执行触摸事件
        try {
            viewDragHelper.processTouchEvent(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public void computeScroll() {
        // 以下代码是什么意思？
        // viewDragHelper.continueSettling(true)：控制是否滑动结束
        // true：代表可以滑动（说白了继续滑） false：滑动结束
        // 老师你怎么知道？---答案：源码解析得出结论
        // 再来一个问题？
        // 为什么传true？不能传false？
        // 有什么区别？
        // 根据源码得出
        // 设置为true：代表回调onViewDragStateChanged方法
        // 设置false：回调onViewDragStateChanged方法不被执行
        if (viewDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

}
