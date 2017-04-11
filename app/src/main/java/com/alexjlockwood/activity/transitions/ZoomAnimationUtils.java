package com.alexjlockwood.activity.transitions;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.Window;

import static android.view.FrameMetrics.ANIMATION_DURATION;

/**
 * ============================
 * Author：  hammercui
 * Version： 1.0
 * Time:     2017/4/10
 * Description:
 * Fix History:
 * =============================
 */

public class ZoomAnimationUtils {

    private static int duration = 200;

    /**
     * 进入动画
     * @param preViewInfo
     * @param targetView
     * @param listener
     */
    public static void startZoomEnterAnim(ZoomInfo preViewInfo, final View targetView, final Animator.AnimatorListener listener){
        int startWidth = preViewInfo.getWidth();
        int startHeight = preViewInfo.getHeight();
        int endWidth = targetView.getWidth();
        int endHeight = targetView.getHeight();

        int[] screenLocation = new int[2];
        targetView.getLocationOnScreen(screenLocation);
        int endX = screenLocation[0];
        int endY = screenLocation[1];
        float startScaleX = (float) endWidth / startWidth;
        float startScaleY = (float) endHeight / startHeight;
        int translationX = preViewInfo.getScreenX() - endX;
        int translationY = preViewInfo.getScreenY() - endY;

        targetView.setPivotX(0);
        targetView.setPivotY(0);
        targetView.setTranslationX(translationX);
        targetView.setTranslationY(translationY);
        targetView.setScaleX(1 / startScaleX);
        targetView.setScaleY(1 / startScaleY);

        ViewPropertyAnimator animator = targetView.animate();
        animator.setDuration(duration)
                .scaleX(1f)
                .scaleY(1f)
                .translationX(0)
                .translationY(0);
        if (listener != null) {
            animator.setListener(listener);
        }
        animator.start();
    }

    /**
     * 退出动画
     * @param preViewInfo
     * @param targetView
     * @param listener
     */
    public static void startZoomExitAnim(ZoomInfo preViewInfo, final View targetView, final Animator.AnimatorListener listener){
        int endWidth = preViewInfo.getWidth();
        int endHeight = preViewInfo.getHeight();

        int startWidth = targetView.getWidth();
        int startHeight = targetView.getHeight();

        int endX = preViewInfo.getScreenX();
        int endY = preViewInfo.getScreenY();

        float endScaleX = (float) endWidth / startWidth;
        float endScaleY = (float) endHeight / startHeight;
        int[] screenLocation = new int[2];
        targetView.getLocationOnScreen(screenLocation);
        int startX = screenLocation[0];
        int startY = screenLocation[1];
        int translationX = endX - startX;
        int translationY = endY - startY;
        targetView.setPivotX(0);
        targetView.setPivotY(0);
        targetView.setVisibility(View.VISIBLE);
        ViewPropertyAnimator animator = targetView.animate();
        animator.setDuration(duration)
                .scaleX(endScaleX)
                .scaleY(endScaleY)
                .translationX(translationX)
                .translationY(translationY);
        if (listener != null) {
            animator.setListener(listener);
        }
        animator.start();
    }



    /**
     * 根据图片获得zoom信息
     * @param view
     * @return
     */
    public static ZoomInfo getZoomInfo(@NonNull View view){
        int[] screenLocation = new int[2];
        view.getLocationOnScreen(screenLocation);
        ZoomInfo zoomInfo = new ZoomInfo();
        zoomInfo.setScreenX(screenLocation[0]);
        zoomInfo.setScreenY(screenLocation[1]);
        zoomInfo.setWidth(view.getWidth());
        zoomInfo.setHeight(view.getHeight());
        return zoomInfo;
    }

    /**
     *   背景透明动画
     * @param targetWindow
     * @param color
     * @param values
     */
    public static void startBackgroundAlphaAnim(final Window targetWindow,
                                                final ColorDrawable color,
                                                int...values) {
        if (targetWindow == null) return;

        if (values == null || values.length == 0) {
            values = new int[]{0, 255};
        }

        ObjectAnimator bgAnim = ObjectAnimator.ofInt(color, "alpha", values);
        bgAnim.setDuration(duration);
        bgAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int)animation.getAnimatedValue();
                Log.d("Test","value:"+value);
                color.setAlpha(value);
                color.setColor(Color.argb(value,255,255,255));
                targetWindow.setBackgroundDrawable(color);
            }
        });

        bgAnim.start();
    }
}
