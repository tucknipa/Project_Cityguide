package com.robotemplates.cityguide.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.robotemplates.cityguide.CityGuideConfig;
import com.robotemplates.cityguide.R;
import com.robotemplates.cityguide.activity.MapActivity;
import com.robotemplates.cityguide.activity.PoiDetailActivity;
import com.robotemplates.cityguide.database.DatabaseCallListener;
import com.robotemplates.cityguide.database.DatabaseCallManager;
import com.robotemplates.cityguide.database.DatabaseCallTask;
import com.robotemplates.cityguide.database.data.Data;
import com.robotemplates.cityguide.database.model.PoiModel;
import com.robotemplates.cityguide.database.query.PoiReadAllQuery;
import com.robotemplates.cityguide.database.query.Query;
import com.robotemplates.cityguide.utility.Logcat;
import com.robotemplates.cityguide.utility.NetworkManager;
import com.robotemplates.cityguide.utility.Preferences;
import com.robotemplates.cityguide.utility.Version;
import com.robotemplates.cityguide.view.ViewState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;


public class MapFragment extends TaskFragment implements DatabaseCallListener
{
	private static final int MAP_ZOOM = 14;

	private ViewState mViewState = null;
	private View mRootView;
	private MapView mMapView;
	private DatabaseCallManager mDatabaseCallManager = new DatabaseCallManager();

	private List<PoiModel> mPoiList = new ArrayList<>();
	private HashMap<Marker, PoiModel> mMarkerMap = new HashMap<>();
	private long mPoiId = -1l;
	private double mPoiLatitude = 0.0;
	private double mPoiLongitude = 0.0;
	
	
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
	}
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		mRootView = inflater.inflate(R.layout.fragment_map, container, false);

		// initialize map
		if(!Version.isSupportedOpenGlEs2(getActivity()))
		{
			Toast.makeText(getActivity(), R.string.global_map_fail_toast, Toast.LENGTH_LONG).show();
		}
		try
		{
			MapsInitializer.initialize(getActivity());
		}
		catch(Exception e)
		{
		}
		mMapView = (MapView) mRootView.findViewById(R.id.fragment_map_mapview);
		mMapView.onCreate(savedInstanceState);
		setupMap();

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
			if(mPoiList!=null) renderView();
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

		// map
		if(mMapView!=null) mMapView.onResume();
	}
	
	
	@Override
	public void onPause()
	{
		super.onPause();

		// map
		if(mMapView!=null) mMapView.onPause();
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

		// map
		if(mMapView!=null) mMapView.onDestroy();

		// cancel async tasks
		mDatabaseCallManager.cancelAllTasks();
	}
	
	
	@Override
	public void onDetach()
	{
		super.onDetach();
	}


	@Override
	public void onLowMemory()
	{
		super.onLowMemory();

		// map
		if(mMapView!=null) mMapView.onLowMemory();
	}
	
	
	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		// save current instance state
		super.onSaveInstanceState(outState);
		setUserVisibleHint(true);

		// map
		if(mMapView!=null) mMapView.onSaveInstanceState(outState);
	}
	
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		// action bar menu
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.menu_map, menu);
	}
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
		// action bar menu behaviour
		switch(item.getItemId())
		{
			case R.id.menu_layers_normal:
				setMapType(GoogleMap.MAP_TYPE_NORMAL);
				return true;

			case R.id.menu_layers_satellite:
				setMapType(GoogleMap.MAP_TYPE_SATELLITE);
				return true;

			case R.id.menu_layers_hybrid:
				setMapType(GoogleMap.MAP_TYPE_HYBRID);
				return true;

			case R.id.menu_layers_terrain:
				setMapType(GoogleMap.MAP_TYPE_TERRAIN);
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

				if(task.getQuery().getClass().equals(PoiReadAllQuery.class))
				{
					Logcat.d("Fragment.onDatabaseCallRespond(PoiReadAllQuery)");

					// get data
					Data<List<PoiModel>> poiReadAllData = (Data<List<PoiModel>>) data;
					List<PoiModel> poiList = poiReadAllData.getDataObject();
					mPoiList.clear();
					Iterator<PoiModel> iterator = poiList.iterator();
					while(iterator.hasNext())
					{
						PoiModel poi = iterator.next();
						mPoiList.add(poi);
					}
				}

				// hide progress and render view
				if(mPoiList!=null && mPoiList.size()>0)
				{
					renderView();
					showContent();
				}
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

				// hide progress
				if(mPoiList!=null && mPoiList.size()>0) showContent();
				else showEmpty();

				// handle fail
				handleFail();

				// finish query
				mDatabaseCallManager.finishTask(task);
			}
		});
	}


	private void handleFail()
	{
		Toast.makeText(getActivity(), R.string.global_database_fail_toast, Toast.LENGTH_LONG).show();
	}


	private void handleExtras(Bundle extras)
	{
		if(extras.containsKey(MapActivity.EXTRA_POI_ID))
		{
			mPoiId = extras.getLong(MapActivity.EXTRA_POI_ID);
		}
		if(extras.containsKey(MapActivity.EXTRA_POI_LATITUDE))
		{
			mPoiLatitude = extras.getDouble(MapActivity.EXTRA_POI_LATITUDE);
		}
		if(extras.containsKey(MapActivity.EXTRA_POI_LONGITUDE))
		{
			mPoiLongitude = extras.getDouble(MapActivity.EXTRA_POI_LONGITUDE);
		}
	}
	
	
	private void loadData()
	{
		if(!mDatabaseCallManager.hasRunningTask(PoiReadAllQuery.class))
		{
			// show progress
			showProgress();

			// run async task
			Query query = new PoiReadAllQuery();
			mDatabaseCallManager.executeTask(query, this);
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
	}


	private void renderView()
	{
		// reference
		final GoogleMap map = ((MapView) mRootView.findViewById(R.id.fragment_map_mapview)).getMap();
		final AdView adView = (AdView) mRootView.findViewById(R.id.fragment_map_adview);

		// map
		if(map!=null)
		{
			// get accent color
			TypedValue typedValue = new TypedValue();
			getActivity().getTheme().resolveAttribute(R.attr.colorAccent, typedValue, true);
			int markerColor = typedValue.data;

			// get hue
			float[] hsv = new float[3];
			Color.colorToHSV(markerColor, hsv);

			// create marker
			BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.defaultMarker(hsv[0]);

			// add pois
			map.clear();
			mMarkerMap.clear();
			for(PoiModel poi : mPoiList)
			{
				Marker marker = map.addMarker(new MarkerOptions()
						.position(new LatLng(poi.getLatitude(), poi.getLongitude()))
						.title(poi.getName())
						.icon(bitmapDescriptor)
				);
				mMarkerMap.put(marker, poi);
			}

			// click listener
			map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener()
			{
				@Override
				public void onInfoWindowClick(Marker marker)
				{
					PoiModel poi = mMarkerMap.get(marker);
					startPoiDetailActivity(poi.getId());
				}
			});
		}

		// admob
		if(CityGuideConfig.ADMOB_MAP_BANNER && NetworkManager.isOnline(getActivity()))
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


	private void setupMap()
	{
		// reference
		GoogleMap map = ((MapView) mRootView.findViewById(R.id.fragment_map_mapview)).getMap();

		// settings
		if(map!=null)
		{
			Preferences preferences = new Preferences(getActivity());

			map.setMapType(preferences.getMapType());
			map.setMyLocationEnabled(true);

			UiSettings settings = map.getUiSettings();
			settings.setAllGesturesEnabled(true);
			settings.setMyLocationButtonEnabled(true);
			settings.setZoomControlsEnabled(true);

			LatLng latLng = null;
			if(mPoiLatitude==0.0 && mPoiLongitude==0.0)
			{
				LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
				Location location = getLastKnownLocation(locationManager);
				if(location!=null) latLng = new LatLng(location.getLatitude(), location.getLongitude());
			}
			else
			{
				latLng = new LatLng(mPoiLatitude, mPoiLongitude);
			}

			if(latLng != null)
			{
				CameraPosition cameraPosition = new CameraPosition.Builder()
						.target(latLng)
						.zoom(MAP_ZOOM)
						.bearing(0)
						.tilt(0)
						.build();
				map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
			}
		}
	}


	private Location getLastKnownLocation(LocationManager locationManager)
	{
		Location locationNet = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		Location locationGps = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

		long timeNet = 0l;
		long timeGps = 0l;

		if(locationNet!=null)
		{
			timeNet = locationNet.getTime();
		}

		if(locationGps!=null)
		{
			timeGps = locationGps.getTime();
		}

		if(timeNet>timeGps) return locationNet;
		else return locationGps;
	}


	private void setMapType(int type)
	{
		GoogleMap map = ((MapView) mRootView.findViewById(R.id.fragment_map_mapview)).getMap();
		if(map!=null)
		{
			map.setMapType(type);

			Preferences preferences = new Preferences(getActivity());
			preferences.setMapType(type);
		}
	}


	private void startPoiDetailActivity(long poiId)
	{
		Intent intent = PoiDetailActivity.newIntent(getActivity(), poiId);
		startActivity(intent);
	}
}
