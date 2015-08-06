package TestMain;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import CoreRecSys.RecSys;
import CoreRecSys.Simulation;
import Data.ForumThread;
import Data.Post;
import Data.ScoreRecord;
import Data.User;
import MySQL.ForumSQL;
import MySQL.ScoreSQL;
import MySQL.SQL;

public class OfflineTraining
{
	private static final long START_TIME = 7 * 24 * 60 * 60;
	private static final int TOP_LIMIT = 300;

	public static void main(String args[]) {
	  // Test Locally, Connecting with moocdb
		String address = new String("128.2.220.118");
		// need to be changed for our new course
		String databaseName = new String("dalmooc");
		String username = new String("diyiy");
		String password = new String("password");
		String forumTableName = "collaborations";
		String scoreTableName = "score";
		
		for (int i = 0; i + 1 < args.length; ++ i) {
			if (args[i].equals("-forum")) {
				forumTableName = args[i + 1];
			} else if (args[i].equals("-score")) {
				scoreTableName = args[i + 1];
			} else if (args[i].equals("-address")) {
				address = args[i + 1];
			} else if (args[i].equals("-database")) {
				databaseName = args[i + 1];
			} else if (args[i].equals("-username")) {
				username = args[i + 1];
			} else if (args[i].equals("-password")) {
				password = args[i + 1];
			}
		}
		System.err.println("score = " + scoreTableName);
		System.err.println("forum = " + forumTableName);
		System.err.println("address = " + address);
		System.err.println("database = " + databaseName);
		System.err.println("username = " + username);
		System.err.println("password = " + password);
		
		SQL.setAddress(address);
		SQL.setDatabaseName(databaseName);
		SQL.setUsername(username);
		SQL.setPassword(password);
		
		ForumSQL sql = new ForumSQL(forumTableName);
		ArrayList<Post> posts = null;
		try {
			posts = sql.readPosts();
		} catch (Exception e) {
			System.err.println("error while reading posts");
			e.printStackTrace();
		}
		System.out.println("# posts loaded from database = " + posts.size());
		
		RecSys rs = new RecSys();
		long mini = Long.MAX_VALUE, maxi = Long.MIN_VALUE;
		HashMap<String, Long> lastAct = new HashMap<String, Long>();
		for (int i = 0; i < posts.size(); ++ i) {
			rs.addPost(posts.get(i));
			long cur = Long.parseLong(posts.get(i).timestamp);
			if (lastAct.containsKey(posts.get(i).author)) {
				lastAct.put(posts.get(i).author, Math.max((long)lastAct.get(posts.get(i).author), cur));
			} else {
				lastAct.put(posts.get(i).author, cur);
			}
			maxi = Math.max(maxi, cur);
			mini = Math.min(mini, cur);
		}
		
		ArrayList<ScoreRecord> select = new ArrayList<ScoreRecord>();
			
		Simulation.simulate(rs); // TODO how to use the simulations
		System.out.println("similations done.");
		
		try {
			rs.train();
		} catch (IOException e) {
			System.err.println("error while training models");
			e.printStackTrace();
		}
		System.out.println("training done.");
		
		ArrayList<Integer> threads = new ArrayList<Integer>();
		ArrayList<String> users = new ArrayList<String>();
		int active_users = 0;
		for (User user : rs.forum.users.values()) {
			if (lastAct.containsKey(user) && (maxi - lastAct.get(user)) < (2 * 7 * 24 * 60 * 60)) {
				continue;
			}
			active_users += 1;
			HashSet<Integer> participated = new HashSet<Integer>();// avoid to recommend the participated threads
			for (Post post : user.posts) {
				participated.add(post.threadId);
			}
			
			for (Integer thread : rs.forum.threads.keySet()) {
				if (participated.contains(thread)) {
					continue;
				}
				users.add(user.name);
				threads.add(thread);
			}
		}
		System.err.println("Active Users = " + active_users);
		
		double [] scores = null;
		try {
			scores = rs.evaluate(users.toArray(new String[users.size()]), threads.toArray(new Integer[threads.size()]));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		ArrayList<ScoreRecord> all = new ArrayList<ScoreRecord>();
		for (int i = 0; i < scores.length; ++ i) {
			all.add(new ScoreRecord(users.get(i), threads.get(i), scores[i]));
		}
		Collections.sort(all);
		
		for (int i = 0; i < all.size(); ++ i) {
			int j = i;
			while (j < all.size() && all.get(i).user.equals(all.get(j).user)) {
				++ j;
			}
			for (int k = i; k < j && k < i + TOP_LIMIT; ++ k) {
				select.add(all.get(k));
			}
			i = j - 1;
		}
		System.out.println("Top " + TOP_LIMIT + " rated threads got for each user. " + select.size());
		
		ScoreSQL scoreSQL = new ScoreSQL(scoreTableName);
		try {
			scoreSQL.clear();
		} catch (Exception e1) {
			System.err.println("error while clearing score SQL");
			e1.printStackTrace();
		}
		System.out.println("Score SQL cleared.");
		try {
			scoreSQL.writeScore(select.toArray(new ScoreRecord[select.size()]));
		} catch (Exception e) {
			System.err.println("error while writing scores");
			e.printStackTrace();
		}
		
		System.out.println("done.");
	}
}