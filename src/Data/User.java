package Data;

import java.util.ArrayList;

public class User {
	public String name;
	public int gender; // 0 - male, 1 - female, 2 - secret
	public int age;
	public int capacity;
	public int capability;
	public ArrayList<Post> posts;
	
	User(String name, int gender, int age) {
		this.name = name;
		this.gender = gender;
		this.age = age;
		posts = new ArrayList<Post>();
	}
	
	/**
	 * update the user's basic information
	 * @param name
	 * @param gender
	 * @param age
	 */
	public void updateUser(String name, int gender, int age) {
		this.name = name;
		this.gender = gender;
		this.age = age;
	}
	
	/**
	 * add a post
	 * @param post
	 */
	public void addPost(Post post) {
		posts.add(post);
	}
	
	/**
	 * update a post
	 * @param post
	 * @return
	 */
	public boolean updatePost(Post post) {
		for (int i = 0; i < posts.size(); ++ i) {
			if (posts.get(i).postId == post.postId) {
				posts.set(i, post);
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 
	 * @return the sum of votes
	 */
	public int getTotalVotes() {
		int totalVotes = 0;
		for (Post p : posts) {
			totalVotes += p.vote;
		}
		return totalVotes;
	}
}
