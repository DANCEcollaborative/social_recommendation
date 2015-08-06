package Data;

public class Post {
	public String author;
	public int threadId, postId, parentPostId;
	public String content;
	public int vote;
	public String timestamp;
	
	public Post(String author, int threadId, int postId, int parentPostId, String content, int vote, String timestamp) {
		this.author = author;
		this.threadId = threadId;
		this.postId = postId;
		this.parentPostId = parentPostId;
		this.content = content;
		this.vote = vote;
		this.timestamp = timestamp;
	}
	
	public String toString() {
		return new String("(\"" + author + "\",\"" + content + "\"," + postId + "," + threadId + "," + parentPostId + "," + vote + ",\"" + timestamp + "\")");
	}
}
