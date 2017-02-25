package com.cee.ljr.intg.fileparser.impl;

import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.cee.file.csv.CSVRecord;
import com.cee.file.csv.criteria.Criteria;
import com.cee.file.csv.criteria.condition.Condition;
import com.cee.ljr.domain.common.Developer;
import com.cee.ljr.intg.fileparser.DeveloperFileParser;
import com.cee.ljr.intg.mapping.DeveloperMapper;

@Component
public class DeveloperCsvFileParser extends BaseCsvFileParser<Developer> implements DeveloperFileParser {
	
	private static final boolean WITH_HEADER = true;
	
	@Value("${developers.url}")
	String filePath;
	
	@Autowired
	DeveloperMapper mapper;
	
	
	public DeveloperCsvFileParser() {
		super(WITH_HEADER);
	}
	
	
	@Override
	public List<Developer> parseAll() {
		return parseForAll(filePath);
	}
	
	
	@Override
	public Developer parseForName(String key) {		
		if (key == null) {
			throw new IllegalArgumentException("nameInJira must not be null.");
		}
		
		Criteria criteria = new Criteria(
				Condition.eq(DeveloperHeader.KEY, key)
		);
		
		return parseForSingle(filePath, criteria);
	}

	
	@Override
	public List<Developer> parseByKeys(Collection<String> keys) {		
		
		Criteria criteria = new Criteria(
				Condition.containsOne(DeveloperHeader.KEY, keys)
		);
		
		return parseForAll(filePath, criteria);
	}
	
	@Override
	public List<Developer> parseByProjectKey(String projectKey) {
		
		Criteria criteria = new Criteria(
			Condition.eq(DeveloperHeader.PROJECT_KEY, projectKey)
		);
		
		return parseForAll(filePath, criteria);
	}

	
	@Override
	protected List<Developer> parseForAll(String filePaths, Criteria criteria) {
		
		Collection<CSVRecord> records = parseForAllRecords(filePaths, criteria);
		
		List<Developer> developers = mapper.map(records);
		
		return developers;
	}

	
	@Override
	protected List<Developer> parseForAll(String filePaths) {
		
		Collection<CSVRecord> records = parseForAllRecords(filePaths);
		
		List<Developer> developers = mapper.map(records);
		
		return developers;
	}

	
	@Override
	protected Developer parseForSingle(String filePaths, Criteria criteria) {
		
		CSVRecord record = parseForSingleRecord(filePaths, criteria);
		
		Developer developer = null;	
		
		if (record != null) {
			developer = mapper.map(record);
		}
		
		return developer;
	}
	
	
}
