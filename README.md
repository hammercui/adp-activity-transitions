# activity-transitions
![image](image/1.gif)

A simple application that illustrates some advanced activity transition 
examples and techniques. See the corresponding [blog post](http://www.androiddesignpatterns.com/2014/12/activity-fragment-transitions-in-android-lollipop-part1.html) for more information.

# 新增

相对于[alexjlockwood](https://github.com/alexjlockwood)的[adp-activity-transitions](https://github.com/alexjlockwood/adp-activity-transitions),新增了以下内容。

* 共享动画，详情页使用PhotoView替换ImageView。以支持手势缩放
* 共享动画，除了5.0官方方案外，新增4.x版本兼容性的自己实现

# 解决问题

## 1 PhotoView闪烁
本工程主要解决PhotoView共享动画时的闪烁问题，原来使用的PhotoView版本是1.2.4,在动画缩放时，明显能感觉不管`ImageScaleType`使用的是`FIT_CENTER`,还是`CENTER_CROP`,在动画缩放中，都是以`MATRIX`的形式 ,这样在做共享动画时，就会出现图像内容不匹配，产生闪烁的现象。

后来决定不使用dependencies的方式引入photoview依赖，直接引入PhotoView的源码工程，发现没有这个问题了，应该是做了修复，等有时间，再好好分析PhotoView的源码。

## 2 4x兼容性问题

通过屏蔽自带转场动画，自己实现转场动画。

```
   // 去掉自带的转场动画
   
  from.overridePendingTransition(0,0);
```

然后在detail界面自己实现view的位移，以及缩小，放大动画

```
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
```

当然，我们最好在detail页的ImageView加载完毕，并且测量完毕，已经分配完Layout之后，再调用动画


Picasso加载完毕回调

```
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
                
```    


onPreDraw会在onLayout之后，onDraw之前调用，可以做一些Draw的准备工作，比如重置布局

```
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

```            
