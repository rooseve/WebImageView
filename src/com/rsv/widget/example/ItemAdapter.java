package com.rsv.widget.example;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.rsv.widget.R;
import com.rsv.widget.WebImageView;
import com.rsv.widget.WebImageView.WebImageProgressListener;

public class ItemAdapter extends ArrayAdapter<String> {

	private final Context context;
	private final String[] urls;
	private final int layoutId;

	public ItemAdapter(Context context, int viewResourceId, String[] urls) {

		super(context, viewResourceId, urls);

		this.context = context;
		this.urls = urls;
		this.layoutId = viewResourceId;

	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		View rowView;

		if (convertView != null) {
			rowView = convertView;

			//LogUtils.i(this, "convertView" + position);

		} else {

			//LogUtils.i(this, "InflateView" + position);

			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			rowView = inflater.inflate(layoutId, parent, false);
		}

		WebImageView wImageView = (WebImageView) rowView.findViewById(R.id.imageView);

		final TextView textView = (TextView) rowView.findViewById(R.id.textView);

		textView.setText(".");

		wImageView.setWebImageProgressListener(new WebImageProgressListener() {

			@Override
			public void onLoading(WebImageView v, final int progress) {

				textView.setText(progress + "%");

			}

			@Override
			public void onLoad(WebImageView v) {
				// TODO Auto-generated method stub

				textView.setText(textView.getText() + " Done");

			}

			@Override
			public void onStart(WebImageView v) {
				textView.setText("...");
			}

			@Override
			public void onError(WebImageView v, Exception e) {
				// TODO Auto-generated method stub

			}
		});

		wImageView.setWebImagePlaceholder(R.drawable.wimg_placeholder);
		wImageView.setWebImageUrl(this.urls[position]);

		return rowView;

	}

}
