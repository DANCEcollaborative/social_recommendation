package MySQL;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;

import Data.Post;
import Data.ScoreRecord;

public class ScoreSQL extends SQL {
	private Connection connect = null;
	private Statement statement = null;
	private ResultSet resultSet = null;
  
	public String tablename = new String("score");
	
	public ScoreSQL(String tablename) {
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
	
	public ScoreRecord [] getScore() throws Exception {
	  	ArrayList<ScoreRecord> results = new ArrayList<ScoreRecord>();
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
	  			int threadId = resultSet.getInt("thread");
	  			double score = resultSet.getDouble("score");
	  			results.add(new ScoreRecord(user, threadId, score));
	  		}
	  	} catch (Exception e) {
	  		throw e;
	  	} finally {
	  		close();
	  	}
	  	return results.toArray(new ScoreRecord[results.size()]);
	}
	
	public void writeScore(ScoreRecord [] records) throws Exception {
	  	try {
	  		//this will load the MySQL driver, each DB has its own driver
	  		Class.forName("com.mysql.jdbc.Driver");
	  		// setup the connection with the DB.
	  		connect = DriverManager.getConnection("jdbc:mysql://" + address + "/" + databaseName + "?" + "user=" + username + "&password=" + password);
  			
  			PreparedStatement statement = connect.prepareStatement("INSERT INTO "  + databaseName + "." + tablename + " " + "(user,thread,score)" + " VALUES (?,?,?)");
  			for (int i = 0; i < records.length; ++ i) {
  				String user = records[i].user;
  				int thread = records[i].thread;
  				double score = records[i].score;
  				statement.clearParameters();
  				statement.setString(1, user);
  				statement.setInt(2, thread);
  				statement.setDouble(3, score);
  				statement.addBatch();
  			}
  			System.out.println("start to update the score sql...");
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
