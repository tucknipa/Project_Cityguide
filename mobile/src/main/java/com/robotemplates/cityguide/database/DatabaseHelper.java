package com.robotemplates.cityguide.database;

import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;

import com.robotemplates.cityguide.CityGuideApplication;
import com.robotemplates.cityguide.CityGuideConfig;
import com.robotemplates.cityguide.database.model.CategoryModel;
import com.robotemplates.cityguide.database.model.PoiModel;
import com.robotemplates.cityguide.utility.Logcat;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class DatabaseHelper extends OrmLiteSqliteOpenHelper
{
	private static final String DATABASE_NAME = CityGuideConfig.DATABASE_NAME;
	private static final String DATABASE_PATH = "/data/data/" + CityGuideApplication.getContext().getPackageName() + "/databases/";
	private static final int DATABASE_VERSION = CityGuideConfig.DATABASE_VERSION;
	private static final String PREFS_KEY_DATABASE_VERSION = "database_version";

	private Dao<CategoryModel, Long> mCategoryDao = null;
	private Dao<PoiModel, Long> mPoiDao = null;


	// singleton
	private static DatabaseHelper instance;
	public static synchronized DatabaseHelper getInstance()
	{
		if(instance==null) instance = new DatabaseHelper();
		return instance;
	}


	private DatabaseHelper()
	{
		super(CityGuideApplication.getContext(), DATABASE_PATH + DATABASE_NAME, null, DATABASE_VERSION);
		if(!databaseExists() || DATABASE_VERSION>getVersion())
		{
			synchronized(this)
			{
				boolean success = copyPrepopulatedDatabase();
				if(success)
				{
					setVersion(DATABASE_VERSION);
				}
			}
		}
	}


	@Override
	public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource)
	{
		try
		{
			Logcat.d("DatabaseHelper.onCreate()");
			//TableUtils.createTable(connectionSource, CategoryModel.class);
			//TableUtils.createTable(connectionSource, PoiModel.class);
		}
		catch(android.database.SQLException e)
		{
			Logcat.e("DatabaseHelper.onCreate(): can't create database", e);
			e.printStackTrace();
		}
	}


	@Override
	public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, int newVersion)
	{
		try
		{
			Logcat.d("DatabaseHelper.onUpgrade()");
		}
		catch(android.database.SQLException e)
		{
			Logcat.e("DatabaseHelper.onUpgrade(): can't upgrade database", e);
			e.printStackTrace();
		}
	}


	@Override
	public void close()
	{
		super.close();
		mCategoryDao = null;
		mPoiDao = null;
	}


	public synchronized void clearDatabase()
	{
		try
		{
			Logcat.d("DatabaseHelper.clearDatabase()");

			TableUtils.dropTable(getConnectionSource(), CategoryModel.class, true);
			TableUtils.dropTable(getConnectionSource(), PoiModel.class, true);

			TableUtils.createTable(getConnectionSource(), CategoryModel.class);
			TableUtils.createTable(getConnectionSource(), PoiModel.class);
		}
		catch(android.database.SQLException e)
		{
			Logcat.e("DatabaseHelper.clearDatabase(): can't clear database", e);
			e.printStackTrace();
		}
		catch(java.sql.SQLException e)
		{
			Logcat.e("DatabaseHelper.clearDatabase(): can't clear database", e);
			e.printStackTrace();
		}
	}


	public synchronized Dao<CategoryModel, Long> getCategoryDao() throws java.sql.SQLException
	{
		if(mCategoryDao==null)
		{
			mCategoryDao = getDao(CategoryModel.class);
		}
		return mCategoryDao;
	}


	public synchronized Dao<PoiModel, Long> getPoiDao() throws java.sql.SQLException
	{
		if(mPoiDao ==null)
		{
			mPoiDao = getDao(PoiModel.class);
		}
		return mPoiDao;
	}


	private boolean databaseExists()
	{
		File file = new File(DATABASE_PATH + DATABASE_NAME);
		boolean exists = file.exists();
		Logcat.d("DatabaseHelper.databaseExists(): " + exists);
		return exists;
	}


	private boolean copyPrepopulatedDatabase()
	{
		// copy database from assets
		try
		{
			// create directories
			File dir = new File(DATABASE_PATH);
			dir.mkdirs();

			// output file name
			String outputFileName = DATABASE_PATH + DATABASE_NAME;
			Logcat.d("DatabaseHelper.copyDatabase(): " + outputFileName);

			// create streams
			InputStream inputStream = CityGuideApplication.getContext().getAssets().open(DATABASE_NAME);
			OutputStream outputStream = new FileOutputStream(outputFileName);

			// write input to output
			byte[] buffer = new byte[1024];
			int length;
			while((length = inputStream.read(buffer))>0)
			{
				outputStream.write(buffer, 0, length);
			}

			// close streams
			outputStream.flush();
			outputStream.close();
			inputStream.close();
			return true;
		}
		catch(IOException e)
		{
			e.printStackTrace();
			return false;
		}
	}


	private int getVersion()
	{
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(CityGuideApplication.getContext());
		return sharedPreferences.getInt(PREFS_KEY_DATABASE_VERSION, 0);
	}


	private void setVersion(int version)
	{
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(CityGuideApplication.getContext());
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putInt(PREFS_KEY_DATABASE_VERSION, version);
		editor.commit();
	}
}
