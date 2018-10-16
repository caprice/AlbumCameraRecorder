package com.zhongjh.cameraviewsoundrecorder.soundrecording.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zhongjh.cameraviewsoundrecorder.R;
import com.zhongjh.cameraviewsoundrecorder.widget.OperationButton;
import com.zhongjh.cameraviewsoundrecorder.widget.OperationLayout;
import com.zhongjh.cameraviewsoundrecorder.widget.clickorlongbutton.ClickOrLongButton;

/**
 * 录音控件，多了一个控件集成
 * Created by zhongjh on 2018/10/16.
 */
public class SoundrecordingLayout extends OperationLayout {

    public SoundrecordingLayout(@NonNull Context context) {
        super(context);
    }

    public SoundrecordingLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SoundrecordingLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public ViewHolder newViewHolder() {
        return new ViewHolder(View.inflate(getContext(), R.layout.layout_soundrecording_operae, this));
    }

    @Override
    public void startOperaeBtnAnimator() {
        super.startOperaeBtnAnimator();
        // 显示播放的按钮
        ((ViewHolder)mViewHolder).rlSoundRecording.setVisibility(VISIBLE);
    }

    /**
     * 重置本身
     */
    public void reset() {
        super.reset();
        // 隐藏播放的按钮
        ((ViewHolder)mViewHolder).rlSoundRecording.setVisibility(INVISIBLE);
    }

    public class ViewHolder extends OperationLayout.ViewHolder {

        public ImageView iv_ring;
        public ImageView iv_record;
        public RelativeLayout rlSoundRecording;

        public ViewHolder(View rootView) {
            super(rootView);
            this.iv_ring = rootView.findViewById(R.id.iv_ring);
            this.iv_record = rootView.findViewById(R.id.iv_record);
            this.rlSoundRecording = rootView.findViewById(R.id.rlSoundRecording);
        }

    }

}
