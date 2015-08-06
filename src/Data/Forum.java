package Data;

import java.util.HashMap;

public class Forum {
	public HashMap<String, User> users;
	public HashMap<Integer, ForumThread> threads;
	
	public Forum() {
		users = new HashMap<String, User>();
		threads = new HashMap<Integer, ForumThread>();
	}
	
	/**
	 * Try to add a new user. 
	 * @param user
	 * @return false if the user is already existed.
	 */
	public boolean addUser(User user) {
		if (!users.containsKey(user.name)) {
			users.put(user.name, user);
			return true;
		}
		return false;
	}
	
	/**
	 * Try to add a new post.
	 * @param post
	 * @return true if the post is successfully added.
	 */
	public boolean addPost(Post post) {
		String author = post.author;
		if (!users.containsKey(author)) {
			users.put(author, new User(author, -1, -1));
		}
		int threadId = post.threadId;
		if (!threads.containsKey(threadId)) {
			threads.put(threadId, new ForumThread(post));
		}
		
		//users.get(author).addPost(post);
		User u = users.get(author);
		u.addPost(post);
		users.put(author, u);
		
		//threads.get(threadId).addPost(post);
		ForumThread t = threads.get(threadId);
		t.addPost(post);
		threads.put(threadId, t);
		
		return true;
	}
	
	/**
	 * Try to update the post
	 * @param post
	 * @return true if the post is successfully updated.
	 */
	public boolean updatePost(Post post) {
		String author = post.author;
		if (!users.containsKey(author)) {
			return false;
		}
		int threadId = post.threadId;
		if (!threads.containsKey(threadId)) {
			return false;
		}
		return users.get(author).updatePost(post) && threads.get(threadId).updatePost(post);
	}
	
	/**
	 * Load the existed forum records from files.
	 * @param filename
	 * @return # of the posts loaded.
	 */
	public int loadFromFile(String filename) {
		//TODO
		return 0;
	}
}
