package TestMain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import CoreRecSys.RecSys;
import Data.Post;
import MySQL.ForumSQL;
import MySQL.PhotoSQL;
import MySQL.SQL;
import MySQL.TASQL;
import MySQL.UserSQL;

public class CreateTA {
  static public void main(String args[]) {
    String address = new String("128.2.220.118");
    String databaseName = new String("dalmooc");
    String username = new String("diyiy");
    String password = new String("password");
    
    SQL.setAddress(address);
    SQL.setDatabaseName(databaseName);
    SQL.setUsername(username);
    SQL.setPassword(password);
    
    String[] TA = new String[] {
      "-1",
      "-3",
      "-4",
      "-5"
    };
    Random rnd = new Random(19910820);
    ArrayList<String> user = new ArrayList<String>();
    for (String u : TA) {
      user.add(u);
    }
    TASQL sql = new TASQL("TA");
    try {
      sql.clear();
      sql.writeTA(user);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
