package com.cee.ljr.intg.fileparser;

import java.util.Collection;
import java.util.List;

import com.cee.ljr.domain.common.Developer;

public interface DeveloperFileParser {
	
	List<Developer> parseAll();
	
	Developer parseForName(String nameInJira);
	
	List<Developer> parseByKeys(Collection<String> keys);
	
	List<Developer> parseByProjectKey(String projectKey);
}
