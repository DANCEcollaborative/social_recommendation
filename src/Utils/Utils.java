package Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import Data.ForumThread;
import Data.User;

public class Utils
{
	public static void executeBash(String command) throws IOException {
    	Process process = Runtime.getRuntime().exec(command);
        BufferedReader errorReader = new BufferedReader(new InputStreamReader(  
                process.getInputStream()));  
        String line = null;  
        while ((line = errorReader.readLine()) != null) {
        }  
        errorReader.close();  
        BufferedReader infoReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));  
        while ((line = infoReader.readLine()) != null) {
        }  
        infoReader.close();
    }
    
	
	@SuppressWarnings("unchecked")
	public static HashMap<User, Double> sortUsersByValues(HashMap<User, Double> map) { 
	      List list = new LinkedList(map.entrySet());
	     
	      Collections.sort(list, new Comparator() {
	           public int compare(Object o1, Object o2) {
	              return ((Comparable) ((Map.Entry) (o2)).getValue())
	                 .compareTo(((Map.Entry) (o1)).getValue());
	           }
	      });

	      HashMap<User, Double> sortedHashMap = new LinkedHashMap<User, Double>();
	      for (Iterator it = list.iterator(); it.hasNext();) {
	    	  Map.Entry<User, Double> entry = (java.util.Map.Entry<User, Double>) it.next();
	             sortedHashMap.put(entry.getKey(), entry.getValue());
	      } 
	      return sortedHashMap;
	}
	
	@SuppressWarnings("unchecked")
	public static HashMap<ForumThread, Double> sortThreadsByValues(HashMap<ForumThread, Double> map) { 
	      List list = new LinkedList(map.entrySet());
	     
	      Collections.sort(list, new Comparator() {
	           public int compare(Object o1, Object o2) {
	              return ((Comparable) ((Map.Entry) (o2)).getValue())
	                 .compareTo(((Map.Entry) (o1)).getValue());
	           }
	      });

	      
	      HashMap<ForumThread, Double> sortedHashMap = new LinkedHashMap<ForumThread, Double>();
	      for (Iterator it = list.iterator(); it.hasNext();) {
	    	  Map.Entry<ForumThread, Double> entry = (Map.Entry<ForumThread, Double>) it.next();
	             sortedHashMap.put(entry.getKey(), entry.getValue());
	      } 
	      return sortedHashMap;
	}
}