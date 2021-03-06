/*
The contents of this file are subject to the Mozilla Public License
Version 1.1 (the "License"); you may not use this file except in
compliance with the License. You may obtain a copy of the License at
http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS"
basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
License for the specific language governing rights and limitations
under the License.

The Original Code is collection of files collectively known as Open Camera.

The Initial Developer of the Original Code is Almalence Inc.
Portions created by Initial Developer are Copyright (C) 2013 
by Almalence Inc. All Rights Reserved.
 */

package com.zihuatanejo.finalcamera.plugins.processing.groupshot;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.zihuatanejo.finalcamera.R;
import com.zihuatanejo.finalcamera.SwapHeap;
import com.zihuatanejo.finalcamera.opencam.ApplicationInterface;
import com.zihuatanejo.finalcamera.opencam.ApplicationScreen;
import com.zihuatanejo.finalcamera.opencam.PluginManager;
import com.zihuatanejo.finalcamera.opencam.cameracontroller.CameraController;
import com.zihuatanejo.finalcamera.plugins.processing.multishot.MultiShotProcessingPlugin;

import java.util.ArrayList;

/* <!-- +++
 import com.zihuatanejo.finalcamera_plus.ApplicationScreen;
 import com.zihuatanejo.finalcamera_plus.ApplicationInterface;
 import com.zihuatanejo.finalcamera_plus.PluginManager;
 import com.zihuatanejo.finalcamera_plus.R;
 import com.zihuatanejo.finalcamera_plus.cameracontroller.CameraController;
 +++ --> */
// <!-- -+-
//-+- -->

/***
 * Implements group shot processing
 ***/
@SuppressWarnings("deprecation")
public class GroupShotProcessingPluginRefactored extends MultiShotProcessingPlugin
{
	private View				postProcessingView;

	private long				sessionID					= 0;

	private static final int	MSG_PROGRESS_BAR_INVISIBLE	= 1;
	private static final int	MSG_PROGRESS_BAR_VISIBLE	= 2;
	private static final int	MSG_LEAVING					= 3;
	private static final int	MSG_END_OF_LOADING			= 4;
	private static final int	MSG_SELECTOR_VISIBLE		= 5;
	private static final int	MSG_SELECTOR_INVISIBLE		= 6;

	static final int			img2lay						= 8;					// 16
																					// image-to-layout
																					// subsampling
																					// factor

	private int					mDisplayOrientation;

	static int					OutNV21						= 0;

	static int[]				mPixelsforPreview			= null;

	static int					mBaseFrame					= 0;					// temporary

	static int[]				crop						= new int[5];			// crop
																					// parameters
																					// and
																					// base
																					// image
																					// are
																					// stored
																					// here

	private ImageView			mResultImageView;
	private Button				mSaveButton;

	private final Handler		mHandler					= new Handler(this);

	private ProgressBar			mProgressBar;
	private Gallery				mGallery;
	private TextView			mInfoTextVeiw;

	private ImageAdapter		mImageAdapter;

	@Override
	public void setYUVBufferList(ArrayList<Integer> YUVBufferList)
	{
		GroupShotCore.getInstance().setYUVBufferList(YUVBufferList);
	}

	private final Object	syncObject			= new Object();

	private boolean			postProcessingRun	= false;

	// indicates that no more user interaction needed
	private boolean			mFinishing			= false;
	private boolean			mChangingFace		= false;

	public GroupShotProcessingPluginRefactored()
	{
	}

	public View getPostProcessingView()
	{
		return postProcessingView;
	}

	public void onStart()
	{
	}

	public void onStartProcessing(long SessionID)
	{
		mFinishing = false;
		mChangingFace = false;
		Message msg = new Message();
		msg.what = ApplicationInterface.MSG_PROCESSING_BLOCK_UI;
		ApplicationScreen.getMessageHandler().sendMessage(msg);

		PluginManager.getInstance().sendMessage(ApplicationInterface.MSG_BROADCAST,
				ApplicationInterface.MSG_CONTROL_LOCKED);

		ApplicationScreen.getGUIManager().lockControls = true;

		sessionID = SessionID;

		PluginManager.getInstance().addToSharedMem("modeSaveName" + sessionID,
				PluginManager.getInstance().getActiveMode().modeSaveName);

		int imageDataOrientation = Integer.valueOf(PluginManager.getInstance().getFromSharedMem(
				"frameorientation1" + sessionID));
		int deviceOrientation = Integer.valueOf(PluginManager.getInstance().getFromSharedMem(
				"deviceorientation1" + sessionID));
		boolean cameraMirrored = Boolean.valueOf(PluginManager.getInstance().getFromSharedMem(
				"framemirrored1" + sessionID));

		try
		{
			GroupShotCore.getInstance().initializeProcessingParameters(imageDataOrientation, deviceOrientation, cameraMirrored);
			GroupShotCore.getInstance().ProcessFaces();
		} catch (Exception e)
		{
			// make notifier in main thread
			e.printStackTrace();
		}

		PluginManager.getInstance().addToSharedMem("resultfromshared" + sessionID, "false");
		PluginManager.getInstance().addToSharedMem("amountofresultframes" + sessionID, "1");

		CameraController.Size imageSize = CameraController.getCameraImageSize();
		int imageWidth = imageSize.getWidth();
		int imageHeight = imageSize.getHeight();
		PluginManager.getInstance().addToSharedMem("saveImageWidth" + sessionID, String.valueOf(imageWidth));
		PluginManager.getInstance().addToSharedMem("saveImageHeight" + sessionID, String.valueOf(imageHeight));
		
		mDisplayOrientation = ApplicationScreen.getGUIManager().getDisplayOrientation();
		mImageAdapter = new ImageAdapter(ApplicationScreen.getMainContext(), GroupShotCore.getInstance().getYUVBufferList(), imageDataOrientation, cameraMirrored);
	}

	/************************************************
	 * POST PROCESSING
	 ************************************************/
	public boolean isPostProcessingNeeded()
	{
		return true;
	}

	public void onStartPostProcessing()
	{
		LayoutInflater inflator = ApplicationScreen.instance.getLayoutInflater();
		postProcessingView = inflator.inflate(R.layout.plugin_processing_groupshot_postprocessing, null, false);

		mResultImageView = ((ImageView) postProcessingView.findViewById(R.id.groupshotImageHolder));

		mResultImageView.setImageBitmap(GroupShotCore.getInstance().getPreviewBitmap());

		mInfoTextVeiw = ((TextView) postProcessingView.findViewById(R.id.groupshotTextView));
		mInfoTextVeiw.setText("Loading image ...");

		mHandler.sendEmptyMessage(MSG_END_OF_LOADING);
	}

	private void setupImageSelector()
	{
		mGallery = (Gallery) postProcessingView.findViewById(R.id.groupshotGallery);
		mGallery.setAdapter(mImageAdapter);
		mGallery.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View v, int position, long id)
			{
				mImageAdapter.setCurrentSeleted(position);
				mImageAdapter.notifyDataSetChanged();
				mGallery.setVisibility(Gallery.INVISIBLE);
				mBaseFrame = position;
				GroupShotCore.getInstance().setBaseFrame(mBaseFrame);
				new Thread(new Runnable()
				{
					public void run()
					{
						mHandler.sendEmptyMessage(MSG_PROGRESS_BAR_VISIBLE);
						GroupShotCore.getInstance().updateBitmap();
						mHandler.post(new Runnable()
						{
							public void run()
							{
								if (GroupShotCore.getInstance().getPreviewBitmap() != null)
								{
									mResultImageView.setImageBitmap(GroupShotCore.getInstance().getPreviewBitmap());
								}
							}
						});

						mHandler.sendEmptyMessage(MSG_PROGRESS_BAR_INVISIBLE);
					}
				}).start();
			}
		});

		mGallery.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
		{
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3)
			{
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0)
			{
			}
		});
		return;
	}

	private void setupImageView()
	{
		GroupShotCore.getInstance().updateBitmap();
		Bitmap mPreviewBitmap = GroupShotCore.getInstance().getPreviewBitmap();
		if (mPreviewBitmap != null)
		{
			mResultImageView.setImageBitmap(mPreviewBitmap);
		}

		mResultImageView.setOnTouchListener(new View.OnTouchListener()
		{
			@Override
			public boolean onTouch(final View v, final MotionEvent event)
			{
				if (event.getAction() == MotionEvent.ACTION_DOWN)
				{
					if (mFinishing || mChangingFace)
						return true;
					if (mGallery.getVisibility() == Gallery.VISIBLE)
					{
						mGallery.setVisibility(Gallery.INVISIBLE);
						return false;
					}
					mHandler.sendEmptyMessage(MSG_PROGRESS_BAR_VISIBLE);
					new Thread(new Runnable()
					{
						public void run()
						{
							synchronized (syncObject)
							{
								mChangingFace = true;
								if (GroupShotCore.getInstance().eventContainsFace(event.getX(), event.getY(), v))
								{
									GroupShotCore.getInstance().updateBitmap();

									// Update screen
									mHandler.post(new Runnable()
									{
										public void run()
										{
											if (GroupShotCore.getInstance().getPreviewBitmap() != null)
											{
												mResultImageView.setImageBitmap(GroupShotCore.getInstance()
														.getPreviewBitmap());
											}
										}
									});
								}
								mHandler.sendEmptyMessage(MSG_PROGRESS_BAR_INVISIBLE);
								mChangingFace = false;
							}
						}
					}).start();
				}
				return false;
			}
		});
	}

	private void setupProgress()
	{
		mProgressBar = (ProgressBar) postProcessingView.findViewById(R.id.groupshotProgressBar);
		mProgressBar.setVisibility(View.INVISIBLE);
	}

	public void setupSaveButton()
	{
		mSaveButton = (Button) postProcessingView.findViewById(R.id.groupshotSaveButton);
		mSaveButton.setOnClickListener(this);
		mSaveButton.setRotation(mDisplayOrientation);
	}

	@Override
	public boolean handleMessage(Message msg)
	{
		switch (msg.what)
		{
		case MSG_END_OF_LOADING:
			setupImageView();
			setupImageSelector();
			setupSaveButton();
			setupProgress();
			mInfoTextVeiw.setVisibility(TextView.INVISIBLE);
			postProcessingRun = true;
			break;
		case MSG_PROGRESS_BAR_INVISIBLE:
			mProgressBar.setVisibility(View.INVISIBLE);
			break;
		case MSG_PROGRESS_BAR_VISIBLE:
			mProgressBar.setVisibility(View.VISIBLE);
			break;
		case MSG_SELECTOR_VISIBLE:
			mGallery.setVisibility(View.VISIBLE);
			break;
		case MSG_SELECTOR_INVISIBLE:
			mGallery.setVisibility(View.GONE);
			break;
		case MSG_LEAVING:
			ApplicationScreen.getMessageHandler().sendEmptyMessage(ApplicationInterface.MSG_POSTPROCESSING_FINISHED);
			GroupShotCore.getInstance().release();

			PluginManager.getInstance().sendMessage(ApplicationInterface.MSG_BROADCAST,
					ApplicationInterface.MSG_CONTROL_UNLOCKED);

			ApplicationScreen.getGUIManager().lockControls = false;

			postProcessingRun = false;
			return false;
		default:
			return true;
		}
		return true;
	}

	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK
				&& ApplicationScreen.instance.findViewById(R.id.postprocessingLayout).getVisibility() == View.VISIBLE)
		{
			if (mFinishing || mChangingFace)
				return true;
			mFinishing = true;
			mHandler.sendEmptyMessage(MSG_LEAVING);
			return true;
		}

		return false;
	}

	@Override
	public void onClick(View v)
	{
		if (v == mSaveButton)
		{
			if (mFinishing || mChangingFace)
				return;
			mFinishing = true;
			savePicture(ApplicationScreen.getMainContext());

			mHandler.sendEmptyMessage(MSG_LEAVING);
		}
	}

	public void onOrientationChanged(int orientation)
	{
		if (orientation != mDisplayOrientation)
		{
			mDisplayOrientation = (orientation == 0 || orientation == 180) ? orientation + 90 : orientation - 90;
			mDisplayOrientation = orientation;
			if (postProcessingRun)
				mSaveButton.setRotation(mDisplayOrientation);
		}
	}

	public void savePicture(Context context)
	{
		byte[] result = GroupShotCore.getInstance().processingSaveData();
		if (result == null)
		{
			Log.e("GroupShot", "Exception occured in processingSaveData. Picture not saved.");
			return;
		}

		int frame_len = result.length;
		int frame = SwapHeap.SwapToHeap(result);
		PluginManager.getInstance().addToSharedMem("resultframeformat1" + sessionID, "jpeg");
		PluginManager.getInstance().addToSharedMem("resultframe1" + sessionID, String.valueOf(frame));
		PluginManager.getInstance().addToSharedMem("resultframelen1" + sessionID, String.valueOf(frame_len));

		// Nexus 6 has a original front camera sensor orientation, we have to
		// manage it
		//PluginManager.getInstance().addToSharedMem("resultframeorientation1" + sessionID,
		//		String.valueOf(((CameraController.isFlippedSensorDevice() && mCameraMirrored) ? 180 : 0)));
		//PluginManager.getInstance().addToSharedMem("resultframemirrored1" + sessionID, String.valueOf(mCameraMirrored));

		PluginManager.getInstance().addToSharedMem("amountofresultframes" + sessionID, String.valueOf(1));
		PluginManager.getInstance().addToSharedMem("sessionID", String.valueOf(sessionID));
	}
}
