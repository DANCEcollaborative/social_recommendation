package Data;

public class MyPair<T> implements Comparable<MyPair<T>>{
	public T value;
	public double key;
	
	public MyPair (T value, double key) {
		this.value = value;
		this.key = key;
	}
	
	@Override
	public int compareTo(MyPair<T> o) {
		if (key > o.key) {
			return -1;
		}
		if (key < o.key) {
			return 1;
		}
		return 0;
	}
	
}
