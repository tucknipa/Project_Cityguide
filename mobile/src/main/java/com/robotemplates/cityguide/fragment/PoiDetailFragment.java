package com.robotemplates.cityguide.fragment;

import android.animation.Animator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.maps.model.LatLng;
import com.melnykov.fab.FloatingActionButton;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.robotemplates.cityguide.CityGuideApplication;
import com.robotemplates.cityguide.CityGuideConfig;
import com.robotemplates.cityguide.R;
import com.robotemplates.cityguide.activity.MapActivity;
import com.robotemplates.cityguide.activity.PoiDetailActivity;
import com.robotemplates.cityguide.database.DatabaseCallListener;
import com.robotemplates.cityguide.database.DatabaseCallManager;
import com.robotemplates.cityguide.database.DatabaseCallTask;
import com.robotemplates.cityguide.database.dao.PoiDAO;
import com.robotemplates.cityguide.database.data.Data;
import com.robotemplates.cityguide.database.model.PoiModel;
import com.robotemplates.cityguide.database.query.PoiReadQuery;
import com.robotemplates.cityguide.database.query.Query;
import com.robotemplates.cityguide.dialog.AboutDialogFragment;
import com.robotemplates.cityguide.geolocation.Geolocation;
import com.robotemplates.cityguide.geolocation.GeolocationListener;
import com.robotemplates.cityguide.listener.AnimateImageLoadingListener;
import com.robotemplates.cityguide.utility.LocationUtility;
import com.robotemplates.cityguide.utility.Logcat;
import com.robotemplates.cityguide.utility.NetworkManager;
import com.robotemplates.cityguide.utility.ResourcesHelper;
import com.robotemplates.cityguide.view.ObservableStickyScrollView;
import com.robotemplates.cityguide.view.ViewState;

import java.sql.SQLException;
import java.util.Date;


public class PoiDetailFragment extends TaskFragment implements DatabaseCallListener, GeolocationListener
{
	private static final String DIALOG_ABOUT = "about";
	private static final long TIMER_DELAY = 60000l; // in milliseconds
	private static final int MAP_ZOOM = 14;

	private ViewState mViewState = null;
	private View mRootView;
	private DatabaseCallManager mDatabaseCallManager = new DatabaseCallManager();
	private ImageLoader mImageLoader = ImageLoader.getInstance();
	private DisplayImageOptions mDisplayImageOptions;
	private ImageLoadingListener mImageLoadingListener;
	private Geolocation mGeolocation = null;
	private Location mLocation = null;
	private Handler mTimerHandler;
	private Runnable mTimerRunnable;

	private long mPoiId;
	private PoiModel mPoi;

	
	@Override
	public void onAttach(Activity activity) 
	{
		super.onAttach(activity);
	}
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		setHasOptionsMenu(true);
		setRetainInstance(true);

		// handle intent extras
		Bundle extras = getActivity().getIntent().getExtras();
		if(extras != null)
		{
			handleExtras(extras);
		}

		// image caching options
		mDisplayImageOptions = new DisplayImageOptions.Builder()
				.showImageOnLoading(android.R.color.transparent)
				.showImageForEmptyUri(R.drawable.placeholder_photo)
				.showImageOnFail(R.drawable.placeholder_photo)
				.cacheInMemory(true)
				.cacheOnDisk(true)
				.displayer(new SimpleBitmapDisplayer())
				.build();
		mImageLoadingListener = new AnimateImageLoadingListener();
	}
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		mRootView = inflater.inflate(R.layout.fragment_poi_detail, container, false);
		return mRootView;
	}
	
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		
		// load and show data
		if(mViewState==null || mViewState==ViewState.OFFLINE)
		{
			loadData();
		}
		else if(mViewState==ViewState.CONTENT)
		{
			if(mPoi !=null) renderView();
			showContent();
		}
		else if(mViewState==ViewState.PROGRESS)
		{
			showProgress();
		}
		else if(mViewState==ViewState.EMPTY)
		{
			showEmpty();
		}

		// init timer task
		setupTimer();
	}
	
	
	@Override
	public void onStart()
	{
		super.onStart();
	}
	
	
	@Override
	public void onResume()
	{
		super.onResume();

		// timer
		startTimer();
	}
	
	
	@Override
	public void onPause()
	{
		super.onPause();

		// timer
		stopTimer();

		// stop geolocation
		if(mGeolocation!=null) mGeolocation.stop();
	}
	
	
	@Override
	public void onStop()
	{
		super.onStop();
	}
	
	
	@Override
	public void onDestroyView()
	{
		super.onDestroyView();
		mRootView = null;
	}
	
	
	@Override
	public void onDestroy()
	{
		super.onDestroy();

		// cancel async tasks
		mDatabaseCallManager.cancelAllTasks();
	}
	
	
	@Override
	public void onDetach()
	{
		super.onDetach();
	}
	
	
	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		// save current instance state
		super.onSaveInstanceState(outState);
		setUserVisibleHint(true);
	}
	
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		// action bar menu
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.menu_poi_detail, menu);
	}
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
		// action bar menu behaviour
		switch(item.getItemId())
		{
			case R.id.menu_share:
				if(mPoi != null)
				{
					startShareActivity(getString(R.string.fragment_poi_detail_share_subject), getPoiText());
				}
				return true;

			case R.id.menu_rate:
				startWebActivity(getString(R.string.app_store_uri, CityGuideApplication.getContext().getPackageName()));
				return true;

			case R.id.menu_about:
				showAboutDialog();
				return true;

			default:
				return super.onOptionsItemSelected(item);
		}
	}


	@Override
	public void onDatabaseCallRespond(final DatabaseCallTask task, final Data<?> data)
	{
		runTaskCallback(new Runnable()
		{
			public void run()
			{
				if(mRootView == null) return; // view was destroyed

				if(task.getQuery().getClass().equals(PoiReadQuery.class))
				{
					Logcat.d("Fragment.onDatabaseCallRespond(PoiReadQuery)");

					// get data
					Data<PoiModel> poiReadData = (Data<PoiModel>) data;
					mPoi = poiReadData.getDataObject();
				}

				// hide progress and render view
				if(mPoi != null)
				{
					renderView();
					showContent();
				}
				else
				{
					showEmpty();
				}

				// finish query
				mDatabaseCallManager.finishTask(task);
			}
		});
	}


	@Override
	public void onDatabaseCallFail(final DatabaseCallTask task, final Exception exception)
	{
		runTaskCallback(new Runnable()
		{
			public void run()
			{
				if(mRootView == null) return; // view was destroyed

				if(task.getQuery().getClass().equals(PoiReadQuery.class))
				{
					Logcat.d("Fragment.onDatabaseCallFail(PoiReadQuery): " + exception.getClass().getSimpleName() + " / " + exception.getMessage());
				}

				// hide progress
				if(mPoi != null) showContent();
				else showEmpty();

				// handle fail
				handleFail();

				// finish query
				mDatabaseCallManager.finishTask(task);
			}
		});
	}


	@Override
	public void onGeolocationRespond(Geolocation geolocation, final Location location)
	{
		runTaskCallback(new Runnable()
		{
			public void run()
			{
				if(mRootView == null) return; // view was destroyed

				Logcat.d("Fragment.onGeolocationRespond(): " + location.getProvider() + " / " + location.getLatitude() + " / " + location.getLongitude() + " / " + new Date(location.getTime()).toString());
				mLocation = location;
				if(mPoi != null) renderViewInfo();
			}
		});
	}


	@Override
	public void onGeolocationFail(Geolocation geolocation)
	{
		runTaskCallback(new Runnable()
		{
			public void run()
			{
				if(mRootView == null) return; // view was destroyed

				Logcat.d("Fragment.onGeolocationFail()");
			}
		});
	}


	private void handleFail()
	{
		Toast.makeText(getActivity(), R.string.global_database_fail_toast, Toast.LENGTH_LONG).show();
	}


	private void handleExtras(Bundle extras)
	{
		mPoiId = extras.getLong(PoiDetailActivity.EXTRA_POI_ID);
	}

	
	private void loadData()
	{
		// load poi
		if(!mDatabaseCallManager.hasRunningTask(PoiReadQuery.class))
		{
			// show progress
			showProgress();

			// run async task
			Query query = new PoiReadQuery(mPoiId);
			mDatabaseCallManager.executeTask(query, this);
		}
	}


	private void showFloatingActionButton(boolean visible)
	{
		final FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
		if(visible)
		{
			fab.animate()
					.scaleX(1)
					.scaleY(1)
					.setDuration(300)
					.setInterpolator(new AccelerateDecelerateInterpolator())
					.setListener(new Animator.AnimatorListener()
					{
						@Override
						public void onAnimationStart(Animator animator)
						{
							fab.show(false);
							fab.setVisibility(View.VISIBLE);
							fab.setEnabled(false);
						}

						@Override
						public void onAnimationEnd(Animator animator)
						{
							fab.setEnabled(true);
						}

						@Override
						public void onAnimationCancel(Animator animator) {}

						@Override
						public void onAnimationRepeat(Animator animator) {}
					});
		}
		else
		{
			fab.animate()
					.alpha(0f)
					.setDuration(50)
					.setInterpolator(new AccelerateDecelerateInterpolator())
					.setListener(new Animator.AnimatorListener()
					{
						@Override
						public void onAnimationStart(Animator animator)
						{
							fab.setEnabled(false);
						}


						@Override
						public void onAnimationEnd(Animator animator)
						{
							fab.setScaleX(0);
							fab.setScaleY(0);
							fab.setAlpha(1f);
							fab.hide(false);
							fab.setVisibility(View.GONE);
							fab.setEnabled(true);
						}


						@Override
						public void onAnimationCancel(Animator animator)
						{
						}


						@Override
						public void onAnimationRepeat(Animator animator)
						{
						}
					});
		}
	}
	
	
	private void showContent()
	{
		// show content container
		ViewGroup containerContent = (ViewGroup) mRootView.findViewById(R.id.container_content);
		ViewGroup containerProgress = (ViewGroup) mRootView.findViewById(R.id.container_progress);
		ViewGroup containerOffline = (ViewGroup) mRootView.findViewById(R.id.container_offline);
		ViewGroup containerEmpty = (ViewGroup) mRootView.findViewById(R.id.container_empty);
		containerContent.setVisibility(View.VISIBLE);
		containerProgress.setVisibility(View.GONE);
		containerOffline.setVisibility(View.GONE);
		containerEmpty.setVisibility(View.GONE);
		mViewState = ViewState.CONTENT;

		// set toolbar background
		Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
		toolbar.setBackgroundColor(getResources().getColor(android.R.color.transparent));
		toolbar.setVisibility(View.VISIBLE);
	}
	
	
	private void showProgress()
	{
		// show progress container
		ViewGroup containerContent = (ViewGroup) mRootView.findViewById(R.id.container_content);
		ViewGroup containerProgress = (ViewGroup) mRootView.findViewById(R.id.container_progress);
		ViewGroup containerOffline = (ViewGroup) mRootView.findViewById(R.id.container_offline);
		ViewGroup containerEmpty = (ViewGroup) mRootView.findViewById(R.id.container_empty);
		containerContent.setVisibility(View.GONE);
		containerProgress.setVisibility(View.VISIBLE);
		containerOffline.setVisibility(View.GONE);
		containerEmpty.setVisibility(View.GONE);
		mViewState = ViewState.PROGRESS;

		// floating action button
		showFloatingActionButton(false);

		// set toolbar background
		Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
		toolbar.setBackgroundColor(ResourcesHelper.getValueOfAttribute(getActivity(), R.attr.colorPrimary));
		toolbar.setVisibility(View.GONE);
	}
	
	
	private void showOffline()
	{
		// show offline container
		ViewGroup containerContent = (ViewGroup) mRootView.findViewById(R.id.container_content);
		ViewGroup containerProgress = (ViewGroup) mRootView.findViewById(R.id.container_progress);
		ViewGroup containerOffline = (ViewGroup) mRootView.findViewById(R.id.container_offline);
		ViewGroup containerEmpty = (ViewGroup) mRootView.findViewById(R.id.container_empty);
		containerContent.setVisibility(View.GONE);
		containerProgress.setVisibility(View.GONE);
		containerOffline.setVisibility(View.VISIBLE);
		containerEmpty.setVisibility(View.GONE);
		mViewState = ViewState.OFFLINE;

		// floating action button
		showFloatingActionButton(false);

		// set toolbar background
		Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
		toolbar.setBackgroundColor(ResourcesHelper.getValueOfAttribute(getActivity(), R.attr.colorPrimary));
		toolbar.setVisibility(View.VISIBLE);
	}
	
	
	private void showEmpty()
	{
		// show empty container
		ViewGroup containerContent = (ViewGroup) mRootView.findViewById(R.id.container_content);
		ViewGroup containerProgress = (ViewGroup) mRootView.findViewById(R.id.container_progress);
		ViewGroup containerOffline = (ViewGroup) mRootView.findViewById(R.id.container_offline);
		ViewGroup containerEmpty = (ViewGroup) mRootView.findViewById(R.id.container_empty);
		containerContent.setVisibility(View.GONE);
		containerProgress.setVisibility(View.GONE);
		containerOffline.setVisibility(View.GONE);
		containerEmpty.setVisibility(View.VISIBLE);
		mViewState = ViewState.EMPTY;

		// floating action button
		showFloatingActionButton(false);

		// set toolbar background
		Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
		toolbar.setBackgroundColor(ResourcesHelper.getValueOfAttribute(getActivity(), R.attr.colorPrimary));
		toolbar.setVisibility(View.VISIBLE);
	}


	private void renderView()
	{
		renderViewToolbar();
		renderViewInfo();
		renderViewBanner();
		renderViewMap();
		renderViewDescription();
		renderViewGap();
	}

	
	private void renderViewToolbar()
	{
		// reference
		final ObservableStickyScrollView observableStickyScrollView = (ObservableStickyScrollView) mRootView.findViewById(R.id.container_content);
		final FloatingActionButton floatingActionButton = (FloatingActionButton) getActivity().findViewById(R.id.fab);
		final View panelTopView = mRootView.findViewById(R.id.toolbar_image_panel_top);
		final View panelBottomView = mRootView.findViewById(R.id.toolbar_image_panel_bottom);
		final ImageView imageView = (ImageView) mRootView.findViewById(R.id.toolbar_image_imageview);
		final TextView titleTextView = (TextView) mRootView.findViewById(R.id.toolbar_image_title);

		// title
		titleTextView.setText(mPoi.getName());

		// image
		mImageLoader.displayImage(mPoi.getImage(), imageView, mDisplayImageOptions, mImageLoadingListener);

		// scroll view
		observableStickyScrollView.setOnScrollViewListener(new ObservableStickyScrollView.ScrollViewListener()
		{
			private final int THRESHOLD = PoiDetailFragment.this.getResources().getDimensionPixelSize(R.dimen.toolbar_image_gap_height);
			private final int PADDING_LEFT = PoiDetailFragment.this.getResources().getDimensionPixelSize(R.dimen.toolbar_image_title_padding_right);
			private final int PADDING_BOTTOM = PoiDetailFragment.this.getResources().getDimensionPixelSize(R.dimen.global_spacing_xs);
			private final float SHADOW_RADIUS = 16;

			private int mPreviousY = 0;
			private ColorDrawable mTopColorDrawable = new ColorDrawable();
			private ColorDrawable mBottomColorDrawable = new ColorDrawable();


			@Override
			public void onScrollChanged(ObservableStickyScrollView scrollView, int x, int y, int oldx, int oldy)
			{
				// floating action button
				if(y > THRESHOLD)
				{
					if(floatingActionButton.getVisibility() == View.GONE && floatingActionButton.isEnabled())
					{
						showFloatingActionButton(true);
					}
				}
				else
				{
					if(floatingActionButton.getVisibility() == View.VISIBLE && floatingActionButton.isEnabled())
					{
						showFloatingActionButton(false);
					}
				}

				// do not calculate if header is hidden
				if(y > THRESHOLD && mPreviousY > THRESHOLD) return;

				// calculate panel alpha
				int alpha = (int) (y * (255f / (float) THRESHOLD));
				if(alpha > 255) alpha = 255;

				// set color drawables
				mTopColorDrawable.setColor(ResourcesHelper.getValueOfAttribute(getActivity(), R.attr.colorPrimary));
				mTopColorDrawable.setAlpha(alpha);
				mBottomColorDrawable.setColor(ResourcesHelper.getValueOfAttribute(getActivity(), R.attr.colorPrimary));
				mBottomColorDrawable.setAlpha(alpha);

				// set panel background
				if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
				{
					panelTopView.setBackground(mTopColorDrawable);
					panelBottomView.setBackground(mBottomColorDrawable);
				}
				else
				{
					panelTopView.setBackgroundDrawable(mTopColorDrawable);
					panelBottomView.setBackgroundDrawable(mBottomColorDrawable);
				}

				// calculate image translation
				float translation = y / 2;

				// set image translation
				imageView.setTranslationY(translation);

				// calculate title padding
				int paddingLeft = (int) (y * (float) PADDING_LEFT / (float) THRESHOLD);
				if(paddingLeft > PADDING_LEFT) paddingLeft = PADDING_LEFT;

				int paddingRight = PADDING_LEFT - paddingLeft;

				int paddingBottom = (int) ((THRESHOLD - y) * (float) PADDING_BOTTOM / (float) THRESHOLD);
				if(paddingBottom < 0) paddingBottom = 0;

				// set title padding
				titleTextView.setPadding(paddingLeft, 0, paddingRight, paddingBottom);

				// calculate title shadow
				float radius = ((THRESHOLD - y) * SHADOW_RADIUS / (float) THRESHOLD);

				// set title shadow
				titleTextView.setShadowLayer(radius, 0f, 0f, getResources().getColor(android.R.color.black));

				// previous y
				mPreviousY = y;
			}
		});


		// invoke scroll event because of orientation change toolbar refresh
		observableStickyScrollView.post(new Runnable()
		{
			@Override
			public void run()
			{
				observableStickyScrollView.scrollTo(0, observableStickyScrollView.getScrollY() - 1);
			}
		});

		// floating action button
		ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) floatingActionButton.getLayoutParams();
		params.topMargin = getResources().getDimensionPixelSize(R.dimen.toolbar_image_collapsed_height) - getResources().getDimensionPixelSize(R.dimen.fab_mini_size) / 2;
		floatingActionButton.setLayoutParams(params);
		floatingActionButton.setImageDrawable(mPoi.isFavorite() ? getResources().getDrawable(R.drawable.ic_menu_favorite_checked) : getResources().getDrawable(R.drawable.ic_menu_favorite_unchecked));
		floatingActionButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				try
				{
					mPoi.setFavorite(!mPoi.isFavorite());
					PoiDAO.update(mPoi);
					floatingActionButton.setImageDrawable(mPoi.isFavorite() ? getResources().getDrawable(R.drawable.ic_menu_favorite_checked) : getResources().getDrawable(R.drawable.ic_menu_favorite_unchecked));
				}
				catch(SQLException e)
				{
					e.printStackTrace();
				}
			}
		});
	}


	private void renderViewInfo()
	{
		// reference
		TextView introTextView = (TextView) mRootView.findViewById(R.id.fragment_poi_detail_info_intro);
		TextView addressTextView = (TextView) mRootView.findViewById(R.id.fragment_poi_detail_info_address);
		TextView distanceTextView = (TextView) mRootView.findViewById(R.id.fragment_poi_detail_info_distance);
		TextView linkTextView = (TextView) mRootView.findViewById(R.id.fragment_poi_detail_info_link);
		TextView phoneTextView = (TextView) mRootView.findViewById(R.id.fragment_poi_detail_info_phone);
		TextView emailTextView = (TextView) mRootView.findViewById(R.id.fragment_poi_detail_info_email);

		// intro
		if(mPoi.getIntro()!=null && !mPoi.getIntro().trim().equals(""))
		{
			introTextView.setText(mPoi.getIntro());
			introTextView.setVisibility(View.VISIBLE);
		}
		else
		{
			introTextView.setVisibility(View.GONE);
		}

		// address
		if(mPoi.getAddress()!=null && !mPoi.getAddress().trim().equals(""))
		{
			addressTextView.setText(mPoi.getAddress());
			addressTextView.setVisibility(View.VISIBLE);
			addressTextView.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					startMapActivity(mPoi);
				}
			});
		}
		else
		{
			addressTextView.setVisibility(View.GONE);
		}

		// distance
		if(mLocation!=null)
		{
			LatLng myLocation = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
			LatLng poiLocation = new LatLng(mPoi.getLatitude(), mPoi.getLongitude());
			String distance = LocationUtility.getDistanceString(LocationUtility.getDistance(myLocation, poiLocation), LocationUtility.isMetricSystem());
			distanceTextView.setText(distance);
			distanceTextView.setVisibility(View.VISIBLE);
			distanceTextView.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					startNavigateActivity(mPoi.getLatitude(), mPoi.getLongitude());
				}
			});
		}
		else
		{
			distanceTextView.setVisibility(View.GONE);
		}

		// link
		if(mPoi.getLink()!=null && !mPoi.getLink().trim().equals(""))
		{
			linkTextView.setText(mPoi.getLink());
			linkTextView.setVisibility(View.VISIBLE);
			linkTextView.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					startWebActivity(mPoi.getLink());
				}
			});
		}
		else
		{
			linkTextView.setVisibility(View.GONE);
		}

		// phone
		if(mPoi.getPhone()!=null && !mPoi.getPhone().trim().equals(""))
		{
			phoneTextView.setText(mPoi.getPhone());
			phoneTextView.setVisibility(View.VISIBLE);
			phoneTextView.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					startCallActivity(mPoi.getPhone());
				}
			});
		}
		else
		{
			phoneTextView.setVisibility(View.GONE);
		}

		// email
		if(mPoi.getEmail()!=null && !mPoi.getEmail().trim().equals(""))
		{
			emailTextView.setText(mPoi.getEmail());
			emailTextView.setVisibility(View.VISIBLE);
			emailTextView.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					startEmailActivity(mPoi.getEmail());
				}
			});
		}
		else
		{
			emailTextView.setVisibility(View.GONE);
		}
	}


	private void renderViewBanner()
	{
		// reference
		final AdView adView = (AdView) mRootView.findViewById(R.id.fragment_poi_detail_banner_adview);
		final ViewGroup bannerViewGroup = (ViewGroup) mRootView.findViewById(R.id.fragment_poi_detail_banner);

		// admob
		if(CityGuideConfig.ADMOB_POI_DETAIL_BANNER && NetworkManager.isOnline(getActivity()))
		{
			AdRequest adRequest = new AdRequest.Builder()
					.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
					.addTestDevice(getString(R.string.admob_test_device_id))
					.build();
			adView.loadAd(adRequest);
			adView.setVisibility(View.VISIBLE);
			bannerViewGroup.setVisibility(View.VISIBLE);
		}
		else
		{
			adView.setVisibility(View.GONE);
			bannerViewGroup.setVisibility(View.GONE);
		}
	}


	private void renderViewMap()
	{
		// reference
		final ImageView imageView = (ImageView) mRootView.findViewById(R.id.fragment_poi_detail_map_image);
		final ViewGroup wrapViewGroup = (ViewGroup) mRootView.findViewById(R.id.fragment_poi_detail_map_image_wrap);
		final Button exploreButton = (Button) mRootView.findViewById(R.id.fragment_poi_detail_map_explore);
		final Button navigateButton = (Button) mRootView.findViewById(R.id.fragment_poi_detail_map_navigate);

		// image
		String key = getString(R.string.maps_api_key);
		String url = getStaticMapUrl(key, mPoi.getLatitude(), mPoi.getLongitude(), MAP_ZOOM);
		mImageLoader.displayImage(url, imageView, mDisplayImageOptions, mImageLoadingListener);

		// wrap
		wrapViewGroup.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				startMapActivity(mPoi);
			}
		});

		// explore
		exploreButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				startMapActivity(mPoi);
			}
		});

		// navigate
		navigateButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				startNavigateActivity(mPoi.getLatitude(), mPoi.getLongitude());
			}
		});
	}


	private void renderViewDescription()
	{
		// reference
		TextView descriptionTextView = (TextView) mRootView.findViewById(R.id.fragment_poi_detail_description_text);

		// content
		if(mPoi.getDescription()!=null && !mPoi.getDescription().trim().equals(""))
		{
			descriptionTextView.setText(mPoi.getDescription());
		}
	}


	private void renderViewGap()
	{
		// reference
		final View gapView = mRootView.findViewById(R.id.fragment_poi_detail_gap);
		final CardView mapCardView = (CardView) mRootView.findViewById(R.id.fragment_poi_detail_map);

		// add gap in scroll view so favorite floating action button can be shown on tablet
		if(gapView!=null)
		{
			mapCardView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener()
			{
				@Override
				public void onGlobalLayout()
				{
					// cardview height
					int cardHeight = mapCardView.getHeight();

					// toolbar height
					int toolbarHeight = getResources().getDimensionPixelSize(R.dimen.toolbar_image_collapsed_height);

					// screen height
					Display display = getActivity().getWindowManager().getDefaultDisplay();
					Point size = new Point();
					display.getSize(size);
					int screenHeight = size.y;

					// calculate gap height
					int gapHeight = screenHeight - cardHeight - toolbarHeight;
					if(gapHeight > 0)
					{
						ViewGroup.LayoutParams params = gapView.getLayoutParams();
						params.height = gapHeight;
						gapView.setLayoutParams(params);
					}

					// remove layout listener
					if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
					{
						mapCardView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
					}
					else
					{
						mapCardView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
					}
				}
			});
		}
	}


	private void setupTimer()
	{
		mTimerHandler = new Handler();
		mTimerRunnable = new Runnable()
		{
			@Override
			public void run()
			{
				Logcat.d("Fragment.timerRunnable()");

				// start geolocation
				mGeolocation = null;
				mGeolocation = new Geolocation((LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE), PoiDetailFragment.this);

				mTimerHandler.postDelayed(this, TIMER_DELAY);
			}
		};
	}


	private void startTimer()
	{
		mTimerHandler.postDelayed(mTimerRunnable, 0);
	}


	private void stopTimer()
	{
		mTimerHandler.removeCallbacks(mTimerRunnable);
	}


	private String getPoiText()
	{
		StringBuilder builder = new StringBuilder();
		builder.append(mPoi.getName());
		builder.append("\n\n");
		if(mPoi.getAddress()!=null && !mPoi.getAddress().trim().equals(""))
		{
			builder.append(mPoi.getAddress());
			builder.append("\n\n");
		}
		if(mPoi.getIntro()!=null && !mPoi.getIntro().trim().equals(""))
		{
			builder.append(mPoi.getIntro());
			builder.append("\n\n");
		}
		if(mPoi.getDescription()!=null && !mPoi.getDescription().trim().equals(""))
		{
			builder.append(mPoi.getDescription());
			builder.append("\n\n");
		}
		if(mPoi.getLink()!=null && !mPoi.getLink().trim().equals(""))
		{
			builder.append(mPoi.getLink());
		}
		return builder.toString();
	}


	private String getStaticMapUrl(String key, double lat, double lon, int zoom)
	{
		TypedValue typedValue = new TypedValue();
		getActivity().getTheme().resolveAttribute(R.attr.colorAccent, typedValue, true);
		int markerColor = typedValue.data;
		String markerColorHex = String.format("0x%06x", (0xffffff & markerColor));

		StringBuilder builder = new StringBuilder();
		builder.append("https://maps.googleapis.com/maps/api/staticmap");
		builder.append("?key=");
		builder.append(key);
		builder.append("&size=320x320");
		builder.append("&scale=2");
		builder.append("&maptype=roadmap");
		builder.append("&zoom=");
		builder.append(zoom);
		builder.append("&center=");
		builder.append(lat);
		builder.append(",");
		builder.append(lon);
		builder.append("&markers=color:");
		builder.append(markerColorHex);
		builder.append("%7C");
		builder.append(lat);
		builder.append(",");
		builder.append(lon);
		return builder.toString();
	}


	private void showAboutDialog()
	{
		// create and show the dialog
		DialogFragment newFragment = AboutDialogFragment.newInstance();
		newFragment.setTargetFragment(this, 0);
		newFragment.show(getFragmentManager(), DIALOG_ABOUT);
	}


	private void startMapActivity(PoiModel poi)
	{
		Intent intent = MapActivity.newIntent(getActivity(), poi.getId(), poi.getLatitude(), poi.getLongitude());
		startActivity(intent);
	}


	private void startShareActivity(String subject, String text)
	{
		try
		{
			Intent intent = new Intent(android.content.Intent.ACTION_SEND);
			intent.setType("text/plain");
			intent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
			intent.putExtra(android.content.Intent.EXTRA_TEXT, text);
			startActivity(intent);
		}
		catch(android.content.ActivityNotFoundException e)
		{
			// can't start activity
		}
	}


	private void startWebActivity(String url)
	{
		try
		{
			Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url));
			startActivity(intent);
		}
		catch(android.content.ActivityNotFoundException e)
		{
			// can't start activity
		}
	}


	private void startCallActivity(String phoneNumber)
	{
		try
		{
			StringBuilder builder = new StringBuilder();
			builder.append("tel:");
			builder.append(phoneNumber);

			Intent intent = new Intent(android.content.Intent.ACTION_DIAL, Uri.parse(builder.toString()));
			startActivity(intent);
		}
		catch(android.content.ActivityNotFoundException e)
		{
			// can't start activity
		}
	}


	private void startEmailActivity(String email)
	{
		try
		{
			StringBuilder builder = new StringBuilder();
			builder.append("mailto:");
			builder.append(email);

			Intent intent = new Intent(android.content.Intent.ACTION_SENDTO, Uri.parse(builder.toString()));
			startActivity(intent);
		}
		catch(android.content.ActivityNotFoundException e)
		{
			// can't start activity
		}
	}


	private void startNavigateActivity(double lat, double lon)
	{
		try
		{
			String uri = String.format("http://maps.google.com/maps?daddr=%s,%s", Double.toString(lat), Double.toString(lon));
			Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uri));
			startActivity(intent);
		}
		catch(android.content.ActivityNotFoundException e)
		{
			// can't start activity
		}
	}
}
