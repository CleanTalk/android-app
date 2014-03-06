package org.cleantalk.app.views;

import org.cleantalk.app.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class ProgressBarIndeterminate extends FrameLayout {

	private final ImageView mImageInner;
	private final ImageView mImageOuter;

	public ProgressBarIndeterminate(Context context, AttributeSet attrs) {
		super(context, attrs);

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.view_progressbar_indeterminate, this, true);
		mImageInner = (ImageView) findViewById(R.id.imageViewInner);
		mImageOuter = (ImageView) findViewById(R.id.imageViewOuter);

		Animation rotation_cw = new RotateAnimation(0, 1080, Animation.RELATIVE_TO_SELF, .5f, Animation.RELATIVE_TO_SELF, .5f);
		rotation_cw.setInterpolator(new LinearInterpolator());
		rotation_cw.setRepeatCount(Animation.INFINITE);
		rotation_cw.setDuration(2000);
		mImageInner.startAnimation(rotation_cw);

		Animation rotation_ccw = new RotateAnimation(720, 0, Animation.RELATIVE_TO_SELF, .5f, Animation.RELATIVE_TO_SELF, .5f);
		rotation_ccw.setInterpolator(new LinearInterpolator());
		rotation_ccw.setRepeatCount(Animation.INFINITE);
		rotation_ccw.setDuration(2000);
		mImageOuter.startAnimation(rotation_ccw);
	}

}
