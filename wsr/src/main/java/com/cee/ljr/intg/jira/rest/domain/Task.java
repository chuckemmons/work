package com.cee.ljr.intg.jira.rest.domain;

import java.io.Serializable;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
public class Task implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4041647267119807325L;
	private String key;
	
	public Task() {
	}
	
	

	/**
	 * @return the key
	 */
	public String getKey() {
		return key;
	}



	/**
	 * @param key the key to set
	 */
	public void setKey(String key) {
		this.key = key;
	}



	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
