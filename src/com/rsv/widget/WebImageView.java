package com.rsv.widget;

import java.lang.ref.WeakReference;

import android.annotation.SuppressLint;
import android.content.Context;
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

/**
 * ImageView but load from an url
 * 
 */
public class WebImageView extends ImageView {

	/**
	 * An AsyncTask instance to manage the url loading
	 */
	private RetrieveImageTask task;

	/**
	 * The image url
	 */
	private String webImageUrl;

	/**
	 * The loading progress, 0~100
	 */
	private long progress = -1;

	/**
	 * The outer side listener
	 */
	private WebImageProgressListener userImgLoadListener;

	private WebImageProgressListener internImgLoadListener = new WebImageProgressListener() {

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

	private Runnable runBgTask = null;

	private Handler handler = new Handler();

	/**
	 * The image placeholder
	 */
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

	/**
	 * Load some custom attrs, like webImageUrl
	 * 
	 * @param context
	 * @param attrs
	 */
	private void loadAttrs(Context context, AttributeSet attrs) {
		if (attrs == null)
			return;

		String url = attrs.getAttributeValue("http://schemas.android.com/apk/res-auto",
				"webImageUrl");

		if (url != null) {

			if (url.charAt(0) == '@') {
				this.setWebImageUrl(Integer.parseInt(url.substring(1)));
			} else
				this.setWebImageUrl(url);
		}
	}

	/**
	 * Listen for the image loading progress
	 */
	public void setWebImageProgressListener(final WebImageProgressListener imgLoadListener) {
		this.userImgLoadListener = imgLoadListener;
	}

	/**
	 * Set web image placeholder, which will showed before the real image loaded
	 * 
	 * @param placeholder
	 */
	public void setWebImagePlaceholder(final Drawable placeholder) {
		this.webImagePlaceholder = placeholder;
	}

	/**
	 * Set web image placeholder, which will showed before the real image loaded
	 * 
	 * @param placeholder
	 */
	public void setWebImagePlaceholder(final Bitmap placeholder) {
		this.webImagePlaceholder = new BitmapDrawable(this.getContext().getResources(), placeholder);
	}

	/**
	 * Set web image placeholder, which will showed before the real image loaded
	 * 
	 * @param placeholder
	 */
	public void setWebImagePlaceholder(int resouceId) {
		this.webImagePlaceholder = this.getContext().getResources().getDrawable(resouceId);
	}

	private void fillPlaceholder() {
		if (webImagePlaceholder != null) {
			setImageDrawable(webImagePlaceholder);

		} else {
			// setImageResource(android.R.color.transparent);
		}
	}

	/**
	 * 
	 * @return the web image url
	 */
	public String getWebImageUrl() {
		return this.webImageUrl;
	}

	public void setWebImageUrl(int resourceId) {
		this.setWebImageUrl(getResources().getString(resourceId));
	}

	/**
	 * Set the webimage url
	 * 
	 * @param url
	 */
	public void setWebImageUrl(final String url) {

		if (this.webImageUrl == null && url == null)
			return;

		if (url != null && url.equals(this.webImageUrl)) {

			if (this.progress >= 100) {
				this.internImgLoadListener.onLoad(this);
			}

			return;
		}

		if (task != null) {
			task.tryCancel();
			this.disconnectWebImageTask();
		}

		this.webImageUrl = url;

		this.fillPlaceholder();

		if (runBgTask != null) {
			handler.removeCallbacks(runBgTask);
		}

		if (this.webImageUrl == null)
			return;

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

		// delayed a little, as the View might be adjust many times
		handler.postDelayed(runBgTask, 10);
	}

	/**
	 * Web image data got
	 * 
	 * @param bm
	 */
	private void setWebImageBitmap(final Bitmap bm, final Exception e) {

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

	private void disconnectWebImageTask() {
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

		/**
		 * the last reported progress
		 */
		private long lastReportedProgress = 0;

		/**
		 * the last reporting timestamp
		 */
		private long lastReportedTime = 0;

		private Exception bgExp = null;

		private RetrieveImageTask(final WebImageView imageView) throws Exception {
			this.imageViewReference = new WeakReference<WebImageView>(imageView);

		}

		private void tryCancel() {
			this.clearRef();

			if (this.getStatus() == Status.PENDING || !this.isCancelled()) {

				// LogUtils.i(this, "cancel RetrieveImageTask");
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

					// LogUtils.v(this, "Load " + imageView.webImageUrl);

					ImageLoader imgLoader = ImageLoader.getImageLoader(imageView.getContext());

					return imgLoader.downloadImg(imageView.webImageUrl, new IProgressListener() {

						@Override
						public void reportProgress(long progress) {

							if (lastReportedProgress == progress)
								return;

							long ctime = System.currentTimeMillis();

							if (ctime - lastReportedTime > 100
									&& progress - lastReportedProgress > 0) {

								lastReportedProgress = progress;
								lastReportedTime = ctime;

								RetrieveImageTask.this.publishProgress((int) progress);
							}

						}
					});

				} catch (Exception e) {
					LogUtils.logException(e);

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

				imageView.disconnectWebImageTask();
			}

			this.clearRef();
		}
	};

	/**
	 * Listen for web image loading
	 * 
	 */
	public interface WebImageProgressListener {

		/**
		 * Web image start loading
		 * 
		 * @param view
		 */
		public void onStart(final WebImageView view);

		/**
		 * Web image loading
		 * 
		 * @param view
		 * @param progress
		 *            0~100
		 */
		public void onLoading(final WebImageView view, final int progress);

		/**
		 * Web image loaded
		 * 
		 * @param view
		 */
		public void onLoad(final WebImageView view);

		/**
		 * Some error happened during loading
		 * 
		 * @param view
		 * @param e
		 */
		public void onError(final WebImageView view, Exception e);

	}

}
