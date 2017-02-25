package com.cee.ljr.domain.common;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

public class Epic extends BaseIssue {
	private static final Logger log = LoggerFactory.getLogger(Epic.class);	
	private String name;
	private double hoursWorkedBetween;
	private double totalHoursWorked;
	
	private Map<String, Story> keyToStoryMap = new HashMap<String, Story>(); 
	
	
	public static final String TIME_OFF_EPIC_KEY = "Time Off";
	
	public Epic(String name) {
		this.name = name;
	}
	
	public Epic(String key, String name) {
		this.key = key;
		this.name = name;
	}
	
	@Override
	public double getTotalHoursWorked() {
		if (totalHoursWorked <=0 ) {
			////log.debug("totalHoursWorked <=0");
			for (Story story : keyToStoryMap.values()) {
				totalHoursWorked += story.getTotalHoursWorked();
			}
		}
		return totalHoursWorked;
	}
	
	@Override
	public double getHoursWorkedBetween(Date startDate, Date endDate) {
		//log.debug("getting hours worked between {} and {}", startDate, endDate);
		if (hoursWorkedBetween <=0 ) {
			//log.debug("\thoursWorkedBetween <=0");
			for (Story story : keyToStoryMap.values()) {
				double storyHours = story.getHoursWorkedBetween(startDate, endDate);
				//log.debug("\tadding storyHours: {}", storyHours);
				hoursWorkedBetween += storyHours;
			}
		}
		//log.debug("returning hoursWorkedBetween: {}", hoursWorkedBetween);
		return hoursWorkedBetween;
	}
	
	public double getTimeSpentInHours() {
		double timeSpent = 0;
		for (Story story : keyToStoryMap.values()) {
			timeSpent += story.getTimeSpentInHours();
		}
		return timeSpent;
	}
	
	public void addStory(Story story) {
		if (story == null) {
			throw new IllegalArgumentException("Story cannot be null.");
		}
		String storyKey = story.getKey();
		////log.debug("addStory storyKey= '{}'", storyKey);
		if (storyKey == null) {
			throw new RuntimeException("Story must contain a key.");
		}
		if (keyToStoryMap.containsKey(storyKey)) {
			throw new RuntimeException("Story " + storyKey + " already exists in Epic " + getKey());
		}
		if (CollectionUtils.isEmpty(keyToStoryMap)) {
			keyToStoryMap = new HashMap<String, Story>();
		}
		//log.debug("keyToStoryMap.put(\n{},\n{})", storyKey, story);
		keyToStoryMap.put(storyKey, story);
		
	}
	
	public Story getStory(String storyKey) {
		return keyToStoryMap.get(storyKey);
	}
	
	public Collection<Story> getStories() {
		return keyToStoryMap.values();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	
}
