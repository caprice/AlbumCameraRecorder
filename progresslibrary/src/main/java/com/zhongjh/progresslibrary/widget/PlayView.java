package com.zhongjh.progresslibrary.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.zhongjh.progresslibrary.R;
import com.zhongjh.progresslibrary.entity.RecordingItem;
import com.zhongjh.progresslibrary.listener.MaskProgressLayoutListener;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * 一个播放的view
 * Created by zhongjh on 2019/2/1.
 */
public class PlayView extends FrameLayout {

    private static final String LOG_TAG = "PlayView";
    private static final String ARG_ITEM = "recording_item";
    private RecordingItem mRecordingItem;   // 当前音频数据

    public ViewHolder mViewHolder;          // 控件集合

    private boolean isPlaying = false;      // 标记当前播放状态

    private String mFileLength;// 该音频文件的总时

    private MaskProgressLayoutListener listener;   // 相关事件
    private boolean operation;

    public void setListener(MaskProgressLayoutListener listener) {
        this.listener = listener;
    }

    // region 有关音频

    private MediaPlayer mMediaPlayer = new MediaPlayer();
    //定时器  由于功能中有付费播放功能，需要定时器来判断
    private Timer mTimer = null;
    private TimerTask mTimerTask = null;
    private boolean isChanging = false;  //互斥变量，防止定时器与SeekBar拖动时进度冲突


    private Handler mHandler = new MyHandler(PlayView.this);

    // endregion 有关音频

    public PlayView(@NonNull Context context) {
        this(context, null);
    }

    public PlayView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PlayView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(attrs);
    }

    /**
     * 初始化view
     */
    private void initView(AttributeSet attrs) {
        // 自定义View中如果重写了onDraw()即自定义了绘制，那么就应该在构造函数中调用view的setWillNotDraw(false).
        setWillNotDraw(false);

        mViewHolder = new ViewHolder(View.inflate(getContext(), R.layout.layout_play, this));
        mViewHolder.seekbar.setEnabled(false);

        initListener();
    }

    /**
     * 初始化相关数据
     *
     * @param recordingItem      音频数据源
     * @param audioProgressColor 进度条颜色
     */
    public void setData(RecordingItem recordingItem, int audioProgressColor) {
        this.mRecordingItem = recordingItem;
        // 设置进度条颜色
        ColorFilter filter = new LightingColorFilter
                (audioProgressColor, audioProgressColor);
        mViewHolder.seekbar.getProgressDrawable().setColorFilter(filter);
        mViewHolder.seekbar.getThumb().setColorFilter(filter);

        if (!TextUtils.isEmpty(mRecordingItem.getFilePath())) {
            mViewHolder.seekbar.setEnabled(true);
        } else {
            mViewHolder.seekbar.setEnabled(false);
        }

        initData();
    }

    /**
     * 根据当前文件初始化一些相关数据
     */
    private void initData() {
        long itemDuration = mRecordingItem.getLength();
        long minutes = TimeUnit.MILLISECONDS.toMinutes(itemDuration);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(itemDuration)
                - TimeUnit.MINUTES.toSeconds(minutes);
        mFileLength = String.format(Locale.CANADA, "%02d:%02d", minutes, seconds);

        // 准备播放
        play();
    }

    /**
     * 所有事件
     */
    private void initListener() {
        // 进度条
        mViewHolder.seekbar.setOnSeekBarChangeListener(new MySeekbar());

        // 播放按钮
        mViewHolder.imgPlay.setOnClickListener(v -> {
            // 判断该音频是否有文件地址，如果没有则请求下载
            if (!TextUtils.isEmpty(mRecordingItem.getFilePath())) {
                onPlay();
            } else {
                // 调用下载
                listener.onItemAudioStartDownload(mRecordingItem.getUrl());
            }
        });

        //异步准备（准备完成），准备到准备完成期间可以显示进度条之类的东西。
        mMediaPlayer.setOnPreparedListener(mediaPlayer -> {
            mViewHolder.seekbar.setProgress(0);
            mViewHolder.imgPlay.setEnabled(true);
            mViewHolder.tvCurrentProgress.setText("00:00/");// 当前时间
            mViewHolder.tvTotalProgress.setText(generateTime(mMediaPlayer.getDuration() )); // 总计时间
            mViewHolder.seekbar.setMax(mMediaPlayer.getDuration() );//设置进度条
        });

        // 播放完成事件
        mMediaPlayer.setOnCompletionListener(mediaPlayer -> {
            //进度归零
            mMediaPlayer.seekTo(0);
            //进度条归零
            mViewHolder.seekbar.setProgress(0);
            //控制栏中的播放按钮显示暂停状态
            mViewHolder.imgPlay.setImageResource(R.drawable.ic_play_circle_outline_black_24dp);
            isPlaying = false;
            //重置并准备重新播放
            mMediaPlayer.reset();
            play();
        });

    }

    // region 有关音频的方法

    /**
     * 播放
     */
    public void play() {
        if (!TextUtils.isEmpty(mRecordingItem.getFilePath()))
            try {
                mMediaPlayer.setDataSource(mRecordingItem.getFilePath());
                //采用同步准备，使用prepare方法时，用户进入该界面需要等待几秒，如同死机一般，，，
                mMediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    /**
     * 播放或者暂停
     */
    private void onPlay() {
        if (isPlaying) {
            //如果当前正在播放  停止播放 更改控制栏播放状态
            if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
                mViewHolder.imgPlay.setImageResource(R.drawable.ic_play_circle_outline_black_24dp);
            }
        } else {
            //如果当前停止播放  继续播放 更改控制栏状态
            if (mMediaPlayer != null) {
                mViewHolder.imgPlay.setImageResource(R.drawable.ic_pause_circle_outline_black_24dp);
                mMediaPlayer.start();
                //定时器 更新进度
                if (mTimer == null) {
                    mTimer = new Timer();
                    mTimerTask = new TimerTask() {
                        @Override
                        public void run() {
                            if (isChanging) {
                                return;
                            }
                            if (isPlaying) {
                                mHandler.sendEmptyMessage(0);
                                mViewHolder.seekbar.setProgress(mMediaPlayer.getCurrentPosition());
                            }
                        }
                    };
                    mTimer.schedule(mTimerTask, 0, 1000);
                }
            }
        }
        isPlaying = !isPlaying;
    }

    /**
     * 重置播放器
     */
    public void reset(){
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
            mTimerTask = null;
        }
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
            }
            mMediaPlayer.reset();
        }
    }

    /**
     * 销毁播放器
     */
    public void deStory() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
            mTimerTask = null;
        }
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
            }
            mMediaPlayer.release();
        }
    }

    /**
     * 格式化显示的时间
     */
    @SuppressLint("DefaultLocale")
    private String generateTime(long time) {
        int totalSeconds = (int) (time / 1000);
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;
        return hours > 0 ? String.format("%02d:%02d:%02d", hours, minutes,
                seconds) : String.format("%02d:%02d", minutes, seconds);
    }

    /**
     * 操作ui
     */
    private static class MyHandler extends Handler {
        private final WeakReference<PlayView> mPlayView;

        public MyHandler(PlayView playView) {
            mPlayView = new WeakReference<>(playView);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void handleMessage(Message msg) {
            PlayView playView = mPlayView.get();
            if (playView != null) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 0:
                        //设置当前播放进度
                        playView.mViewHolder.tvCurrentProgress.setText(playView.generateTime(playView.mMediaPlayer.getCurrentPosition()) + "/");
                        break;
                }
            }
        }
    }

    // endregion

    public static class ViewHolder {

        public View rootView;
        public ImageView imgPlay;
        public SeekBar seekbar;
        public TextView tvCurrentProgress;
        public TextView tvTotalProgress;

        public ViewHolder(View rootView) {
            this.rootView = rootView;
            this.imgPlay = rootView.findViewById(R.id.imgPlay);
            this.seekbar = rootView.findViewById(R.id.seekbar);
            this.tvCurrentProgress = rootView.findViewById(R.id.tvCurrentProgress);
            this.tvTotalProgress = rootView.findViewById(R.id.tvTotalProgress);
        }

    }

    /**
     * 进度条的进度变化事件
     */
    class MySeekbar implements SeekBar.OnSeekBarChangeListener {

        //当进度条变化时触发
        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
        }

        //开始拖拽进度条
        public void onStartTrackingTouch(SeekBar seekBar) {
            isChanging = true;
        }

        //停止拖拽进度条
        public void onStopTrackingTouch(SeekBar seekBar) {
            mMediaPlayer.seekTo(mViewHolder.seekbar.getProgress());
            mViewHolder.tvCurrentProgress.setText(generateTime(mMediaPlayer.getCurrentPosition()) + "/");
            isChanging = false;
        }

    }

}
