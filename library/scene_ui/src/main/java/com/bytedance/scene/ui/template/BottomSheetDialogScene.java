package com.bytedance.scene.ui.template;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.bytedance.scene.Scene;
import com.bytedance.scene.navigation.OnBackPressedListener;

/**
 * 底部对话框 BottomSheetDialogScene 的快速实现类
 *
 * 继承自这个类后重写 {@link #setBottomSheetDialogLayoutID()} 来设置布局的 id
 * 这里需要注意的是，布局的最外层的宽高设置是无效的，会被覆盖为宽填充父布局，高适应大小
 * 如果需要调整大小，可以在 {@link #afterInflateView(View)} 后修改其 LayoutParams 参数
 * 在 {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}中加载完布局之后，
 * 会调用 {@link #afterInflateView(View)}，可以在这个里面进行一些布局的设置和绑定
 * 当 dialog 发生移动时，会回调 {@link #onTranslationYChange(float)}，
 * 可以主动调用 {@link #setTranslationY(float)} 来设置偏移
 *
 * - 默认可以向下滑动 dialog，如果你不想让他可以滑动，可以调用 {@link #setDisableSwipe(boolean)} 来禁用
 * - 默认可以点击空白处/按返回键/向下滑动超过 60% 来 pop dialog，
 * 可以通过 {@link #isCancelable()} 来禁用 pop 行为，和 {@link #setPercentToClose(float)} 来设置滑动关闭的阈值
 *
 * @author gtf35
 * 2020/3/14
 */
public abstract class BottomSheetDialogScene extends Scene {
    // 容器布局
    private FrameLayout mContainerLayout;
    // 用户的view
    private View mUserView;
    // 触摸管理者
    private TouchHelper mTouchHelper = new TouchHelper();
    // 向下拖动以至于关闭的百分比
    private float mPercentToClose = 0.6f;
    // 可取消（可滑动到底部取消/可按返回键取消/可点击空白处取消)
    private boolean mIsCancelable = true;

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 构建容器布局
        mContainerLayout = new FrameLayout(requireSceneContext());
        // 引入用户的布局
        mUserView = inflater.inflate(setBottomSheetDialogLayoutID(), null);
        // 设置用户的布局底部对齐
        FrameLayout.LayoutParams userViewLP = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        userViewLP.gravity = Gravity.BOTTOM;
        mUserView.setLayoutParams(userViewLP);
        mContainerLayout.addView(mUserView);
        // 设置可被点击
        mContainerLayout.setClickable(true);
        // 回调用户
        afterInflateView(mUserView);
        // 设置滑动监听
        setSwipeListener();
        // 设置返回键处理
        setBackKeyListener();
        // 返回容器布局
        return mContainerLayout;
    }

    /**
     * 设置滑动监听
     */
    @SuppressLint("ClickableViewAccessibility")
    private void setSwipeListener() {
        View.OnTouchListener listener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mTouchHelper.setTouchAny(event);
                return mTouchHelper.shouldEatTouchEvent;
            }
        };
        mContainerLayout.setOnTouchListener(listener);
    }

    /**
     * 设置返回键处理
     */
    private void setBackKeyListener() {
        requireNavigationScene().addOnBackPressedListener(this, new OnBackPressedListener() {
            @Override
            public boolean onBackPressed() {
                // 不可取消的对话框不可点击
                return !mIsCancelable;
            }
        });
    }

    /**
     * 设置禁用滑动
     *
     * @param isDisable 如果禁用传入 true
     */
    public void setDisableSwipe(boolean isDisable) {
        mTouchHelper.isDisableSwipe = isDisable;
    }

    /**
     * 获取禁用滑动
     *
     * @return true 如果目前是禁用状态
     */
    public boolean isDisableSwipe() {
        return mTouchHelper.isDisableSwipe;
    }

    /**
     * 设置是否可取消
     */
    public void setCancelable(boolean isCancelable) {
        mIsCancelable = isCancelable;
    }

    /**
     * 获取是否可取消
     *
     * @return true 如果可取消
     */
    public boolean isCancelable() {
        return mIsCancelable;
    }

    /**
     * 获取自动关闭的百分比 0-1
     *
     * @return 自动关闭的百分比 0-1
     */
    public float getPercentToClose() {
        return mPercentToClose;
    }

    /**
     * 设置自动关闭的百分比 0-1
     *
     * @param percent 当前自动关闭的百分比 0-1
     */
    public void setPercentToClose(float percent) {
        if (percent > 1 || percent < 0) throw new RuntimeException("PercentToClose should in 1-0");
        mPercentToClose = percent;
    }

    /**
     * 设置卡片的纵向偏移
     *
     * @param translationY 纵向偏移
     */
    public void setTranslationY(float translationY) {
        mTouchHelper.setTranslationY(translationY);
    }

    /**
     * 获取卡片的纵向偏移
     *
     * @return 当前卡片的纵向偏移
     */
    public float getTranslationY() {
        return mContainerLayout.getTranslationY();
    }

    /**
     * 触摸管理器
     */
    private class TouchHelper {
        // 上次触摸的Y
        private float mLastTouchY = 0;
        private float mLastTouchRawY = 0;
        // 上次的偏移量
        private float mLastTranslationY = 0;
        // 是否应该吞了触摸事件
        private boolean shouldEatTouchEvent = false;
        // 是否应该 pop
        private boolean shouldPopThisScene = false;
        // 禁用滑动
        private boolean isDisableSwipe = false;
        // 移动过(移动过就不能抬手取消啦，因为这就不是点击了)
        private boolean isMoved = false;

        void setTouchAny(MotionEvent event) {
            // 分流点击事件
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    setTouchActionDown(event);
                    break;
                case MotionEvent.ACTION_MOVE:
                    setTouchActionMove(event);
                    break;
                case MotionEvent.ACTION_UP:
                    setTouchActionUp(event);
                    break;
            }
        }

        /**
         * 处理 ACTION_DOWN
         *
         * @param event 触摸事件
         */
        void setTouchActionDown(MotionEvent event) {
            // 重置移动过标志位
            isMoved = false;
            // 保存点下去的 Y 坐标
            mLastTouchY = event.getY();
            mLastTouchRawY = event.getRawY();
            mLastTranslationY = mContainerLayout.getTranslationY();
            // 后续一切交给 move 处理，来一起吞了这个事件，除非他点在了外边还不可取消
            if (isTouchOutSideUserView(event) && !isCancelable()) {
                shouldEatTouchEvent = false;
                return;
            }
            shouldEatTouchEvent = true;
        }

        /**
         * 处理 ACTION_UP
         *
         * @param event 触摸事件
         */
        void setTouchActionUp(MotionEvent event) {
            // 保存点下去的 Y 坐标
            mLastTouchY = event.getY();
            mLastTouchRawY = event.getRawY();
            // 抬手不吃事件
            shouldEatTouchEvent = false;
            // 如果抬手的时候在外边，而且可取消，dialog 没移动
            if (isTouchOutSideUserView(event) && mIsCancelable && !isMoved) {
                // 小伙计点在了外边，收起来
                if (getNavigationScene() != null) getNavigationScene().pop();
                return;
            }
            // 看看应不应该 pop 掉当前的 scene
            if (shouldPopThisScene && mIsCancelable) {
                // 看来需要 pop 掉了
                if (getNavigationScene() != null) getNavigationScene().pop();
            } else {
                // 嘿，不需要 pop，我们就让他带着动画弹起
                ExpandWithAnim();
            }
        }

        /**
         * 处理 ACTION_MOVE
         *
         * @param event 触摸事件
         */
        void setTouchActionMove(MotionEvent event) {
            // 要是不可滑动而且不可取消，直接不吃事件，让 up 不响应
            if (isDisableSwipe && !isCancelable()) {
                shouldEatTouchEvent = false;
                return;
            }
            // 点在外边时，就不用移动啦
            if (isTouchOutSideUserView(event)) {
                shouldEatTouchEvent = false;
                return;
            }
            // 本次移动距离
            float moveY = event.getY() - mLastTouchY;
            float moveRawY = event.getRawY() - mLastTouchRawY;
            // 本次移动之后，容器布局的Y轴偏移量
            float translationY = mContainerLayout.getTranslationY() + moveY;
            // 如果偏移量小于0，证明底部已经高于屏幕了，就不能再拉了
            if (translationY < 0) translationY = 0;
            // 计算一下现在用户布局漏在外边的区域
            float userViewOutsideHeight = mUserView.getHeight() - mLastTranslationY;
            // 进度
            float progress = moveRawY / userViewOutsideHeight;
            // 如果这次拖动的距离大于用户布局的70%(打比方)，那么就给他 pop 掉
            shouldPopThisScene = progress > mPercentToClose;
            // 最后确认，如果不在外边就写入移动
            if (!isTouchOutSideUserView(event) && !isDisableSwipe) setTranslationY(translationY);
            // 后续还有 up 要接受，就吞下事件
            shouldEatTouchEvent = true;
        }

        /**
         * 带着动画展开
         */
        private void ExpandWithAnim() {
            ValueAnimator showAnim = ValueAnimator.ofFloat(mContainerLayout.getTranslationY(), 0);
            showAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    setTranslationY((Float)animation.getAnimatedValue());
                }
            });
            showAnim.start();
        }

        /**
         * 统一设置偏移量，主要是在这里回调一下
         */
        private void setTranslationY(float translationY) {
            // 记录下已经移动过了
            isMoved = true;
            // 回调参数
            onTranslationYChange(translationY);
            // 偏移 View
            mContainerLayout.setTranslationY(translationY);
        }

        private boolean isTouchOutSideUserView(MotionEvent event) {
            float rawFingerY = event.getRawY();
            int[] location = new int[2];
            mUserView.getLocationOnScreen(location);
            float rawUserViewY = location[1];
            return rawFingerY < rawUserViewY;
        }
    }

    /**
     * 设置底部对话框的布局ID
     *
     * @return 你要设置的布局ID
     */
    public abstract @LayoutRes int setBottomSheetDialogLayoutID();

    /**
     * dialog 偏移回调
     *
     * @param translationY 偏移量
     */
    public abstract void onTranslationYChange(float translationY);

    /**
     * 在加载了布局之后
     *
     * @param view 加载进去了的布局
     */
    public abstract void afterInflateView(View view);

}