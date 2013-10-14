package com.rsv.widget;

import java.io.IOException;
import java.lang.ref.WeakReference;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.rsv.comp.IProgressListener;
import com.rsv.comp.ImageLoader;
import com.rsv.utils.LogUtils;
import com.rsv.widget.webimageview.ImageLoadListener;

public class WebImageView extends ImageView {

	/**
	 * An AsyncTask instance to manage the url loading
	 */
	private RetrieveImageTask task;

	private String webImageUrl;

	private long progress = -1;

	private ImageLoadListener userImgLoadListener;

	private ImageLoadListener internImgLoadListener = new ImageLoadListener() {

		@Override
		public void onError(WebImageView v, Exception e) {
			if (WebImageView.this.userImgLoadListener != null)
				WebImageView.this.userImgLoadListener.onError(v, e);

			WebImageView.this.progress = -2;
		}

		@Override
		public void onStart(WebImageView v) {
			if (WebImageView.this.userImgLoadListener != null)
				WebImageView.this.userImgLoadListener.onStart(v);

			WebImageView.this.progress = 0;
		}

		@Override
		public void onLoading(WebImageView v, int progress) {
			if (WebImageView.this.userImgLoadListener != null)
				WebImageView.this.userImgLoadListener.onLoading(v, progress);

			WebImageView.this.progress = Math.min(99, progress);
		}

		@Override
		public void onLoad(WebImageView v) {
			if (WebImageView.this.userImgLoadListener != null)
				WebImageView.this.userImgLoadListener.onLoad(v);

			WebImageView.this.progress = 100;
		}
	};

	protected Handler handler = new Handler();

	private Runnable runBgTask = null;

	protected Drawable webImagePlaceholder;

	public WebImageView(Context context) {
		super(context);
	}

	public WebImageView(Context context, AttributeSet attrs) {
		super(context, attrs);

		loadAttrs(context, attrs);
	}

	public WebImageView(Context context, AttributeSet attrs, int defaultStyle) {
		super(context, attrs, defaultStyle);
		loadAttrs(context, attrs);
	}

	private void loadAttrs(Context context, AttributeSet attrs) {
		if (attrs == null)
			return;

		TypedArray wAttrs = context.obtainStyledAttributes(attrs, R.styleable.WebImageView);

		String url = wAttrs.getString(R.styleable.WebImageView_webImageUrl);

		if (url != null) {
			this.setWebImageUrl(url);
		}

		wAttrs.recycle();
	}

	public void setImgLoadListener(ImageLoadListener imgLoadListener) {
		this.userImgLoadListener = imgLoadListener;
	}

	public void setPlaceholder(Drawable placeholder) {
		this.webImagePlaceholder = placeholder;
	}

	public void setPlaceholder(Bitmap placeholder) {
		this.webImagePlaceholder = new BitmapDrawable(this.getContext().getResources(), placeholder);
	}

	public void setPlaceholder(int resouceId) {
		this.webImagePlaceholder = this.getContext().getResources().getDrawable(resouceId);
	}

	private void fillPlaceholder() {
		if (webImagePlaceholder != null) {
			setImageDrawable(webImagePlaceholder);
		} else {
			setImageResource(android.R.color.transparent);
		}
	}

	public String getImageUrl() {
		return this.webImageUrl;
	}

	public void setWebImageUrl(final String url) {

		if (url != null && url.equals(this.webImageUrl)) {

			if (this.progress >= 100) {
				this.internImgLoadListener.onLoad(this);
			}

			return;
		}

		if (task != null) {
			task.tryCancel();
			this.disconnectImageTask();
		}

		this.webImageUrl = url;

		this.fillPlaceholder();

		if (this.webImageUrl == null)
			return;

		if (runBgTask != null) {
			handler.removeCallbacks(runBgTask);
		}

		runBgTask = new Runnable() {

			@SuppressLint("NewApi")
			@Override
			public void run() {
				try {
					task = new RetrieveImageTask(WebImageView.this);

					WebImageView.this.internImgLoadListener.onStart(WebImageView.this);

					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
						task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
					else
						task.execute();

				} catch (Exception e) {
					LogUtils.logException(e);
				} finally {

					WebImageView.this.runBgTask = null;
				}

			}
		};

		handler.post(runBgTask);

	}

	/**
	 * Web image data got
	 * 
	 * @param bm
	 */
	private void setWebImageBitmap(Bitmap bm, Exception e) {

		if (bm == null) {

			// well, something wrong
			this.setScaleType(ScaleType.CENTER_INSIDE);
			// this.setImageResource(R.drawable.holder);

		} else {

			/*
			 * if (this.mMaxWidth > 0 && this.mMaxHeight > 0) { bm =
			 * Bitmap.createScaledBitmap(bm, this.mMaxWidth, this.mMaxHeight,
			 * false); }
			 */

			super.setImageBitmap(bm);
		}

		if (e != null) {
			this.internImgLoadListener.onError(this, e);

		} else {
			this.internImgLoadListener.onLoading(this, 100);
		}

		this.internImgLoadListener.onLoad(this);

	}

	public void disconnectImageTask() {
		task = null;
	}

	/**
	 * 
	 * Manage the image loading
	 */
	private class RetrieveImageTask extends AsyncTask<Void, Integer, Bitmap> {

		/**
		 * Keep a weak reference, as the view instance might be destroyed during
		 * the app life cycle
		 */
		private final WeakReference<WebImageView> imageViewReference;

		private ImageLoader imgLoader;

		private long lastp = 0;

		private long lastTime = 0;

		private Exception bgExp = null;

		private RetrieveImageTask(final WebImageView imageView) throws Exception {
			this.imageViewReference = new WeakReference<WebImageView>(imageView);

			imgLoader = ImageLoader.getImageLoader(imageView.getContext());
		}

		private void tryCancel() {
			this.clearRef();

			if (this.getStatus() == Status.PENDING || !this.isCancelled()) {
				LogUtils.i(this, "cancel RetrieveImageTask");
				this.cancel(false);
			}
		}

		private void clearRef() {
			this.imageViewReference.clear();
		}

		private WebImageView tryGetImageView() {
			return this.imageViewReference.get();
		}

		@Override
		protected void onProgressUpdate(Integer... values) {

			final WebImageView imageView = this.tryGetImageView();

			if (imageView != null) {

				final int lp = values[0];

				imageView.internImgLoadListener.onLoading(imageView, lp);

			}
		}

		@Override
		protected Bitmap doInBackground(Void... args) {

			WebImageView imageView = this.tryGetImageView();

			if (imageView != null) {

				try {

					//LogUtils.v(this, "Load " + imageView.webImageUrl);

					return imgLoader.loadImg(imageView.webImageUrl, new IProgressListener() {

						@Override
						public void reportProgress(long progress) {

							if (lastp == progress)
								return;

							long ctime = System.currentTimeMillis();

							if (ctime - lastTime > 100 && progress - lastp > 0) {

								lastp = progress;
								lastTime = ctime;

								RetrieveImageTask.this.publishProgress((int) progress);
							}

						}
					});

				} catch (IOException e) {

					bgExp = e;

					return null;
				}

			} else {
				LogUtils.w(this, "doInBackground miss ref to imageView");
			}

			return null;
		}

		@Override
		protected void onPostExecute(Bitmap bitmap) {

			if (!isCancelled()) {

				WebImageView imageView = this.tryGetImageView();

				if (imageView != null) {

					imageView.setWebImageBitmap(bitmap, this.bgExp);

				} else {
					LogUtils.w(this, "onPostExecute miss ref to imageView");
				}

				imageView.disconnectImageTask();
			}

			this.clearRef();
		}
	};

}
