package com.cee.ljr.intg.jira.rest;

import org.apache.commons.codec.binary.Base64;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;

import com.cee.ljr.config.ApplicationConfig;
import com.cee.ljr.intg.jira.rest.domain.Task;
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ApplicationConfig.class)
public class JiraRestServiceTest {
	private static final Logger log = LoggerFactory.getLogger(JiraRestServiceTest.class);
	private static final String pacaf403TaskIssueUrl = "https://logcop.atlassian.net/rest/api/2/issue/PACAF-403";
	private static final String plainCreds = "charles.e.emmons.ctr@navy.mil:logcop@2016!";
	
	@Test
	public void testConnection() {	
		RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = getHttpHeaders(plainCreds);
		HttpEntity<String> request = new HttpEntity<String>(headers);
		ResponseEntity<Task> response = restTemplate.exchange(pacaf403TaskIssueUrl, HttpMethod.GET, request, Task.class);
		Task task = response.getBody();
        log.debug(task.toString());
	}
	
	private String getBase64Credentials(String credentials) {		
		byte[] plainCredsBytes = credentials.getBytes();
		byte[] base64CredsBytes = Base64.encodeBase64(plainCredsBytes);
		String base64Creds = new String(base64CredsBytes);
		return base64Creds;
	}
	
	private HttpHeaders getHttpHeaders(String credentials) {
		String base64Creds = getBase64Credentials(credentials);
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Basic " + base64Creds);
		headers.add("contentType", "application/json");
		return headers;
	}

}
