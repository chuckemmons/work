package com.cee.ljr.word.POC;

import java.util.Date;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.cee.ljr.config.ApplicationConfig;
import com.cee.ljr.document.generator.WeeklyStatusReportDocxGenerator;
import com.cee.ljr.domain.report.WeeklyStatusReport;
import com.cee.ljr.service.WeeklyStatusReportService;
import com.cee.ljr.test.BaseTest;

public class WSRproofOfConcept extends BaseTest {
	//private static ObjectFactory objectFactory = new ObjectFactory();
	/*private static final String WrsPath = System.getProperty("user.dir")
			+ "/WRSproofOfConcept.docx";
	private static final String xlsPath = "C:/Users/chuck/Desktop/JIRA.xlsx";*/
	
	public static void main(String[] args) throws Exception {
		@SuppressWarnings("resource")
		ApplicationContext ctx = new AnnotationConfigApplicationContext(ApplicationConfig.class);

		WeeklyStatusReportService srService = ctx.getBean(WeeklyStatusReportService.class);
		WeeklyStatusReportDocxGenerator weeklyStatusReportDocxGenerator = ctx.getBean(WeeklyStatusReportDocxGenerator.class);

		// Sprint will be passed into the production code, lives here for now..
		WeeklyStatusReport weeklyStatusReport = srService.getWeeklyStatusReport(csvPaths, 0, new Date(), new Date());
		weeklyStatusReportDocxGenerator.generateDocument(weeklyStatusReport);
		
		/*WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
		// Delete the Styles part, since it clutters up our output
		MainDocumentPart mdp = wordMLPackage.getMainDocumentPart();
		Relationship styleRel = mdp.getStyleDefinitionsPart().getSourceRelationships().get(0);
		mdp.getRelationshipsPart().removeRelationship(styleRel);

		
		addHeader(wordMLPackage, statusReport.getTitle(), statusReport.getWeekEndingDate());		
		addSprint(wordMLPackage, statusReport.getSprint()); 
		addProjects(wordMLPackage, statusReport.getProjects());
		
		wordMLPackage.save(new File(WrsPath));
		// Display the result as Flat OPC XML
		FlatOpcXmlCreator worker = new FlatOpcXmlCreator(wordMLPackage);
		worker.marshal(System.out);*/
	}
	
/*	public static final void addProjects(WordprocessingMLPackage wordMLPackage, List<Project> projects) {
		for (Project project : projects) {
			addProject(wordMLPackage, project);
		}
	}
	
	public static final void addProject(WordprocessingMLPackage wordMLPackage, Project project) {
		MainDocumentPart mdp = wordMLPackage.getMainDocumentPart();
		String projectFontSize = "11";
		P emptyP = createP("", false, false, projectFontSize);
		mdp.addObject(emptyP);
		
		P projectP = createP(project.getName(), true, false, projectFontSize);
		mdp.addObject(projectP);
		
		Set<String> epics = project.getEpics();		
		for (String epic : epics) {
			addEpic(epic, project.getTasksByEpic(epic), mdp, projectFontSize);
		}
	}
	
	public static final void addEpic(String epic, List<JiraIssue> tasks, MainDocumentPart mdp, String epicFontSize) {
		P emptyP = createP("", false, false, epicFontSize);
		mdp.addObject(emptyP);
		P epicP = createP(epic, false, true, epicFontSize);
		mdp.addObject(epicP);
		
		for (JiraIssue task : tasks) {
			addTask(task, mdp, epicFontSize);
		}
	}
	
	public static final void addTask(JiraIssue task, MainDocumentPart mdp, String taskFontSize) {
		P summaryP = createP("     " + task.getSummary(), false, false, taskFontSize);
		mdp.addObject(summaryP);
		P statusP = createP("          Status: " + task.getStatus(), false, false, taskFontSize);
		mdp.addObject(statusP);
		P developerP = createP("          Developer: " + task.getDeveloper(), false, false, taskFontSize);
		mdp.addObject(developerP);
		
	}
	
	public static final void addSprint(WordprocessingMLPackage wordMLPackage, Sprint sprint) {
		MainDocumentPart mdp = wordMLPackage.getMainDocumentPart();
		String sprintFontSize = "11";
		
		P beginEmptyP = createP("", false, false, sprintFontSize);
		mdp.addObject(beginEmptyP);
		P sprintNumberP = createLabelValueP("Current Sprint: ", String.valueOf(sprint.getNumber()), sprintFontSize);
		mdp.addObject(sprintNumberP);
		P sprintStartDateP = createLabelValueP("Start Date: ", DateUtil.toString(sprint.getStartDate()), sprintFontSize);
		mdp.addObject(sprintStartDateP);
		P sprintEndDateP = createLabelValueP("End Date: ", DateUtil.toString(sprint.getEndDate()), sprintFontSize);
		mdp.addObject(sprintEndDateP);		
	}
	
	public static final void addHeader(WordprocessingMLPackage wordMLPackage, String title, Date weekEndingDate) throws Exception{
		// the Header part
		Relationship relationship = createHeaderPart(wordMLPackage, title, weekEndingDate);		
		// an entry in SectPr
		createHeaderReference(wordMLPackage, relationship);		
	}
	
	private static Relationship createHeaderPart(WordprocessingMLPackage wordprocessingMLPackage, String title, Date weekEndingDate)
			throws Exception {

		HeaderPart headerPart = new HeaderPart();
		Relationship rel = wordprocessingMLPackage.getMainDocumentPart().addTargetPart(headerPart);

		Hdr hdr = getHdr(title, weekEndingDate);
		headerPart.setJaxbElement(hdr);

		return rel;
	}

	private static void createHeaderReference(WordprocessingMLPackage wordprocessingMLPackage,
			Relationship relationship) throws InvalidFormatException {

		List<SectionWrapper> sections = wordprocessingMLPackage.getDocumentModel().getSections();

		SectPr sectPr = sections.get(sections.size() - 1).getSectPr();
		// There is always a section wrapper, but it might not contain a sectPr
		if (sectPr == null) {
			sectPr = objectFactory.createSectPr();
			wordprocessingMLPackage.getMainDocumentPart().addObject(sectPr);
			sections.get(sections.size() - 1).setSectPr(sectPr);
		}

		HeaderReference headerReference = objectFactory.createHeaderReference();
		headerReference.setId(relationship.getId());
		headerReference.setType(HdrFtrRef.DEFAULT);
		sectPr.getEGHdrFtrReferences().add(headerReference);// add header or
		// footer references

	}
	
	private static Hdr getHdr( String title, Date weekEndingDate)	throws Exception {
		Hdr hdr = objectFactory.createHdr();
		
		P p = createP(title, true, false, "14");
		hdr.getContent().add(p);
		
		String weekEndDate = DateUtil.toString(weekEndingDate);
		hdr.getContent().add(createP("Week Ending: " + weekEndDate, false, false, "12"));
		
		return hdr;
	}
	
	private static P createLabelValueP(String label, String value, String fontSize) {
		P paragraph = objectFactory.createP();		
		addGlobalParagraphStyle(paragraph);
		
		R labelRun = createStyledRun(label, true, false, fontSize);
		paragraph.getContent().add(labelRun);
		
		R valueRun = createStyledRun(value, false, false, fontSize);
		paragraph.getContent().add(valueRun);
		return paragraph;
	}*/
	
	/*public static P createUnnumberedList(String textValue, Boolean bold, String fontSize) {
        P  p = objectFactory.createP();
                                      
        R run = createStyledRun(textValue, bold, false, fontSize);
       
        p.getContent().add(run);
       
        addUnnumberedListPPr(p);
       
        return p;
	}*/
	
	/*public static P createTabP(String textValue, Boolean bold, Boolean italic, String fontSize) {
        P  p = objectFactory.createP();
                                      
        R run = createStyledRun(textValue, bold, italic, fontSize);
       
        p.getContent().add(run);
       
        addOneIndentPPr(p);
        addUnnumberedListPPr(p);
       
        return p;
  }*/
	/*private static void addOneIndentPPr(P p) {
		PPr ppr = objectFactory.createPPr();
		p.setPPr( ppr );
		
		Indent indent = objectFactory.createPPr
	}*/
	
	/*private static void addUnnumberedListPPr(P p) {
		PPr ppr = objectFactory.createPPr();
        
        p.setPPr( ppr );
       
        // Create and add <w:numPr>
        NumPr numPr =  objectFactory.createPPrBaseNumPr();
        ppr.setNumPr(numPr); 
        
        // The <w:ilvl> element
        Ilvl ilvlElement = objectFactory.createPPrBaseNumPrIlvl();
        numPr.setIlvl(ilvlElement);
        ilvlElement.setVal(BigInteger.valueOf(0));
        
        // The <w:numId> element
        NumId numIdElement = objectFactory.createPPrBaseNumPrNumId();
        numPr.setNumId(numIdElement);
        numIdElement.setVal(BigInteger.valueOf(1));
	}
	
	private static P createP(String textValue, Boolean bold, Boolean italic, String fontSize) {
		P p = objectFactory.createP();
		addGlobalParagraphStyle(p);
		R r = createStyledRun(textValue, bold, italic, fontSize);
		p.getContent().add(r);
		return p;
	}
	
	private static R createStyledRun(String textValue, Boolean bold, Boolean italic, String fontSize) {
		R run = objectFactory.createR();
		Text text = objectFactory.createText();
		text.setValue(textValue);
		text.setSpace("preserve");
		run.getContent().add(text);
		addStyling(run, bold, italic, fontSize);
		return run;
	}
	
	*//**
     *  This is where we add the actual styling information. In order to do this
     *  we first create a paragraph. Then we create a text with the content of
     *  the cell as the value. Thirdly, we create a so-called run, which is a
     *  container for one or more pieces of text having the same set of
     *  properties, and add the text to it. We then add the run to the content
     *  of the paragraph.
     *  So far what we've done still doesn't add any styling. To accomplish that,
     *  we'll create run properties and add the styling to it. These run
     *  properties are then added to the run. Finally the paragraph is added
     *  to the content of the table cell.
     *//*
    private static void addStyling(R run,
                    Boolean bold, Boolean italic, String fontSize) { 
        RPr runProperties = objectFactory.createRPr();
        
        if (bold != null && bold) {
            addBoldStyle(runProperties);
        }
 
        if (fontSize != null && !fontSize.isEmpty()) {
            setFontSize(runProperties, fontSize);
        }
        
        if (italic != null && italic) {
        	addItalicStyle(runProperties);
        }
 
        run.setRPr(runProperties);        
    }
    
	*//**
     *  In this method we're going to add the font size information to the run
     *  properties. First we'll create a half-point measurement. Then we'll
     *  set the fontSize as the value of this measurement. Finally we'll set
     *  the non-complex and complex script font sizes, sz and szCs respectively.
     *//*
    private static void setFontSize(RPr runProperties, String fontSize) {
        HpsMeasure size = new HpsMeasure();
        size.setVal(new BigInteger(fontSize).multiply(new BigInteger("2")));
        runProperties.setSz(size);
        runProperties.setSzCs(size);
    }
	
	*//**
     *  In this method we'll add the bold property to the run properties.
     *  BooleanDefaultTrue is the Docx4j object for the b property.
     *  Technically we wouldn't have to set the value to true, as this is
     *  the default.
     *//*
    private static void addBoldStyle(RPr runProperties) {
        BooleanDefaultTrue b = new BooleanDefaultTrue();
        b.setVal(true);
        runProperties.setB(b);
    }
    
    private static void addItalicStyle(RPr runProperties) {
    	BooleanDefaultTrue b = new BooleanDefaultTrue();
        b.setVal(true);
    	runProperties.setI(b);
    }
    
    private static void addOneTabParagraphStyle(P paragraph) {
	    PPr paragraphProperties = objectFactory.createPPr();
	    // add the global paragraph styles
	    setOneTabSpaceStyle(paragraphProperties);
	    // set the properties to the paragraph
	    paragraph.setPPr(paragraphProperties);
    }
    
    private static void setOneTabSpaceStyle(PPr paragraphProperties) {
    	Spacing spacing = objectFactory.createPPrBaseSpacing();
    	
    	spacing.setBefore(new BigInteger("0"));
    	spacing.setAfter(new BigInteger("0"));
    	spacing.setBeforeAutospacing(false);
    	spacing.setAfterAutospacing(false);
    	spacing.setBeforeLines(new BigInteger("15"));
    	paragraphProperties.setSpacing(spacing);
    }    
    
    private static void addGlobalParagraphStyle(P paragraph) {
	    PPr paragraphProperties = objectFactory.createPPr();
	    // add the global paragraph styles
	    setNoSpaceStyle(paragraphProperties);
	    // set the properties to the paragraph
	    paragraph.setPPr(paragraphProperties);
    }    
    
    private static void setNoSpaceStyle(PPr paragraphProperties) {
    	Spacing spacing = objectFactory.createPPrBaseSpacing();
    	
    	spacing.setBefore(new BigInteger("0"));
    	spacing.setAfter(new BigInteger("0"));
    	spacing.setBeforeAutospacing(false);
    	spacing.setAfterAutospacing(false);

    	paragraphProperties.setSpacing(spacing);
    }     */
}
