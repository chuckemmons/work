package com.cee.ljr.document.generator;

import java.awt.Color;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.cee.ljr.domain.common.Epic;
import com.cee.ljr.domain.common.Project;
import com.cee.ljr.domain.common.Story;
import com.cee.ljr.domain.common.Task;
import com.cee.ljr.domain.report.WeeklyStatusReport;
import com.cee.ljr.properties.ReportProperties;
import com.cee.ljr.utils.FileUtil;

@Component
public class AllComsWsrXlsxGenerator {

		private static final Logger log = LoggerFactory.getLogger(AllComsWsrXlsxGenerator.class);
		
		public static final DateFormat weekEndingDateFormater = new SimpleDateFormat("yyyyMMdd");
		
		@Autowired
		ReportProperties props;
		
		// Project related layout and styling
		private static final int projectColIndex = 0;
		private static final int projectWorkLogColIndex =0;
		private static final int projectOverheadColIndex = 1;
		private static final int projectLeaveColIndex = 2;
		private static final XSSFColor projectFillColor = new XSSFColor(new Color(112,112,112));
		private static final XSSFColor projectFontColor = new XSSFColor(new Color(255,255,255));

		// Epic related layout and styling
		private static final int epicColIndex = 0;
		private static final XSSFColor epicFillColor = new XSSFColor(new Color(211,211,211));
		private static final XSSFColor epicFontColor = new XSSFColor(new Color(0,0,0));

		// Story related layout and styling
		private static final int storyColIndex = 0;
		private static final XSSFColor storyFontColor = new XSSFColor(new Color(0,0,0));
		private static final XSSFColor storyFillColor = new XSSFColor(new Color(236,237,225));

		// Task related layout
		private static final int taskSummaryColIndex = 0;
		
		// Weekly totals layout
		private static final int weeklyTotalLabelColIndex = 3;
		private static final int weeklyTotalValueColIndex = 4;

		// Cell styles
		private static XSSFCellStyle  projectCellStyle;	
		private static XSSFCellStyle projectHoursCellStyle;
		private static XSSFCellStyle projectTotalHoursCellStyle;
		
		private static XSSFCellStyle epicCellStyle;
		private static XSSFCellStyle epicHoursCellStyle;
		
		private static XSSFCellStyle storyCellStyle;
		private static XSSFCellStyle storyHoursCellStyle;
		
		private static XSSFCellStyle taskCellStyle;
		private static XSSFCellStyle taskHoursCellStyle;
		
		private static DataFormat dataFormat;
		private static short hoursDataFormat;
		
		/**
		 * Generates the Weekly Status Report spreadsheet from the given report and report path.
		 * @param weeklyStatusReport The report used to generate the document.
		 * @param reportPath The path to save the document to.<br/>
		 * <code>null</code> value will default to the "default.report.path" in the weekly-status-report.properties.
		 */
		public void generateWsrDocument(WeeklyStatusReport weeklyStatusReport, String reportPath) {		
			if (weeklyStatusReport == null) {
				throw new IllegalArgumentException("statusReport must not be null.");
			}
			if (CollectionUtils.isEmpty(weeklyStatusReport.getProjects())) {
				log.error("statusReport must contain at least one project");
				return;
			}
			
			if (weeklyStatusReport.getHoursWorkedBetween() <= 0) {
				log.info("Unable to create report, no hours were logged between {} and {}.", 
				weeklyStatusReport.getWeekStartDate(), weeklyStatusReport.getWeekEndingDate());			
			}
			
			if (reportPath != null && !FileUtil.isValidPath(reportPath)) {
				log.error("report path {} is not a valid path.", reportPath);
				return;
			}
			
			generateSpreadsheet(weeklyStatusReport, reportPath);		
		}
		
		private String buildFilePath(String reportPath, Date weekEndingDate) {
			if (reportPath == null) {
				reportPath = props.getReportPath();
			}
			if (!FileUtil.isValidPath(reportPath)) {
				log.error("report path [{}] is invalid!", reportPath);
				return null;
			}
			String documentName = generateReportName(weekEndingDate);
			
			return Paths.get(reportPath, documentName).toAbsolutePath().toString();
			
		}
		
		private String generateReportName(Date weekEndingDate) {
			String dateString = weekEndingDateFormater.format(weekEndingDate);
			
			return new StringBuilder()
						.append(props.getWeeklyStatusReportTitle())
						.append(" WE ")
						.append(dateString)
						.append(".xlsx")
						.toString();
		}
			
		/**
		 * Generates a Weekly Status Report Document from the given object to the given path.
		 * @param weeklyStatusReport
		 * @param reportPath
		 */
		private void generateSpreadsheet(WeeklyStatusReport weeklyStatusReport, String reportPath) {
			log.info("{} generating.", props.getWeeklyStatusReportTitle());
			
			XSSFWorkbook wb = new XSSFWorkbook();
			
			Date startDate = weeklyStatusReport.getWeekStartDate();
			Date endDate = weeklyStatusReport.getWeekEndingDate();
			
			initializeCellStyles(wb);
			
			for (Project project : weeklyStatusReport.getProjects()) {
				double workLoggedHours = project.getHoursWorkedBetween(startDate, endDate);
				//log.debug("Weekly hours worked for project: " + hoursWorkedForWeek);
				if (workLoggedHours > 0) {
					addProject(wb, project, startDate, endDate);
				}
			}
			
			String filePath = buildFilePath(reportPath, weeklyStatusReport.getWeekEndingDate());
			
			FileOutputStream fileOut = null;
			try {
				
				fileOut = new FileOutputStream(filePath);
		        wb.write(fileOut);
		        fileOut.close();
		        log.info("{} saved to {}.", props.getWeeklyStatusReportTitle(), filePath);
			}
			catch (IOException ioe) {
				log.error("Unable to create new workbook.", ioe);
			}
		}
		
		private void initializeCellStyles(XSSFWorkbook  wb) {
			initializeHoursDataFormat(wb);
			initializeProjectStyles(wb);
			initializeEpicStyles(wb);
			initializeStoryStyles(wb);
			initializeTaskStyles(wb);
			initializeStoryTotalsStyles(wb);
		}
		
		private void initializeHoursDataFormat(XSSFWorkbook  wb) {
			dataFormat = wb.createDataFormat();
			hoursDataFormat = dataFormat.getFormat("#,##0.00");		
		}
		
		private void initializeStoryTotalsStyles(XSSFWorkbook wb) {
			storyHoursCellStyle = wb.createCellStyle();
			storyHoursCellStyle.setBorderTop(BorderStyle.MEDIUM);
			storyHoursCellStyle.setDataFormat(hoursDataFormat);
			//storyHoursCellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
			//storyHoursCellStyle.setDataFormat(hoursDataFormat);
			
			//XSSFFont storyHoursFont = wb.createFont();
			//storyHoursFont.setColor(storyFontColor);
			//storyHoursFont.setBold(true);
			//storyHoursFont.setFontName("Calibri");
			//storyHoursFont.setFontHeightInPoints((short) 11);
			//storyHoursCellStyle.setFont(storyHoursFont);
			
		}
		
		private void initializeTaskStyles(XSSFWorkbook  wb){
			taskCellStyle = wb.createCellStyle();
			//taskCellStyle.setFillForegroundColor(IndexedColors.SKY_BLUE.getIndex());
			//taskCellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
			
			taskHoursCellStyle = wb.createCellStyle();
			//taskHoursCellStyle.setFillForegroundColor(IndexedColors.SKY_BLUE.getIndex());
			//taskHoursCellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
			taskHoursCellStyle.setDataFormat(hoursDataFormat);
		}
		
		private void initializeStoryStyles(XSSFWorkbook  wb){
			storyCellStyle = wb.createCellStyle();
			storyCellStyle.setFillForegroundColor(storyFillColor);
			storyCellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
			//storyCellStyle.setAlignment(storyHorizontalAlignment);
			
			XSSFFont storyFont = wb.createFont();
			storyFont.setColor(storyFontColor);
			storyFont.setBold(true);
			storyFont.setFontName("Cambria");
			storyFont.setFontHeightInPoints((short) 11);
			storyCellStyle.setFont(storyFont);
			
			//storyHoursCellStyle = wb.createCellStyle();
			//storyHoursCellStyle.setFillForegroundColor(storyFillColor);
			//storyHoursCellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
			//storyHoursCellStyle.setDataFormat(hoursDataFormat);
			
			//XSSFFont storyHoursFont = wb.createFont();
			//storyHoursFont.setColor(storyFontColor);
			//storyHoursFont.setBold(true);
			//storyHoursFont.setFontName("Calibri");
			//storyHoursFont.setFontHeightInPoints((short) 11);
			//storyHoursCellStyle.setFont(storyHoursFont);
		}
		
		private void initializeEpicStyles(XSSFWorkbook  wb) {
			epicCellStyle = wb.createCellStyle();
			epicCellStyle.setFillForegroundColor(epicFillColor);
			epicCellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
			
			XSSFFont epicFont = wb.createFont();
			epicFont.setColor(epicFontColor);
			epicFont.setBold(true);
			epicFont.setFontName("Cambria");
			epicFont.setFontHeightInPoints((short) 16);
			epicCellStyle.setFont(epicFont);
			
			epicHoursCellStyle = wb.createCellStyle();
			epicHoursCellStyle.setFillForegroundColor(epicFillColor);
			epicHoursCellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
			epicHoursCellStyle.setDataFormat(hoursDataFormat);
			
			XSSFFont epicHoursFont = wb.createFont();
			epicHoursFont.setColor(epicFontColor);
			epicHoursFont.setBold(true);
			epicHoursFont.setFontName("Calibri");
			epicHoursFont.setFontHeightInPoints((short) 11);
			epicHoursCellStyle.setFont(epicHoursFont);
		}
		
		private void initializeProjectStyles(XSSFWorkbook  wb) {
			projectCellStyle = wb.createCellStyle();
			projectCellStyle.setFillForegroundColor(projectFillColor);
			projectCellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
			projectCellStyle.setAlignment(HorizontalAlignment.CENTER);
			
			XSSFFont projectFont = wb.createFont();
			projectFont.setColor(projectFontColor);
			projectFont.setBold(true);
			projectFont.setFontName("Cambria");
			projectFont.setFontHeightInPoints((short) 20);
			projectCellStyle.setFont(projectFont);
			
			
			projectHoursCellStyle = wb.createCellStyle();
			projectHoursCellStyle.setFillForegroundColor(projectFillColor);
			projectHoursCellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
			projectHoursCellStyle.setDataFormat(hoursDataFormat);
			
			XSSFFont projectHoursFont = wb.createFont();
			projectHoursFont.setColor(projectFontColor);
			projectHoursFont.setBold(true);
			projectHoursFont.setFontName("Calibri");
			projectHoursFont.setFontHeightInPoints((short) 12);
			projectHoursCellStyle.setFont(projectHoursFont);
			
			projectTotalHoursCellStyle = wb.createCellStyle();
			projectTotalHoursCellStyle.setFillForegroundColor(projectFillColor);
			projectTotalHoursCellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
			projectTotalHoursCellStyle.setDataFormat(hoursDataFormat);
			
			XSSFFont projectTotalHoursFont = wb.createFont();
			projectTotalHoursFont.setColor(projectFontColor);
			projectTotalHoursFont.setBold(true);
			projectTotalHoursFont.setFontName("Calibri");
			projectTotalHoursFont.setFontHeightInPoints((short) 14);
			projectTotalHoursCellStyle.setFont(projectTotalHoursFont);
		}
		
		private void addProject(XSSFWorkbook  wb, Project project, Date startDate, Date endDate) {
			Sheet sheet = wb.createSheet(project.getName());
			MutableInt nextRowIndex = new MutableInt(0);
			
			addProjectHeader(sheet, nextRowIndex, project);
			nextRowIndex.add(1);
			
			addProjectTotals(sheet, nextRowIndex, project, startDate, endDate);
			nextRowIndex.add(1);
			
			addProjectBottomRow(sheet, nextRowIndex);
			
			//addWeeklyTotal(row, project.getHoursWorkedBetween(startDate, endDate), projectHoursCellStyle, projectHoursCellStyle, true);
			int epicNumber = 1;
			for (Epic epic : project.getEpics()) {
				double hoursWorkedForWeek = epic.getHoursWorkedBetween(startDate, endDate);
				//log.debug("Weekly hours worked for epic: " + hoursWorkedForWeek);
				if (hoursWorkedForWeek > 0) {
					nextRowIndex.add(1);
					addEpic(wb, sheet, nextRowIndex, epicNumber++, epic, startDate, endDate);
				}
			}
			// have to autosize after sheet is populated
			autosizeSheet(sheet); 
		}
		
		private void addProjectBottomRow(Sheet sheet, MutableInt nextRowIndex) {
			int rowIndex = nextRowIndex.intValue();
			Row row = sheet.createRow(rowIndex);
			Cell cell = row.createCell(projectColIndex);
			// below is temp to get it to run...
			cell.setCellValue(" ");
			cell.setCellStyle(projectHoursCellStyle);
			mergeCellsInRow(sheet, rowIndex, projectColIndex, weeklyTotalValueColIndex);
		}
		
		private void addProjectTotals(Sheet sheet, MutableInt nextRowIndex, Project project, Date startDate, Date endDate) {
			int rowIndex = nextRowIndex.intValue();
			Row row = sheet.createRow(rowIndex);
			Cell cell = row.createCell(projectWorkLogColIndex);
			// create Work Logged
			cell.setCellValue("Work Logged: " + project.getHoursWorkedBetween(startDate, endDate)); 
			cell.setCellStyle(projectHoursCellStyle);			
			// create Email/Meetings/Admin
			cell = row.createCell(projectOverheadColIndex);
			cell.setCellValue("Email/Meetings/Admin: " + "<0.00>");
			cell.setCellStyle(projectHoursCellStyle);
			// create Leave
			cell = row.createCell(projectLeaveColIndex);
			cell.setCellValue("Leave: " + "<0.00>");
			cell.setCellStyle(projectHoursCellStyle);
			// create Total
			cell = row.createCell(weeklyTotalLabelColIndex);
			cell.setCellValue("Total:");
			cell.setCellStyle(projectTotalHoursCellStyle);
			cell.getCellStyle().setAlignment(CellStyle.ALIGN_RIGHT);
			cell = row.createCell(weeklyTotalValueColIndex);
			cell.setCellValue("<0.00>");
			cell.setCellStyle(projectTotalHoursCellStyle);
		}
		
		private void addProjectHeader(Sheet sheet, MutableInt nextRowIndex, Project project) {
			int rowIndex = nextRowIndex.intValue();
			Row row = sheet.createRow(rowIndex);
			Cell cell = row.createCell(projectColIndex);
			cell.setCellValue(project.getName());
			cell.setCellStyle(projectCellStyle);
			
			mergeCellsInRow(sheet, rowIndex, projectColIndex, weeklyTotalValueColIndex);
		}
		
		private void autosizeSheet(Sheet sheet) {
			int numberOfColumns = getLastColumnNumber(sheet);
			
			for (int i = 0; i < numberOfColumns; i++) {
				sheet.autoSizeColumn(i);
			}
		}
		
		private int getLastColumnNumber(Sheet sheet) {
			int maxColumnNum = -1;
			int lastRowIndex = sheet.getLastRowNum();
			
			for (int i = 0; i <= lastRowIndex; i++) {
				Row row = sheet.getRow(i);
				// helps in debugging when some changes are made...
				//System.out.println("row index: " + i);
				//System.out.println("first cell in row value" + row.getCell(0).getStringCellValue());
				short lastColumnNum = row.getLastCellNum();
				if (lastColumnNum > maxColumnNum) {
					maxColumnNum = lastColumnNum;
				}
			}
			return maxColumnNum;
		}
		
		private void addEpic(XSSFWorkbook  wb, Sheet sheet, MutableInt nextRowIndex, int epicNumber, Epic epic, Date startDate, Date endDate) {
			int rowIndex = nextRowIndex.intValue();
			Row row = sheet.createRow(rowIndex);
			Cell cell = row.createCell(epicColIndex);
			cell.setCellValue(epicNumber + ". " + epic.getName());
			cell.setCellStyle(epicCellStyle);
			
			mergeCellsInRow(sheet, rowIndex, epicColIndex, weeklyTotalLabelColIndex - 1);		
			
			//addRunningTotal(row, epic.getTotalHoursWorked(), epicHoursCellStyle, epicHoursCellStyle, true);
			addWeeklyTotal(row, epic.getHoursWorkedBetween(startDate, endDate), epicHoursCellStyle, epicHoursCellStyle, true);
			
			for (Story story : epic.getStories()) {
				double hoursWorkedForWeek = story.getHoursWorkedBetween(startDate, endDate);
				//log.debug("Weekly hours worked for story: " + hoursWorkedForWeek);
				if (hoursWorkedForWeek > 0) {
					nextRowIndex.add(1);
					addStory(wb, sheet, nextRowIndex, story, startDate, endDate);
				}
			}
		}
		
		private void addStory(XSSFWorkbook  wb, Sheet sheet, MutableInt nextRowIndex, Story story, Date startDate, Date endDate) {
			int storyRowIndex = nextRowIndex.intValue();
			
			Row row = sheet.createRow(storyRowIndex);
			Cell cell = row.createCell(storyColIndex);
			cell.setCellStyle(storyCellStyle);
			cell.setCellValue(story.getName());
			
			mergeCellsInRow(sheet, storyRowIndex, storyColIndex, weeklyTotalValueColIndex);		
			//mergeCellsInRow(sheet, storyRowIndex, storyColIndex, mergedColIndex);		
			
			//addRunningTotal(row, story.getTotalHoursWorked(), storyHoursCellStyle, storyHoursCellStyle, true);			
			
			for (Task task : story.getTasks()) {
				double hoursWorkedForWeek = task.getHoursWorkedBetween(startDate, endDate);
				//log.debug("Weekly hours worked for task: " + hoursWorkedForWeek);
				if (hoursWorkedForWeek > 0) {
					nextRowIndex.add(1);
					addTask(sheet, nextRowIndex, task, startDate, endDate);
				}
			}
			nextRowIndex.add(1);
			
			addStoryTotal(sheet, nextRowIndex, story.getHoursWorkedBetween(startDate, endDate));
			
			//Group the Rows together
	        sheet.groupRow(storyRowIndex+1,nextRowIndex.intValue());
	        sheet.setRowGroupCollapsed(storyRowIndex+1, false);
		}
		

		
		/*private void addRunningTotal(Row row, double value, CellStyle labelCellStyle, CellStyle valueCellStyle, boolean showLabel) {
			Cell cell = row.createCell(runningTotalLabelColIndex);
			if (labelCellStyle != null) {
				cell.setCellStyle(labelCellStyle);
			}
			if (showLabel) {
				cell.setCellValue("Running Total:");
			}
			
			cell = row.createCell(runningTotalValueColIndex);
			cell.setCellValue(value);
			if (valueCellStyle != null) {
				cell.setCellStyle(valueCellStyle);
			}
		}*/
		
		private void addTask(Sheet sheet, MutableInt nextRowIndex, Task task, Date startDate, Date endDate) {
			int taskRowIndex = nextRowIndex.intValue();
			
			Row row = sheet.createRow(taskRowIndex);
			Cell cell = row.createCell(taskSummaryColIndex);
			cell.setCellValue(task.getSummary());

			mergeCellsInRow(sheet, taskRowIndex, taskSummaryColIndex, weeklyTotalLabelColIndex);
			
			addWeeklyTotal(row, task.getHoursWorkedBetween(startDate, endDate), taskCellStyle, taskHoursCellStyle, false);
		}
		
		private void addStoryTotal(Sheet sheet, MutableInt nextRowIndex, double value) {
			int storyTotalRowIndex = nextRowIndex.intValue();
			
			Row row = sheet.createRow(storyTotalRowIndex);			
			Cell emptyCellToMerge = row.createCell(taskSummaryColIndex);
			emptyCellToMerge.setCellValue(" "); // do not remove, or it will break the autosizing...
			mergeCellsInRow(sheet, storyTotalRowIndex, taskSummaryColIndex, weeklyTotalLabelColIndex);
			
			Cell cell = row.createCell(weeklyTotalValueColIndex);
			cell.setCellStyle(storyHoursCellStyle);
			cell.setCellValue(value);
		}
		
		private void addWeeklyTotal(Row row, double value,  CellStyle labelCellStyle, CellStyle valueCellStyle, boolean showLabel) {
			Cell cell = row.createCell(weeklyTotalLabelColIndex);
			if (labelCellStyle != null) {
				cell.setCellStyle(labelCellStyle);
			}
			if (showLabel) {
				cell.setCellValue("Total");
				cell.getCellStyle().setAlignment(CellStyle.ALIGN_RIGHT);
			}
			
			cell = row.createCell(weeklyTotalValueColIndex);
			cell.setCellValue(value);
			if (valueCellStyle != null) {
				cell.setCellStyle(valueCellStyle);
			}
		}
		

		
		private void mergeCellsInRow(Sheet sheet, int rowIndex, int beginCellIndex, int endCellIndex) {
			sheet.addMergedRegion(new CellRangeAddress(
					rowIndex, //first row (0-based)
					rowIndex, //last row  (0-based)
					beginCellIndex, //first column (0-based)
					endCellIndex  //last column  (0-based)
		    ));
		}

}
