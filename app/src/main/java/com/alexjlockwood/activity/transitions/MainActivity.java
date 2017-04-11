package com.alexjlockwood.activity.transitions;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.SharedElementCallback;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Map;

import static com.alexjlockwood.activity.transitions.Constants.ALBUM_IMAGE_URLS;
import static com.alexjlockwood.activity.transitions.Constants.ALBUM_NAMES;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName()+"Test";
    private static final boolean DEBUG = false;

    static final String EXTRA_STARTING_ALBUM_POSITION = "extra_starting_item_position";
    static final String EXTRA_CURRENT_ALBUM_POSITION = "extra_current_item_position";
    static final String EXTRA_SHARE_ZOOM_INFO = "extra_share_zoom_info";
    private RecyclerView mRecyclerView;
    private Bundle mTmpReenterState;
    private boolean mIsDetailsActivityStarted;
    public static int thumbWidth = 0;
    private CardAdapter mCardAdapter;
//    RadioGroup mRadioGroup;
//    RadioButton mRadioButton1,mRadioButton2;
    private boolean isImageMode = true; //是图片模式还是文章模式
    private boolean is5xVersion = true;//是5.x版本还是4.x版本

    /**
     * 初始化共享
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void initExitShare(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            SharedElementCallback mCallback = new SharedElementCallback() {
                @Override
                public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                    //返回进入 动画
                    if (mTmpReenterState != null) {
                        int startingPosition = mTmpReenterState.getInt(EXTRA_STARTING_ALBUM_POSITION);
                        int currentPosition = mTmpReenterState.getInt(EXTRA_CURRENT_ALBUM_POSITION);
                        if (startingPosition != currentPosition) {
                            // If startingPosition != currentPosition the user must have swiped to a
                            // different page in the DetailsActivity. We must update the shared element
                            // so that the correct one falls into place.
                            String newTransitionName = ALBUM_NAMES[currentPosition];
                            View newSharedElement = mRecyclerView.findViewWithTag(newTransitionName);
                            if (newSharedElement != null) {
                                names.clear();
                                names.add(newTransitionName);
                                sharedElements.clear();
                                sharedElements.put(newTransitionName, newSharedElement);
                            }
                        }

                        mTmpReenterState = null;
                    } else { //退出动画
                        // If mTmpReenterState is null, then the activity is exiting.
                        View navigationBar = findViewById(android.R.id.navigationBarBackground);
                        View statusBar = findViewById(android.R.id.statusBarBackground);
                        if (navigationBar != null) {
                            names.add(navigationBar.getTransitionName());
                            sharedElements.put(navigationBar.getTransitionName(), navigationBar);
                        }
                        if (statusBar != null) {
                            names.add(statusBar.getTransitionName());
                            sharedElements.put(statusBar.getTransitionName(), statusBar);
                        }
                    }
                }
            };

            setExitSharedElementCallback(mCallback);
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        thumbWidth = this.getResources().getDisplayMetrics().widthPixels / 3;
        initExitShare();
        setContentView(R.layout.activity_main_test);
        //resetWindowBackground();
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this,
                getResources().getInteger(R.integer.activity_main_num_grid_columns)));
        mCardAdapter = new CardAdapter();
        mRecyclerView.setAdapter(mCardAdapter);

        //模式
        RadioGroup radioGroupMode = (RadioGroup)this.findViewById(R.id.radioGroup_mode);
        RadioButton radioButtonArticle = (RadioButton)this.findViewById(R.id.radioButton_article);
        final RadioButton radioButtonImage = (RadioButton) findViewById(R.id.radioButton_image);
        radioGroupMode.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                isImageMode = radioButtonImage.getId() == checkedId;
            }
        });
        //版本
        RadioGroup radioGroupVersion = (RadioGroup)this.findViewById(R.id.radioGroup_version);
        final RadioButton radioButton5X = (RadioButton)this.findViewById(R.id.radioButton_5x);
        final RadioButton radioButton4X = (RadioButton) findViewById(R.id.radioButton_4x);
        radioGroupVersion.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                is5xVersion = radioButton5X.getId() == checkedId;
                //resetWindowBackground();
            }
        });
    }

//    private void resetWindowBackground(){
//        getWindow().setBackgroundDrawableResource(is5xVersion?android.R.color.black:android.R.color.transparent);
//    }

    @Override
    protected void onResume() {
        super.onResume();
        mIsDetailsActivityStarted = false;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onActivityReenter(int requestCode, Intent data) {
        super.onActivityReenter(requestCode, data);
        mTmpReenterState = new Bundle(data.getExtras());
        int startingPosition = mTmpReenterState.getInt(EXTRA_STARTING_ALBUM_POSITION);
        int currentPosition = mTmpReenterState.getInt(EXTRA_CURRENT_ALBUM_POSITION);
        if (startingPosition != currentPosition) {
            mRecyclerView.scrollToPosition(currentPosition);
        }
        postponeEnterTransition();
        mRecyclerView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                mRecyclerView.getViewTreeObserver().removeOnPreDrawListener(this);
                // TODO: figure out why it is necessary to request layout here in order to get a smooth transition.
                mRecyclerView.requestLayout();
                startPostponedEnterTransition();
                return true;
            }
        });

        mCardAdapter.notifyDataSetChanged();

    }

    private class CardAdapter extends RecyclerView.Adapter<CardHolder> {
        private final LayoutInflater mInflater;

        public CardAdapter() {
            mInflater = LayoutInflater.from(MainActivity.this);
        }

        @Override
        public CardHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            return new CardHolder(mInflater.inflate(R.layout.album_image_card, viewGroup, false));
        }

        @Override
        public void onBindViewHolder(CardHolder holder, int position) {
            holder.bind(position);
        }

        @Override
        public int getItemCount() {
            return ALBUM_IMAGE_URLS.length;
        }
    }

    private class CardHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final ImageView mAlbumImage;
        private int mAlbumPosition;

        public CardHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            //重置尺寸
            ViewGroup.LayoutParams lp = itemView.getLayoutParams();
            lp.width  = thumbWidth ;
            lp.height = thumbWidth;
            itemView.setLayoutParams(lp);


            mAlbumImage = (ImageView) itemView.findViewById(R.id.main_card_album_image);

        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        public void bind(int position) {
            Picasso.with(MainActivity.this).load(ALBUM_IMAGE_URLS[position]).into(mAlbumImage);
            if(Utils.isLOLLIPOP())
                mAlbumImage.setTransitionName(ALBUM_NAMES[position]);

            mAlbumImage.setTag(ALBUM_NAMES[position]);
            mAlbumPosition = position;
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onClick(View v) {
            // TODO: is there a way to prevent user from double clicking and starting activity twice?
            Intent intent = null;
            if(isImageMode){
                 intent = new Intent(MainActivity.this, PreviewActivity.class);
            }
            else{
                 intent = new Intent(MainActivity.this, DetailsActivity.class);
            }

            intent.putExtra(EXTRA_STARTING_ALBUM_POSITION, mAlbumPosition);

            if (!mIsDetailsActivityStarted) {
                mIsDetailsActivityStarted = true;
                if(is5xVersion){
                    if(Utils.isLOLLIPOP()){
                        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(MainActivity.this,
                                mAlbumImage, mAlbumImage.getTransitionName()).toBundle());
                    }
                    else{
                        startActivity(intent);
                    }
                }
                else{
                    PreviewActivity4x.startCustomActivity(MainActivity.this,isImageMode,v,mAlbumPosition);
                }


            }
        }
    }
}
