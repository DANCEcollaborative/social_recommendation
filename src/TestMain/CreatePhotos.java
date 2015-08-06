package TestMain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import CoreRecSys.RecSys;
import Data.Post;
import MySQL.ForumSQL;
import MySQL.PhotoSQL;
import MySQL.SQL;
import MySQL.UserSQL;

public class CreatePhotos {
  static public void main(String args[]) {
    String address = new String("128.2.220.118");
    // need to be changed for our new course
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

    String photoTableName = "photo";

    String[] photo1 = new String[] {
      "http://www.cs.cmu.edu/~diyiy/images/photo0/profile_anon_1a.jpg",
      "http://www.cs.cmu.edu/~diyiy/images/photo0/profile_anon_2a.jpg",
      "http://www.cs.cmu.edu/~diyiy/images/photo0/profile_anon_3a.jpg",
      "http://www.cs.cmu.edu/~diyiy/images/photo0/profile_anon_4a.jpg"
    };
    
    String[] photo2 = new String[] {
      "http://www.cs.cmu.edu/~diyiy/images/photo1/profile_1a.jpg",
      "http://www.cs.cmu.edu/~diyiy/images/photo1/profile_2a.jpg",
      "http://www.cs.cmu.edu/~diyiy/images/photo1/profile_3a.jpg",
      "http://www.cs.cmu.edu/~diyiy/images/photo1/profile_4a.jpg"
    };
    Random rnd = new Random(19910820);
    ArrayList<String> user = new ArrayList<String>();
    ArrayList<String> ph1 = new ArrayList<String>();
    ArrayList<String> ph2 = new ArrayList<String>();
    for (String u : user2name.keySet()) {
      String p1 = photo1[rnd.nextInt(photo1.length)];
      String p2 = photo2[rnd.nextInt(photo2.length)];
      user.add(u);
      ph1.add(p1);
      ph2.add(p2);
    }
    PhotoSQL psql = new PhotoSQL(photoTableName);
    try {
      psql.clear();
      psql.writePhoto(user, ph1, ph2);
      
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
