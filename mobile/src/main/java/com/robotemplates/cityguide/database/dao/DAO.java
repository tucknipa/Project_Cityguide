package com.robotemplates.cityguide.database.dao;

import com.robotemplates.cityguide.database.DatabaseHelper;
import com.robotemplates.cityguide.utility.Logcat;

import java.sql.SQLException;


public class DAO
{
	public static void printDatabaseInfo()
	{
		DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
		try
		{
			Logcat.d("DAO.printDatabaseInfo(): categories " + databaseHelper.getCategoryDao().countOf());
			Logcat.d("DAO.printDatabaseInfo(): pois " + databaseHelper.getPoiDao().countOf());
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
	}
}
