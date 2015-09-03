package com.robotemplates.cityguide.fragment;

import android.animation.Animator;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.SearchRecentSuggestions;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.melnykov.fab.FloatingActionButton;
import com.robotemplates.cityguide.CityGuideApplication;
import com.robotemplates.cityguide.CityGuideConfig;
import com.robotemplates.cityguide.R;
import com.robotemplates.cityguide.activity.MapActivity;
import com.robotemplates.cityguide.activity.PoiDetailActivity;
import com.robotemplates.cityguide.adapter.PoiListAdapter;
import com.robotemplates.cityguide.adapter.SearchSuggestionAdapter;
import com.robotemplates.cityguide.content.PoiSearchRecentSuggestionsProvider;
import com.robotemplates.cityguide.database.DatabaseCallListener;
import com.robotemplates.cityguide.database.DatabaseCallManager;
import com.robotemplates.cityguide.database.DatabaseCallTask;
import com.robotemplates.cityguide.database.data.Data;
import com.robotemplates.cityguide.database.model.PoiModel;
import com.robotemplates.cityguide.database.query.PoiReadAllQuery;
import com.robotemplates.cityguide.database.query.PoiReadByCategoryQuery;
import com.robotemplates.cityguide.database.query.PoiReadFavoritesQuery;
import com.robotemplates.cityguide.database.query.PoiSearchQuery;
import com.robotemplates.cityguide.database.query.Query;
import com.robotemplates.cityguide.dialog.AboutDialogFragment;
import com.robotemplates.cityguide.geolocation.Geolocation;
import com.robotemplates.cityguide.geolocation.GeolocationListener;
import com.robotemplates.cityguide.listener.OnSearchListener;
import com.robotemplates.cityguide.utility.Logcat;
import com.robotemplates.cityguide.utility.NetworkManager;
import com.robotemplates.cityguide.view.GridSpacingItemDecoration;
import com.robotemplates.cityguide.view.ViewState;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;


public class PoiListFragment extends TaskFragment implements DatabaseCallListener, GeolocationListener, PoiListAdapter.PoiViewHolder.OnItemClickListener
{
	public static final long CATEGORY_ID_ALL = -1l;
	public static final long CATEGORY_ID_FAVORITES = -2l;
	public static final long CATEGORY_ID_SEARCH = -3l;

	private static final String ARGUMENT_CATEGORY_ID = "category_id";
	private static final String ARGUMENT_SEARCH_QUERY = "search_query";
	private static final String DIALOG_ABOUT = "about";
	private static final long TIMER_DELAY = 60000l; // in milliseconds
	private static final int LAZY_LOADING_TAKE = 128;
	private static final int LAZY_LOADING_OFFSET = 4;

	private boolean mLazyLoading = false;
	private ViewState mViewState = null;
	private View mRootView;
	private PoiListAdapter mAdapter;
	private OnSearchListener mSearchListener;
	private ActionMode mActionMode;
	private DatabaseCallManager mDatabaseCallManager = new DatabaseCallManager();
	private Geolocation mGeolocation = null;
	private Location mLocation = null;
	private Handler mTimerHandler;
	private Runnable mTimerRunnable;

	private long mCategoryId;
	private String mSearchQuery;
	private List<PoiModel> mPoiList = new ArrayList<>();
	private List<Object> mFooterList = new ArrayList<>();


	public static PoiListFragment newInstance(long categoryId)
	{
		PoiListFragment fragment = new PoiListFragment();

		// arguments
		Bundle arguments = new Bundle();
		arguments.putLong(ARGUMENT_CATEGORY_ID, categoryId);
		fragment.setArguments(arguments);

		return fragment;
	}


	public static PoiListFragment newInstance(String searchQuery)
	{
		PoiListFragment fragment = new PoiListFragment();

		// arguments
		Bundle arguments = new Bundle();
		arguments.putLong(ARGUMENT_CATEGORY_ID, CATEGORY_ID_SEARCH);
		arguments.putString(ARGUMENT_SEARCH_QUERY, searchQuery);
		fragment.setArguments(arguments);

		return fragment;
	}
	
	
	@Override
	public void onAttach(Activity activity) 
	{
		super.onAttach(activity);

		// set search listener
		try
		{
			mSearchListener = (OnSearchListener) activity;
		}
		catch(ClassCastException e)
		{
			throw new ClassCastException(activity.getClass().getName() + " must implement " + OnSearchListener.class.getName());
		}
	}
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		setHasOptionsMenu(true);
		setRetainInstance(true);

		// handle fragment arguments
		Bundle arguments = getArguments();
		if(arguments != null)
		{
			handleArguments(arguments);
		}
	}
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{	
		mRootView = inflater.inflate(R.layout.fragment_poi_list, container, false);
		setupRecyclerView();
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
			if(mPoiList !=null) renderView();
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

		// lazy loading progress
		if(mLazyLoading) showLazyLoadingProgress(true);

		// show toolbar if hidden
		showToolbar(true);

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
		
		// stop adapter
		if(mAdapter!=null) mAdapter.stop();

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
		inflater.inflate(R.menu.menu_poi_list, menu);
	}
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
		// action bar menu behaviour
		switch(item.getItemId())
		{
			case R.id.menu_map:
				startMapActivity();
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
	public void onItemClick(View view, int position, long id, int viewType)
	{
		// position
		int poiPosition = mAdapter.getPoiPosition(position);

		// start activity
		PoiModel poi = mPoiList.get(poiPosition);
		startPoiDetailActivity(view, poi.getId());
	}


	@Override
	public void onDatabaseCallRespond(final DatabaseCallTask task, final Data<?> data)
	{
		runTaskCallback(new Runnable()
		{
			public void run()
			{
				if(mRootView == null) return; // view was destroyed

				if(task.getQuery().getClass().equals(PoiReadAllQuery.class))
				{
					Logcat.d("Fragment.onDatabaseCallRespond(PoiReadAllQuery)");

					// get data
					Data<List<PoiModel>> poiReadAllData = (Data<List<PoiModel>>) data;
					List<PoiModel> poiList = poiReadAllData.getDataObject();
					Iterator<PoiModel> iterator = poiList.iterator();
					while(iterator.hasNext())
					{
						PoiModel poi = iterator.next();
						mPoiList.add(poi);
					}
				}
				else if(task.getQuery().getClass().equals(PoiReadFavoritesQuery.class))
				{
					Logcat.d("Fragment.onDatabaseCallRespond(PoiReadFavoritesQuery)");

					// get data
					Data<List<PoiModel>> poiReadFavoritesData = (Data<List<PoiModel>>) data;
					List<PoiModel> poiList = poiReadFavoritesData.getDataObject();
					Iterator<PoiModel> iterator = poiList.iterator();
					while(iterator.hasNext())
					{
						PoiModel poi = iterator.next();
						mPoiList.add(poi);
					}
				}
				else if(task.getQuery().getClass().equals(PoiSearchQuery.class))
				{
					Logcat.d("Fragment.onDatabaseCallRespond(PoiSearchQuery)");

					// get data
					Data<List<PoiModel>> poiSearchData = (Data<List<PoiModel>>) data;
					List<PoiModel> poiList = poiSearchData.getDataObject();
					Iterator<PoiModel> iterator = poiList.iterator();
					while(iterator.hasNext())
					{
						PoiModel poi = iterator.next();
						mPoiList.add(poi);
					}
				}
				else if(task.getQuery().getClass().equals(PoiReadByCategoryQuery.class))
				{
					Logcat.d("Fragment.onDatabaseCallRespond(PoiReadByCategoryQuery)");

					// get data
					Data<List<PoiModel>> poiReadByCategoryData = (Data<List<PoiModel>>) data;
					List<PoiModel> poiList = poiReadByCategoryData.getDataObject();
					Iterator<PoiModel> iterator = poiList.iterator();
					while(iterator.hasNext())
					{
						PoiModel poi = iterator.next();
						mPoiList.add(poi);
					}
				}

				// render view
				if(mLazyLoading && mViewState == ViewState.CONTENT && mAdapter != null)
				{
					mAdapter.notifyDataSetChanged();
				}
				else
				{
					if(mPoiList != null) renderView();
				}

				// hide progress
				showLazyLoadingProgress(false);
				if(mPoiList != null && mPoiList.size() > 0) showContent();
				else showEmpty();

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

				if(task.getQuery().getClass().equals(PoiReadAllQuery.class))
				{
					Logcat.d("Fragment.onDatabaseCallFail(PoiReadAllQuery): " + exception.getClass().getSimpleName() + " / " + exception.getMessage());
				}
				else if(task.getQuery().getClass().equals(PoiReadFavoritesQuery.class))
				{
					Logcat.d("Fragment.onDatabaseCallFail(PoiReadFavoritesQuery): " + exception.getClass().getSimpleName() + " / " + exception.getMessage());
				}
				else if(task.getQuery().getClass().equals(PoiSearchQuery.class))
				{
					Logcat.d("Fragment.onDatabaseCallFail(PoiSearchQuery): " + exception.getClass().getSimpleName() + " / " + exception.getMessage());
				}
				else if(task.getQuery().getClass().equals(PoiReadByCategoryQuery.class))
				{
					Logcat.d("Fragment.onDatabaseCallFail(PoiReadByCategoryQuery): " + exception.getClass().getSimpleName() + " / " + exception.getMessage());
				}

				// hide progress
				showLazyLoadingProgress(false);
				if(mPoiList != null && mPoiList.size() > 0) showContent();
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
				if(mAdapter != null) mAdapter.setLocation(mLocation);
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


	private void handleArguments(Bundle arguments)
	{
		mCategoryId = arguments.getLong(ARGUMENT_CATEGORY_ID, CATEGORY_ID_ALL);
		mSearchQuery = arguments.getString(ARGUMENT_SEARCH_QUERY, "");
	}

	
	private void loadData()
	{
		if(!mDatabaseCallManager.hasRunningTask(PoiReadAllQuery.class) &&
				!mDatabaseCallManager.hasRunningTask(PoiReadFavoritesQuery.class) &&
				!mDatabaseCallManager.hasRunningTask(PoiSearchQuery.class) &&
				!mDatabaseCallManager.hasRunningTask(PoiReadByCategoryQuery.class))
		{
			// show progress
			showProgress();

			// run async task
			Query query;
			if(mCategoryId==CATEGORY_ID_ALL)
			{
				query = new PoiReadAllQuery(0, LAZY_LOADING_TAKE);
			}
			else if(mCategoryId==CATEGORY_ID_FAVORITES)
			{
				query = new PoiReadFavoritesQuery(0, LAZY_LOADING_TAKE);
			}
			else if(mCategoryId==CATEGORY_ID_SEARCH)
			{
				query = new PoiSearchQuery(mSearchQuery, 0, LAZY_LOADING_TAKE);
			}
			else
			{
				query = new PoiReadByCategoryQuery(mCategoryId, 0, LAZY_LOADING_TAKE);
			}
			mDatabaseCallManager.executeTask(query, this);
		}
	}
	
	
	private void lazyLoadData()
	{
		// show lazy loading progress
		showLazyLoadingProgress(true);

		// run async task
		Query query;
		if(mCategoryId==CATEGORY_ID_ALL)
		{
			query = new PoiReadAllQuery(mPoiList.size(), LAZY_LOADING_TAKE);
		}
		else if(mCategoryId==CATEGORY_ID_FAVORITES)
		{
			query = new PoiReadFavoritesQuery(mPoiList.size(), LAZY_LOADING_TAKE);
		}
		else if(mCategoryId==CATEGORY_ID_SEARCH)
		{
			query = new PoiSearchQuery(mSearchQuery, mPoiList.size(), LAZY_LOADING_TAKE);
		}
		else
		{
			query = new PoiReadByCategoryQuery(mCategoryId, mPoiList.size(), LAZY_LOADING_TAKE);
		}
		mDatabaseCallManager.executeTask(query, this);
	}
	
	
	private void showLazyLoadingProgress(boolean visible)
	{
		if(visible)
		{
			mLazyLoading = true;

			// show footer
			if(mFooterList.size()<=0)
			{
				mFooterList.add(new Object());
				mAdapter.notifyItemInserted(mAdapter.getRecyclerPositionByFooter(0));
			}
		}
		else
		{
			// hide footer
			if(mFooterList.size()>0)
			{
				mFooterList.remove(0);
				mAdapter.notifyItemRemoved(mAdapter.getRecyclerPositionByFooter(0));
			}

			mLazyLoading = false;
		}
	}


	private void showToolbar(boolean visible)
	{
		final Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
		if(visible)
		{
			toolbar.animate()
					.translationY(0)
					.setDuration(200)
					.setInterpolator(new AccelerateDecelerateInterpolator())
					.setListener(new Animator.AnimatorListener()
					{
						@Override
						public void onAnimationStart(Animator animator)
						{
							toolbar.setVisibility(View.VISIBLE);
							toolbar.setEnabled(false);
						}

						@Override
						public void onAnimationEnd(Animator animator)
						{
							toolbar.setEnabled(true);
						}

						@Override
						public void onAnimationCancel(Animator animator) {}

						@Override
						public void onAnimationRepeat(Animator animator) {}
					});
		}
		else
		{
			toolbar.animate()
					.translationY(-toolbar.getBottom())
					.setDuration(200)
					.setInterpolator(new AccelerateDecelerateInterpolator())
					.setListener(new Animator.AnimatorListener()
					{
						@Override
						public void onAnimationStart(Animator animator)
						{
							toolbar.setEnabled(false);
						}


						@Override
						public void onAnimationEnd(Animator animator)
						{
							toolbar.setVisibility(View.GONE);
							toolbar.setEnabled(true);
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


	private void showFloatingActionButton(boolean visible)
	{
		final FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
		if(visible)
		{
			fab.show();
		}
		else
		{
			fab.hide();
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

		// floating action button
		showFloatingActionButton(true);
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
	}
	
	
	private void renderView()
	{
		// reference
		final RecyclerView recyclerView = getRecyclerView();
		final FloatingActionButton floatingActionButton = (FloatingActionButton) getActivity().findViewById(R.id.fab);
		final AdView adView = (AdView) mRootView.findViewById(R.id.fragment_poi_list_adview);

		// content
		if(recyclerView.getAdapter()==null)
		{
			// create adapter
			mAdapter = new PoiListAdapter(mPoiList, mFooterList, this, getGridSpanCount(), mLocation);
		}
		else
		{
			// refill adapter
			mAdapter.refill(mPoiList, mFooterList, this, getGridSpanCount(), mLocation);
		}

		// set fixed size
		recyclerView.setHasFixedSize(false);

		// add decoration
		RecyclerView.ItemDecoration itemDecoration = new GridSpacingItemDecoration(getResources().getDimensionPixelSize(R.dimen.fragment_poi_list_recycler_item_padding));
		recyclerView.addItemDecoration(itemDecoration);

		// set animator
		recyclerView.setItemAnimator(new DefaultItemAnimator());

		// set adapter
		recyclerView.setAdapter(mAdapter);

		// lazy loading
		recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener()
		{
			private static final int THRESHOLD = 100;

			private int mCounter = 0;
			private Toolbar mToolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);


			@Override
			public void onScrollStateChanged(RecyclerView recyclerView, int newState)
			{
				super.onScrollStateChanged(recyclerView, newState);

				// reset counter
				if(newState == RecyclerView.SCROLL_STATE_DRAGGING)
				{
					mCounter = 0;
				}

				// disable item animation in adapter
				if(newState == RecyclerView.SCROLL_STATE_DRAGGING)
				{
					mAdapter.setAnimationEnabled(false);
				}
			}


			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy)
			{
				super.onScrolled(recyclerView, dx, dy);

				GridLayoutManager layoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
				int firstVisibleItem = layoutManager.findFirstVisibleItemPosition();
				int visibleItemCount = layoutManager.getChildCount();
				int totalItemCount = layoutManager.getItemCount();
				int lastVisibleItem = firstVisibleItem + visibleItemCount;

				// lazy loading
				if(totalItemCount - lastVisibleItem <= LAZY_LOADING_OFFSET && mPoiList.size() % LAZY_LOADING_TAKE == 0 && !mPoiList.isEmpty())
				{
					if(!mLazyLoading) lazyLoadData();
				}

				// toolbar and FAB animation
				mCounter += dy;
				if(recyclerView.getScrollState() == RecyclerView.SCROLL_STATE_DRAGGING || recyclerView.getScrollState() == RecyclerView.SCROLL_STATE_SETTLING)
				{
					// scroll down
					if(mCounter > THRESHOLD && firstVisibleItem > 0)
					{
						// hide toolbar
						if(mToolbar.getVisibility() == View.VISIBLE && mToolbar.isEnabled())
						{
							showToolbar(false);
						}

						// hide FAB
						showFloatingActionButton(false);

						mCounter = 0;
					}

					// scroll up
					else if(mCounter < -THRESHOLD || firstVisibleItem == 0)
					{
						// show toolbar
						if(mToolbar.getVisibility() == View.GONE && mToolbar.isEnabled())
						{
							showToolbar(true);
						}

						// show FAB
						showFloatingActionButton(true);

						mCounter = 0;
					}
				}
			}
		});

		// floating action button
		floatingActionButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				mActionMode = ((ActionBarActivity) getActivity()).getSupportActionBar().startActionMode(new SearchActionModeCallback());
			}
		});

		// admob
		if(CityGuideConfig.ADMOB_POI_LIST_BANNER && NetworkManager.isOnline(getActivity()))
		{
			AdRequest adRequest = new AdRequest.Builder()
					.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
					.addTestDevice(getString(R.string.admob_test_device_id))
					.build();
			adView.loadAd(adRequest);
			adView.setVisibility(View.VISIBLE);
		}
		else
		{
			adView.setVisibility(View.GONE);
		}
	}


	private RecyclerView getRecyclerView()
	{
		return mRootView!=null ? (RecyclerView) mRootView.findViewById(R.id.fragment_poi_list_recycler) : null;
	}


	private void setupRecyclerView()
	{
		GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), getGridSpanCount());
		gridLayoutManager.setOrientation(GridLayoutManager.VERTICAL);
		RecyclerView recyclerView = getRecyclerView();
		recyclerView.setLayoutManager(gridLayoutManager);
	}


	private int getGridSpanCount()
	{
		Display display = getActivity().getWindowManager().getDefaultDisplay();
		DisplayMetrics displayMetrics = new DisplayMetrics();
		display.getMetrics(displayMetrics);
		float screenWidth  = displayMetrics.widthPixels;
		float cellWidth = getResources().getDimension(R.dimen.fragment_poi_list_recycler_item_size);
		return Math.round(screenWidth / cellWidth);
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
				mGeolocation = new Geolocation((LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE), PoiListFragment.this);

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


	private void showAboutDialog()
	{
		// create and show the dialog
		DialogFragment newFragment = AboutDialogFragment.newInstance();
		newFragment.setTargetFragment(this, 0);
		newFragment.show(getFragmentManager(), DIALOG_ABOUT);
	}


	private void startPoiDetailActivity(View view, long poiId)
	{
		Intent intent = PoiDetailActivity.newIntent(getActivity(), poiId);
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
		{
			ActivityOptions options = ActivityOptions.makeScaleUpAnimation(view, 0, 0, view.getWidth(), view.getHeight());
			getActivity().startActivity(intent, options.toBundle());
		}
		else
		{
			startActivity(intent);
		}
	}


	private void startMapActivity()
	{
		Intent intent = MapActivity.newIntent(getActivity());
		startActivity(intent);
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


	private class SearchActionModeCallback implements ActionMode.Callback
	{
		private SearchView mSearchView;
		private SearchSuggestionAdapter mSearchSuggestionAdapter;


		@Override
		public boolean onCreateActionMode(ActionMode actionMode, Menu menu)
		{
			// search view
			mSearchView = new SearchView(((ActionBarActivity) getActivity()).getSupportActionBar().getThemedContext());
			setupSearchView(mSearchView);

			// search menu item
			MenuItem searchMenuItem = menu.add(Menu.NONE, Menu.NONE, 1, getString(R.string.menu_search));
			searchMenuItem.setIcon(R.drawable.ic_menu_search);
			MenuItemCompat.setActionView(searchMenuItem, mSearchView);
			MenuItemCompat.setShowAsAction(searchMenuItem, MenuItem.SHOW_AS_ACTION_ALWAYS);

			return true;
		}


		@Override
		public boolean onPrepareActionMode(ActionMode actionMode, Menu menu)
		{
			showFloatingActionButton(false);
			return true;
		}


		@Override
		public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem)
		{
			return false;
		}


		@Override
		public void onDestroyActionMode(ActionMode actionMode)
		{
			showFloatingActionButton(true);
		}


		private void setupSearchView(SearchView searchView)
		{
			// expand action view
			searchView.setIconifiedByDefault(true);
			searchView.setIconified(false);
			searchView.onActionViewExpanded();

			// search hint
			searchView.setQueryHint(getString(R.string.menu_search_hint));

			// text color
			AutoCompleteTextView searchText = (AutoCompleteTextView) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
			searchText.setTextColor(getResources().getColor(R.color.global_text_primary_inverse));
			searchText.setHintTextColor(getResources().getColor(R.color.global_text_secondary_inverse));

			// suggestion listeners
			searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener()
			{
				@Override
				public boolean onQueryTextSubmit(String query)
				{
					// listener
					mSearchListener.onSearch(query);

					// save query for suggestion
					SearchRecentSuggestions suggestions = new SearchRecentSuggestions(getActivity(), PoiSearchRecentSuggestionsProvider.AUTHORITY, PoiSearchRecentSuggestionsProvider.MODE);
					suggestions.saveRecentQuery(query, null);

					// close action mode
					mActionMode.finish();

					return true;
				}

				@Override
				public boolean onQueryTextChange(String query)
				{
					if(query.length()>2)
					{
						updateSearchSuggestion(query);
					}
					return true;
				}
			});
			searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener()
			{
				@Override
				public boolean onSuggestionSelect(int position)
				{
					return false;
				}

				@Override
				public boolean onSuggestionClick(int position)
				{
					// get query
					Cursor cursor = (Cursor) mSearchSuggestionAdapter.getItem(position);
					String title = cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1));

					// listener
					mSearchListener.onSearch(title);

					// close action mode
					mActionMode.finish();

					return true;
				}
			});
		}


		private void updateSearchSuggestion(String query)
		{
			// cursor
			ContentResolver contentResolver = getActivity().getApplicationContext().getContentResolver();
			String contentUri = "content://" + PoiSearchRecentSuggestionsProvider.AUTHORITY + '/' + SearchManager.SUGGEST_URI_PATH_QUERY;
			Uri uri = Uri.parse(contentUri);
			Cursor cursor = contentResolver.query(uri, null, null, new String[] { query }, null);

			// searchview content
			if(mSearchSuggestionAdapter==null)
			{
				// create adapter
				mSearchSuggestionAdapter = new SearchSuggestionAdapter(getActivity(), cursor);

				// set adapter
				mSearchView.setSuggestionsAdapter(mSearchSuggestionAdapter);
			}
			else
			{
				// refill adapter
				mSearchSuggestionAdapter.refill(getActivity(), cursor);

				// set adapter
				mSearchView.setSuggestionsAdapter(mSearchSuggestionAdapter);
			}
		}
	}
}
