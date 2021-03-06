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

/* <!-- +++
 package com.zihuatanejo.finalcamera_plus.ui;
 +++ --> */
// <!-- -+-
package com.zihuatanejo.finalcamera.opencam.ui;
//-+- -->

import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.webkit.WebView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zihuatanejo.finalcamera.R;
import com.zihuatanejo.finalcamera.opencam.ApplicationInterface;
import com.zihuatanejo.finalcamera.opencam.MainScreen;
import com.zihuatanejo.finalcamera.opencam.PluginManager;
import com.zihuatanejo.finalcamera.opencam.cameracontroller.CameraController;
import com.zihuatanejo.finalcamera.ui.RotateImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/* <!-- +++
 import com.zihuatanejo.finalcamera_plus.ApplicationInterface;
 import com.zihuatanejo.finalcamera_plus.MainScreen;
 import com.zihuatanejo.finalcamera_plus.PluginManager;
 import com.zihuatanejo.finalcamera_plus.R;
 import com.zihuatanejo.finalcamera_plus.cameracontroller.CameraController;
 +++ --> */
// <!-- -+-
//-+- -->

public class AlmalenceStore
{
	// store grid adapter
	private ElementAdapter			storeAdapter;
	private List<View>				storeViews;
	private HashMap<View, Integer>	buttonStoreViewAssoc;
	private View					guiView;

	private static final int STORE_ELEMENTS_NUMBER = 6;
	
	AlmalenceStore(View gui)
	{
		guiView = gui;
		storeAdapter = new ElementAdapter();
		storeViews = new ArrayList<View>();
		buttonStoreViewAssoc = new HashMap<View, Integer>();
	}

	public void showStore()
	{
		LayoutInflater inflater = LayoutInflater.from(MainScreen.getInstance());
		List<RelativeLayout> pages = new ArrayList<RelativeLayout>();
		
		// <!-- -+-
		final boolean unlocked = false;
		//-+- -->
		/* <!-- +++
		final boolean unlocked = true; 
		 +++ --> */
		
		// page 1
		RelativeLayout page = (RelativeLayout) inflater.inflate(R.layout.gui_almalence_pager_fragment, null);
		initStoreList();
		
		RelativeLayout store = (RelativeLayout) inflater.inflate(R.layout.gui_almalence_store, null);
		final ImageView imgStoreNext = (ImageView) store.findViewById(R.id.storeWhatsNew);
		GridView gridview = (GridView) store.findViewById(R.id.storeGrid);
		gridview.setAdapter(storeAdapter);
	
		if (!unlocked)
		{
			page.addView(store);
			pages.add(page);
		}

		// page 2
		page = (RelativeLayout) inflater.inflate(R.layout.gui_almalence_pager_fragment, null);
		RelativeLayout features = (RelativeLayout) inflater.inflate(R.layout.gui_almalence_features, null);
		final ImageView imgFeaturesPrev = (ImageView) features.findViewById(R.id.storeWhatsNew);
		imgFeaturesPrev.setVisibility(View.INVISIBLE);
		WebView wv = (WebView)features.findViewById(R.id.text_features);
		wv.loadUrl("file:///android_asset/www/features.html");

		page.addView(features);
		pages.add(page);

		SamplePagerAdapter pagerAdapter = new SamplePagerAdapter(pages);
		final ViewPager viewPager = new ViewPager(MainScreen.getInstance());
		viewPager.setAdapter(pagerAdapter);
		if (!unlocked)
			viewPager.setCurrentItem(0);
		else
			viewPager.setCurrentItem(1);
		viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener()
		{
			@Override
			public void onPageSelected(int position)
			{
				switch (position)
				{
				case 0:
					// 0
					imgStoreNext.setVisibility(View.VISIBLE);
					// 1
					imgFeaturesPrev.setVisibility(View.INVISIBLE);
					break;
				case 1:
					// 0
					imgStoreNext.setVisibility(View.INVISIBLE);
					// 1
					if (!unlocked)
						imgFeaturesPrev.setVisibility(View.VISIBLE);
					else
						imgFeaturesPrev.setVisibility(View.INVISIBLE);
					break;
				default:
					break;
				}
			}
		});
		
		imgStoreNext.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				viewPager.setCurrentItem(1);
			}
		});
		
		imgFeaturesPrev.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				viewPager.setCurrentItem(0);
			}
		});
		
		guiView.findViewById(R.id.buttonGallery).setEnabled(false);
		guiView.findViewById(R.id.buttonShutter).setEnabled(false);
		guiView.findViewById(R.id.buttonSelectMode).setEnabled(false);

		PluginManager.getInstance().sendMessage(ApplicationInterface.MSG_BROADCAST,
				ApplicationInterface.MSG_CONTROL_LOCKED);

		MainScreen.getGUIManager().lockControls = true;

		// <!-- -+-
		if (MainScreen.getInstance().showPromoRedeemed)
		{
			Toast.makeText(MainScreen.getInstance(),
					"The promo code has been successfully redeemed. All PRO-Features are unlocked", Toast.LENGTH_LONG)
					.show();
			MainScreen.getInstance().showPromoRedeemed = false;
		}
		if (MainScreen.getInstance().showPromoRedeemedJulius)
		{
			Toast.makeText(MainScreen.getInstance(),MainScreen.getInstance().getResources()
					.getString(R.string.promoRedeemedJulius), Toast.LENGTH_LONG)
					.show();
			MainScreen.getInstance().showPromoRedeemedJulius = false;
		}
		//-+- -->
		
		final RelativeLayout pagerLayout = ((RelativeLayout) guiView.findViewById(R.id.viewPagerLayout));
		pagerLayout.addView(viewPager);
				
		final RelativeLayout pagerLayoutMain = ((RelativeLayout) guiView.findViewById(R.id.viewPagerLayoutMain));
		pagerLayoutMain.setVisibility(View.VISIBLE);
		pagerLayoutMain.bringToFront();


		// We need this timer, to show store on top, after we return from google
		// play.
		// In MainScreen there is timer, which brings main buttons on top,
		// after MainScreen activity resumed.
		// So this timer "blocks" timer from MainScreen if we want to show
		// store.
		new CountDownTimer(600, 10)
		{
			public void onTick(long millisUntilFinished)
			{
				pagerLayoutMain.bringToFront();
			}

			public void onFinish()
			{
				pagerLayoutMain.bringToFront();
			}
		}.start();
	}

	public void hideStore()
	{
		((RelativeLayout) guiView.findViewById(R.id.viewPagerLayoutMain)).setVisibility(View.INVISIBLE);

		guiView.findViewById(R.id.buttonGallery).setEnabled(true);
		guiView.findViewById(R.id.buttonShutter).setEnabled(true);
		guiView.findViewById(R.id.buttonSelectMode).setEnabled(true);

		PluginManager.getInstance().sendMessage(ApplicationInterface.MSG_BROADCAST, 
				ApplicationInterface.MSG_CONTROL_UNLOCKED);

		MainScreen.getGUIManager().lockControls = false;
	}

	
	private void initStoreList()
	{
		storeViews.clear();
		buttonStoreViewAssoc.clear();

		// <!-- -+-
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainScreen.getMainContext());
		boolean bOnSale = prefs.getBoolean("bOnSale", false);

		for (int i = 0; i < STORE_ELEMENTS_NUMBER; i++)
		{
			LayoutInflater inflator = MainScreen.getInstance().getLayoutInflater();
			View item = inflator.inflate(R.layout.gui_almalence_store_grid_element, null, false);
			ImageView icon = (ImageView) item.findViewById(R.id.storeImage);
			TextView description = (TextView) item.findViewById(R.id.storeText);
			TextView price = (TextView) item.findViewById(R.id.storePriceText);
			switch (i)
			{
			case 0:
				// unlock all
				icon.setImageDrawable(MainScreen.getAppResources().getDrawable(R.drawable.store_all));
				description.setText(MainScreen.getAppResources()
						.getString(R.string.Pref_Upgrde_All_Preference_Title));

				if (MainScreen.getInstance().isPurchasedAll())
					price.setText(R.string.already_unlocked);
				else
				{
					if (MainScreen.getInstance().isCouponSale())
					{
						price.setText(MainScreen.getInstance().titleUnlockAllCoupon);
						((ImageView) item.findViewById(R.id.storeSaleImage)).setVisibility(View.VISIBLE);
					} else
					{
						price.setText(MainScreen.getInstance().titleUnlockAll);
						if (bOnSale)
							((ImageView) item.findViewById(R.id.storeSaleImage)).setVisibility(View.VISIBLE);
					}
				}
				break;
			case 1:
				// Super
				icon.setImageDrawable(MainScreen.getAppResources().getDrawable(R.drawable.store_super));
				description.setText(MainScreen.getAppResources()
						.getString(R.string.Pref_Upgrde_Super_Preference_Title));
				if (MainScreen.getInstance().isPurchasedSuper() || MainScreen.getInstance().isPurchasedAll())
					price.setText(R.string.already_unlocked);
				else
				{
					if (CameraController.isSuperModePossible())
						price.setText(MainScreen.getInstance().titleUnlockSuper);
					else
						price.setText(MainScreen.getAppResources()
								.getString(R.string.Pref_Upgrde_SuperNotSupported));
				}
				break;
			case 2:
				// HDR
				icon.setImageDrawable(MainScreen.getAppResources().getDrawable(R.drawable.store_hdr));
				description.setText(MainScreen.getAppResources()
						.getString(R.string.Pref_Upgrde_HDR_Preference_Title));
				if (MainScreen.getInstance().isPurchasedHDR() || MainScreen.getInstance().isPurchasedAll())
					price.setText(R.string.already_unlocked);
				else
					price.setText(MainScreen.getInstance().titleUnlockHDR);
				break;
			case 3:
				// Panorama
				icon.setImageDrawable(MainScreen.getAppResources().getDrawable(R.drawable.store_panorama));
				description.setText(MainScreen.getAppResources()
						.getString(R.string.Pref_Upgrde_Panorama_Preference_Title));
				if (MainScreen.getInstance().isPurchasedPanorama() || MainScreen.getInstance().isPurchasedAll())
					price.setText(R.string.already_unlocked);
				else
					price.setText(MainScreen.getInstance().titleUnlockPano);
				break;
			case 4:
				// multishot
				icon.setImageDrawable(MainScreen.getAppResources().getDrawable(R.drawable.store_moving));
				description.setText(MainScreen.getAppResources()
						.getString(R.string.Pref_Upgrde_Moving_Preference_Title));
				if (MainScreen.getInstance().isPurchasedMoving() || MainScreen.getInstance().isPurchasedAll())
					price.setText(R.string.already_unlocked);
				else
					price.setText(MainScreen.getInstance().titleUnlockMoving);
				break;
			case 5:
				// Promo code
				icon.setImageDrawable(MainScreen.getAppResources().getDrawable(R.drawable.store_promo));
				description.setText(MainScreen.getAppResources()
						.getString(R.string.Pref_Upgrde_PromoCode_Preference_Title));
				if (MainScreen.getInstance().isPurchasedAll())
					price.setText(R.string.already_unlocked);
				else
					price.setText("");
				break;
			default:
				break;
			}

			item.setOnClickListener(new OnClickListener()
			{
				public void onClick(View v)
				{
					// get inapp associated with pressed button
					purchasePressed(v);
				}
			});

			buttonStoreViewAssoc.put(item, i);
			storeViews.add(item);
		}
		//-+- -->

		storeAdapter.Elements = storeViews;
	}

	private void purchasePressed(View v)
	{
		// <!-- -+-
		// get inapp associated with pressed button
		Integer id = buttonStoreViewAssoc.get(v);
		if (id == null)
			return;
		switch (id)
		{
		case 0:// unlock all
			MainScreen.getInstance().purchaseAll();
			break;
		case 1:// HDR
			if (CameraController.isSuperModePossible())
				MainScreen.getInstance().purchaseSuper();
			else
				Toast.makeText(MainScreen.getMainContext(), "Not supported", Toast.LENGTH_LONG).show();
			break;
		case 2:// HDR
			MainScreen.getInstance().purchaseHDR();
			break;
		case 3:// Panorama
			MainScreen.getInstance().purchasePanorama();
			break;
		case 4:// multishot
			MainScreen.getInstance().purchaseMultishot();
			break;
		case 5:// Promo
			if (!MainScreen.getInstance().isPurchasedAll())
				MainScreen.getInstance().enterPromo();
			break;
		
		default:
			break;
		}
		//-+- -->
	}

	//-+- -->
	
	public void ShowUnlockControl()
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainScreen.getMainContext());
		boolean bOnSale = prefs.getBoolean("bOnSale", false);
		final RotateImageView unlock = ((RotateImageView) guiView.findViewById(R.id.Unlock));
		unlock.setImageDrawable(MainScreen.getAppResources().getDrawable(bOnSale ? R.drawable.unlock_sale : R.drawable.unlock));
		unlock.setAlpha(1.0f);
		unlock.setVisibility(View.VISIBLE);

		Animation invisible_alpha = new AlphaAnimation(1, 0.4f);
		invisible_alpha.setDuration(7000);
		invisible_alpha.setRepeatCount(0);

		invisible_alpha.setAnimationListener(new AnimationListener()
		{
			@Override
			public void onAnimationEnd(Animation animation)
			{
				unlock.clearAnimation();
				unlock.setImageDrawable(MainScreen.getAppResources().getDrawable(R.drawable.unlock_gray));
				unlock.setAlpha(0.4f);
			}

			@Override
			public void onAnimationRepeat(Animation animation)
			{
			}

			@Override
			public void onAnimationStart(Animation animation)
			{
			}
		});

		unlock.startAnimation(invisible_alpha);
	}

	public void ShowGrayUnlockControl()
	{
		final RotateImageView unlock = ((RotateImageView) guiView.findViewById(R.id.Unlock));
		if (unlock.getVisibility() == View.VISIBLE)
			return;
		unlock.setImageDrawable(MainScreen.getAppResources().getDrawable(R.drawable.unlock_gray));
		unlock.setAlpha(0.4f);
		unlock.setVisibility(View.VISIBLE);
	}

	public void HideUnlockControl()
	{
		final RotateImageView unlock = ((RotateImageView) guiView.findViewById(R.id.Unlock));
		unlock.setVisibility(View.GONE);
	}

	public void setOrientation()
	{
		((RotateImageView) guiView.findViewById(R.id.Unlock)).setOrientation(AlmalenceGUI.mDeviceOrientation);
	}
}
