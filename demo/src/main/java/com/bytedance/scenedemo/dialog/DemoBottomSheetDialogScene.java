package com.bytedance.scenedemo.dialog;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.bytedance.scene.ui.template.BottomSheetDialogScene;
import com.bytedance.scenedemo.R;

/**
 * 底部对话框 BottomSheetDialogScene 的快速实现演示，详细使用请阅读 {@link BottomSheetDialogScene}
 *
 * - 默认可以向下滑动 dialog，如果你不想让他可以滑动，可以调用 {@link #setDisableSwipe(boolean)} 来禁用
 * - 默认可以点击空白处/按返回键/向下滑动超过 60% 来 pop dialog，
 * 可以通过 {@link #isCancelable()} 来禁用 pop 行为，和 {@link #setPercentToClose(float)} 来设置滑动关闭的阈值
 *
 * @author gtf35
 * 2020/3/14
 */
public class DemoBottomSheetDialogScene extends BottomSheetDialogScene {

    /**
     * 设置底部对话框的布局ID
     *
     * @return 你要设置的布局ID
     */
    @Override
    public int setBottomSheetDialogLayoutID() {
        return R.layout.layout_bottom_sheet_dialog_demo;
    }

    /**
     * dialog 偏移回调
     *
     * @param translationY 偏移量
     */
    @SuppressLint("SetTextI18n")
    @Override
    public void onTranslationYChange(float translationY) {
        TextView msgTv = getView().findViewById(R.id.tv_bottom_dialog_demo_text);
        msgTv.setText("偏移量 " + translationY);
    }

    /**
     * 在加载了布局之后
     *
     * @param view 加载进去了的布局
     */
    @Override
    public void afterInflateView(View view) {
        // 给按钮设置一个 pop 当前的点击事件
        view.findViewById(R.id.btn_bottom_dialog_demo_hide).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 返回，如果之前正确设置了 BottomDialogSceneAnimatorExecutor 动画的话
                // pop 的时候就是向下滑出的动画
                requireNavigationScene().pop();
            }
        });

        // 动态的启用/禁用滑动
        view.findViewById(R.id.btn_bottom_dialog_demo_disable_swipe).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDisableSwipe(!isDisableSwipe());
                Button button = (Button)v;
                button.setText(isDisableSwipe()? "启用滑动": "禁用滑动");
            }
        });

        // 动态的启用/禁用可取消
        view.findViewById(R.id.btn_bottom_dialog_demo_disable_cancelable).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCancelable(!isCancelable());
                Button button = (Button)v;
                button.setText(isCancelable()? "禁用可取消": "启用可取消");
            }
        });
    }

}
