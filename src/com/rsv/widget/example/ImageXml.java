package com.rsv.widget.example;

import com.rsv.utils.LogUtils;
import com.rsv.widget.R;
import com.rsv.widget.WebImageView;
import com.rsv.widget.WebImageView.WebImageProgressListener;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ProgressBar;

public class ImageXml extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.imgxml);

		WebImageView timg = (WebImageView) this.findViewById(R.id.webImage);
		
		LogUtils.i(this, timg.getWebImageUrl());

		final ProgressBar pbar = (ProgressBar) this.findViewById(R.id.progressBarHz);

		timg.setWebImageProgressListener(new WebImageProgressListener() {

			@Override
			public void onStart(WebImageView view) {

				pbar.setProgress(0);
			}

			@Override
			public void onLoading(WebImageView view, int progress) {
				pbar.setProgress(progress);
			}

			
			@Override
			public void onLoad(WebImageView view) {

			}

			@Override
			public void onError(WebImageView view, Exception e) {

			}
		});
	}
}
