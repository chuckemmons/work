package com.cee.ljr.intg.fileparser.impl;

public enum DeveloperHeader {
	KEY("Jira Name"),
	FIRST_NAME("First Name"),
	LAST_NAME("Last Name"),
	PROJECT_KEY("Project");
	
	private String value;
	
	private DeveloperHeader(String value) {
		this.value = value;
	}
	
	@Override
	public String toString() {
		return this.value;
	}
}
