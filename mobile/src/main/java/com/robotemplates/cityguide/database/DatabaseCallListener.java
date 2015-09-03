package com.robotemplates.cityguide.database;

import com.robotemplates.cityguide.database.data.Data;


public interface DatabaseCallListener
{
	public void onDatabaseCallRespond(DatabaseCallTask task, Data<?> data);
	public void onDatabaseCallFail(DatabaseCallTask task, Exception exception);
}
