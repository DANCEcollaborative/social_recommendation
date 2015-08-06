package MySQL;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import Data.Post;

public class ForumSQL extends SQL {
	private Connection connect = null;
	private Statement statement = null;
	private ResultSet resultSet = null;
	private ResultSet resultSet_c = null;
  
	public String tablename = new String("forum");
	
	public ForumSQL(String tablename) {
		this.tablename = tablename;
	}
	
	public ArrayList<Post> readPosts() throws Exception {
	  	ArrayList<Post> results = new ArrayList<Post>();
	  	try {
	  		//this will load the MySQL driver, each DB has its own driver
	  		Class.forName("com.mysql.jdbc.Driver");
	  		// setup the connection with the DB.
	  		connect = DriverManager.getConnection("jdbc:mysql://" + address + "/" + databaseName + "?" + "user=" + username + "&password=" + password);
	  		

	  		// statements allow to issue SQL queries to the database
	  		statement = connect.createStatement();
	  		// resultSet gets the result of the SQL query
	  		/* POST*/
	  		resultSet = statement.executeQuery("select * from " + tablename);
	  		//System.err.println(tablename);
      
	  		int threadId = 0;
	  		HashMap<Integer, Integer> postId2ThreadId = new HashMap<Integer, Integer> ();
	  		while (resultSet.next()) {
	  		  
	  		  int type = resultSet.getInt("collaboration_type_id");
  	  		// author, content, postId, threadId, parentId, timestamp
	  		  if (type == 1) {
	  		    // posts, comments
	  		    String author = resultSet.getString("user_id");
	          String content = resultSet.getString("collaboration_content");
	          int postId = resultSet.getInt("collaboration_id");
	          int parentPostId = resultSet.getInt("collaboration_parent_id");
	          Long timestamp = resultSet.getTimestamp("collaboration_timestamp").getTime();
	          timestamp /= 1000;
	          int vote = 0;
	          if (type == 1) {
	            postId2ThreadId.put(postId, threadId ++);
	          } else {
	            postId2ThreadId.put(postId, postId2ThreadId.get(parentPostId));
	          }
            Post p = new Post(author, postId2ThreadId.get(postId), postId, parentPostId, content, vote, timestamp.toString());
            results.add(p);
          }
	  		}
	  		//System.err.println(results.size() + " posts, " + threadId + " threads");
	 
	  	/*COMMENT*/
	  	resultSet_c = statement.executeQuery("select * from " + tablename);
      while (resultSet_c.next()) {
        
        int type = resultSet_c.getInt("collaboration_type_id");
        // author, content, postId, threadId, parentId, timestamp
        if (type == 3) {
          String author = resultSet_c.getString("user_id");
          String content = resultSet_c.getString("collaboration_content");
          int postId = resultSet_c.getInt("collaboration_id");
          int parentPostId = resultSet_c.getInt("collaboration_parent_id");
          Long timestamp = resultSet_c.getTimestamp("collaboration_timestamp").getTime();
          timestamp /= 1000;
          int vote = 0;
          if (type == 1) {
            postId2ThreadId.put(postId, threadId ++);
          } else {
            postId2ThreadId.put(postId, postId2ThreadId.get(parentPostId));
          }
          Post p = new Post(author, postId2ThreadId.get(postId), postId, parentPostId, content, vote, timestamp.toString());
          results.add(p);
        }
      }
      System.err.println(results.size() + " posts, " + threadId + " threads");
    } catch (Exception e) {
      throw e;
    } finally {
      close();
    }
	  	return results;
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
