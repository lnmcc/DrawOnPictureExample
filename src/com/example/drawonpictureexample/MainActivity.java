package com.example.drawonpictureexample;

import java.io.OutputStream;

import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.BitmapFactory.Options;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends Activity {

	ImageView imageView;
	Button chooseButton;
	Button saveButton;

	Bitmap bmp;
	Bitmap alteredBitmap;
	Canvas canvas;
	Paint paint;
	Matrix matrix;

	float startX = 0f, startY = 0f;
	float stopX = 0f, stopY = 0f;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
//		requestWindowFeature(Window.FEATURE_NO_TITLE);
//		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//	    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.activity_main);
		
		imageView = (ImageView)findViewById(R.id.imageView);
		imageView.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				
				switch(event.getAction()) {
				
				case MotionEvent.ACTION_DOWN:
					startX = event.getX();
					startY = event.getY();
					break;
					
				case MotionEvent.ACTION_UP:
					stopX = event.getX();
					stopY = event.getY();
					canvas.drawLine(startX, startY, stopX, stopY, paint);
					imageView.invalidate();
					break;
					
				case MotionEvent.ACTION_MOVE:
					stopX = event.getX();
					stopY = event.getY();
					canvas.drawLine(startX, startY, stopX, stopY, paint);
					imageView.invalidate();
					startX = stopX;
					startY = stopY;
					break;
					
				case MotionEvent.ACTION_CANCEL:
					break;
					
				default:
					break;
				}
				
				return true;
			}
		});
		
		chooseButton = (Button)findViewById(R.id.ChoosePicture);
		chooseButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				startActivityForResult(i, 0);
			}
		});
		
		saveButton = (Button)findViewById(R.id.SavaButton);
		saveButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				try {
					Uri imageFileUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new ContentValues());
					OutputStream fileOS = getContentResolver().openOutputStream(imageFileUri);
					if(alteredBitmap != null) {
						alteredBitmap.compress(CompressFormat.JPEG, 100, fileOS);
						Toast.makeText(MainActivity.this, "save ok", Toast.LENGTH_SHORT).show();
					}
				}catch(Exception e) {
					Log.v("Save", e.getMessage());
				}
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_OK) {
			Uri imageFileUri = data.getData();
			Display currentDisplay = getWindowManager().getDefaultDisplay();
			float dw = currentDisplay.getWidth();
			float dh = currentDisplay.getHeight();
			Toast.makeText(this, dw + ":" + dh, Toast.LENGTH_LONG).show();

			try {
				BitmapFactory.Options bitmapFactoryOptions = new BitmapFactory.Options();
				bitmapFactoryOptions.inJustDecodeBounds = true;
				bmp = BitmapFactory.decodeStream(getContentResolver()
						.openInputStream(imageFileUri), null,
						bitmapFactoryOptions);
				int heightRatio = (int) Math
						.ceil(bitmapFactoryOptions.outHeight / dh);
				int widthRatio = (int) Math.ceil(bitmapFactoryOptions.outWidth
						/ dw);

				if (heightRatio > 1 && widthRatio > 1) {
					if (heightRatio > widthRatio) {
						bitmapFactoryOptions.inSampleSize = heightRatio;
					} else {
						bitmapFactoryOptions.inSampleSize = widthRatio;
					}
				}
				bitmapFactoryOptions.inJustDecodeBounds = false;
				bmp = BitmapFactory.decodeStream(getContentResolver()
						.openInputStream(imageFileUri), null,
						bitmapFactoryOptions);

				alteredBitmap = Bitmap.createBitmap(bmp.getWidth(),
						bmp.getHeight(), bmp.getConfig());
				Toast.makeText(this, bmp.getWidth() + ":" + bmp.getHeight(),
						Toast.LENGTH_LONG).show();
				canvas = new Canvas(alteredBitmap);
				paint = new Paint();
				paint.setColor(Color.RED);
				paint.setStrokeWidth(5);
				matrix = new Matrix();

				canvas.drawBitmap(bmp, matrix, paint);

				imageView.setImageBitmap(alteredBitmap);
			} catch (Exception e) {
				Log.v("OnActivityResult", e.getMessage());
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
