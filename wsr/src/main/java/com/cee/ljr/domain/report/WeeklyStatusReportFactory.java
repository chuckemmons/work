package com.cee.ljr.domain.report;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cee.ljr.domain.common.Developer;
import com.cee.ljr.domain.common.Epic;
import com.cee.ljr.domain.common.Project;
import com.cee.ljr.domain.common.Sprint;
import com.cee.ljr.domain.common.Story;
import com.cee.ljr.domain.common.Task;
import com.cee.ljr.intg.dao.DeveloperDao;
import com.cee.ljr.intg.jira.domain.IssueType;
import com.cee.ljr.intg.jira.domain.JiraIssue;
import com.cee.ljr.intg.jira.domain.JiraIssues;
import com.cee.ljr.intg.jira.domain.JiraIssuesFactory;
import com.cee.ljr.intg.mapping.JiraIssueMapper;
import com.cee.ljr.properties.ReportProperties;

@Component
public class WeeklyStatusReportFactory {
	private static final Logger log = LoggerFactory.getLogger(WeeklyStatusReportFactory.class);
	
	@Autowired 
	ReportProperties srProps;
	
	@Autowired
	DeveloperDao developerDao;
	
	@Autowired
	JiraIssuesFactory jiraIssuesFactory;
	
	@Autowired
	JiraIssueMapper jiraIssueMapper;
	
	public WeeklyStatusReport getWeeklyStatusReport(List<JiraIssue> jiraIssueList, Integer holidayDays, Date weekStartDate, Date weekEndingDate) {
		JiraIssues jiraIssues = jiraIssuesFactory.getJiraIssues(jiraIssueList);
		
		WeeklyStatusReport weeklyStatusReport = new WeeklyStatusReport(
				srProps.getWeeklyStatusReportTitle(), 
				srProps.getReportClassification(), 
				createSprint(), 
				createAuthor(),
				holidayDays,
				weekStartDate,
				weekEndingDate);	
		
		// iterate over tasks, bugs, and sub-tasks
		addTasksToStatusReport(jiraIssues, weeklyStatusReport);	
		addBugsToStatusReport(jiraIssues, weeklyStatusReport);
		addSubTasksToStatusReport(jiraIssues, weeklyStatusReport);
		
		return weeklyStatusReport;
	}
	
	private void addBugsToStatusReport(JiraIssues jiraIssues, WeeklyStatusReport weeklyStatusReport) {
		for (JiraIssue taskJiraIssue : jiraIssues.getTasks()) {
			String projectKey = taskJiraIssue.getProjectKey();
			Project project = weeklyStatusReport.getProject(projectKey);
			
			if (project == null) {
				project = createNewProject(taskJiraIssue, jiraIssues, weeklyStatusReport.getHolidayDays());
				weeklyStatusReport.addProject(project);
			} 
			else {
				addTaskToProject(taskJiraIssue, project, jiraIssues);			
			}
		}
	}
	
	private void addSubTasksToStatusReport(JiraIssues jiraIssues, WeeklyStatusReport weeklyStatusReport) {
		for (JiraIssue taskJiraIssue : jiraIssues.getSubTasks()) {
			String projectKey = taskJiraIssue.getProjectKey();
			Project project = weeklyStatusReport.getProject(projectKey);
			
			if (project == null) {
				project = createNewProject(taskJiraIssue, jiraIssues, weeklyStatusReport.getHolidayDays());
				weeklyStatusReport.addProject(project);
			} 
			else {
				addTaskToProject(taskJiraIssue, project, jiraIssues);			
			}
		}
	}
	
	private void addTasksToStatusReport(JiraIssues jiraIssues, WeeklyStatusReport weeklyStatusReport) {		
		for (JiraIssue taskJiraIssue : jiraIssues.getTasks()) {
			String projectKey = taskJiraIssue.getProjectKey();
			Project project = weeklyStatusReport.getProject(projectKey);
			
			if (project == null) {
				project = createNewProject(taskJiraIssue, jiraIssues, weeklyStatusReport.getHolidayDays());
				weeklyStatusReport.addProject(project);
			} 
			else {
				addTaskToProject(taskJiraIssue, project, jiraIssues);			
			}
		}
	}
	
		
	private void addTaskToProject(JiraIssue taskJiraIssue, Project project, JiraIssues jiraIssues) {
		String epicKey = taskJiraIssue.getEpicKey().trim();
		
		//log.debug("addTaskToProject epicKey = '{}'", epicKey);
		Epic epic = project.getEpic(epicKey);
		System.out.println("epicKey = [" + epicKey + "]");
		if (epic == null) {
			epic = createNewEpic(taskJiraIssue, jiraIssues);
			project.addEpic(epic);
		} 
		else {					
			addTaskToEpic(taskJiraIssue, epic, jiraIssues);
		}
	}
	
	private void addTaskToEpic(JiraIssue taskJiraIssue, Epic epic, JiraIssues jiraIssues) {
		String storyKey = jiraIssues.getStoryKey(taskJiraIssue.getKey());
		if (storyKey == null) {
			storyKey = "";
		}
		//log.debug("addTaskToEpic storyKey = '{}'", storyKey);
		Story story = epic.getStory(storyKey);
		if (story == null) {
			story = createNewStory(taskJiraIssue, jiraIssues);
			epic.addStory(story);
		}
		else {
			Task task = createNewTask(taskJiraIssue);
			story.addTask(task);
		}
	}
	
	private Project createNewProject(JiraIssue taskJiraIssue, JiraIssues jiraIssues, int holidayDays) {
		String issueType = taskJiraIssue.getType();
		if(!IssueType.TASK.equals(issueType)) {
			throw new IllegalArgumentException("JiraIssue is of type '" + issueType + ", must be a Task.");
		}
		String projectName = taskJiraIssue.getProjectName();
		String projectKey = taskJiraIssue.getProjectKey();
		List<Developer> developers = developerDao.getByProjectKey(projectKey);
		
		Project project = new Project(projectKey, projectName, developers, holidayDays);		
		
		Epic epic = createNewEpic(taskJiraIssue, jiraIssues);
		project.addEpic(epic);
		
		return project;
	}
	
	private Epic createNewEpic(JiraIssue taskJiraIssue, JiraIssues jiraIssues) {
		Epic epic = null;
		
		String epicKey = taskJiraIssue.getEpicKey();
		//log.debug("createNewEpic epicKey = '{}'", epicKey);
		System.out.println("task = " + taskJiraIssue.getKey());
		System.out.println("createNewEpic epicKey = [" + epicKey + "]");
		JiraIssue epicJiraIssue = jiraIssues.getEpic(epicKey);
		
		if (epicJiraIssue != null) {
			System.out.println("epicJiraIssue != null, creating epic from jira issue");
			epic = JiraIssueMapper.createEpic(epicJiraIssue);
		} 
		else {
			System.out.println("epicJiraIssue == null, creating new 'MISC' epic. ");
			epic = new Epic("Miscellaneous");
			epic.setKey("");
		}
		
		Story story = createNewStory(taskJiraIssue, jiraIssues);
		epic.addStory(story);
		
		return epic;
		
	}
	
	private Story createNewStory(JiraIssue taskJiraIssue, JiraIssues jiraIssues) {
		Story story = null;
		
		String taskKey = taskJiraIssue.getKey();
		JiraIssue storyJiraIssue = jiraIssues.getStoryByTaskKey(taskKey);
		
		if (storyJiraIssue != null) {
			story = jiraIssueMapper.createStory(storyJiraIssue);
		} else {
			story = new Story("Misc. Tasks");
			story.setKey("");
		}
		
		Task task = createNewTask(taskJiraIssue);
		//if (IssueType.BUG.equals(task.getType()))
		//log.debug("created story {} and added task {} where total hours worked = {}", story, task, task.getTotalHoursWorked());
		story.addTask(task);
		
		return story;
	}
	
	private Task createNewTask(JiraIssue taskJiraIssue) {
		return jiraIssueMapper.createTask(taskJiraIssue);		
	}
	
	private Author createAuthor() {
		return new Author(
				srProps.getAuthorName(), 
				srProps.getAuthorType(), 
				srProps.getAuthorTitle());
	}
	
	private Sprint createSprint() {
		return new Sprint("Sprint 0", new Date(), new Date()); //TODO: have to figure out how to inject sprint hours.
		/*return new Sprint(
				srProps.getSprintNumber(), 
				srProps.getSprintStartDate(), 
				srProps.getSprintEndDate());*/
	}
}
