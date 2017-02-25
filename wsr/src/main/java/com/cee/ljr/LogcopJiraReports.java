package com.cee.ljr;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.cee.ljr.config.ApplicationConfig;
import com.cee.ljr.report.generator.WeeklyStatusReportGenerator;

public class LogcopJiraReports {
	
	public static void main(String[] args) {
		@SuppressWarnings("resource")
		ApplicationContext ctx = new AnnotationConfigApplicationContext(ApplicationConfig.class);
		
		// weekly status report generation
		String holidays = "0";
		String weekEndDate = "01/29/2017";
		String pathToReports = null;
		String pathToJiraCsvs = "C:/wsr_dev/JIRA1.csv;C:/wsr_dev/JIRA2.csv;C:/wsr_dev/JIRA3.csv;C:/wsr_dev/JIRA4.csv";
		
		WeeklyStatusReportGenerator wsrGenerator = ctx.getBean(WeeklyStatusReportGenerator.class);
		wsrGenerator.generateV3(holidays, weekEndDate, pathToReports, pathToJiraCsvs);
		//wsrGenerator.generateV2(weekEndDate, pathToReports, pathToJiraCsvs);
		
		//int sprintNumber = 10;
		//DeveloperSprintReportGenerator dsrGenerator = ctx.getBean(DeveloperSprintReportGenerator.class);
		//dsrGenerator.generateAll(sprintNumber, pathToReports, pathToJiraCsvs);
		
	}
	
	

}
