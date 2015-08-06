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

public class UserSQL extends SQL {
  private Connection connect = null;
  private Statement statement = null;
  private ResultSet resultSet = null;
  
  public String tablename = new String("forum");
  
  public UserSQL(String tablename) {
    this.tablename = tablename;
  }
  
  public void readNameEmail(HashMap<String, String> user2name, HashMap<String, String> user2email) throws Exception {
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
          String user = resultSet.getString("id");
          String email = resultSet.getString("email");
          String name = resultSet.getString("username");
          
          user2name.put(user, name);
          user2email.put(user, email);
        }
        System.err.println(user2name.size() + " users loaded");
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
