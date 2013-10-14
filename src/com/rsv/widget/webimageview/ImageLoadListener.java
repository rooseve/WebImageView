package com.rsv.widget.webimageview;

import com.rsv.widget.WebImageView;

public interface ImageLoadListener {

	public void onError(WebImageView v, Exception e);

	public void onStart(WebImageView v);

	/**
	 * Web image loading
	 * 
	 * @param v
	 * @param progress
	 *            0~100
	 */
	public void onLoading(WebImageView v, int progress);

	/**
	 * Web image loaded
	 * 
	 * @param v
	 */
	public void onLoad(WebImageView v);
}
