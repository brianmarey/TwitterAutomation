package com.careydevelopment.twitterautomation.exec;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;

import com.careydevelopment.twitterautomation.config.MyTwitter;
import com.careydevelopment.twitterautomation.util.Constants;

import twitter4j.Friendship;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

/**
 * Auto follow Twitter users based on interest
 */
public class AutoFollow implements Constants {
	
	private static final int NUMBER_OF_RUNS = 7;
	private static final int FOLLOW_SET_PAUSE_SIZE = 3600000;
	private static final int FOLLOW_SET_PAUSE_MIN = 180000;
	private static final int FOLLOW_PAUSE_SIZE = 30000;
	private static final int FOLLOW_PAUSE_MIN = 60000;
	
			
	//populate this array with the hashtags used by people you want to follow
	private static final String[] keywords = {"#TrumpTrain","#tcot","#2a"};
	
	private static final Logger LOGGER = Logger.getLogger(AutoFollow.class);
	
	private List<Long> dnfIds = new ArrayList<Long>();
	private Twitter twitter = null;

	public static void main(String[] args) {
		//get out of the static context
		AutoFollow af = new AutoFollow();
		af.go();
	}
	
	
	private void go() {
		//get the twitter4j Twitter object from the singleton
		twitter = MyTwitter.instance().getTwitter();
	
		//fetch the DNF ids
		fetchDnfs();
		
		//we'll cycle thru the set of keywords a number of times
		//based on the NUMBER_OF_RUNS value
		for (int count = 0;count<NUMBER_OF_RUNS;count++) {			
			LOGGER.info("On run #" + count);
				
			for (String key : keywords) {
				processKeyword(key);
			}
			
			pauseBetweenFollowSets();	
		}
	}

	
	/**
	 * Follows people based on the given keyword
	 */
	private void processKeyword(String key) {
		try {
			Query query = new Query(key).count(100);
			QueryResult result = twitter.search(query);
					    
			String[] returnedDudes = getReturnedDudes(result);		
			ResponseList<Friendship> friendships = twitter.lookupFriendships(returnedDudes);
			
			stepThruFriendships(friendships);			
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
	
	
	/**
	 * Pauses between sets of follows
	 */
	private void pauseBetweenFollowSets() {
		Random rand = new Random();
		int  n = rand.nextInt(FOLLOW_SET_PAUSE_SIZE) + FOLLOW_SET_PAUSE_MIN;
			
		LOGGER.info("wait period is " + n);
			
		Calendar calendar = Calendar.getInstance();
		DateFormat df = new SimpleDateFormat("HH:mm");
		String time = df.format(calendar.getTime());
		LOGGER.info("Current time is " + time);
			
		calendar.add(Calendar.MILLISECOND, n);
			
		String newTime = df.format(calendar.getTime());
		LOGGER.info("Will resume at " + newTime);
							
		try {
			Thread.sleep(n);
		} catch (Exception e) {
			e.printStackTrace();
		}			
	}
	
	
	/**
	 * Follows people that we're not already following if they were in the result set
	 */
	private void stepThruFriendships(ResponseList<Friendship> friendships) {
		//step thru each friendship object
		for (Friendship friendship : friendships) {
			
			//make sure we're not following him already
			if (!friendship.isFollowing()) {
				
				//get the id
				long id = friendship.getId();
				
				//make sure he's not in DNF
			    if (!dnfIds.contains(id)) {
			    	try {
			    		LOGGER.info("Now following " + friendship.getScreenName() + " " + friendship.getId());
					    twitter.createFriendship(friendship.getScreenName());
					    addToDnf(friendship.getId());
					 } catch (Exception e) {
						 e.printStackTrace();
				     }
				    
			    	/*if (numberFollowed > 50) {
				    	break;
				    }*/
				    		
				    pauseBetweenFollows();
			    } else {
			    	//LOGGER.info("Not following " + id + " because that user is DNF");
			    }
			}
		}
	}

	
	/**
	 * Pauses between individual follows
	 */
	private void pauseBetweenFollows() {
		Random rand = new Random();
		int n = rand.nextInt(FOLLOW_PAUSE_SIZE) + FOLLOW_PAUSE_MIN;
	    		
		try {
			Thread.sleep(n);
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		}
	}
	
	
	/**
	 * Adds the given id to the DNF file so we don't try to follow him again
	 */
	private void addToDnf(long id) {
		try {
			PrintWriter f0 = new PrintWriter(new FileWriter(DNF_FILE,true));
			f0.println(""+id);
			f0.close();
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error("Problem writing ID to file!",e);
		}
	}

	
	/**
	 * Returns candidates for following
	 */
	private String[] getReturnedDudes(QueryResult result) throws TwitterException {
		String[] returnedDudes = new String[100];
		long[] blocks = twitter.getBlocksIDs().getIDs();

		int i = 0;
		
		for (Status status : result.getTweets()) {
			User thisUser = status.getUser();
	    	boolean isBlocked = false;
	    	for (long l : blocks) {
	    		if (l == thisUser.getId()) {
	    			isBlocked = true;
	    		}
	    	}
			
	    	if (!isBlocked) {
	    		returnedDudes[i] = thisUser.getScreenName();
			    i++;
			}
		}
		
		return returnedDudes;
	}


	/**
	 * Reads a list of do-not-follows from the file.
	 * That file will be appended with the IDs of people we try to follow here
	 * So we don't keep following the same person over and over
	 */
	private void fetchDnfs() {
		BufferedReader br = null;
		
	    try {
	    	br = new BufferedReader(new FileReader(DNF_FILE));
	        String line = br.readLine();

	        while (line != null) {
	        	if (!line.trim().equals("")) {
			        Long id = new Long(line);
			        dnfIds.add(id);
	        	}

	            line = br.readLine();
	        }
	    } catch (Exception e) {
	    	e.printStackTrace();
	    } finally {
	    	try {
	    		br.close();
	    	} catch (Exception e) {
	    		e.printStackTrace();
	    	}
	    }
	    	    
	    /*for (Long id : dnfIds) {
	    	LOGGER.debug(id);
	    }*/
	}
}

