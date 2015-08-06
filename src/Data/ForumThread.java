package Data;

import java.util.ArrayList;


public class ForumThread {
	public int threadId;
	public String owner;
	public int hardness;
	public ArrayList<Post> posts;
	
	public ForumThread(Post firstPost) {
		owner = firstPost.author;
		threadId = firstPost.threadId;
		posts = new ArrayList<Post>();
		posts.add(firstPost);
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
	 * @return the sum of the votes
	 */
	public int getTotalVotes() {
		int totalVotes = 0;
		for (Post p : posts) {
			totalVotes += p.vote;
		}
		return totalVotes;
	}
}
