package org.summit.keepcraft.data;

import org.summit.keepcraft.data.models.UserFaction;
import org.summit.keepcraft.data.models.UserPrivilege;
import org.summit.keepcraft.data.models.User;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import org.summit.keepcraft.Keepcraft;

public class UserDataManager extends DataManager<User>
{
	public UserDataManager(Database database)
	{
		super(database);
		init();
	}
	
	private void init()
	{
		try 
		{
			PreparedStatement statement = 
					database.createStatement("CREATE TABLE IF NOT EXISTS users (Name, Privilege, Faction, Money, LastPlotId, FirstOnline, LastOnline)");
			statement.execute();
		}
		catch(Exception e)
		{
			Keepcraft.log("Error initializing table: " + e.getMessage());
		}
		finally 
		{
			database.close();
		}
	}

    @Override
	public void updateData(User data)
	{
        Keepcraft.log("Updating data for " + data.getName());
		try 
		{
			PreparedStatement statement = 
					database.createStatement("UPDATE users SET Privilege = ?, Faction = ?, Money = ?, LastPlotId = ?, LastOnline = datetime('now') WHERE ROWID = ?");
			statement.setInt(1, data.getPrivilege());
			statement.setInt(2, data.getFaction());
			statement.setInt(3, data.getMoney());
            statement.setInt(4, data.getLastPlotId());
			statement.setInt(5, data.getId());
			statement.execute();
			
			// TODO: if the record did not exist, we'll have to create it
		}
		catch(Exception e)
		{
			Keepcraft.log("Error setting user data: " + e.getMessage());
		}
		finally
		{
			database.close();
		}
	}

    @Override
	public User getData(Object key)
	{		
		String name;
		if(key instanceof String)
		{
			name = (String) key;
		}
		else
		{
			return null;
		}
		
		Keepcraft.log("Beginning lookup on " + name);
		int id = 0, privilege = 0, faction = 0, money = 0, lastPlotId = 0;
		try 
		{
			PreparedStatement statement = 
					database.createStatement("SELECT ROWID, Privilege, Faction, Money, LastPlotId FROM users WHERE Name = ? LIMIT 1");
			statement.setString(1, name);
			ResultSet result = statement.executeQuery();
			
			boolean found = result.next();
			
			if(!found)
			{
				Keepcraft.log("No user was found, creating record");
				result.close();
                
                // Determine faction to place on
                int redCount = this.getFactionCount(UserFaction.FactionRed);
                int blueCount = this.getFactionCount(UserFaction.FactionBlue);
                int greenCount = 9999;//this.getFactionCount(UserFaction.FactionGreen);
                
				User newUser = new User(0);
				newUser.setName(name);
				newUser.setPrivilege(UserPrivilege.MEMBER);
				newUser.setFaction(UserFaction.getSmallestFaction(redCount, blueCount, greenCount));
				newUser.setMoney(0);
				
				putData(newUser);
				return getData(name);
			}
			else
			{
				Keepcraft.log("Lookup on " + name + " successful");
				id = result.getInt("ROWID");
				privilege = result.getInt("Privilege");
				faction = result.getInt("Faction");		
				money = result.getInt("Money");
                lastPlotId = result.getInt("LastPlotId");
			}
			
			result.close();
		}
		catch(Exception e)
		{
			Keepcraft.log("Error during user data lookup: " + e.getMessage());
		}
		finally
		{
			database.close();
		}
		
		User user = new User(id);
		user.setName(name);
		user.setPrivilege(privilege);
		user.setFaction(faction);	
		user.setMoney(money);
        user.setLastPlotId(lastPlotId);
		
		Keepcraft.log("User data was retrieved with values: " + user);
        
		return user;
	}
	
    @Override
	public Map<Object, User> getAllData()
	{		
		Map<Object, User> allData = new HashMap<Object, User>();
		
		Keepcraft.log("Beginning lookup of all user data");
		
		try 
		{
			PreparedStatement statement = 
					database.createStatement("SELECT ROWID, Name, Privilege, Faction, Money FROM users");
			ResultSet result = statement.executeQuery();

			while(result.next())
			{
				int id = result.getInt("ROWID");
				String name = result.getString("Name");
				int privilege = result.getInt("Privilege");
				int faction = result.getInt("Faction");		
				int money = result.getInt("Money");
                int lastPlotId = result.getInt("LastPlotId");
				
				User user = new User(id);
				user.setName(name);
				user.setPrivilege(privilege);
				user.setFaction(faction);	
				user.setMoney(money);
                user.setLastPlotId(lastPlotId);
				
				allData.put(name, user);
			}
			
			result.close();
		}
		catch(Exception e)
		{
			Keepcraft.log("Error during all user data lookup: " + e.getMessage());
		}
		finally
		{
			database.close();
		}
		
		return allData;
	}

	@Override
	public void putData(User user) {
		
		Keepcraft.log("Creating record for " + user.getName());
		try 
		{
			PreparedStatement statement = 
					database.createStatement("INSERT INTO users (Name, Privilege, Faction, Money, LastPlotId, FirstOnline, LastOnline) VALUES(?, ?, ?, ?, ?, datetime('now'), datetime('now'))");
			statement.setString(1, user.getName());
			statement.setInt(2, user.getPrivilege());
			statement.setInt(3, user.getFaction());
			statement.setInt(4, user.getMoney());
			statement.setInt(5, user.getLastPlotId());
			statement.execute();
		}
		catch(Exception e)
		{
			Keepcraft.log("Error creating user data: " + e.getMessage());
		}
		finally
		{
			database.close();
		}
	}

	@Override
	public void deleteData(User user) 
	{
		Keepcraft.log("Deleting record for " + user.getName());
		try 
		{
			PreparedStatement statement = 
					database.createStatement("DELETE FROM users WHERE Name = ?");
			statement.setString(1, user.getName());
			statement.execute();
		}
		catch(Exception e)
		{
			Keepcraft.log("Error deleting user data: " + e.getMessage());
		}
		finally
		{
			database.close();
		}
	}

	@Override
	public boolean exists(Object key) 
	{
		String name;
		if(key instanceof String)
		{
			name = (String) key;
		}
		else
		{
			return false;
		}
		
		boolean found = false;
		Keepcraft.log("Checking for existence of " + name);
		try 
		{
			PreparedStatement statement = 
					database.createStatement("SELECT ROWID FROM users WHERE Name = ? LIMIT 1");
			statement.setString(1, name);
			ResultSet result = statement.executeQuery();
			
			found = result.next();
			
			result.close();
		}
		catch(Exception e)
		{
			Keepcraft.log("Error during user data lookup: " + e.getMessage());
		}
		finally
		{
			database.close();
		}

		return found;
	}
    
    public int getFactionCount(int faction)
    {
        int memberCount = 0;
        try 
		{
			PreparedStatement statement = database.createStatement(
                    "SELECT ROWID FROM users WHERE Faction = ? AND Privilege = ? AND " +
                    "((julianday(datetime('now')) - julianday(LastOnline)) < ?)"
            );
			statement.setInt(1, faction);
            statement.setInt(2, UserPrivilege.MEMBER);
            statement.setFloat(3, 3.0f);
			ResultSet result = statement.executeQuery();
            
            while(result.next()) memberCount++;
			
			result.close();
		}
		catch(Exception e)
		{
			Keepcraft.log("Error counting faction members: " + e.getMessage());
		}
		finally
		{
			database.close();
		}
        
        Keepcraft.log("Active member count for " + UserFaction.asString(faction) + " is " + memberCount);

		return memberCount;
    }
    
    public void updateLastOnline(User user)
    {
        updateData(user);        
    }

}
