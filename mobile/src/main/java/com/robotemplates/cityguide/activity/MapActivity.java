package com.robotemplates.cityguide.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.robotemplates.cityguide.CityGuideApplication;
import com.robotemplates.cityguide.R;


public class MapActivity extends ActionBarActivity
{
	public static final String EXTRA_POI_ID = "poi_id";
	public static final String EXTRA_POI_LATITUDE = "poi_latitude";
	public static final String EXTRA_POI_LONGITUDE = "poi_longitude";


	public static Intent newIntent(Context context)
	{
		return new Intent(context, MapActivity.class);
	}


	public static Intent newIntent(Context context, long poiId, double poiLatitude, double poiLongitude)
	{
		Intent intent = new Intent(context, MapActivity.class);

		// extras
		intent.putExtra(EXTRA_POI_ID, poiId);
		intent.putExtra(EXTRA_POI_LATITUDE, poiLatitude);
		intent.putExtra(EXTRA_POI_LONGITUDE, poiLongitude);

		return intent;
	}


	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		setupActionBar();

		// init analytics tracker
		((CityGuideApplication) getApplication()).getTracker();
	}
	
	
	@Override
	public void onStart()
	{
		super.onStart();

		// analytics
		GoogleAnalytics.getInstance(this).reportActivityStart(this);
	}
	
	
	@Override
	public void onResume()
	{
		super.onResume();
	}
	
	
	@Override
	public void onPause()
	{
		super.onPause();
	}
	
	
	@Override
	public void onStop()
	{
		super.onStop();

		// analytics
		GoogleAnalytics.getInstance(this).reportActivityStop(this);
	}
	
	
	@Override
	public void onDestroy()
	{
		super.onDestroy();
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// action bar menu behaviour
		switch(item.getItemId())
		{
			case android.R.id.home:
				finish();
				return true;

			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	
	private void setupActionBar()
	{
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		ActionBar bar = getSupportActionBar();
		bar.setDisplayUseLogoEnabled(false);
		bar.setDisplayShowTitleEnabled(true);
		bar.setDisplayShowHomeEnabled(true);
		bar.setDisplayHomeAsUpEnabled(true);
		bar.setHomeButtonEnabled(true);
	}
}
