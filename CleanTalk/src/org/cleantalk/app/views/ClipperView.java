package org.cleantalk.app.views;

import org.cleantalk.app.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

public class ClipperView extends View {

	private Bitmap roundedBitmap_;
	private int mFocusingResId;

	public ClipperView(Context context) {
		super(context);
	}

	public ClipperView(Context context, AttributeSet attrs) {
		super(context, attrs);

		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ClipperView);

		final int N = a.getIndexCount();
		for (int i = 0; i < N; ++i) {
			int attr = a.getIndex(i);
			switch (attr) {
			case R.styleable.ClipperView_focusingView:
				mFocusingResId = a.getResourceId(attr, 0);
				break;
			}
		}
		a.recycle();

	}

	public ClipperView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (roundedBitmap_ != null) {
			canvas.drawBitmap(roundedBitmap_, 0, 0, null);
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		refreshBitmap(w, h);
		super.onSizeChanged(w, h, oldw, oldh);
	}

	private void refreshBitmap(int w, int h) {
		roundedBitmap_ = getCroppedBitmap(w, h);
		invalidate();
	}

	public Bitmap getCroppedBitmap(int w, int h) {
		ViewGroup parent = (ViewGroup) getRootView();

		View v = parent.findViewById(mFocusingResId);
		Bitmap clipped = Bitmap.createBitmap(w, h, Config.ARGB_8888);

		Canvas canvas = new Canvas(clipped);
		canvas.drawARGB(100, 0, 0, 0);

		final Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setFilterBitmap(true);
		paint.setDither(true);
		paint.setXfermode(new PorterDuffXfermode(Mode.DST_OUT));
		paint.setStyle(Paint.Style.FILL);
		paint.setColor(Color.WHITE);
		
		Log.i("!!!", String.valueOf(v.getLeft()));
		Log.i("!!!", String.valueOf(v.getRight()));
		Log.i("!!!", String.valueOf(v.getTop()));
		Log.i("!!!", String.valueOf(v.getBottom()));
		Log.i("!!!", String.valueOf(v.getWidth()));
		Log.i("!!!", String.valueOf(v.getHeight()));
		
		RectF r = new RectF(v.getLeft(), getTop(), v.getLeft() + v.getWidth(), getTop()+40);
		canvas.drawRoundRect(r, 10f, 10f, paint);

		return clipped;
	}
	
	@Override
	public void setVisibility(int visibility) {
		refreshBitmap(getWidth(), getHeight());
		super.setVisibility(visibility);
	}
}
