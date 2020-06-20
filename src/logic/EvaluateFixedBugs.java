package logic;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.json.JSONException;

import com.opencsv.CSVWriter;

public class EvaluateFixedBugs {
	
	public static Calendar toCalendar(Date date){ 
		  Calendar cal = Calendar.getInstance();
		  cal.setTime(date);
		  return cal;
		}

	public static void Evaluate(String projName) throws IOException, JSONException, InvalidRemoteException, TransportException, GitAPIException{
		
		
		String path ="C:\\Users\\Utente\\Desktop\\ISPW2\\Falessi\\progetti\\"+projName;
		
		projName.toLowerCase();
		File f = new File(path);
		
		if(!f.exists()) {
		Git.cloneRepository()
		  .setURI("https://github.com/apache/"+ projName)
		  .setDirectory(new File(path))
		  .call();
		}
		
		List<String> tickets = new ArrayList<String>();
		RetrieveTicketsID.getIdFixedTicketList(projName,tickets);             //creates a list containig all tickets IDs relative to fixed bugs
			
		Git git = Git.open(new File(path));
		Iterable<RevCommit> projLog = git.log().call();    					//gets all commits in log
		List<RevCommit> commitList = new  ArrayList<RevCommit>();
		
		for (RevCommit commitLog : projLog) {
			
			commitList.add(commitLog);
	
		}
		
		
		int ticketWithoutCommit = 0;
		int ticketsNumber = tickets.size();
		int commitWithoutDate =0;
		
		List<Date> latestCommitsDates = new ArrayList<Date>();
		
		for(String ticket : tickets) {                                      // for every ticket gets the last commit relative to it
			
			
			System.out.println( ticket + " " +"\n\n");
			
			List<RevCommit> fixedCommits = new ArrayList<RevCommit>();

			
			for (RevCommit commit : commitList) {
				
				//System.out.println( commit  +"\n");
				if(commit.getFullMessage().contains((ticket + " ")) == true ) {        //gets all commits which contain same ticket ID
					fixedCommits.add(commit);
					if(commit.getAuthorIdent().getWhen()== null) {
						commitWithoutDate = commitWithoutDate + 1;
					}
				}
			}
		
			Date authorDate,date;
			if(fixedCommits.size()==0) {
				
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
		
		System.out.print("le date sono : " + latestCommitsDates.size() + "\n");
		System.out.print("i ticket totali sono : " + ticketsNumber + "\n");
		System.out.print("i ticket senza alcun commit sono : " + ticketWithoutCommit + "\n");
		
		System.out.print("le date sono : " + latestCommitsDates + "\n");
		
		Collections.sort(latestCommitsDates, new Comparator<Date>(){
			 
            @Override
            public int compare(Date o1, Date o2) {
                return o1.compareTo(o2);
            }
        });
		System.out.print("le date sono : " + latestCommitsDates + "\n");
		
		Calendar calendar1 = toCalendar(latestCommitsDates.get(0));
		int year1 = calendar1.get(Calendar.YEAR);
		int month1= calendar1.get(Calendar.MONTH)+1;
		
		System.out.print("l anno iniziale è : " + year1 + "\n");
		System.out.print("il mese iniziale è : " + month1 + "\n");
		
		Calendar calendar2 = toCalendar(latestCommitsDates.get(latestCommitsDates.size()-1));
		int year2 = calendar2.get(Calendar.YEAR);
		int month2= calendar2.get(Calendar.MONTH)+1;
		
		
		System.out.print("l anno finale è : " + year2 + "\n");
		System.out.print("il mese finale è : " + month2 + "\n");
		
		
		int totalMonths = (year2-year1)*12 + (month2-month1) +1;
		System.out.print("mesi totali : " + totalMonths + "\n");
		
		List<GraphMonth> graphMonths = new ArrayList<GraphMonth>();
		int bugsPerMonth = 0 ;
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
			System.out.print("mese : " + monthDate + "\n");
			graphMonths.add(new GraphMonth(monthDate,bugsPerMonth));
			month = month+1;
		}
		
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
			System.out.print("bug risolti : " + monthBugs.getBugsNumber() + "\n");
		}
		
		
		File file = new File(path+"\\"+ projName+".csv"); 
	    try { 
	        // create FileWriter object with file as parameter 
	        FileWriter outputfile = new FileWriter(file); 
	  
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
	    } 
	    catch (IOException e) { 
	        // TODO Auto-generated catch block 
	        e.printStackTrace(); 
	    } 
	}
	
	
}
