package org.summit.keepcraft.data;

import java.util.Map;
import java.util.logging.Logger;

public abstract class DataManager<T> 
{
	// For logging of course
	protected final static Logger logger = Logger.getLogger("Minecraft");
	
	// Database we'll be utilizing
	protected final Database database;
	
	public DataManager(Database database)
	{
		this.database = database;
	}
	
	public abstract boolean exists(Object key);
	
	public abstract T getData(Object key);
	
	public abstract Map<Object, T> getAllData();

	public abstract void updateData(T value);
	
	public abstract void putData(T value);
	
	public abstract void deleteData(T value);
}
