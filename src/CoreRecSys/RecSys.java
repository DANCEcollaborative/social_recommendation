package CoreRecSys;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

import Utils.Utils;
import Data.Forum;
import Data.ForumThread;
import Data.Post;
import Data.User;

public class RecSys {
	final private boolean KEEP_SCILIENT = true; 
	private static final int NegativeSampleSize = 5;

	public Forum forum;
    
    private HashMap<String, Integer> user2UserId;
    private HashMap<Integer, Integer> thread2ThreadId;
    
    private int numberOfUsers;
    private int numberOfThreads;
    private int numberOfUserFeatures;
    private int numberOfThreadFeatures;
    private int numberOfGlobalFeatures;
    
    final private String trainingFilename = new String("train");
    final private String trainingFeaturePrefix = new String("features/train");
    final private String configFilename = new String("config.conf");
    
    private void outputConfig(String configFilename) throws IOException {
        // output the training configuration file
        // use the number of users, thread, user features, and thread features
        // default set the learning rate, the L2 norm terms
    	
    	BufferedWriter config = new BufferedWriter(new FileWriter(new File(configFilename)));
    	
    	config.write("# example config for Basic Matrix Fatocirzation\n");
    	config.write("# the global constant bias for prediction\n");
    	config.write("base_score = " + 1.0/(NegativeSampleSize + 1) + "\n\n");
    	
    	config.write("# learning rate for SGD\n");
    	config.write("learning_rate = 0.03\n\n");
    	
    	config.write("# regularization constant for factor usually denote \\lambda in CFx papers\n");
    	config.write("wd_item       = 0.00\n");
    	config.write("wd_user       = 0.00\n\n");
    	
    	config.write("# number of each kind of features\n");
    	config.write("num_user   = " + numberOfUserFeatures + "\n");
    	config.write("num_item   = " + numberOfThreadFeatures + "\n");
    	config.write("num_global = " + numberOfGlobalFeatures + "\n\n");
    	
    	config.write("# number of factor\n");
    	config.write("num_factor = 32\n\n");
    	
    	config.write("# translation function for output, 0:linear 2:sigmoid\n");
    	config.write("# setting for RANK\n");
    	config.write("model_type = 1\n");
    	config.write("active_type = 2\n");
    	config.write("no_user_bias = 1\n");
    	
    	config.write("# use user grouped format\n\n");
    	config.write("format_type = 1\n");
    	
    	config.write("# use auto pairing provided in training\n");
    	config.write("input_type = 0\n\n");
    	
    	config.write("# buffer for training, binary format, created by make_feature_buffer\n");
    	config.write("buffer_feature = \"buffer.train\"\n\n");
    	
    	config.write("# folder to store the model file\n");
    	config.write("model_out_folder=\"./model/\"\n\n");
    	
    	config.write("# data for evaluation, binary format, used by svd_feature_infer\n");
    	config.write("test::input_type = 0\n");
    	config.write("test:buffer_feature=\"buffer.test\"\n");
    	
    	config.close();
    }
    
    @SuppressWarnings("null")
	private void outputTrainingData() throws IOException {
        // for checking the negative samples, avoid same/positive ones
        HashSet<Integer> [] occur = new HashSet[numberOfUsers];
        for (int i = 0; i < numberOfUsers; ++ i) {
           // occur.add(new HashSet<Integer>());
            occur[i] = new HashSet<Integer>();
        }
       
        for (Map.Entry<Integer, ForumThread> entry : forum.threads.entrySet()) {
            for (Post post : entry.getValue().posts) {
                String user = post.author;
                int thread = post.threadId;
                
                int userId = user2UserId.get(user);
                int threadId = thread2ThreadId.get(thread);
                
                occur[userId].add(threadId);
            }
        }
        BufferedWriter trainingData = new BufferedWriter(new FileWriter(new File(trainingFilename)));
        BufferedWriter imfbFile = new BufferedWriter(new FileWriter(new File(trainingFilename + ".imfb")));
        
        Random rand = new Random(19910820);
        for (Map.Entry<String, User> entry : forum.users.entrySet()) {
        	int cnt = 0;
        	for (Post post : entry.getValue().posts) {
                String user = post.author;
                int thread = post.threadId;
                
                int userId = user2UserId.get(user);
                int threadId = thread2ThreadId.get(thread);
                
                // generate the train data
                // write them to a specific file
                // 3 columns, u, i, r
                trainingData.write(userId + "\t" + threadId + "\t" + 1 + "\n");
                ++ cnt;
                // negative samples are also needed
                // Random numbers are needed, better to fix the random seed
                // check the occur, and add it

    		    for( int i = 0;i < NegativeSampleSize; i ++){
    		    	int randomNum = rand.nextInt(numberOfThreads);
    		    	while(occur[userId].contains(randomNum) || randomNum == threadId){
    		    		randomNum = rand.nextInt(numberOfThreads);
    		    	}
    		    	trainingData.write(userId + "\t" + randomNum + "\t" + 0 + "\n");
    		    	++ cnt;
    		    }
            }
        	
        	imfbFile.write(cnt + "\t0\n");
        }
        trainingData.close(); // it is important
        imfbFile.close();
        System.out.println("Training Data Done.");
    }
    
    private void outputFeatures(String recordFilename, String featuresPrefix) throws IOException {
        // read the record file
        // 3 columns, u, i, r
        // extract different types of features for them
    	BufferedReader br = new BufferedReader(new FileReader(recordFilename));
        String line = "";
        ArrayList<Integer> users = new ArrayList<Integer>();
        ArrayList<Integer> threads = new ArrayList<Integer>();
        while ((line = br.readLine()) != null) {
        	String [] cur = line.split("\t");
        	int uid = Integer.parseInt(cur[0]);
        	int iid = Integer.parseInt(cur[1]);
        	
        	users.add(uid);
        	threads.add(iid);
        }
        
        // e.g. the basic mf
        // print the ids of u and i as user feature and item feature to the featuresPrefix + ".id"
        
        numberOfUserFeatures = numberOfThreadFeatures = numberOfGlobalFeatures = 0;
        
        numberOfUserFeatures += extractUserID(featuresPrefix, users, threads);
        numberOfUserFeatures += extractCohort(featuresPrefix, users, threads);
        numberOfUserFeatures += extractPost(featuresPrefix, users, threads);
    	
        numberOfThreadFeatures += extractThreadID(featuresPrefix, users, threads);
        numberOfThreadFeatures += extractThreadInfo(featuresPrefix, users, threads);
        numberOfThreadFeatures += extractIMFB(featuresPrefix, users, threads);
    }
    
    private int extractIMFB(String featuresPrefix, ArrayList<Integer> users, ArrayList<Integer> threads) throws IOException {
    	HashMap<Integer, Integer> threadId2Thread = new HashMap<Integer, Integer>();
    	for (Integer thread : thread2ThreadId.keySet()) {
    		threadId2Thread.put(thread2ThreadId.get(thread), thread);
    	}
    	
    	BufferedWriter writer = new BufferedWriter(new FileWriter(new File(featuresPrefix + ".imfb")));
    	writer.write(user2UserId.size() + "\n");
    	for (int i = 0; i < threads.size(); ++ i) {
    		Integer threadId = threadId2Thread.get(threads.get(i));
    		HashSet<Integer> neighbors = new HashSet<Integer>();
    		if (threadId != null) {
    			ForumThread thread = forum.threads.get(threadId);
    			for (Post post : thread.posts) {
    				String username = post.author;
    				if (user2UserId.containsKey(username)) {
    					int userId = user2UserId.get(username);
    					neighbors.add(userId);
    				}
    			}
    		}
    		writer.write("" + neighbors.size());
    		double coef = 1.0 / Math.sqrt(neighbors.size());
    		for (Integer neighbor : neighbors) {
    			writer.write("\t" + neighbor + ":" + coef);
    		}
    		writer.write("\n");
    	}
    	writer.close();
		return user2UserId.size();
	}
    
    private int extractThreadInfo(String featuresPrefix, ArrayList<Integer> users, ArrayList<Integer> threads) throws IOException {
    	HashMap<Integer, Integer> userCount = new HashMap<Integer, Integer>();
    	HashMap<Integer, Integer> tokenCount = new HashMap<Integer, Integer>();
    	int maxiUser = 0, maxiToken = 0;
    	for (ForumThread thread : forum.threads.values()) {
    		if (thread2ThreadId.containsKey(thread.threadId)) {
    			int threadid = thread2ThreadId.get(thread.threadId);
    			userCount.put(threadid, thread.posts.size());
    			int tokenCnt = 0;
    			for (Post post : thread.posts) {
    				tokenCnt += post.content.split("\\s").length;
    			}
    			tokenCount.put(threadid, tokenCnt);
    			maxiUser = Math.max(maxiUser, thread.posts.size());
    			maxiToken = Math.max(maxiToken, tokenCnt);
    		}
    	}
    	
    	BufferedWriter writer = new BufferedWriter(new FileWriter(new File(featuresPrefix + ".thInfo")));
    	writer.write(2 + "\n");
    	for (int i = 0; i < threads.size(); ++ i) {
    		int cnt = 0, tokenCnt = 0;
    		if (userCount.containsKey(threads.get(i))) {
    			cnt = userCount.get(threads.get(i));
    			tokenCnt = tokenCount.get(threads.get(i));
    		}
    		writer.write(2 + "\t" + "0:" + (double)cnt / maxiUser + "\t1:" + (double)tokenCnt / maxiToken + "\n");
    	}
    	writer.close();
		return 2;
	}
    
    private int extractPost(String featuresPrefix, ArrayList<Integer> users, ArrayList<Integer> threads) throws IOException {
    	HashMap<Integer, Integer> postCount = new HashMap<Integer, Integer>();
    	int maxiPost = 0;
    	for (User user : forum.users.values()) {
    		if (user2UserId.containsKey(user.name)) {
    			postCount.put(user2UserId.get(user.name), user.posts.size());
    			maxiPost = Math.max(maxiPost, user.posts.size());
    		}
    	}
    	
    	BufferedWriter writer = new BufferedWriter(new FileWriter(new File(featuresPrefix + ".post")));
    	writer.write(1 + "\n");
    	for (int i = 0; i < users.size(); ++ i) {
    		int cnt = 0;
    		if (postCount.containsKey(users.get(i))) {
    			cnt = postCount.get(users.get(i));
    		}
    		writer.write(1 + "\t" + "0:" + (double)cnt / maxiPost + "\n");
    	}
    	writer.close();
		return 1;
	}
    
    private int extractCohort(String featuresPrefix, ArrayList<Integer> users, ArrayList<Integer> threads) throws IOException {
    	int base = Integer.MAX_VALUE;
    	for (User user : forum.users.values()) {
    		int start = Integer.MAX_VALUE;
    		for (Post post : user.posts) {
    			int t = Integer.parseInt(post.timestamp);
    			start = Math.min(start, t);
    		}
    		if (start > 1357850949) { // some toooo early one?
    			base = Math.min(base, start);
    		}
    	}
    	HashSet<Integer> cohort1 = new HashSet<Integer>();
    	for (User user : forum.users.values()) {
    		int start = Integer.MAX_VALUE;
    		for (Post post : user.posts) {
    			int t = Integer.parseInt(post.timestamp);
    			start = Math.min(start, t);
    		}
    		int week = (start - base) / (7 * 24 * 60 * 60);
    		if (user2UserId.containsKey(user.name)) {
    			int uid = user2UserId.get(user.name);
    			if (week == 0) {
    				cohort1.add(uid);
    			}
    		}
    	}
    	
    	BufferedWriter writer = new BufferedWriter(new FileWriter(new File(featuresPrefix + ".cohort")));
    	writer.write(2 + "\n");
    	for (int i = 0; i < users.size(); ++ i) {
    		writer.write(1 + "\t" + (cohort1.contains(users.get(i)) ? 1 : 0) + ":1\n");
    	}
    	writer.close();
		return 2;
	}
    
    private int extractUserID(String featuresPrefix, ArrayList<Integer> users, ArrayList<Integer> threads) throws IOException {
    	BufferedWriter bwu = new BufferedWriter(new FileWriter(new File(featuresPrefix + ".uID")));
    	bwu.write(forum.users.size() + "\n");
    	for (int i = 0; i < users.size(); ++ i) {
    		bwu.write(1 + "\t" + users.get(i) + ":1\n");
    	}
    	bwu.close();
		return forum.users.size();
	}
    
    private int extractThreadID(String featuresPrefix, ArrayList<Integer> users, ArrayList<Integer> threads) throws IOException {
    	BufferedWriter bwi = new BufferedWriter(new FileWriter(new File(featuresPrefix + ".iID")));
    	bwi.write(forum.threads.size() + "\n");
    	for (int i = 0; i < threads.size(); ++ i) {
    		bwi.write(1 + "\t" + threads.get(i) + ":1\n");
    	}
    	bwi.close();
		return forum.threads.size();
	}

    public void train() throws IOException {
        user2UserId = new HashMap<String, Integer>();
        thread2ThreadId = new HashMap<Integer, Integer>();
        
        for (User user : forum.users.values()) {
            int cnt = user2UserId.size();
            user2UserId.put(user.name, cnt);
        }
        
        for (ForumThread thread : forum.threads.values()) {
            int cnt = thread2ThreadId.size();
            thread2ThreadId.put(thread.threadId, cnt);
        }
        //same for thread id, give them new ids, encoded for svd feature
        numberOfUsers = forum.users.size();
        numberOfThreads = forum.threads.size();
        
        outputTrainingData();
        
        outputFeatures(trainingFilename, trainingFeaturePrefix);
        
        outputConfig(configFilename);
        
        Utils.executeBash("./svd_train.sh");
        
        // example.sh
        // call Svd feature
        // *********************************************************************************************
        //finally, we need to call the external sh commands
        // in that sh, we need first combine the features with the training data
        // then, call the svdfeature to train a model, under the settings of the configuration file we printed before
        // please write the .sh file first, and use the same file name as we stored in this program
        // and then, use the Java methods (maybe the Runtime, you can search for the "Java call bash") to call the .sh file
        // chmod +x is needed for the .sh file
        // also, some folders are needed to be created firstly, before the running of the code, e.g. model/, features/, ...
    }
    
    final private String testFilename = new String("test");
    final private String testFeaturePrefix = new String("features/test");
    
    public double[] evaluate(String[] users, Integer[] threads) throws IOException {
        // return an array of predicted scores for each pair of users and threads
    	double [] current = new double [users.length];
        if (users.length != threads.length) {
            System.err.println("[ERROR] Different lengths of users/threads!");
            return current;
        }

        BufferedWriter test = new BufferedWriter(new FileWriter(new File(testFilename)));
        BufferedWriter testImfb = new BufferedWriter(new FileWriter(new File(testFilename + ".imfb")));
        
        for (int i = 0; i < users.length; ++ i) {
            //write a 3 columns file to a test file, testFilename
            // u, j, 0
            int u = 0, j = 0;
            if (user2UserId.containsKey(users[i])) {
                u = user2UserId.get(users[i]);
            }
            if (thread2ThreadId.containsKey(threads[i])) {
                j = thread2ThreadId.get(threads[i]);
            }
            test.write(u + "\t" + j + "\t0\n");
            testImfb.write("1\t0\n");
        }
        test.close();
        testImfb.close();
        
		outputFeatures(testFilename, testFeaturePrefix); // as same as training
        
		outputConfig(configFilename);
        
        // finally, we need to call the external sh commands
		Utils.executeBash("./evaluate.sh");
        // in that sh, we need first combine the features with the test data
        // then, call the svdinfer, using a model we saved before (e.g. model/model.20, write it into the test configuration), under the settings of the configuration file we printed before
        // please write the .sh file first, and use the same file name as we stored in this program
        // and then, use the Java methods (maybe the Runtime, you can search for the "Java call bash") to call the .sh file
        // chmod +x is needed for the .sh file
        
		Scanner sc = new Scanner(new File("pred.txt"));
        double [] ret = new double[users.length];
        // load the predicted scores from file
        for (int i = 0; i < users.length; ++ i) {
            double score = sc.nextDouble();
            if (user2UserId.containsKey(users[i]) && thread2ThreadId.containsKey(threads[i])) {
                // this is a correct pair, use the predicted scores
                ret[i] = score;
            } else {
                // TODO, some different values, e.g. -1?
                ret[i] = -1;
            }
        }
        sc.close();
        return ret;
    }
    
	public ArrayList<ForumThread> recommendThreads(User user, int topK) {
		HashSet<Integer> participated = new HashSet<Integer>(); // avoid to recommend the participated threads
		for (Post post : user.posts) {
			participated.add(post.threadId);
		}
		
		ArrayList<Integer> threads = new ArrayList<Integer>();
		for (int thread : thread2ThreadId.keySet()) {
			if (participated.contains(thread)) {
				continue;
			}
			threads.add(thread);
		}
		String [] users = new String[threads.size()];
		for (int i = 0; i < users.length; ++ i) {
			users[i] = new String(user.name);
		}
		
		double [] scores = null;
		try {
			scores = evaluate(users, threads.toArray(new Integer[threads.size()]));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		HashMap<ForumThread, Double> result = new HashMap<ForumThread, Double>();
		for (int i = 0; i < scores.length; ++ i) {
			result.put(forum.threads.get(threads.get(i)), scores[i]);
		}
		
		result = Utils.sortThreadsByValues(result);
		//sort 
      
		ArrayList<ForumThread> answer = new ArrayList<ForumThread>();
        Iterator it2 = result.entrySet().iterator();
        int cnt = 0;
        while (it2.hasNext()) {
        	Map.Entry<ForumThread, Double> cur2 = (Map.Entry<ForumThread, Double>) it2.next();
        	answer.add(cur2.getKey());
        	cnt ++ ;
        	if(cnt == topK) break;
        }
		return answer;
	}
    
    public HashMap<String, HashMap<String, Double>> userFeatures;
    public HashMap<Integer, HashMap<String, Double>> threadFeatures;
	
    public RecSys() {
      this.forum = new Forum();
      this.userFeatures = new HashMap<String, HashMap<String, Double>>();
      this.threadFeatures = new HashMap<Integer, HashMap<String, Double>>();
    }
    
    public HashMap<String, Double> bagOfWords(User user) {
        HashMap<String, Double> ret = new HashMap<String, Double>();
        for (Post post : user.posts) {
            String content = post.content;
            String [] words = content.split("\\s+");
            for (String word : words) {
                // you may introduce a wordlist to reduce the # of valid words
                // try to load the wordlist in the construction function
                if (!ret.containsKey(word)) {
                    ret.put(word, (double) 1);
                } else {
                    ret.put(word, ret.get(word) + 1);
                }
            }
        }
        //  normalize it
        double total = 0;
        for(java.util.Map.Entry<String, Double> entry: ret.entrySet()){
        	total = total + entry.getValue();
        }
        total = Math.sqrt(total);
        
        for(java.util.Map.Entry<String, Double> entry: ret.entrySet()){
        	//total = total + entry.getValue();
        	String key = entry.getKey();
        	double value = entry.getValue()/total;
        	//ret.remove(key);
        	ret.put(key, value);
        }
        return ret;
    }
    
    public HashMap<String, Double> bagOfWords(ForumThread thread) {
        HashMap<String, Double> ret = new HashMap<String, Double>();
        for (Post post : thread.posts) {
            String content = post.content;
            String [] words = content.split("\\s+");
            for (String word : words) {
                // you may introduce a wordlist to reduce the # of valid words
                // try to load the wordlist in the construction function
                if (!ret.containsKey(word)) {
                    ret.put(word, (double) 1);
                } else {
                    ret.put(word, ret.get(word) + 1);
                }
            }
        }
        double total = 0;
        for(java.util.Map.Entry<String, Double> entry: ret.entrySet()){
        	total = total + entry.getValue();
        }
        total = Math.sqrt(total);
        
        for(java.util.Map.Entry<String, Double> entry: ret.entrySet()){
        	//total = total + entry.getValue();
        	String key = entry.getKey();
        	double value = entry.getValue()/total;
        	//ret.remove(key);
        	ret.put(key, value);
        }
        return ret;
    }
    
    public void addPost(Post post) {
        forum.addPost(post);
        
        userFeatures.put(post.author, bagOfWords(forum.users.get(post.author)));
        threadFeatures.put(post.threadId, bagOfWords(forum.threads.get(post.threadId)));
    }
    
	// when a user creates a thread question, which users should come to this thread for discussion?
	// Given a user, a new thread
	
	public ArrayList<User> answersGenerate(ForumThread thread, Integer K) {
		
		// Direct Content Matching 
		// Constraint Filters
		// User has capacity, capability
		// Thread has question difficulty
        
    // enumerate all users, calcualte the DCM simularities, choose the top-K
		ArrayList<User> answer = new ArrayList<User> ();
		HashMap<User, Double> res = new HashMap<User, Double> ();
		HashMap<String, User> us = forum.users;
		for(java.util.Map.Entry<String, User> entry: us.entrySet()){
			if (entry.getValue().name.equals(thread.owner)) {
				continue;
			}
			double cur = calculateDCM(thread,entry.getValue());
			res.put(entry.getValue(),cur);
		}
        res = Utils.sortUsersByValues(res);
		//sort
        // greedy 
        // constraint 
      
        Iterator it2 = res.entrySet().iterator();
        int cnt = 0;
        while(it2.hasNext()){
        	Map.Entry<User, Double> cur2 = (java.util.Map.Entry<User, Double>) it2.next();
        	answer.add(cur2.getKey());
        	cnt ++ ;
        	
        	if(cnt==K) break;
        }
		return answer;
	}
	
	// direct content matching
	public double calculateDCM(ForumThread thread, User user){
		double simi = 0; 
		if (thread.owner.equals(user)) return simi;
		HashMap<String, Double> a = threadFeatures.get(thread.threadId);
        HashMap<String, Double> b = userFeatures.get(user.name);
        
        //dot product
        
        for (java.util.Map.Entry<String, Double> entry : a.entrySet()){
        	String curkey = entry.getKey();
        	if(b.containsKey(curkey)){
        		simi += entry.getValue()*b.get(curkey);
        	}
        }
		return simi;
	}
	
	// direct content matching
	public double calculateDCM(HashMap<String, Double> a, HashMap<String, Double> b){
		double simi = 0; 
        //dot product
        double aRes = 0, bRes = 0;
        for (java.util.Map.Entry<String, Double> entry : a.entrySet()){
        	String curkey = entry.getKey();
        	aRes += entry.getValue()*entry.getValue();
        	if(b.containsKey(curkey)){
        		simi += entry.getValue()*b.get(curkey);
        		//bRes +=b.get(curkey)*b.get(curkey);
        	}
        }
        for (java.util.Map.Entry<String, Double> entry : b.entrySet()){
          String curkey = entry.getKey();
          bRes += entry.getValue()*entry.getValue();   
        }
        
        simi = simi/ (Math.sqrt(aRes)*Math.sqrt(bRes));
		return simi;
	}
}
