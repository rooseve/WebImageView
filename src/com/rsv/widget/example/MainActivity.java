package com.rsv.widget.example;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.rsv.widget.R;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

	}

	public void btnXmlClick(View v) {
		Intent intent = new Intent(this, ImageXml.class);
		startActivity(intent);
	}

	public void btnJavaClick(View v) {
		Intent intent = new Intent(this, ImageJava.class);
		startActivity(intent);
	}

	public void btnListClick(View v) {
		Intent intent = new Intent(this, ImageList.class);
		startActivity(intent);
	}

}
