package TestMain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import CoreRecSys.RecSys;
import Data.Post;
import MySQL.EmailSQL;
import MySQL.ForumSQL;
import MySQL.PhotoSQL;
import MySQL.SQL;
import MySQL.UserSQL;

public class UpdateEmail {
  static public void main(String args[]) {
	/* database information */
    String address = new String("128.2.220.118");
    String databaseName = new String("dalmooc");
    String username = new String("diyiy");
    String password = new String("password");
    
    SQL.setAddress(address);
    SQL.setDatabaseName(databaseName);
    SQL.setUsername(username);
    SQL.setPassword(password);
    
    HashMap<String, String> user2name = new HashMap<String, String>();
    HashMap<String, String> user2email = new HashMap<String, String>();
    UserSQL sql = new UserSQL("users");
    try {
      sql.readNameEmail(user2name, user2email);
    } catch (Exception e) {
      e.printStackTrace();
    }

    String photoTableName = "users";

   
    ArrayList<String> user = new ArrayList<String>();
    ArrayList<String> ph1 = new ArrayList<String>();
    ArrayList<String> ph2 = new ArrayList<String>();
    for (String u : user2name.keySet()) {
     // String p1 = photo1[rnd.nextInt(photo1.length)];
     // String p2 = photo2[rnd.nextInt(photo2.length)];
     user.add(u);
     // ph1.add(p1);
     //ph2.add(p2);
    }
    EmailSQL psql = new EmailSQL(photoTableName);
    try {
     // psql.clear();
      psql.updateEmail(user);
      
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
