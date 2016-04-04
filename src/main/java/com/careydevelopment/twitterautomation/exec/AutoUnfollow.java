package com.careydevelopment.twitterautomation.exec;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import com.careydevelopment.twitterautomation.config.MyTwitter;
import com.careydevelopment.twitterautomation.util.Constants;

import twitter4j.Friendship;
import twitter4j.IDs;
import twitter4j.ResponseList;
import twitter4j.Twitter;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import twitter4j.Friendship;
import twitter4j.IDs;
import twitter4j.RateLimitStatus;
import twitter4j.ResponseList;
import twitter4j.Twitter;
import twitter4j.User;

public class AutoUnfollow implements Constants {

	public static void main(String[] args) {
		PrintWriter writer = null;
		try {
			
			Twitter twitter = MyTwitter.instance().getTwitter();
			

				List<Long> allFollowees = new LinkedList<Long>();
				
				long cursor = -1; //1463043556452890873l;
				for (int i=0;i<2;i++) {
					IDs ids = twitter.getFriendsIDs(cursor);
					long longids[] = ids.getIDs();
					//System.err.println("Length of longids is " + longids.length);
					for (int j=0;j<longids.length;j++) {
						allFollowees.add(longids[j]);
					}
					cursor = ids.getNextCursor();
					//System.err.println("next cursor is " + cursor);
				}
				
				System.err.println("Allfolowees size is " + allFollowees.size());
				
				int length = 100;
				int offset = 0;
				int unfollowCount = 0;
				long[] realids = new long[length];
				boolean keepGoing = true;
				
				List<String> whiteList = getWhiteList();
				
				while (keepGoing) {			
					int arrayIndex = 0;
					for (int i=allFollowees.size()-(1+offset);i>allFollowees.size()-(length+1+offset);i--) {
						realids[arrayIndex] = allFollowees.get(i);
						arrayIndex++;
					}
					offset+=length;
						
					writer = new PrintWriter(new BufferedWriter(new FileWriter(DNF_FILE, true)));
					ResponseList<Friendship> friendships = null;
					try {
						friendships = twitter.lookupFriendships(realids);
					} catch (Exception e) {
						System.err.println("Taking a break for 15 minutes");
						Thread.sleep(1000*60*15);
						System.err.println("resuming");
					}
					
					if (friendships!=null) {
						for (Friendship fs : friendships) {
							//System.err.println(fs.getId() + " is following " + fs.isFollowedBy());
							if (!fs.isFollowedBy()) {
								User user  = twitter.showUser(fs.getId());
								if (!fs.isFollowedBy()) {
									System.err.println("writing " + fs.getId());
									if (!whiteList.contains(fs.getScreenName().toLowerCase())) {
										writer.println(fs.getId());
										twitter.destroyFriendship(fs.getId());
										Thread.sleep(6000);
									}
								}
								//System.err.println("user name is " + user.getScreenName()+ " " + fs.getId());
								//break;
								unfollowCount++;
								System.err.println("Unfollow count is at " + unfollowCount);
								if (unfollowCount>990) {
									keepGoing = false;
									break;
								}
							}
						}
					} else {
						System.err.println("friendships is null");
					}
					
					Map<String,RateLimitStatus> rateMap = twitter.getRateLimitStatus();
					RateLimitStatus rls = rateMap.get("/friendships/lookup");
					//System.err.println(rls.getLimit());
					//break;
					Thread.sleep(1000);
					/*for (String s : rateMap.keySet()) {
						RateLimitStatus rls1 = rateMap.get(s);
						System.err.println(s + " " + rls1.getRemaining() + " " + rls1.getResetTimeInSeconds());
					}*/
				}			
				

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}
	
	private static final List<String> getWhiteList() {
		List<String> whiteList = new ArrayList<String>();
		
		whiteList.add("jrsalzman");
		whiteList.add("kattimpf");
		whiteList.add("anthonybialy");
		whiteList.add("gabrielmalor");
		whiteList.add("gaypatriot");
		whiteList.add("lauren_southern");
		whiteList.add("prisonplanet");
		whiteList.add("metricbuttload ");
		whiteList.add("tarynonthenews");
		whiteList.add("jasonmattera");
		whiteList.add("anncoulter");
		whiteList.add("imao_");
		whiteList.add("esotericcd");
		whiteList.add("sistertoldjah");
		whiteList.add("redsteeze");
		whiteList.add("exjon");
		whiteList.add("blakeneff");
		whiteList.add("matthops82");
		whiteList.add("countermoonbat");
		whiteList.add("lilmissrightie");
		whiteList.add("justicewillett");
		whiteList.add("heminator");
		whiteList.add("cjoe15");
		whiteList.add("shannityshair");
		whiteList.add("gopteens");
		whiteList.add("baseballcrank");
		whiteList.add("benshapiro");
		whiteList.add("redsteeze");
		whiteList.add("leonhwolf");
		whiteList.add("ladylibertas76");
		whiteList.add("ag_conservative");
		whiteList.add("awrhawkins");
		whiteList.add("martian_munk");
		whiteList.add("biasedgirl");
		whiteList.add("rbpundit");
		whiteList.add("guypbenson");
		whiteList.add("charlescwcooke");
		whiteList.add("jonahnro");
		whiteList.add("richkaszak");
		whiteList.add("sargon_of_akkad");
		return whiteList;
	}


	
	
	/**
	 * Reads a list of people who we won't unfollow
	 */
	private static List<String> fetchWhitelist() {
		BufferedReader br = null;
		List<String> whiteList = new ArrayList<String>();

	    try {
	    	br = new BufferedReader(new FileReader(WHITELIST_FILE));
	        String line = br.readLine();

	        while (line != null) {
	        	if (!line.trim().equals("")) {
			        String id = line.trim();
			        whiteList.add(id);
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
	    
	    return whiteList;
	}
}