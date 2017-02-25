package com.cee.ljr.report.generator;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cee.ljr.document.generator.AllComsWsrXlsxGenerator;
import com.cee.ljr.document.generator.WeeklyStatusReportDocxGenerator;
import com.cee.ljr.document.generator.WeeklyStatusReportXlsxGenerator;
import com.cee.ljr.domain.report.WeeklyStatusReport;
import com.cee.ljr.service.WeeklyStatusReportService;
import com.cee.ljr.utils.DateUtil;

@Component
public class WeeklyStatusReportGenerator {
	private static final Logger log = LoggerFactory.getLogger(WeeklyStatusReportGenerator.class);
	
	@Autowired
	private WeeklyStatusReportService srService;
	@Autowired
	private WeeklyStatusReportDocxGenerator weeklyStatusReportDocxGenerator;
	@Autowired
	private WeeklyStatusReportXlsxGenerator weeklyStatusReportXlsxGenerator;
	@Autowired
	private AllComsWsrXlsxGenerator allComsWsrXlsxGenerator;
	
	public void generateV1(String weekEndingDateStr, String jiraFilePaths) {
		Date weekStartDate = DateUtil.getWeekStartDate(weekEndingDateStr);
		Date weekEndingDate = DateUtil.getWeekEndingDate(weekEndingDateStr);
		
		WeeklyStatusReport weeklyStatusReport = srService.getWeeklyStatusReport(jiraFilePaths, 0, weekStartDate, weekEndingDate);
		
		weeklyStatusReportDocxGenerator.generateDocument(weeklyStatusReport);
	}
	
	
	public void generateV2(String weekEndingDateStr, String reportPath, String jiraFilePaths){
		Date weekStartDate = DateUtil.getWeekStartDate(weekEndingDateStr);
		Date weekEndingDate = DateUtil.getWeekEndingDate(weekEndingDateStr);
		
		WeeklyStatusReport weeklyStatusReport = srService.getWeeklyStatusReport(jiraFilePaths, 0, weekStartDate, weekEndingDate);
		
		weeklyStatusReportXlsxGenerator.generateWsrDocument(weeklyStatusReport, reportPath);
	}
	
	public void generateV3(String numberOfHolidays, String weekEndingDateStr, String reportPath, String jiraFilePaths) {
		Date weekStartDate = DateUtil.getWeekStartDate(weekEndingDateStr);
		Date weekEndingDate = DateUtil.getWeekEndingDate(weekEndingDateStr);
		int holidaysInt = Integer.parseInt(numberOfHolidays);
		WeeklyStatusReport weeklyStatusReport = srService.getWeeklyStatusReport(jiraFilePaths, holidaysInt, weekStartDate, weekEndingDate);
		
		allComsWsrXlsxGenerator.generateWsrDocument(weeklyStatusReport, reportPath);
	}
}
