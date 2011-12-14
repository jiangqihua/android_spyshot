package org.home.androidspyshot;

import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;


import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;




import android.widget.FrameLayout;


public class android_spyshot extends Activity {
	private static final String TAG = "CameraDemo";
	Camera camera;
	Preview preview;
	Button buttonClick;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

	
		preview = new Preview(this);
//      preview.setVisibility(0);
		preview.camera = Camera.open();
		preview.camera.setDisplayOrientation(90);
//		preview.setVisibility(4);
		((FrameLayout) findViewById(R.id.preview)).addView(preview);

		
		buttonClick = (Button) findViewById(R.id.buttonClick);
		buttonClick.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {				
				ShotCount counter = new ShotCount(100000,5000,preview);
				counter.start();		
			}
		});
		Log.d(TAG, "onCreate'd");  
	}

	
    @Override
    protected void onResume() {
        super.onResume();
        // Open the default i.e. the first rear facing camera.
        if (preview.camera == null) {
        	preview.camera = Camera.open();
        	preview.camera.setDisplayOrientation(90);
        }
        //preview.startCamera();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (preview.camera != null) {
        	preview.camera.release();
        	preview.camera = null;
        }
    }
}



class ShotCount extends CountDownTimer{
	protected static final String TAG = "ShotCount";

	Preview mpreview;
	
	public ShotCount(long millisInFuture, long countDownInterval, Preview preview) {
		super(millisInFuture, countDownInterval);
		mpreview = preview;
	}
	
	@Override
	public void onFinish() {		
		
	}

	@Override
	public void onTick(long millisUntilFinished) {		
		mpreview.camera.takePicture(shutterCallback, rawCallback, jpegCallback);
	}
	ShutterCallback shutterCallback = new ShutterCallback() {
		public void onShutter() {
			AudioManager mgr = (AudioManager) mpreview.getContext().getSystemService(Context.AUDIO_SERVICE);
			mgr.setStreamMute(AudioManager.STREAM_SYSTEM, true);
			Log.d(TAG, "onShutter'd");
		}
	};

	/** Handles data for raw picture */
	PictureCallback rawCallback = new PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
			Log.d(TAG, "onPictureTaken - raw");
		}
	};

	/** Handles data for jpeg picture */
	PictureCallback jpegCallback = new PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
			FileOutputStream outStream = null;
			try {
				// write to local sandbox file system
				// outStream =
				// CameraDemo.this.openFileOutput(String.format("%d.jpg",
				// System.currentTimeMillis()), 0);
				// Or write to sdcard
				outStream = new FileOutputStream(String.format(
						"/sdcard/shotresult/%d.jpg", System.currentTimeMillis()));
				outStream.write(data);
				outStream.close();
				Log.d(TAG, "onPictureTaken - wrote bytes: " + data.length);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
			}
			Log.d(TAG, "onPictureTaken - jpeg");
			mpreview.camera.startPreview();
			AudioManager mgr = (AudioManager) mpreview.getContext().getSystemService(Context.AUDIO_SERVICE);
			mgr.setStreamMute(AudioManager.STREAM_SYSTEM, false);
		}
	};
}

class Preview extends SurfaceView implements SurfaceHolder.Callback {
	private static final String TAG = "Preview";

	SurfaceHolder mHolder;
	public Camera camera;
	

	Preview(Context context) {
		super(context);

		// Install a SurfaceHolder.Callback so we get notified when the
		// underlying surface is created and destroyed.
		mHolder = getHolder();
		mHolder.addCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}
	
	public void startCamera() {
		
		try {
			camera.setPreviewDisplay(mHolder);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	public void surfaceCreated(SurfaceHolder holder) {
		// The Surface has been created, acquire the camera and tell it where
		// to draw.
		startCamera();
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		// Surface will be destroyed when we return, so stop the preview.
		// Because the CameraDevice object is not a shared resource, it's very
		// important to release it when the activity is paused.
		if (camera != null) {
			camera.stopPreview();
			camera = null;
		}
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		// Now that the size is known, set up the camera parameters and begin
		// the preview.	
		Camera.Parameters parameters = camera.getParameters();
		camera.setParameters(parameters);
		camera.startPreview();
	}

	@Override
	public void draw(Canvas canvas) {
		super.draw(canvas);
		Paint p = new Paint(Color.RED);
		Log.d(TAG, "draw");
		canvas.drawText("PREVIEW", canvas.getWidth() / 2,
				canvas.getHeight() / 2, p);
	}
}