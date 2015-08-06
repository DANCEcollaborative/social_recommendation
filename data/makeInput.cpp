#include "Helper.h"

const int topK = 3;

struct Post
{
	string author, content;
	int postId, threadId, parentPostId;
	int timestamp;
	
	Post(string author, string content, int postId, int threadId, int parentPostId, int timestamp) : author(author), content(content), postId(postId), threadId(threadId), parentPostId(parentPostId), timestamp(timestamp) {
	}
	
	string toString() {
		//author, content, parentPostId, postId, threadId, time stamp;
		ostringstream out;
		out << author << ",\"" << content << "\"," << parentPostId << "," << postId << "," << threadId << "," << timestamp;
		return out.str();
	}
};

bool byTimestamp(const Post &a, const Post &b)
{
	return a.timestamp < b.timestamp;
}

void loadPosts(vector<Post> &posts, string filename)
{
	FILE* in = tryOpen(filename, "r");
	
	getLine(in);
	
	while (true) {
		string temp = "";
		vector<string> tokens;
		bool isEnd = false;
		while (tokens.size() < 5) {
			if (!getLine(in)) {
				isEnd = true;
				break;
			}
			temp += line;
			temp += " ";
			tokens = splitBy(temp, ',');
		}
		if (isEnd) {
			break;
		}
		
		assertTrue(tokens.size() == 5, "Wrong Posting Records!");
		
		//user_id	post_text	id	thread_id	post_time
		string author = tokens[0];
		string content = tokens[1];
		int postId, threadId, parentPostId = -1, timestamp;
		fromString(tokens[2], postId);
		fromString(tokens[3], threadId);
		fromString(tokens[4], timestamp);
		
		posts.push_back(Post(author, content, postId, threadId, parentPostId, timestamp));
	}
	
	fclose(in);
}

void loadComments(vector<Post> &posts, string filename, int currentCommentId)
{
	FILE* in = tryOpen(filename, "r");
	
	getLine(in);
	
	while (true) {
		string temp = "";
		vector<string> tokens;
		bool isEnd = false;
		while (tokens.size() < 5) {
			if (!getLine(in)) {
				isEnd = true;
				break;
			}
			temp += line;
			temp += " ";
			tokens = splitBy(temp, ',');
		}
		if (isEnd) {
			break;
		}
		
		assertTrue(tokens.size() == 5, "Wrong Posting Records!");
		
		//user_id	comment_text	post_id	thread_id	post_time
		string author = tokens[0];
		string content = tokens[1];
		int threadId, parentPostId, timestamp;
		fromString(tokens[2], parentPostId);
		fromString(tokens[3], threadId);
		fromString(tokens[4], timestamp);
		
		posts.push_back(Post(author, content, currentCommentId, threadId, parentPostId, timestamp));
		++ currentCommentId;
	}
	
	fclose(in);
}

int main()
{
	vector<Post> posts;
	loadPosts(posts, "0algebra_posts.csv");
	int maxPostId = 0;
	for (int i = 0; i < posts.size(); ++ i) {
		maxPostId = max(maxPostId, posts[i].postId);
	}
	fprintf(stderr, "%d posts, maxPostId = %d\n", posts.size(), maxPostId);
	
	loadComments(posts, "0algebra_comments.csv", maxPostId + 1);
	
	sort(posts.begin(), posts.end(), byTimestamp);
	fprintf(stderr, "in total, %d posts/comments\n", posts.size());
	
	freopen("postsInOrder.txt", "w", stdout);
	set<int> occur;
	set<string> users;
	for (int i = 0; i < posts.size(); ++ i) {
		Post &post = posts[i];
		printf("%s\n", post.toString().c_str());
/*		printf("ADD %s\n", post.toString().c_str());
		users.insert(post.author);
		if (!occur.count(post.threadId)) {
			occur.insert(post.threadId);
			printf("QUERY_THREAD_USER %d %d\n", post.threadId, topK);
		}
		
		if ((i + 1) % 5000 == 0 || i + 1 == posts.size()) {
			printf("TRAIN\n");
			FOR (user, users) {
				printf("QUERY_USER_THREAD %s %d\n", user->c_str(), topK);
			}
		}*/
	}
	fclose(stdout);
	
	return 0;
}

