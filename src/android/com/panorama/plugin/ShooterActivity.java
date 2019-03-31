package com.dermandar.panoramal;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.media.ExifInterface;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import android.support.v4.content.ContextCompat;
import android.content.pm.PackageManager;

import com.dermandar.dmd_lib.CallbackInterfaceShooter;
import com.dermandar.dmd_lib.DMD_Capture;
import com.dermandar.dmd_lib.DMD_Capture.FinishShootingEnum;

public class ShooterActivity extends Activity {

	private RelativeLayout mRelativeLayoutRoot;
	private ViewGroup mViewGroupCamera;
	private DMD_Capture mDMDCapture;

	public static final String EQUI_FOLDER_NAME = "Panoramas";

	private Display mDisplay;
	private DisplayMetrics mDisplayMetrics;

	private TextView mTextViewInstruction;

	private SimpleDateFormat mSimpleDateFormat;

	private String mPanoramaName, mEquiPath;
	private boolean mIsCapturing, mIsStitching;
	private int mNumberTakenImages;

	private int mCurrentInstructionMessageID = -1;
	private int lAngle = 0;

	private boolean isBackPressed = false;

	public static final int REQUEST_PREVIEW = 101;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		int permissionExternalStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
		int permissionCamera = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE);

		if(permissionExternalStorage != PackageManager.PERMISSION_GRANTED || permissionCamera != PackageManager.PERMISSION_GRANTED) {
			toastMessage("permissions required");
		}
		
		File extCacheDir = getExternalCacheDir();
		tmpPath=null;
		if(extCacheDir==null) {

			toastMessage("Check Storage Permission");
			return;
		}
		tmpPath=extCacheDir.getAbsolutePath() + "/dmd_lite_sample/";
		//getting screen resolution
		mDisplay = getWindowManager().getDefaultDisplay();
		mDisplayMetrics = new DisplayMetrics();
		mDisplay.getMetrics(mDisplayMetrics);

		//File name formatter
		mSimpleDateFormat = new SimpleDateFormat("yyMMdd_HHmmss");

		mRelativeLayoutRoot = new RelativeLayout(this);
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
				RelativeLayout.LayoutParams.MATCH_PARENT);
		mRelativeLayoutRoot.setLayoutParams(layoutParams);

		mDMDCapture = new DMD_Capture();
		mViewGroupCamera = mDMDCapture.initShooter(this, mCallbackInterface, getWindowManager().getDefaultDisplay().getRotation(), true, true);
		mRelativeLayoutRoot.addView(mViewGroupCamera);
		mViewGroupCamera.setOnClickListener(mCameraOnClickListener);

		//Text View instruction
		mTextViewInstruction = new TextView(this);
		mTextViewInstruction.setTextSize(32f);
		mTextViewInstruction.setGravity(Gravity.CENTER);
		setInstructionMessage(R.string.instruction_tap_start);
		mRelativeLayoutRoot.addView(mTextViewInstruction);

		if(mDMDCapture.isTablet()) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		}
		else {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}

		setContentView(mRelativeLayoutRoot);
		//showAngle();
	}

	@Override
	protected void onPause() {
		super.onPause();
		if(mIsCapturing) {
			//clear the flag to prevent the screen of being on
			getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

			mIsCapturing = false;
			setInstructionMessage(R.string.instruction_tap_start);
		}
		mDMDCapture.stopShooting();
		mDMDCapture.stopCamera();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mDMDCapture.startCamera(this, mDisplayMetrics.widthPixels, mDisplayMetrics.heightPixels);
		mTextViewInstruction.setVisibility(View.VISIBLE);
	}

	@Override
	public void onBackPressed() {
		if(mIsStitching) {
			return;
		}
		if(mIsCapturing) {
			//clear the flag to prevent the screen of being on
			getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

			mDMDCapture.restart();
			mIsCapturing = false;
			setInstructionMessage(R.string.instruction_tap_start);
		}
		else {
			isBackPressed = true;
			mDMDCapture.release();
		}
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
	}

	private void setInstructionMessage(int msgID) {
		if(mCurrentInstructionMessageID == msgID)
			return;

		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(mDisplayMetrics.widthPixels, RelativeLayout.LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.CENTER_HORIZONTAL);

		if(msgID == R.string.instruction_empty || msgID == R.string.instruction_hold_vertically || msgID == R.string.instruction_tap_start
				|| msgID == R.string.instruction_focusing) {
			params.addRule(RelativeLayout.CENTER_VERTICAL);
		}
		else {
			params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		}

		mTextViewInstruction.setLayoutParams(params);
		mTextViewInstruction.setText(msgID);
		mCurrentInstructionMessageID = msgID;
	}

	private void toastMessage(String message) {
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
	}

	private String tmpPath = null;

	private View.OnClickListener mCameraOnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if(mIsCapturing) {
				//clear the flag to prevent the screen of being on
				getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

				if(mDMDCapture.finishShooting()) {
					mIsStitching = true;
					mTextViewInstruction.setVisibility(View.INVISIBLE);
				}
				mIsCapturing = false;
				setInstructionMessage(R.string.instruction_tap_start);
			}
			else {
				mNumberTakenImages = 0;
				mPanoramaName = mSimpleDateFormat.format(new Date());

				if(mDMDCapture.startShooting(tmpPath)) {
					setInstructionMessage(R.string.instruction_focusing);
					mIsCapturing = true;
					//set flag to keep the screen on
					getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
				}
			}
		}
	};

	private CallbackInterfaceShooter mCallbackInterface = new CallbackInterfaceShooter() {

		@Override
		public void takingPhoto() {

		}

		@Override
		public void stitchingCompleted(HashMap<String, Object> info) {
			File equiFolder = new File(Environment.getExternalStorageDirectory() + "/" + EQUI_FOLDER_NAME + "/");
			if(equiFolder.exists() == false) {
				equiFolder.mkdir();
			}
			mEquiPath = equiFolder.getPath() + "/" + mPanoramaName + ".jpg";
			mDMDCapture.genEquiAt(mEquiPath, 800, 0, 0, false, false);

			//##########################################

			int obj = (Integer) info.get(FinishShootingEnum.fovx.name());
			Log.wtf("@__@", "++++++   stitchingCompleted:"+obj);
			lAngle = obj;
			//##########################################

		}

		@Override
		public void shootingCompleted(boolean finished) {
			//clear the flag to prevent the screen of being on
			getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

			if(finished) {
				mDMDCapture.stopCamera();
				mTextViewInstruction.setVisibility(View.INVISIBLE);
				mIsStitching = true;
			}
			mIsCapturing = false;
		}

		@Override
		public void preparingToShoot() {
		}

		@Override
		public void photoTaken() {
			mNumberTakenImages++;
			if (mNumberTakenImages == 1) {
				setInstructionMessage(R.string.rotate_left_or_right_or_tap_to_restart);
			} else {
				setInstructionMessage(R.string.tap_to_finish_when_ready_or_continue_rotating);
			}
		}

		@Override
		public void deviceVerticalityChanged(int isVertical) {
			if(!mIsCapturing) {
				if(isVertical == 1) {
					setInstructionMessage(R.string.instruction_tap_start);
				}
				else {
					setInstructionMessage(R.string.instruction_hold_vertically);
				}
			}
		}

		@Override
		public void compassEvent(HashMap<String, Object> info) {
			if(info != null) {
				Object obj = info.get(DMD_Capture.CompassActionEnum.kDMDCompassInterference.name());
				if(obj != null && obj instanceof Boolean && ((Boolean)obj).equals(Boolean.TRUE)) {
					toastMessage(getString(R.string.compass_interference_msg));
				}

				//##########################################

				Log.wtf("@__@", "++++++   compassEvent:"+obj.toString());

				//############################################
			}
		}

		@Override
		public void canceledPreparingToShoot() {
		}

		@Override
		public void shotTakenPreviewReady(Bitmap bitmapPreview) {

		}

		@Override
		public void onFinishGeneratingEqui() {
			new SingleMediaScanner(ShooterActivity.this, mEquiPath);
			saveAngle();
			mIsStitching = false;

			Intent i=new Intent(ShooterActivity.this, PreviewActivity.class);
			i.putExtra(PreviewActivity.EXTRA_IMAGE_PATH, mEquiPath);
			startActivityForResult(i, REQUEST_PREVIEW);
		}

		@Override
		public void onRotatorConnected() {

		}

		@Override
		public void onRotatorDisconnected() {

		}

		@Override
		public void onStartedRotating() {

		}

		@Override
		public void onFinishedRotating() {

		}

		@Override
		public void onGalleryIconClicked() {

		}

		@Override
		public void onCameraStarted() {

		}

		@Override
		public void onCameraStopped() {

		}

		@Override
		public void onFinishClear() {

		}

		@Override
		public void onFinishRelease() {
			if(isBackPressed){
				ShooterActivity.super.onBackPressed();
				isBackPressed = false;
			}
		}

		@Override
		public void onHDRIconClicked_InApp() {

		}

		@Override
		public void onDirectionUpdated(float v) {

		}

		@Override
		public void onHDIconClicked_InApp() {

		}
	};

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_PREVIEW) {
            if (resultCode == PreviewActivity.RESULT_CANCEL) {
                File f = new File(mEquiPath);
                f.delete();
                toastMessage("Panorama ignored");
            } else
                toastMessage("Panorama saved");

            mDMDCapture.startCamera(ShooterActivity.this, mDisplayMetrics.widthPixels, mDisplayMetrics.heightPixels);
            mTextViewInstruction.setVisibility(View.VISIBLE);
            setInstructionMessage(R.string.instruction_tap_start);
        }
    }

    private void saveAngle()
	{
		try {
			ExifInterface ei=new ExifInterface(mEquiPath);
			ei.setAttribute("UserComment", lAngle+"");
			ei.setAttribute("CopyRight", lAngle+"");
			ei.saveAttributes();
		} catch (IOException e) {
			e.printStackTrace();

		}

	}
}
