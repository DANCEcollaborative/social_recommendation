package TestMain;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;

import CoreRecSys.RecSys;
import CoreRecSys.Simulation;
import Data.ForumThread;
import Data.Post;
import Data.ScoreRecord;
import Data.User;
import MySQL.ForumSQL;
import MySQL.ScoreSQL;
import MySQL.SQL;

public class CorpusSelection
{
	private static final long START_TIME = 7 * 24 * 60 * 60;
	private static final int TOP_LIMIT = 100;

	public static void main(String args[]) {
	  // Test Locally, Connecting with moocdb
		String address = new String("128.2.220.118");
		// need to be changed for our new course
		String databaseName = new String("dalmooc");
		String username = new String("diyiy");
		String password = new String("password");
		String forumTableName = "collaborations";
		String scoreTableName = "score_test";
		
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
		RecSys rs_alls = new RecSys();
		long mini = Long.MAX_VALUE, maxi = Long.MIN_VALUE;
        ArrayList<Long> timestamps = new ArrayList<Long>();
        for (int i = 0; i < posts.size(); ++ i) {
            timestamps.add(Long.parseLong(posts.get(i).timestamp));
            rs_alls.addPost(posts.get(i));
        }
        Collections.sort(timestamps);
        long threshold = timestamps.get((int)(timestamps.size() * 0.5));
        
        for (int i = 0; i < posts.size(); ++ i) {
          if (Long.parseLong(posts.get(i).timestamp) <= threshold) {
              rs.addPost(posts.get(i));
          }
          maxi = Math.max(maxi, Long.parseLong(posts.get(i).timestamp));
          mini = Math.min(mini, Long.parseLong(posts.get(i).timestamp));
        }
        System.out.println("Min = " + mini + ", Max = " + maxi + ", Threshold = " + threshold);
          
        
        // generate tests
        ArrayList<Integer> threads = new ArrayList<Integer>();
        ArrayList<String> users = new ArrayList<String>();
        HashSet<String> ground = new HashSet<String>();
        ArrayList<Integer> threadIdList = new ArrayList<Integer>();
        for (Integer thread : rs.forum.threads.keySet()) {
            threadIdList.add(thread);
        }
        Random rand = new Random(19910820);
        for (User user : rs_alls.forum.users.values()) {
            HashSet<Integer> participated = new HashSet<Integer>();// avoid to recommend the participated threads
            for (Post post : user.posts) {
                if (Long.parseLong(post.timestamp) <= threshold) {
                    participated.add(post.threadId);
                }
            }
            for (Post post : user.posts) {
                int thread = post.threadId;
                if (participated.contains(thread) || !threadIdList.contains(thread)) {
                    continue;
                }
                if (Long.parseLong(post.timestamp) > threshold) {
                    users.add(user.name);
                    threads.add(thread);
                    ground.add(user.name + "|" + thread);
                    for (int iter = 0; iter < 5; ++ iter) {
                        int neg_thread = threadIdList.get(rand.nextInt(threadIdList.size()));
                        if (participated.contains(neg_thread) || neg_thread == thread) {
                            -- iter;
                            continue;
                        }
                        users.add(user.name);
                        threads.add(neg_thread);
                    }
                }
            }
        }
        System.err.println("Test Samples generated! " + users.size());
		
		try {
			rs.train();
		} catch (IOException e) {
			System.err.println("error while training models");
			e.printStackTrace();
		}
		System.out.println("training done.");
		
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
		
        double sumMAP = 0;
        int totalUsers = 0;
		for (int i = 0; i < all.size(); ++ i) {
			int j = i;
			while (j < all.size() && all.get(i).user.equals(all.get(j).user)) {
				++ j;
			}
            int correct = 0;
            int total = 0;
            double MAP = 0;
			for (int k = i; k < j && k < j + TOP_LIMIT; ++ k) {
                String key = all.get(k).user + "|" + all.get(k).thread;
                ++ total;
                if (ground.contains(key)) {
                    ++ correct;
                    MAP += (double)correct / total;
                }
			}
			      if (correct > 0) {
			        MAP /= correct;
			      }
            sumMAP += MAP;
            ++ totalUsers;
			i = j - 1;
		}
		System.out.println("#Users = " + totalUsers);
		System.out.println("Average MAP = " + sumMAP / totalUsers);
		
		System.out.println("done.");
	}
}