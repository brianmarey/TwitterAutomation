package com.careydevelopment.twitterautomation.config;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

public class MyTwitter {
	
	public static final String BRIAN_M_CAREY = "brianmcarey";
	
	private static final String PROPERTIES_FILE_LOCATION = "c:/resources/twitter.properties";
	
	private static final MyTwitter INSTANCE = new MyTwitter();
	
	private Map<String,Account> accounts = new HashMap<String,Account>();
	private Map<String,Twitter> twitters = new HashMap<String,Twitter>();
	
	/**
	 * Create it as a singleton
	 */
	public static final MyTwitter instance() {
		return INSTANCE;
	}

	
	/**
	 * private constructor
	 */
	private MyTwitter() {
		loadAccounts();
	}
	
	/**
	 * load the Twitter accounts - only 1 right now
	 */
	private void loadAccounts() {
		Properties props = getProperties();
		String consumerKey = props.getProperty("brianmcarey.consumerKey");
		String consumerSecret = props.getProperty("brianmcarey.consumerSecret");
		String accessToken = props.getProperty("brianmcarey.accessToken");
		String accessSecret = props.getProperty("brianmcarey.accessSecret");
		
		Account bmc = new Account(BRIAN_M_CAREY,consumerKey,consumerSecret,accessToken,accessSecret);
		//Account(String name, String consumerKey, String consumerSecret, String accessToken, String accessTokenSecret)
		
		accounts.put(BRIAN_M_CAREY, bmc);
	}
	
	
	/**
	 * load the properties file containing Twitter credentials
	 */
	private Properties getProperties() {
		Properties props = new Properties();
		
		try {
			File file = new File(PROPERTIES_FILE_LOCATION);
			FileInputStream fis = new FileInputStream(file);
			props.load(fis);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException ("Problem reading properties file!");
		}
		
		return props;
	}

	/**
	 * Default getter for the brianmcarey account
	 */
	public Twitter getTwitter() {
		String twitterName = BRIAN_M_CAREY;
		Twitter thisTwitter = twitters.get(twitterName);
		if (thisTwitter == null) {
			//System.err.println("Getting twitter");
			Account account = accounts.get(twitterName);
			
			if (account != null) {
				ConfigurationBuilder cb = new ConfigurationBuilder();
				cb.setDebugEnabled(true)
				  .setOAuthConsumerKey(account.getConsumerKey())
				  .setOAuthConsumerSecret(account.getConsumerSecret())
				  .setOAuthAccessToken(account.getAccessToken())
				  .setOAuthAccessTokenSecret(account.getAccessTokenSecret());
				
				TwitterFactory tf = new TwitterFactory(cb.build());
				thisTwitter = tf.getInstance();
				twitters.put(twitterName, thisTwitter);
			}
		} 
		
		return thisTwitter;
	}
	

	/**
	 * Gets the Twitter object based on provided name
	 * There should be a name that matches a key in the twitters hashmap
	 */
	public Twitter getTwitter(String name) {
		Twitter thisTwitter = twitters.get(name);
		if (thisTwitter == null) {
			Account account = accounts.get(name);
			if (account != null) {
				ConfigurationBuilder cb = new ConfigurationBuilder();
				cb.setDebugEnabled(true)
				  .setOAuthConsumerKey(account.getConsumerKey())
				  .setOAuthConsumerSecret(account.getConsumerSecret())
				  .setOAuthAccessToken(account.getAccessToken())
				  .setOAuthAccessTokenSecret(account.getAccessTokenSecret());
				
				TwitterFactory tf = new TwitterFactory(cb.build());
				thisTwitter = tf.getInstance();
				twitters.put(name, thisTwitter);
			}
		} 
		
		return thisTwitter;
	}

	
	public Set<String> getAvailableAccounts() {
		return accounts.keySet();
	}
	
	/**
	 * Inner class only used by this singleton
	 */
	class Account {
		private String name;
		private String consumerKey;
		private String consumerSecret;
		private String accessToken;
		private String accessTokenSecret;
		
		public Account(String name, String consumerKey, String consumerSecret, String accessToken, String accessTokenSecret) {
			this.name = name;
			this.consumerKey = consumerKey;
			this.consumerSecret = consumerSecret;
			this.accessToken = accessToken;
			this.accessTokenSecret = accessTokenSecret;
		}
		
		public String getConsumerKey() {
			return consumerKey;
		}
		
		public String getConsumerSecret() {
			return consumerSecret;
		}
		
		public String getAccessToken() {
			return accessToken;
		}
		
		public String getAccessTokenSecret() {
			return accessTokenSecret;
		}
		
		public String getName() {
			return name;
		}
	}
}

