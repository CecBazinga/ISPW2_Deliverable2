package logic;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.json.JSONException;

import com.opencsv.CSVWriter;

public final class EvaluateFixedBugs {
	
	private static int ticketWithoutCommit = 0;
	private static int commitWithoutDate =0;
	private static int bugsPerMonth = 0 ;
	private static List<GraphMonth> graphMonths ;
	private static List<Date> latestCommitsDates ;
	
	private EvaluateFixedBugs() {
		
	}
	
	private static Calendar toCalendar(Date date){ 
		  Calendar cal = Calendar.getInstance();
		  cal.setTime(date);
		  return cal;
		}

	public static void handleTicketWithoutCommit(List<RevCommit> fixedCommits) {
		
		Date authorDate;
		Date date;
		
		if(fixedCommits.isEmpty()) {
			
			ticketWithoutCommit = ticketWithoutCommit+1 ;
		
		}else {
			
			authorDate = fixedCommits.get(0).getAuthorIdent().getWhen();
			
			for(int d=1;d<fixedCommits.size();d++) {                           //gets latest commit among all commits relative to same ticket
				
				date =fixedCommits.get(d).getAuthorIdent().getWhen();
				if(date.after(authorDate)){
					authorDate = date;
				}
			}
			latestCommitsDates.add(authorDate);
			
		}
	}
	
	public static void handleCommitWithoutDate(List<String> tickets , List<RevCommit> commitList ) {
		
		for(String ticket : tickets) {                                      // for every ticket gets the last commit relative to it
			
			List<RevCommit> fixedCommits = new ArrayList<>();

			for (RevCommit commit : commitList) {
				
				if(commit.getFullMessage().contains((ticket + " "))) {        //gets all commits which contain same ticket ID
					fixedCommits.add(commit);
					if(commit.getAuthorIdent().getWhen()== null) {
						commitWithoutDate = commitWithoutDate + 1;
					}
				}
			}
		
			handleTicketWithoutCommit( fixedCommits );
			
		}
	}
	
	public static void initializeBugsPerMonth() {
		
		Calendar calendar1 = toCalendar(latestCommitsDates.get(0));
		int year1 = calendar1.get(Calendar.YEAR);
		int month1= calendar1.get(Calendar.MONTH)+1;
		
		Calendar calendar2 = toCalendar(latestCommitsDates.get(latestCommitsDates.size()-1));
		int year2 = calendar2.get(Calendar.YEAR);
		int month2= calendar2.get(Calendar.MONTH)+1;
		
		int totalMonths = (year2-year1)*12 + (month2-month1) +1;
		
		int month = month1;
		int year =  year1 ;
		String monthDate;
		for(int i=0;i<totalMonths;i++) {
			if(month > 12) {
				month = 1;
				year = year +1;
			}
			if(month<10) {
				monthDate = "0" + month + "-"+ year;
			}
			else {
				monthDate = month + "-"+ year;
			}
			graphMonths.add(new GraphMonth(monthDate,bugsPerMonth));
			month = month+1;
		}
	}
	
	public static void evaluate(String projName,String path) throws IOException, JSONException, GitAPIException{
		
		Log.infoLog("Inizio calcolo dei bug risolti per mese \n");
		
		String csvName = projName + ".csv" ;
				
		File f = new File(path);
		
		if(!f.exists()) {
		Git.cloneRepository()
		  .setURI("https://github.com/apache/"+ projName.toLowerCase())
		  .setDirectory(new File(path))
		  .call();
		}
		
		List<String> tickets = new ArrayList<>();
		RetrieveTicketsID.getIdFixedTicketList(projName,tickets);             //creates a list containig all tickets IDs relative to fixed bugs
			
		Git git = Git.open(new File(path));
		Iterable<RevCommit> projLog = git.log().call();    					//gets all commits in log
		List<RevCommit> commitList = new  ArrayList<>();
		
		for (RevCommit commitLog : projLog) {
			
			commitList.add(commitLog);
	
		}
		
		latestCommitsDates = new ArrayList<>();
		graphMonths = new ArrayList<>();
		
		handleCommitWithoutDate(tickets,commitList);
			 
		Collections.sort(latestCommitsDates, (o1, o2) ->  o1.compareTo(o2));
        
		initializeBugsPerMonth();
		
		for(GraphMonth monthBugs : graphMonths) {
			
			bugsPerMonth = 0;
			
			for(Date d : latestCommitsDates) {
				Calendar calendar = toCalendar(d);
				int dMonth = calendar.get(Calendar.MONTH)+1;
				int dYear = calendar.get(Calendar.YEAR);
				String dDate;
				if(dMonth<10) {
					dDate = "0" + dMonth + "-"+ dYear;
				}
				else {
					dDate = dMonth + "-"+ dYear;
				}
				if(dDate.contentEquals(monthBugs.getDate())) {
					
					bugsPerMonth = bugsPerMonth+1;
				}
					
			}
			monthBugs.setFixedBugs(bugsPerMonth);
		}
		
		
		File file = new File(csvName); 
		
		// create FileWriter object with file as parameter 
	
	    try (FileWriter outputfile = new FileWriter(file)){ 

	        // create CSVWriter object filewriter object as parameter 
	        CSVWriter writer = new CSVWriter(outputfile); 
	  
	        // adding header to csv 
	        String[] header = { "Month", "FixedBugs"}; 
	        writer.writeNext(header); 
	  
	        // add data to csv 
	        for(GraphMonth monthBugs : graphMonths) {
	        	String[] data = { monthBugs.getDate(), String.valueOf(monthBugs.getBugsNumber()) }; 
	        	writer.writeNext(data); 
	        
	        }
	        // closing writer connection 
	        writer.close(); 
	        
	        Log.infoLog("Calcolo dei bug risolti per mese completato con successo \n");
	        Log.infoLog("E' stato creato con successo il relativo file csv : " + csvName + "\n");
	    } 
	    catch (IOException e) { 
	    	
	        Log.errorLog("Errore nella scrittura del csv dei bug risolti per mese \n");
	        StringWriter sw = new StringWriter();
	        PrintWriter pw = new PrintWriter(sw);
	        e.printStackTrace(pw);
	        Log.errorLog(sw.toString());
	    } 
	}
	
	
}
