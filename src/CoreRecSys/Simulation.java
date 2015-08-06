package CoreRecSys;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import Data.ForumThread;
import Data.Post;
import Data.User;

public class Simulation {
	static public HashMap<String, Integer> capacity, capability;
	static public HashMap<Integer, Integer> hardness;
	static final int levels = 5;
	
	static double calc(ArrayList<Integer> a, int l, int r)
	{
		double ex = 0, dist = 0;
		for (int i = l; i < r; ++ i) {
			ex += a.get(i);
		}
		ex /= r - l;
		for (int i = l; i < r; ++ i) {
			dist += (a.get(i) - ex) * (a.get(i) - ex);
		}
		return dist;
	}

	static double calc(ArrayList<Integer> a, ArrayList<Integer> sep)
	{
		double ret = 0;
		for (int i = 1; i < sep.size(); ++ i) {
			ret += calc(a, sep.get(i - 1), sep.get(i));
		}
		return ret;
	}
	
	static HashMap<Integer, Integer> divideIntoLevels(HashMap<Integer, Integer> a) {
		HashMap<Integer, Integer> result = new HashMap<Integer, Integer>();
		ArrayList<Integer> value = new ArrayList<Integer>();
		for (Integer v : a.values()) {
			value.add(v);
		}
		Collections.sort(value);
		ArrayList<Integer> sep = new ArrayList<Integer>();
		sep.add(0);
		sep.add(a.size());
		for (int iter = 0; iter < levels - 1; ++ iter) {
			double mini = 1e100;
			ArrayList<Integer> best = new ArrayList<Integer>();
			for (int i = 1; i < sep.size(); ++ i) {
				for (int j = sep.get(i - 1) + 1; j < sep.get(i); ++ j) {
					ArrayList<Integer> newSep = (ArrayList<Integer>) sep.clone();
					newSep.add(j);
					Collections.sort(newSep);
					double chaos = calc(value, newSep);
					if (chaos < mini) {
						mini = chaos;
						best = newSep;
					}
				}
			}
			sep = (ArrayList<Integer>) best.clone();
		}
		
		for (Integer key : a.keySet()) {
			int v = a.get(key);
			int level = -1;
			for (int j = 1; j < sep.size(); ++ j) {
				if (value.get(sep.get(j - 1)) <= v && v <= value.get(sep.get(j - 1))) {
					level = j - 1;
				}
			}
			result.put(key, level);
		}
		return result;
	}
	
	static HashMap<String, Integer> divideIntoLevelsString(HashMap<String, Integer> a) {
		HashMap<String, Integer> result = new HashMap<String, Integer>();
		ArrayList<Integer> value = new ArrayList<Integer>();
		for (Integer v : a.values()) {
			value.add(v);
		}
		Collections.sort(value);
		ArrayList<Integer> sep = new ArrayList<Integer>();
		sep.add(0);
		sep.add(a.size());
		for (int iter = 0; iter < 2; ++ iter) {
			double mini = 1e100;
			ArrayList<Integer> best = new ArrayList<Integer>();
			for (int i = 1; i < sep.size(); ++ i) {
				for (int j = sep.get(i - 1) + 1; j < sep.get(i); ++ j) {
					ArrayList<Integer> newSep = (ArrayList<Integer>) sep.clone();
					newSep.add(j);
					Collections.sort(newSep);
					double chaos = calc(value, newSep);
					if (chaos < mini) {
						mini = chaos;
						best = newSep;
					}
				}
			}
			sep = (ArrayList<Integer>) best.clone();
		}
		
		for (String key : a.keySet()) {
			int v = a.get(key);
			int level = -1;
			for (int j = 1; j < sep.size(); ++ j) {
				if (value.get(sep.get(j - 1)) <= v && v <= value.get(sep.get(j - 1))) {
					level = j - 1;
				}
			}
			level = 6 + 2 * level;
			result.put(key, level);
		}
		return result;
	}
	
	static public void simulate(RecSys rs) {
		capacity = new HashMap<String, Integer>();
		capability = new HashMap<String, Integer>();
		hardness = new HashMap<Integer, Integer>();
		
		for (ForumThread t : rs.forum.threads.values()) {
			Post starter = t.posts.get(0);
			hardness.put(t.threadId, starter.content.length());
		}
		hardness = divideIntoLevels(hardness);
		
		for (User u : rs.forum.users.values()) {
			capacity.put(u.name, u.posts.size());
			int maxi = 0;
			for (Post p : u.posts) {
				maxi = Math.max(maxi, hardness.get(p.threadId));
			}
			capability.put(u.name, maxi);
		}
		capacity = divideIntoLevelsString(capacity);
	}
}
