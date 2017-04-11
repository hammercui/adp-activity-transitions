package com.alexjlockwood.activity.transitions;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.MarginLayoutParamsCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.github.chrisbanes.photoview.PhotoView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import java.lang.ref.WeakReference;

import static com.alexjlockwood.activity.transitions.Constants.ALBUM_IMAGE_URLS;
import static com.alexjlockwood.activity.transitions.Constants.ALBUM_NAMES;
import static com.alexjlockwood.activity.transitions.MainActivity.EXTRA_SHARE_ZOOM_INFO;
import static com.alexjlockwood.activity.transitions.MainActivity.EXTRA_STARTING_ALBUM_POSITION;

/**
 * ============================
 * Author：  hammercui
 * Version： 1.0
 * Time:     2017/4/10
 * Description: 浏览页4.x版本兼容版
 * Fix History:
 * =============================
 */

public class PreviewActivity4x extends AppCompatActivity{
    private static final String TAG = PreviewActivity.class.getSimpleName()+"Test";
    private static final boolean DEBUG = false;

    private static final String STATE_CURRENT_PAGE_POSITION = "state_current_page_position";

    private int mCurrentPosition;
    private int mStartingPosition;
    private PreviewActivity4x.ImagePageAdapter mImagePageAdapter;
    private ViewPager mViewPager;
    public boolean mIsEntering = false;
    public ZoomInfo mPreViewInfo;
    public static void startCustomActivity(AppCompatActivity from, boolean isImageMode,View shareView,int position ){
        Intent intent = null;
        if(isImageMode){
            intent = new Intent(from, PreviewActivity4x.class);
            intent.putExtra(EXTRA_SHARE_ZOOM_INFO,ZoomAnimationUtils.getZoomInfo(shareView));
        }
        else{
            intent = new Intent(from, DetailsActivity.class);
        }
        intent.putExtra(EXTRA_STARTING_ALBUM_POSITION, position);
        from.startActivity(intent);
        // 去掉自带的转场动画
        from.overridePendingTransition(0,0);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIsEntering = true;

        setContentView(R.layout.activity_preview);
        mStartingPosition = getIntent().getIntExtra(EXTRA_STARTING_ALBUM_POSITION, 0);
        mPreViewInfo = getIntent().getParcelableExtra(EXTRA_SHARE_ZOOM_INFO);
        if (savedInstanceState == null) {
            mCurrentPosition = mStartingPosition;
        } else {
            mCurrentPosition = savedInstanceState.getInt(STATE_CURRENT_PAGE_POSITION);
        }

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mImagePageAdapter = new PreviewActivity4x.ImagePageAdapter(this);
        mImagePageAdapter.setStartPosition(mCurrentPosition);
        mViewPager.setAdapter(mImagePageAdapter);
        mViewPager.setCurrentItem(mCurrentPosition);
        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                mCurrentPosition = position;
            }
        });
    }



    @Override
    public void onBackPressed() {
        tryExitAnimation();
    }




    public void tryEnterAnimation(View view){
        ZoomAnimationUtils.startBackgroundAlphaAnim(getWindow(),
                new ColorDrawable(getResources().getColor(android.R.color.holo_red_dark)),
                0,
                255);


        ZoomAnimationUtils.startZoomEnterAnim(mPreViewInfo, view, new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
            }
        });

    }


    private void tryExitAnimation(){
        View shareView = mImagePageAdapter.getShareViewByPosition(mViewPager,mCurrentPosition);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
//        ZoomAnimationUtils.startBackgroundAlphaAnim(getWindow(),
//                new ColorDrawable(getResources().getColor(android.R.color.holo_red_dark)),
//                255,0);

        ZoomAnimationUtils.startZoomExitAnim(mPreViewInfo,shareView, new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                finish();
            }
        });
      }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0,0);
    }

    private static class ImagePageAdapter extends PagerAdapter {
        private LayoutInflater mLayoutInflater;
        private WeakReference<PreviewActivity4x> mWeakReference ;
        private int startPosition;

        public ImagePageAdapter(PreviewActivity4x previewActivity){
            this.mLayoutInflater = LayoutInflater.from(previewActivity);
            mWeakReference  = new WeakReference<PreviewActivity4x>(previewActivity);
        }

        //private ImageView shareView;

        public void setStartPosition(int position){
            startPosition = position;
        }


        @Override
        public int getCount() {
            return Constants.ALBUM_NAMES.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }


        @Override
        public Object instantiateItem(ViewGroup container, final int position) {

            final PhotoView view = new PhotoView(mWeakReference.get());
            view.setScaleType(ImageView.ScaleType.FIT_CENTER);
            final ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            view.setLayoutParams(layoutParams);
            String albumImageUrl = ALBUM_IMAGE_URLS[position];


            if(mWeakReference.get().mIsEntering && position == startPosition){
                RequestCreator albumImageRequest = Picasso.with(mWeakReference.get()).load(albumImageUrl);
                //渲染图片成功
                albumImageRequest.into(view, new Callback() {
                    @Override
                    public void onSuccess() {
                        shareReadyListener(view);
                    }
                    @Override
                    public void onError() {
                        shareReadyListener(view);
                    }
                });
            }
            else {
                Picasso.with(mWeakReference.get()).load(albumImageUrl).into(view);
            }

            view.setTag(position);
            container.addView(view);
            return view;
        }


        public ImageView getShareViewByPosition(ViewPager viewPager,int position){
            ImageView view = (ImageView)viewPager.findViewWithTag(position);
            view.setScaleType(ImageView.ScaleType.FIT_CENTER);
            ViewGroup.LayoutParams params  = view.getLayoutParams();
            Log.d(TAG,"width"+params.width);
            Log.d(TAG,"height"+params.height);
            return view;
        }


        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View)object);
        }


        public void shareReadyListener(final View view){
            view.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        view.getViewTreeObserver().removeOnPreDrawListener(this);
                        mWeakReference.get().tryEnterAnimation(view);
                        return true;
                    }
                });
        }



    }

}
