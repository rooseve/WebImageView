package com.rsv.widget.example;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ProgressBar;

import com.rsv.widget.R;
import com.rsv.widget.WebImageView;
import com.rsv.widget.WebImageView.WebImageProgressListener;

public class ImageJava extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.imgjava);

		WebImageView timg = (WebImageView) this.findViewById(R.id.webImage);

		timg.setWebImagePlaceholder(R.drawable.ic_launcher);

		timg.setWebImageUrl(R.string.test_img_url);

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
