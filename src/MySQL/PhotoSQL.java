package MySQL;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import Data.Post;
import Data.ScoreRecord;

public class PhotoSQL extends SQL {
	private Connection connect = null;
	private Statement statement = null;
	private ResultSet resultSet = null;
  
	public String tablename = new String("photo");
	
	public PhotoSQL(String tablename) {
		this.tablename = tablename;
	}
	
	public void clear() throws Exception {
		try {
	  		//this will load the MySQL driver, each DB has its own driver
	  		Class.forName("com.mysql.jdbc.Driver");
	  		// setup the connection with the DB.
	  		connect = DriverManager.getConnection("jdbc:mysql://" + address + "/" + databaseName + "?" + "user=" + username + "&password=" + password);
	  		// statements allow to issue SQL queries to the database
	  		statement = connect.createStatement();
	  		// resultSet gets the result of the SQL query
	  		String command = "truncate table " + databaseName + "." + tablename;
	  		int signal = statement.executeUpdate(command);
	  	} catch (Exception e) {
	  		System.err.println("error while deleting records");
	  		throw e;
	  	} finally {
	  		close();
	  	}
	}
	
	public void getPhoto(HashMap<String, String> user2photo1, HashMap<String, String> user2photo2) throws Exception {
	  	try {
	  		//this will load the MySQL driver, each DB has its own driver
	  		Class.forName("com.mysql.jdbc.Driver");
	  		// setup the connection with the DB.
	  		connect = DriverManager.getConnection("jdbc:mysql://" + address + "/" + databaseName + "?" + "user=" + username + "&password=" + password);

	  		// statements allow to issue SQL queries to the database
	  		statement = connect.createStatement();
	  		// resultSet gets the result of the SQL query
	  		resultSet = statement.executeQuery("select * from " + tablename);
      
	  		while (resultSet.next()) {
	  			String user = resultSet.getString("user");
	  			String photo1 = resultSet.getString("photo1");
	  			String photo2 = resultSet.getString("photo2");
	  			user2photo1.put(user, photo1);
	  			user2photo2.put(user, photo2);
	  		}
	  	} catch (Exception e) {
	  		throw e;
	  	} finally {
	  		close();
	  	}
	}
	
	public void writePhoto(ArrayList<String> user, ArrayList<String> photo1, ArrayList<String> photo2) throws Exception {
	  	try {
	  		//this will load the MySQL driver, each DB has its own driver
	  		Class.forName("com.mysql.jdbc.Driver");
	  		// setup the connection with the DB.
	  		connect = DriverManager.getConnection("jdbc:mysql://" + address + "/" + databaseName + "?" + "user=" + username + "&password=" + password);
  			
  			PreparedStatement statement = connect.prepareStatement("INSERT INTO "  + databaseName + "." + tablename + " " + "(user,photo1,photo2)" + " VALUES (?,?,?)");
  			for (int i = 0; i < user.size(); ++ i) {
  			  String u = user.get(i);
  			  String p1 = photo1.get(i);
  			  String p2 = photo2.get(i);
  				statement.clearParameters();
  				statement.setString(1, u);
  				statement.setString(2, p1);
  				statement.setString(3, p2);
  				statement.addBatch();
  			}
  			System.out.println("start to update the photo sql...");
  			int[] signal = statement.executeBatch();
	  	} catch (Exception e) {
	  		throw e;
	  	} finally {
	  		close();
	  	}
	}

	// you need to close all three to make sure
	private void close() throws SQLException {
		if (resultSet != null) {
			resultSet.close();
		}
		if (statement != null) {
			statement.close();
		}
		if (connect != null) {
			connect.close();
		}
	}
} 
