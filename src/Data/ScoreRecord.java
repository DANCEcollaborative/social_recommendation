package Data;

public class ScoreRecord implements Comparable<ScoreRecord> {
	public String user;
	public int thread;
	public double score;
	
	public ScoreRecord(String user, int thread, double score) {
		this.user = user;
		this.thread = thread;
		this.score = score;
	}

	@Override
	public int compareTo(ScoreRecord other) {
		if (!user.equals(other.user)) {
			return user.compareTo(other.user);
		}
		if (score > other.score) {
			return -1;
		}
		if (score < other.score) {
			return 1;
		}
		return 0;
	}
	
	
}
