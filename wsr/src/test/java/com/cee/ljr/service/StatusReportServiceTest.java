package com.cee.ljr.service;

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.cee.ljr.config.ApplicationConfig;
import com.cee.ljr.domain.report.WeeklyStatusReport;
import com.cee.ljr.test.BaseTest;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ApplicationConfig.class)
public class StatusReportServiceTest extends BaseTest {
	Logger log = LoggerFactory.getLogger(StatusReportServiceTest.class);
	
	@Autowired
	WeeklyStatusReportService srService;
	
	@Test
	public void testGetStatusReport() {
		WeeklyStatusReport report = srService.getWeeklyStatusReport(xlsxPaths, 0, new Date(), new Date());
		
		Assert.assertNotNull(report);
		//log.debug(report.logStats());
	}
}
