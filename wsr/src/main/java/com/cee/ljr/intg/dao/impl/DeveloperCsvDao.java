package com.cee.ljr.intg.dao.impl;

import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cee.ljr.domain.common.Developer;
import com.cee.ljr.intg.dao.DeveloperDao;
import com.cee.ljr.intg.fileparser.DeveloperFileParser;

@Component
public class DeveloperCsvDao implements DeveloperDao {
	
	@Autowired
	private DeveloperFileParser fileParser;
	
	@Override
	public List<Developer> getAll() {
		return fileParser.parseAll();
	}
	
	@Override
	public Developer getByNameInJira(String nameInJira) {
		return fileParser.parseForName(nameInJira);
	}
	
	@Override
	public List<Developer> getByKeys(Collection<String> keys) {
		return fileParser.parseByKeys(keys);
	}
	
	@Override
	public List<Developer> getByProjectKey(String projectKey) {
		return fileParser.parseByProjectKey(projectKey);
	}
}
