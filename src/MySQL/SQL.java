package MySQL;

public class SQL {
  
	static public String address = new String("128.2.220.118");
	static public String databaseName = new String("dalmooc");
	static public String username = new String("diyiy");
	static public String password = new String("password");
	
	/*
	 static public String address = new String("127.0.0.1:3306");
	  static public String databaseName = new String("dalmooc");
	  static public String username = new String("root");
	  static public String password = new String("");
	*/
	public static void setAddress(String address) {
		SQL.address = address;
	}
	
	public static void setDatabaseName(String databaseName) {
		SQL.databaseName = databaseName;
	}
	
	public static void setUsername(String username) {
		SQL.username = username;
	}
	
	public static void setPassword(String password) {
		SQL.password = password;
	}
}
