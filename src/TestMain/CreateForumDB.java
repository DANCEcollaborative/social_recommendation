package TestMain;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import au.com.bytecode.opencsv.CSVParser;
import au.com.bytecode.opencsv.CSVReader;
import Data.Post;

public class CreateForumDB {
	private static Connection connect = null;
	private static Statement statement = null;
	private static ResultSet resultSet = null;
	//static String address = new String("127.0.0.1:3306");
	static String address = new String("128.2.220.118");
	//128.2.220.118
	/*
	 *    static String databaseName = new String("dalmooc");
        static String username = new String("root");
        static String password = new String("");
	 * */
  static String databaseName = new String("dalmooc");
  static String username = new String("diyiy");
  static String password = new String("password");
  

	static String tablename = "collaborations";
	static String inputFile = new String("/usr0/home/diyiy/Research/ParseData/11.23.UTArlingtonX-LINK5.10x-3T2014-prod.mongo.csv");
	
	public static void main(String args[]) throws IOException {
		System.err.println("forum = " + tablename);
		System.err.println("address = " + address);
		System.err.println("database = " + databaseName);
		System.err.println("username = " + username);
		System.err.println("password = " + password);

	  	try {
	  		//this will load the MySQL driver, each DB has its own driver
	  		Class.forName("com.mysql.jdbc.Driver");
	  		// setup the connection with the DB.
	  		connect = DriverManager.getConnection("jdbc:mysql://" + address + "/" + databaseName + "?" + "user=" + username + "&password=" + password);

	  		// statements allow to issue SQL queries to the database
	  		statement = connect.createStatement();
	  		// resultSet gets the result of the SQL query
	  		PreparedStatement statement = connect.prepareStatement("INSERT INTO "  + databaseName + "." + tablename + " " +
	  					"(user_id,collaboration_content,collaboration_id,collaboration_parent_id,collaboration_timestamp,collaboration_type_id)" +
	  					" VALUES (?,?,?,?,?,?)");
      
	  		HashMap<String, Integer> oid2Id = new HashMap<String, Integer> ();
	  		//CSVParser paser = new CSVParser(',', '"');
			  //Scanner sc = new Scanner(new File(inputFile));
			  //sc.nextLine();
			  
			  CSVReader reader = new CSVReader(new FileReader(inputFile));
			  String [] nextLine;
			  nextLine = reader.readNext();
			  
			  while((nextLine = reader.readNext())!= null){
			  //while (sc.hasNext()) {
				  //String line = sc.nextLine();
				  //String [] values = paser.parseLine(line);
				  if(nextLine.length != 8) {
				    System.err.println("Wrong CSV Format! ");
            System.exit(-1);
				  }/*
	            if (values.length != 8) {
	                System.err.println("Wrong CSV Format! " + line);
	                System.exit(-1);
	            }*/
	            // author_id	post_id	post_type	time	content	upvote	parent_id	comment_id
	            String author = nextLine[0];
	            String postOID = nextLine[1];
	            if (!oid2Id.containsKey(postOID)) {
	            	oid2Id.put(postOID, oid2Id.size());
	            }
	            int postId = oid2Id.get(postOID);
	            int type = nextLine[2].equals("Comment") ? 3 : 1; // posts, comments
	            long time = Long.parseLong(nextLine[3]);
	            String content = nextLine[4];
	            String parentOID = nextLine[7];
	            int parentId = -1;
	            if (parentOID.length() > 0) {
	            	if (!oid2Id.containsKey(parentOID)) {
		            	oid2Id.put(parentOID, oid2Id.size());
		            }
	            	parentId = oid2Id.get(parentOID);
	            }
	        statement.clearParameters();
  				statement.setString(1, author);
  				statement.setString(2, content);
  				statement.setInt(3, postId);
  				statement.setInt(4, parentId);
  				statement.setTimestamp(5, new Timestamp(time));
  				//System.err.println(new Timestamp(time));
  				statement.setInt(6, type);
  				statement.addBatch();
	  		}
			System.out.println("creating forum sql...");
  			int[] signal = statement.executeBatch();
	  	} catch (Exception e) {
	  		e.printStackTrace();
	  	}
		System.out.println("done.");
	}
}
