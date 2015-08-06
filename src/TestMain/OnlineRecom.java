package TestMain;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Scanner;

import CoreRecSys.RecSys;
import Data.ForumThread;
import Data.MyPair;
import Data.Post;
import Data.ScoreRecord;
import Data.User;
import MySQL.ForumSQL;
import MySQL.PhotoSQL;
import MySQL.SQL;
import MySQL.ScoreSQL;
import MySQL.TASQL;
import MySQL.UserSQL;
import au.com.bytecode.opencsv.CSVParser;
import au.com.bytecode.opencsv.CSVReader;

public class OnlineRecom {
	
	private RecSys rs;
	
	private int topK = 3;
	private int newThreadId = -1;
	private String newContent = 
	        "I have problems in getting the transcripts?? Could you please help me find it?";
	private String mode = "ST";//"TA", "ST", "DCM";
	private String newUserName = new String("newUserName");
	
	// Local Test Setting
	
	private String address = new String("127.0.0.1:3306");
	private String databaseName = new String("dalmooc");
	private String username = new String("root");
	private String password = new String("");
	private String forumTableName = "collaborations";
	private String scoreTableName = "score";
	private String TATableName = "TA";
	
	// Online Setting
	/*
  private String address = new String("128.2.220.118");
  private String databaseName = new String("dalmooc");
  private String username = new String("diyiy");
  private String password = new String("password");
  private String forumTableName = "collaborations";
  private String scoreTableName = "score";
  private String TATableName = "TA";
	 */
	private static ArrayList<String> TAs;
	
	public static void loadTA(String TATableName) {
	  TASQL sql = new TASQL(TATableName);
		try {
      TAs = sql.getTA();
    } catch (Exception e) {
      e.printStackTrace();
    }
	}
	
	private String nameEmailTableName = "users";
	private String photoTableName = "photo";
	
	private int curWeek = 6;
	
	HashMap<String, Integer> userWeek;
	
	void buildUserActivity() {
		userWeek = new HashMap<String, Integer>();
		int base = Integer.MAX_VALUE;
    for (User user : rs.forum.users.values()) {
      int start = Integer.MAX_VALUE;
      for (Post post : user.posts) {
        int t = Integer.parseInt(post.timestamp);
        start = Math.min(start, t);
      }
      if (start > 1357850949) { // some toooo early one?
        base = Math.min(base, start);
      }
    }
    for (User user : rs.forum.users.values()) {
      int start = Integer.MAX_VALUE;
      for (Post post : user.posts) {
        int t = Integer.parseInt(post.timestamp);
        start = Math.min(start, t);
      }
      int week = (start - base) / (7 * 24 * 60 * 60);
      userWeek.put(user.name, week + 1); // 1 based
    }
	}
	
	HashMap<String, String> user2photo1, user2photo2;
	
	void loadPhoto(String photoTableName) {
		PhotoSQL sql = new PhotoSQL(photoTableName);
		user2photo1 = new HashMap<String, String>();
		user2photo2 = new HashMap<String, String>();
		try {
      sql.getPhoto(user2photo1, user2photo2);
    } catch (Exception e) {
      e.printStackTrace();
    }
		System.err.println(user2photo1.size() + " photos loaded for users");
	}
	
	private HashMap<String, String> user2name;
	private HashMap<String, String> user2email;
	
	void loadNameEmail(String tableName) {
		user2name = new HashMap<String, String>();
		user2email = new HashMap<String, String>();
		UserSQL sql = new UserSQL(nameEmailTableName);
		try {
      sql.readNameEmail(user2name, user2email);
    } catch (Exception e) {
      e.printStackTrace();
    }
	}
	
	String toString(String uid, double value) {
		// name
		String name = "UNKNOWN";
		if (user2name.containsKey(uid)) {
			name = user2name.get(uid);
		}
		// email
		String email = "UNKNOWN";
		if (user2email.containsKey(uid)) {
			email = user2email.get(uid);
		}
		// badge information
		int badge = 0;
		
		if(rs.forum.users.containsKey(uid)){
		  int totalPosts = rs.forum.users.get(uid).posts.size();
		  int initialized = 0;
		  for (ForumThread t: rs.forum.threads.values()){
	      if(t.owner.equals(uid)){
	        initialized ++ ;
	      }
	    }
	    
	    badge = totalPosts - initialized;
		}
		
		String photo1 = user2photo2.get(uid);
		String photo0 = user2photo1.get(uid);
		
		// description
		String prefix = "This student has been participating in the course for ";
	  String middle = "and the matching of his/her knowledge and the topic of your question is ";
	    
	  String [] irrelevant = new String[] {
			"This student loves our Course!",
			"This student started the course study earlier.",
			"This student might use Firefox browser!",
			"This student speaks English!"};

    String [] irrelevantTA = new String[] {
      "This colleague has a computer and is ready to go.",
      "This colleague is involved in the course.",
      "This colleague answers email on a regular basis.",
      "This colleague uses Web 2.0 technologies."};
    
    //DecimalFormat v = new DecimalFormat("#.####");
    //value.format(v);
    value *= 100;
    int weekcnt = 0;
    if (userWeek.containsKey(uid)) {
    	weekcnt = curWeek - userWeek.get(uid) + 1;
    } else {
    	userWeek.put(uid, curWeek);
    	weekcnt = 1;
    }
    Random rnd = new Random();
    String description0 = irrelevant[rnd.nextInt(irrelevant.length)];

    BigDecimal b = new BigDecimal(value);
    double value1 = b.setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();
    String description1 = prefix + weekcnt + (weekcnt == 1 ? " week " : " weeks ") + middle + value1 + "%.";
    description0 = "\"" + description0 + "\"";
    description1 = "\"" + description1 + "\"";

    if (TAs.contains(uid)) {
      description1 = "\""+
    " This is one of the Teaching Assistants selected for this course. All of our Teaching Assistants are highly qualified to answer student queries."
              + "\"";
      badge = 100;
      description0 = irrelevantTA[rnd.nextInt(irrelevantTA.length)];
    }
    // userid, username, useremail, userpic0, userpic1, userdesp0, userdesp1;
		return uid + "," + name + "," + email + "," + photo0 + "," + photo1 + "," + description0 + "," + description1 + "," + badge;
	}
	
	void initialize(String args[]) {
		for (int i = 0; i + 1 < args.length; ++ i) {
			if (args[i].equals("-top")) {
				topK = Integer.parseInt(args[i + 1]);
			} else if (args[i].equals("-content")) {
				newContent = args[i + 1];
			} else if (args[i].equals("-threadid")) {
				newThreadId = Integer.parseInt(args[i + 1]);
			} else if (args[i].equals("-mode")) {
				mode = args[i + 1];
			} else if (args[i].equals("-user")) {
				newUserName = args[i + 1];
			} else if (args[i].equals("-forum")) {
				forumTableName = args[i + 1];
			} else if (args[i].equals("-address")) {
				address = args[i + 1];
			} else if (args[i].equals("-database")) {
				databaseName = args[i + 1];
			} else if (args[i].equals("-score")) {
				scoreTableName = args[i + 1];
			} else if (args[i].equals("-username")) {
				username = args[i + 1];
			} else if (args[i].equals("-password")) {
				password = args[i + 1];
			} else if (args[i].equals("-TA")) {
			  TATableName = args[i + 1];
			} else if (args[i].equals("-week")) {
				curWeek = Integer.parseInt(args[i + 1]);
			} else if (args[i].equals("-name/email")) {
				nameEmailTableName = args[i + 1];
			} else if (args[i].equals("-photo")) {
				photoTableName = args[i + 1];
			}
		}
		System.err.println("score = " + scoreTableName);
		System.err.println("forum = " + forumTableName);
		System.err.println("address = " + address);
		System.err.println("database = " + databaseName);
		System.err.println("username = " + username);
		System.err.println("password = " + password);
		
		System.err.println("TA = " + TATableName);
		
		System.err.println("user = " + newUserName);
		System.err.println("threadId = " + newThreadId);
		System.err.println("content = " + newContent);
		System.err.println("top k = " + topK);
		System.err.println("mode = " + mode);
		
		SQL.setAddress(address);
    SQL.setDatabaseName(databaseName);
    SQL.setUsername(username);
    SQL.setPassword(password);
		
		loadTA(TATableName);
		
		loadNameEmail(nameEmailTableName);
		loadPhoto(photoTableName);
		
		ForumSQL sql = new ForumSQL(forumTableName);
		ArrayList<Post> posts = null;
		try {
			posts = sql.readPosts();
		} catch (Exception e) {
			System.err.println("error while reading posts");
			e.printStackTrace();
		}
		System.err.println("# posts loaded from database = " + posts.size());
		
		rs = new RecSys();
		for (int i = 0; i < posts.size(); ++ i) {
			rs.addPost(posts.get(i));
		}
		
		buildUserActivity();
	}
	
	void process(Post newPost) {
		rs.addPost(newPost);
		ForumThread newThread = rs.forum.threads.get(newThreadId);
		
		if (mode.equals("DCM")) {
			ArrayList<User> result = rs.answersGenerate(newThread, topK);
			
			for (int i = 0; i < result.size(); ++ i) {
			  String output = toString(result.get(i).name, 1);
				if (i > 0) {
					System.out.print(";");
				}
				//System.out.print(result.get(i).name);
				System.out.print(output);
			}
			System.out.println();
		} else if (mode.equals("ST")) {
			ScoreSQL scoreSql = new ScoreSQL(scoreTableName);
			ScoreRecord[] records = null;
			try {
				records = scoreSql.getScore();
			} catch (Exception e) {
				System.err.println("error while loading score SQL");
				e.printStackTrace();
			}
			
			System.err.println("# score records loaded = " + records.length);
			
			double best = -1;
			int bestThread = -1;
			HashSet<Integer> considered = new HashSet<Integer>();
			HashMap<String, Double> newThreadFeatures = rs.threadFeatures.get(newThreadId);
			for (int i = 0; i < records.length; ++ i) {
				if (considered.contains(records[i].thread)) {
					continue;
				}
				considered.add(records[i].thread);
				double sim = rs.calculateDCM(rs.threadFeatures.get(records[i].thread), newThreadFeatures);
				if (sim > best) {
					best = sim;
					bestThread = records[i].thread;
				}
			}
			
			System.err.println("Best Similar Thread = " + bestThread + ", similarity = " + best);
			
			
			ArrayList<MyPair<String>> order = new ArrayList<MyPair<String>>();
			for (int i = 0; i < records.length; ++ i) {
				if (records[i].thread == bestThread) {
					if (records[i].user.equals(newUserName)) {
						continue;
					}
					order.add(new MyPair<String>(records[i].user, records[i].score));
				}
			}
			Collections.sort(order);
			
			for (int i = 0; i < order.size() && i < topK; ++ i) {
				String output = toString(order.get(i).value, order.get(i).key);
				if (i > 0) {
					System.out.print(";");
				}
				System.out.print(output);
			}
			System.out.println();
		} else if (mode.equals("TA")) {
			Collections.shuffle(TAs);
			for (int i = 0; i < TAs.size() && i < topK; ++ i) {
				String output = toString(TAs.get(i), 1);
				if (i > 0) {
					System.out.print(";");
				}
				System.out.print(output);
			}
			System.out.println();
		}
	}
	
	void run(String args[]) {
		initialize(args);
		
		Post newPost = new Post(newUserName, newThreadId, -1, -1, newContent, -1, "");
		process(newPost);
	}
	
	public static void main(String args[]) throws IOException {
		new OnlineRecom().run(args);
	}
}
