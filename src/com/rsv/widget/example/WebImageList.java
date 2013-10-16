package com.rsv.widget.example;

import java.util.Random;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

import com.rsv.widget.R;
import com.rsv.widget.WebImageView;
import com.rsv.widget.WebImageView.WebImageProgressListener;

public class WebImageList extends Activity {

	private static Random random = new Random();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.imglist);

		setUp();
	}

	private void setUp() {
		String baseUrl = "http://www.kidsmathgamesonline.com/images/pictures/numbers600/number%d.jpg";

		String[] urls = new String[20];

		for (int i = 0; i < urls.length; i++) {
			urls[i] = String.format(baseUrl + "?t=" + getRandomInt(1, 100), i % 10);
		}

		ListView listView = (ListView) findViewById(R.id.listView);

		ItemAdapter adapter = new ItemAdapter(this, R.layout.imgitem, urls);

		listView.setAdapter(adapter);

		WebImageView timg = (WebImageView) this.findViewById(R.id.titleImage);
		final TextView txt = (TextView) this.findViewById(R.id.titleTxt);

		timg.setWebImageProgressListener(new WebImageProgressListener() {

			@Override
			public void onStart(WebImageView view) {

				txt.setText("start....");
			}

			@Override
			public void onLoading(WebImageView view, int progress) {
				txt.setText("load " + progress + "%");
			}

			@Override
			public void onLoad(WebImageView view) {
				txt.setText("load done");

			}

			@Override
			public void onError(WebImageView view, Exception e) {
				txt.setText("Error");

			}
		});
	}

	@SuppressWarnings("unused")
	private static String getRandomStr(int len) {
		char[] chars = "abcdefghijklmnopqrstuvwxyz1234567890".toCharArray();
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < len; i++) {
			char c = chars[random.nextInt(chars.length)];
			sb.append(c);
		}

		return sb.toString();
	}

	private static int getRandomInt(int min, int max) {
		return random.nextInt(max - min) + min;

	}

}
