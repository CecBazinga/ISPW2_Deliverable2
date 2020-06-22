package logic;



import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.EmptyTreeIterator;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import org.json.JSONException;






public class CalculateBugginess {
	
	private CalculateBugginess() {
		
	}

	private static final String MODE_VARIABLE = "VARIABLE" ;
	private static final String MODE_FIXED = "FIXED" ;
	private static final String TYPE_DELETE = "DELETE" ;
	private static final String TYPE_MODIFY = "MODIFY" ;
	private static final String TYPE_ADD = "ADD" ;
	private static String mode ;
	private static List<Double> proportionArray = new ArrayList<>();
	private static double p = 0;
	
	private static int invalidTickets = 0;
	private static int aVInvalidTickets = 0; 
	private static int invalidTicketsWithoutAV = 0;
	
	private static int ticketsWithoutFixedVersion = 0;
	private static int aVticketsWithoutFixedVersion = 0;
	private static int ticketsWithoutFixedVersionWithoutAV = 0;
	
	private static int ticketsWithoutOpeningVersion = 0;
	private static int aVticketsWithoutOpeningVersion = 0;
	private static int ticketsWithoutOpeningVersionWithoutAV = 0;
	
	private static int ticketsWithoutInjectionVersion = 0;
	private static int aVticketsWithoutInjectionVersion = 0;
	private static int ticketsWithoutInjectionVersionWithoutAV = 0;
	
	private static int ticketWithFVEqualsToOV = 0;
	private static int aVticketWithFVEqualsToOV = 0;
	private static int ticketWithFVEqualsToOVWithoutAV = 0;

	private static int ticketsWithoutAV = 0;
	private static int ticketsWithAV = 0;
	
	
	
	
	

	
	
	public static void useMWVariable() {
		mode = MODE_VARIABLE  ;
	}
	
	public static void useMWFixed() {
		mode = MODE_FIXED  ;
	}

	public static boolean checkRelease(Release release, List<Release> releasesList) {
		
		for(Release r : releasesList ) {
			
			if(r.getName().equals(release.getName())) {
				
				return true;
			}
		}
		
		return false;
	}
	
	public static boolean checkAV(AffectedVersion aV, List<Release> releasesList) {
		
		for(Release r : releasesList ) {
			
			if(r.getName().equals(aV.getName())) {
				
				return true;
			}
		}
		
		return false;
	}
	
	public static boolean checkAVList(List<AffectedVersion> aV, List<Release> releasesList) {
		
		for(Release r : releasesList ) {
			for(AffectedVersion av : aV) {
				if(r.getName().equals(av.getName())) {
					
					return true;
				}
			}
		}
		
		return false;
	}
	
	public static Release compareDateToReleasesArray( List<Release> releases , LocalDate date ) {
		
		//the array is supposed to be sorted from oldest to newest release
		
		for(Release r : releases) {
			if(r.getDate().toLocalDate().compareTo(date) > 0) {
				
				return r ;
			}
		}
		
		return null;
		
		
	}
	
	public static Release compareDateToReleasesArrayDateTime( List<Release> releases , LocalDateTime date ) {
		
		//the array is supposed to be sorted from oldest to newest release
		
		for(Release r : releases) {
			if(r.getDate().compareTo(date) > 0) {
				
				return r ;
			}
		}
		
		return null;
		
		
	}
	
	public static Release getReleaseByName( List<Release> releases , String name) {
		
		//the array is supposed to be sorted from oldest to newest release
		
		for(Release r : releases) {
			if(r.getName().equals(name)) {
				
				return r ;
			}
		}
		
		return null;
		
	}

	public static void calculateProportion(int openingVersion,int fixedVersion,LocalDate injectionVersionDate,Release openingRelease,int injectedVersion) {
		
		if((openingVersion!=0) && (fixedVersion!=0)) {
			
			if(injectionVersionDate.compareTo(openingRelease.getDate().toLocalDate()) > 0) {
				
				invalidTickets++;
				aVInvalidTickets++;		
				
				if((fixedVersion-openingVersion) == 0) {
					ticketWithFVEqualsToOV++;
					aVticketWithFVEqualsToOV++;
				}
			}else {
				
				if((fixedVersion-openingVersion) > 0) {
					
					p = ((double)fixedVersion-(double)injectedVersion)/(fixedVersion-openingVersion);
				
					proportionArray.add(p);
			
				}else {
					
					ticketWithFVEqualsToOV++;
					aVticketWithFVEqualsToOV++;

				}
			}		
		}
	}
	
	public static void checkTicketDates(List<Release> releasesArray,List<AffectedVersion> aV,
			LocalDate creationDate,LocalDate resolutionDate,LocalDate injectionVersionDate) {
		
		int oV;
		int fV;
		int iV;
		
		Release injectedRelease = getReleaseByName(releasesArray,aV.get(0).getName());
		
		if(injectedRelease==null) {
			
			ticketsWithoutInjectionVersion++;
			aVticketsWithoutInjectionVersion++;
			
		}else {
			iV = injectedRelease.getIndex();
			
			Release openingRelease = compareDateToReleasesArray(releasesArray,creationDate);
			if(openingRelease==null) {
				
				ticketsWithoutOpeningVersion++;
				aVticketsWithoutOpeningVersion++;	
				oV = 0;
			}else {
				oV = openingRelease.getIndex();
			}
			
			
			Release fixRelease = compareDateToReleasesArray(releasesArray,resolutionDate);				
			if(fixRelease==null) {		
				
				ticketsWithoutFixedVersion++;	
				aVticketsWithoutFixedVersion++;	
				fV = 0;
			}else {					
				fV = fixRelease.getIndex();
			}
		
			calculateProportion(oV, fV, injectionVersionDate, openingRelease, iV);

		}
	}
	
	public static void calculatePOverTicketList( List<Ticket> tickets ,  List<Release> releasesArray) {
		
		for(Ticket ticket : tickets) {                                           
			
			// per ogni ticket dotato di Av vado a calcolare il proportion
			
			if(!ticket.getAffectedVersions().isEmpty()) {                     
				
			//  calcolo la injectedVersion come la data piu anziana tra le date delle AffectedVersion
				
				ticketsWithAV ++;
				
				List<AffectedVersion> aV = ticket.getAffectedVersions();
				
				// ordino le av di ogni ticket in base alla loro data
				
				Collections.sort(aV, (v1,v2) -> LocalDate.parse(v1.getDate().substring(0 , 10 )).compareTo(LocalDate.parse(v2.getDate().substring(0 , 10 ))));
				
				
				LocalDate injectionVersionDate = LocalDate.parse(aV.get(0).getDate().substring(0 , 10 ));
				
				LocalDate creationDate = LocalDate.parse(ticket.getCreated().substring(0 , 10 ));
				
				LocalDate resolutionDate = LocalDate.parse(ticket.getResolutionDate().substring(0 , 10 ));
				
				if( creationDate.compareTo(resolutionDate) > 0) {       // check on dates consistency	
			
					invalidTickets++;
					aVInvalidTickets++;		
					
					
				}else {                                                                                        
					
					//acquiring indexes of the IV,OV,FV
					checkTicketDates(releasesArray, aV, creationDate,resolutionDate, injectionVersionDate);
					
				}
			}	
		}
	}
	
	public static void calculatePOverTicketListWithoutMetrics( List<Ticket> tickets ,  List<Release> releasesArray) {
	
		//ticket given to this function have already been checked to have consistent dates
		
		int openingVersion;
		int fixedVersion;
		int injectedVersion;
		
		for(Ticket ticket : tickets) {                                           // per ogni ticket dotato di Av vado a calcolare il proportion
			
			if(!ticket.getAffectedVersions().isEmpty()) {                     //  calcolo la injectedVersion come la data piu anziana tra le date delle AffectedVersion
				
				List<AffectedVersion> aV = ticket.getAffectedVersions();
				
				// ordino le av di ogni ticket in base alla loro data
				
				Collections.sort(aV, (v1,v2) -> LocalDate.parse(v1.getDate().substring(0 , 10 )).compareTo(LocalDate.parse(v2.getDate().substring(0 , 10 ))));
				
				
				LocalDate creationDate = LocalDate.parse(ticket.getCreated().substring(0 , 10 ));
				
				LocalDate resolutionDate = LocalDate.parse(ticket.getResolutionDate().substring(0 , 10 ));
				
				//acquiring indexes of the IV,OV,FV
					
				
					injectedVersion = getReleaseByName(releasesArray,aV.get(0).getName()).getIndex();
					
					Release openingRelease = compareDateToReleasesArray(releasesArray,creationDate);
					
					openingVersion = openingRelease.getIndex();
		
					Release fixRelease = compareDateToReleasesArray(releasesArray,resolutionDate);				
								
					fixedVersion = fixRelease.getIndex();
																											
					p = ((double)fixedVersion-(double)injectedVersion)/(fixedVersion-openingVersion);
				
					proportionArray.add(p);
					
			}
		}
			
	}
			
	public static boolean checkTicket( Ticket ticket, List<Release> releasesArray ) {
		
		//check if dates in a ticket are consistent and not null
		// this function is meant to be used on tickets which has ALWAYS a sure IV(AffectedVersion)
		
		int openingVersion;
		int fixedVersion ;
				
			if(!ticket.getAffectedVersions().isEmpty()) {
				
			
				List<AffectedVersion> aV = ticket.getAffectedVersions();
				
				// ordino le av di ogni ticket in base alla loro data
				
				Collections.sort(aV, (v1,v2) -> LocalDate.parse(v1.getDate().substring(0 , 10 )).compareTo(LocalDate.parse(v2.getDate().substring(0 , 10 ))));
				
				Release injectedRelease = getReleaseByName(releasesArray,aV.get(0).getName());
				
				if(injectedRelease==null) {
					
					return false;
					
				}else {
					LocalDate injectionVersionDate = injectedRelease.getDate().toLocalDate();
					
					LocalDate creationDate = LocalDate.parse(ticket.getCreated().substring(0 , 10 ));
					
					LocalDate resolutionDate = LocalDate.parse(ticket.getResolutionDate().substring(0 , 10 ));
					
					if( creationDate.compareTo(resolutionDate) > 0) {       // check on dates consistency	
						
						return false ;			
						
					}else {                                                                                         //acquiring indexes of the IV,OV,FV
						
						
						Release openingRelease = compareDateToReleasesArray(releasesArray,creationDate);
						if(openingRelease==null) {
							return false;
						}else {
							openingVersion = openingRelease.getIndex();
						}
						
						
						Release fixRelease = compareDateToReleasesArray(releasesArray,resolutionDate);				
						if(fixRelease==null) {					
							return false;
						}else {					
							fixedVersion = fixRelease.getIndex();
						}
						
						if(injectionVersionDate.compareTo(openingRelease.getDate().toLocalDate()) > 0) {
							
							return false ;
							
						}
					
							
						if((fixedVersion-openingVersion) > 0) {          // OV and FV aren't the same
							
							return true;
					
						}
									
					}	
				}
			}
			
			return false;
			
	}

	public static void retrieveAndSortCommitsForTicket(Ticket ticket , List<RevCommit> allCommits , List<RevCommit> ticketsCommits ) {
		
		for (RevCommit commit : allCommits) {
			
			if(commit.getFullMessage().contains((ticket.getKey() + ":"))) {        //gets all commits which contain same ticket ID
				
				ticketsCommits.add(commit);
			}
		}
	}
	
	public static List<DiffEntry> calculateDiffEntries(RevCommit commit,Git git) throws  IOException{
		
		ObjectId oldTree = commit.getTree();
		if(commit.getParentCount() != 0) {
			RevCommit parent = (RevCommit) commit.getParent(0).getId();
			
	    	try (DiffFormatter diffFormatter = new DiffFormatter( DisabledOutputStream.INSTANCE )) {
				diffFormatter.setRepository( git.getRepository() );
				diffFormatter.setDiffComparator(RawTextComparator.DEFAULT);
				diffFormatter.setDetectRenames(true);
				return diffFormatter.scan( parent.getTree(),oldTree );
					
			}
		}
		
		return new ArrayList<>() ;
	}
		
	public static String checkAlias(String fileName ,List<FileAlias> filesAlias) {
		
		for(FileAlias fA : filesAlias) {
			if(fA.getAlias().contains(fileName)) {
				
				return fA.getLastFileName();
			}
		}
		
		return null;
	}
	
	public static void  getFilesByCommitAndCalculateLoc(RevCommit commit,List<FileNameAndSize> filePaths,Git git) throws  IOException{
		
		ObjectId treeId = commit.getTree();
		try (TreeWalk treeWalk = new TreeWalk(git.getRepository())) {
			treeWalk.reset(treeId);
			treeWalk.setRecursive(true);
			while(treeWalk.next()) {
				
				int loc = calculateLoc(treeWalk,git);
				FileNameAndSize file = new FileNameAndSize();
				file.setFileName(treeWalk.getPathString());
				file.setSize(loc);
				filePaths.add(file);
				
			}
		}
		
	}
	
	public static RevCommit getLatestCommitBeforeRelease(List<RevCommit> commitList, LocalDateTime releaseDate) {
		
		// given a date return last commit before that date 
		// commitList must be ordered from oldest to youngest date
		
		for(int c=0 ; c < commitList.size() ; c++ ) {
			
			LocalDateTime commitDate = Instant.ofEpochSecond(commitList.get(c).getCommitTime()).atZone(ZoneId.of("UTC")).toLocalDateTime();
			
			if(commitDate.compareTo(releaseDate) > 0) {
				
				return commitList.get(c-1);
				
			}else if((commitDate.compareTo(releaseDate) < 0) && (c == (commitList.size()-1))) {
				
				return commitList.get(c);
			}
		}
		
		return null;
	}

	public static void calculateNFix(List<RevCommit> commitList, List<Ticket> ticketList, List<DBEntry> dBEntriesList,Git git,List<FileAlias> filesAlias ) 
			throws IOException {
		
		for(DBEntry entry : dBEntriesList) {
			
			int nFix = 0;
			
			String fileName = entry.getFileName();
			
			for(Ticket t : ticketList) {
					
				List<RevCommit> ticketCommits = new ArrayList<>();
				
				retrieveAndSortCommitsForTicket(t,commitList,ticketCommits);
				
				if(!ticketCommits.isEmpty()) {
						
					for(int i=0;i<ticketCommits.size();i++) {
				
						LocalDateTime commitDate = Instant.ofEpochSecond(ticketCommits.get(i).getCommitTime()).atZone(ZoneId.of("UTC")).toLocalDateTime();
						
						if(entry.getRelease().getDate().compareTo(commitDate) > 0) {
							
							List<DiffEntry> diffEntries = calculateDiffEntries(ticketCommits.get(i), git);
							
							if(!diffEntries.isEmpty()){
								for(DiffEntry dEntry : diffEntries) {
									
									String type = dEntry.getChangeType().toString();
									
									String oldPath = dEntry.getOldPath().substring( dEntry.getOldPath().indexOf("/")+1);
									oldPath = oldPath.replace("/", "\\");
									
									if(type.equals(TYPE_DELETE) || type.equals(TYPE_MODIFY) ) {
										
										String alias = checkAlias(oldPath,filesAlias);
										String nameToBeUsed = null ;
										
										if( (alias!=null)  ){
											
											nameToBeUsed = alias;
										
										}else { 
										
											nameToBeUsed = oldPath;
										}
										
										if(fileName.equals(nameToBeUsed) || fileName.contains(nameToBeUsed)) {
											
											nFix++;
										}
										
									}
									
								}
							}
						}
					}
				}
			}
			
			entry.setnFix(nFix);
		}
	}	

	public static int calculateLoc(TreeWalk treeWalk, Git git) throws IOException {
		
		ObjectLoader loader = git.getRepository().open(treeWalk.getObjectId(0));
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		
		loader.copyTo(output);
		
		String contentFile = output.toString();
		StringTokenizer token = new StringTokenizer(contentFile, "\n");		//frammenta stringa in token quando trova \n
		
		int count = 0;
		while(token.hasMoreTokens()) {
			count++;
			token.nextToken();
		}
		
		return count;
 	}
	
	
	
	
	public static void calculateMetrics(Git git,List<DBEntry> dBEntries,List<FileAlias> filesAlias) throws IOException {
		
		Log.infoLog("Inizio calcolo metriche : \n\n");
		
		List<Integer> commitsLocAdded = new ArrayList<>();
		List<Integer> commitsChurn = new ArrayList<>();
		List<String>  realChangeSet = new ArrayList<>();
		List<Integer> avgMaxChgSetSize = new ArrayList<>();
		List<String> authors = new ArrayList<>();
		List<String>  changeSet = new ArrayList<>();
		
		// prendo tutti i commit nella release e mi calcolo le metriche per ogni file delal release
		for(DBEntry entry : dBEntries) {
			
				String fileName = entry.getFileName();
				int locAdded = 0;
				int maxLocAdded = 0;
				double avgLocAdded = 0;
				int locTouched = 0;
				int locDeleted = 0;
				int churn = 0;
				int maxChurn = 0;
				double avgChurn = 0;
				int chgSetSize = 0;
				int maxChgSetSize = 0;
				double avgChgSetSize = 0;
				int nR = 0;
				
				Release release = entry.getRelease();
				
				
				commitsLocAdded.clear();
				commitsChurn.clear();
				realChangeSet.clear();
				avgMaxChgSetSize.clear();
				authors.clear(); 
				
				for(RevCommit commit : release.getReleaseCommits()) {
					
					String author = commit.getAuthorIdent().getName();
				
					changeSet.clear() ;
					int commitLocAdded = 0;
					int commitLocDeleted = 0;
					int commitChurn = 0;
					
					RevCommit parent = null;
					
					if(commit.getParentCount()!=0) {
						parent = commit.getParent(0);

					}
					
					DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE);
					df.setRepository(git.getRepository());
					df.setDiffComparator(RawTextComparator.DEFAULT);
					df.setDetectRenames(true);
					List<DiffEntry> diffs;
					if(parent != null) {
							diffs = df.scan(parent.getTree(), commit.getTree());
					}
					else {
						
						RevWalk rw = new RevWalk(git.getRepository());
						
						ObjectReader reader = rw.getObjectReader();
						diffs =df.scan(new EmptyTreeIterator(),
						        new CanonicalTreeParser(null, reader, commit.getTree()));
						 
					}
					
					boolean calculateChgSet = false ;
					
					for (DiffEntry diff : diffs) {    // For each file changed in the commit
						
						String path;
						
						if(diff.getChangeType().toString().equals(TYPE_ADD) ) {
							
							path = diff.getNewPath().substring( diff.getNewPath().indexOf("/")+1);
							path = path.replace("/", "\\");
						}else {
							
							path = diff.getOldPath().substring( diff.getOldPath().indexOf("/")+1);
							path = path.replace("/", "\\");
							
						}
								
						String alias = checkAlias(path,filesAlias);
						String nameToBeUsed = null ;
						
						if( (alias!=null)  ){
							
							nameToBeUsed = alias;
						
						}else { 
						
							nameToBeUsed = path;
						}
								
						if(!changeSet.contains(nameToBeUsed)) {
							changeSet.add(nameToBeUsed);
						}
						
						if(fileName.equals(nameToBeUsed) || fileName.contains(nameToBeUsed) ){
							
							nR++;
							calculateChgSet = true;
							if(!authors.contains(author)) {
								authors.add(author);
							}

							for(Edit edit : df.toFileHeader(diff).toEditList()) {
								
									locAdded += edit.getEndB() - edit.getBeginB();
									locDeleted += edit.getEndA() - edit.getBeginA();
									commitLocAdded += edit.getEndB() - edit.getBeginB();
									commitLocDeleted += edit.getEndA() - edit.getBeginA();

							}
						}
					}
					
					if(calculateChgSet) {
						avgMaxChgSetSize.add(changeSet.size());
						for(int i = 0; i < changeSet.size();i++) {
							if(!realChangeSet.contains(changeSet.get(i))) {
								realChangeSet.add(changeSet.get(i));
							}
						}
					}
					commitChurn = commitLocAdded - commitLocDeleted;
					commitsLocAdded.add(commitLocAdded);
					commitsChurn.add(commitChurn);
				}
				
			Collections.sort(commitsLocAdded);
			maxLocAdded = commitsLocAdded.get(commitsLocAdded.size()-1);
			double sum = 0;
			for(int i=0; i < commitsLocAdded.size() ; i++) {
				sum += commitsLocAdded.get(i);
			}
			avgLocAdded = Math.round(sum/commitsLocAdded.size()) ;
			
			Collections.sort(commitsChurn);
			maxChurn = commitsChurn.get(commitsChurn.size()-1);
			double sumChurn = 0;
			for(int i=0; i < commitsChurn.size() ; i++) {
				sumChurn += commitsChurn.get(i);
			}
			avgChurn = Math.round(sumChurn/commitsChurn.size());
			
			chgSetSize = realChangeSet.size();
			
			Collections.sort(avgMaxChgSetSize);
			maxChgSetSize = avgMaxChgSetSize.get(avgMaxChgSetSize.size()-1);
			double sumChgSetSize = 0;
			for(int i=0; i < avgMaxChgSetSize.size() ; i++) {
				sumChgSetSize += avgMaxChgSetSize.get(i);
			}
			avgChgSetSize = Math.round(sumChgSetSize/avgMaxChgSetSize.size());
					
			locTouched = locAdded+locDeleted;
			//cambio info file
			entry.setLocAdded(locAdded);
			entry.setLocTouched(locTouched);
			entry.setMaxLocAdded(maxLocAdded);
			entry.setAvgLocAdded(avgLocAdded);
			churn = locAdded -locDeleted;
			entry.setChurn(churn);
			entry.setMaxChurn(maxChurn);
			entry.setAvgChurn(avgChurn);
			entry.setChgSet(chgSetSize);
			entry.setMaxChgSetSize(maxChgSetSize);
			entry.setAvgChgSetSize(avgChgSetSize);
			entry.setDistinctAuthors(authors.size());
			entry.setnR(nR);
			
		}
			
	}		
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public static void calculateBugginess(String projName,String path) throws  IOException, JSONException, GitAPIException {
		
		List<FileAlias> filesAlias = new ArrayList<>();
		int totalTickets = 0;
		
		List<Ticket> tickets = new ArrayList<>();                      //creates a list containig all tickets IDs,CreationDate,ResolutionDate, AV relative to fixed bugs
		RetrieveTicketsID.getFixedTicketList(projName,tickets);
		totalTickets = tickets.size();
		
		List<Release> releasesArray= new  ArrayList<>() ;
		GetReleaseInfo.getProjectReleases(projName.toUpperCase(), releasesArray);
		
		//Decide wich sliding window mode to use : fixed window size or variable window size: by default uses fixed one

		useMWFixed();
	
		//P calculation using sliding window

		calculatePOverTicketList(tickets,releasesArray);
		
		
		double sum = 0;
		for(double d : proportionArray) {
			
			sum += d; 
		}
		
		p = sum/proportionArray.size();
		
		int ticketCounter = 0;
		List<Ticket> movingTickets = new ArrayList<>();
		double movingWindow = 0;
		
		
		for(int i =0 ; i < tickets.size() ; i++) {
			
			Ticket t = tickets.get(i);
			
			ticketCounter++;
			
			if(mode.equals(MODE_VARIABLE)) {
				
				movingWindow = (double)ticketCounter/100 ;
				
			}else if(mode.equals(MODE_FIXED)) {
				
				movingWindow = (double)totalTickets/100 ;
				
			}
			
			if(t.getAffectedVersions().isEmpty()) {
				
				ticketsWithoutAV++;
				
				if((mode.equals(MODE_VARIABLE) && (movingWindow > 1) ) || (mode.equals(MODE_FIXED) &&  (ticketCounter >(movingWindow) ) )) {     // posso scegleire quale modalita di moving window usare
					
					proportionArray.clear();
					movingTickets.clear();
					
					int position = i ;
					
					for(int w = 1 ; w < movingWindow + 1; w++ ) {                                                                   //ricalcolo p usando l'ultimo 1% dei ticket processati
						
						if( (position-w) >=0) {
							
							Ticket movingTicket = tickets.get(position-w);
							
							while(!checkTicket(movingTicket,releasesArray) && ((position-w) > 0)) {                                //scarto i ticket non validi dalla movingWindow
		
								position--;
								movingTicket = tickets.get(position-w);
							}
							
							if(checkTicket(movingTicket,releasesArray)) {
								
								movingTickets.add(movingTicket);
						
							}
								
						}
					}
					
					if(!movingTickets.isEmpty()) {
						
						
						calculatePOverTicketListWithoutMetrics(movingTickets,releasesArray);
						
						sum = 0;
						for(double d : proportionArray) {
							
							sum += d; 
						}
						
						p = sum/proportionArray.size();
						
					}
				}
				
				
				// se non ho attraversato il precedente if uso come p quello calcolato precedentemente su tutti i ticket aventi AV
				
				int openingVersion;
				int fixedVersion;
				int injectedVersion;
				
				LocalDate creationDate = LocalDate.parse(t.getCreated().substring(0 , 10 ));
				
				LocalDate resolutionDate = LocalDate.parse(t.getResolutionDate().substring(0 , 10 ));
				
				if( creationDate.compareTo(resolutionDate) > 0) {       // check on dates consistency and remove incomplete tickets with inconsistent dates
					
					invalidTicketsWithoutAV++;
					invalidTickets++;	
					
				}else {                                                                                         //acquiring indexes of the IV,OV,FV
					
					Release openingRelease = compareDateToReleasesArray(releasesArray,creationDate);
					if(openingRelease==null) {
						
						ticketsWithoutOpeningVersionWithoutAV++;
						ticketsWithoutOpeningVersion++;	
						openingVersion = 0;
					}else {
						openingVersion = openingRelease.getIndex();
					}
					
					
					Release fixRelease = compareDateToReleasesArray(releasesArray,resolutionDate);				
					if(fixRelease==null) {	
						
						ticketsWithoutFixedVersionWithoutAV++;
						ticketsWithoutFixedVersion++;	
						fixedVersion = 0;
					}else {					
						fixedVersion = fixRelease.getIndex();
					}
				
					
					if((openingVersion!= 0) && (fixedVersion!= 0)) {
						
						if((fixedVersion-openingVersion) > 0) {
							
							injectedVersion = (int) Math.round(fixedVersion-(fixedVersion-openingVersion)*p);
							
							t.initializeAV();
							
							if(injectedVersion<1) {
								
								injectedVersion = 1;
							}
							
							
							LocalDate injectionDate = releasesArray.get(injectedVersion-1).getDate().toLocalDate();
							
							if( (injectionDate.compareTo(openingRelease.getDate().toLocalDate()) > 0) && (creationDate.compareTo(resolutionDate) <= 0 ) ) {
								
								invalidTicketsWithoutAV++;
								invalidTickets++;
								
								if((fixedVersion-openingVersion) == 0) {
									ticketWithFVEqualsToOVWithoutAV++;
									ticketWithFVEqualsToOV++;
								}
								
							}else {
						
								for(; (injectedVersion-1) < (fixedVersion-1) ; injectedVersion++) {
									
									AffectedVersion av = new AffectedVersion() ;
									Release currentRelease = releasesArray.get(injectedVersion-1);
									av.setId(currentRelease.getId());
									av.setName(currentRelease.getName());
									av.setDate(currentRelease.getDate().toLocalDate().toString());
									t.getAffectedVersions().add(av);
								}
						
							}
						}else {
					
							ticketWithFVEqualsToOVWithoutAV++;
							ticketWithFVEqualsToOV++;
							
						}
					}
				}	
			}	
		}

		 String outputName = projName + "TicketMetrics.csv";
		 
		 
		 try(FileWriter fileWriter = new FileWriter(outputName);) {
	            
	            fileWriter.append("totalTickets , ticketsWithAV , ticketsWithoutAV , invalidTickets , AVInvalidTickets  , invalidTicketsWithoutAV , ticketsWithoutFixedVersion  , "
	            		+ "AVticketsWithoutFixedVersion , ticketsWithoutFixedVersionWithoutAV , ticketsWithoutOpeningVersion , AVticketsWithoutOpeningVersion"
	            		+ "ticketsWithoutOpeningVersionWithoutAV , ticketWithFVEqualsToOV , AVticketWithFVEqualsToOV , ticketWithFVEqualsToOVWithoutAV , ticketsWithoutInjectionVersion"
	            		+ " , AVticketsWithoutInjectionVersion , ticketsWithoutInjectionVersionWithoutAV ");
	            fileWriter.append("\n");
	           
	               
	            
               fileWriter.append(String.valueOf(totalTickets));
               fileWriter.append(",");
               fileWriter.append(String.valueOf(ticketsWithAV));
               fileWriter.append(",");
               fileWriter.append(String.valueOf(ticketsWithoutAV));
               fileWriter.append(",");
               fileWriter.append(String.valueOf(invalidTickets));
               fileWriter.append(",");
               fileWriter.append(String.valueOf(aVInvalidTickets));
               fileWriter.append(",");
               fileWriter.append(String.valueOf(invalidTicketsWithoutAV));
               fileWriter.append(",");
               fileWriter.append(String.valueOf(ticketsWithoutFixedVersion));
               fileWriter.append(",");
               fileWriter.append(String.valueOf(aVticketsWithoutFixedVersion));
               fileWriter.append(",");
               fileWriter.append(String.valueOf(ticketsWithoutFixedVersionWithoutAV));
               fileWriter.append(",");
               fileWriter.append(String.valueOf(ticketsWithoutOpeningVersion));
               fileWriter.append(",");
               fileWriter.append(String.valueOf(aVticketsWithoutOpeningVersion));
               fileWriter.append(",");
               fileWriter.append(String.valueOf(ticketsWithoutOpeningVersionWithoutAV));
               fileWriter.append(",");
               fileWriter.append(String.valueOf(ticketWithFVEqualsToOV));
               fileWriter.append(",");
               fileWriter.append(String.valueOf(aVticketWithFVEqualsToOV));
               fileWriter.append(",");
               fileWriter.append(String.valueOf(ticketWithFVEqualsToOVWithoutAV));
               fileWriter.append(",");
               fileWriter.append(String.valueOf(ticketsWithoutInjectionVersion));
               fileWriter.append(",");
               fileWriter.append(String.valueOf(aVticketsWithoutInjectionVersion));
               fileWriter.append(",");
               fileWriter.append(String.valueOf(ticketsWithoutInjectionVersionWithoutAV));
               fileWriter.append("\n");

	      
	         } catch (Exception e) {

	            Log.errorLog("Error in csv writer \n");
		        StringWriter sw = new StringWriter();
		        PrintWriter pw = new PrintWriter(sw);
		        e.printStackTrace(pw);
		        Log.errorLog(sw.toString());
		        
	         }
	
		File f = new File(path);
		
		if(!f.exists()) {
		Git.cloneRepository()
		  .setURI("https://github.com/apache/"+ projName)                  //projname potrebbe dover essere maiuscolo
		  .setDirectory(new File(path))
		  .call();
		}
		
		
		Git git = Git.open(new File(path));
		Iterable<RevCommit> projLog = git.log().all().call();    					//gets all commits in log
		List<RevCommit> commitList = new  ArrayList<>();
	
		List<DBEntry> dBEntries = new ArrayList<>();
		List<FileNameAndSize> filePaths = new ArrayList<>();
		
		int allReleasesSize = releasesArray.size();
		int releasesSize = (int) Math.ceil( (double)allReleasesSize/2);

		
		
		//lavoro con commit la cui data sia minore della data dell'ultima release che considero
		for (RevCommit commitLog : projLog) {
			
			LocalDateTime commitDate = Instant.ofEpochSecond(commitLog.getCommitTime()).atZone(ZoneId.of("UTC")).toLocalDateTime();
			
			if(compareDateToReleasesArrayDateTime(releasesArray,commitDate)!=null) {
				commitList.add(commitLog);
			}
		}
		
		//ordino la lista dei commit temporalmente dal piu vecchio al piu giovane
		
		Collections.sort(commitList, (c1, c2) -> {
			
			LocalDateTime commitDate1 = Instant.ofEpochSecond(c1.getCommitTime()).atZone(ZoneId.of("UTC")).toLocalDateTime();
        	LocalDateTime commitDate2 = Instant.ofEpochSecond(c2.getCommitTime()).atZone(ZoneId.of("UTC")).toLocalDateTime();
        	
            return commitDate1.compareTo(commitDate2);
		});
		
		
		// calcolo per ogni release i file presenti in quel momento
		
		
		for(Release r : releasesArray) {
				filePaths.clear();
				RevCommit rLatestCommit = getLatestCommitBeforeRelease(commitList,r.getDate());
				if(rLatestCommit!=null) {
	
					getFilesByCommitAndCalculateLoc(rLatestCommit,filePaths,git);
					
					for(FileNameAndSize fN : filePaths) {
						
						String filePath = fN.getFileName().substring( fN.getFileName().indexOf("/")+1);
						filePath = filePath.replace("/", "\\");
						int dotIndex = filePath.lastIndexOf('.');
						if( ((dotIndex == -1) ? "" : filePath.substring(dotIndex + 1)).equals("java") ){
							
							DBEntry entry = new DBEntry();
							entry.setRelease(r);
							entry.setBugginess("no");
							entry.setFileName(filePath);
							entry.setSize(fN.getSize());
							dBEntries.add(entry);
						}
					}
				}
		}
		
		
		//gestisco la presenza di eventuali rename e quindi alias tra i file
		
		for(RevCommit commit:commitList) {
			
			List<DiffEntry> entries = calculateDiffEntries(commit, git);
			
			if(!entries.isEmpty()) {
				for( DiffEntry entry : entries ) {
					
					String type =  entry.getChangeType().toString();
					String oldPath = entry.getOldPath().substring( entry.getOldPath().indexOf("/")+1);
					String newPath = entry.getNewPath().substring( entry.getNewPath().indexOf("/")+1);
					oldPath = oldPath.replace("/", "\\");
					newPath = newPath.replace("/", "\\");
					
					int dotIndex = oldPath.lastIndexOf('.');
					int dotIndex2 = newPath.lastIndexOf('.');
					
					if(type.equals("RENAME") && ((dotIndex == -1) ? "" : oldPath.substring(dotIndex + 1)).equals("java") && 
							((dotIndex2 == -1) ? "" : newPath.substring(dotIndex2 + 1)).equals("java")) {
						
						
						boolean oPCheck = true;
						boolean nPCheck = true;
						
						for(FileAlias fA : filesAlias) {
							
							if(!fA.checkAlias(oldPath)) {
								oPCheck = false;
								if(fA.checkAlias(newPath)) {
									fA.getAlias().add(newPath);
									nPCheck = false;
								}
							}
							if(!fA.checkAlias(newPath)) {
								nPCheck = false;
								if(fA.checkAlias(oldPath)) {
									fA.getAlias().add(oldPath);
									oPCheck = false;
								}
							}
						}
	
						if(oPCheck && nPCheck) {
							
							FileAlias alias = new FileAlias();
							alias.getAlias().add(oldPath);
							alias.getAlias().add(newPath);
							filesAlias.add(alias);
							
						}	
					}
				}
			}
		}
		
		
		// per ogni insieme di alias di ogni file calcolo il nome piu recente ad esso assocaito 
		
		for(DBEntry e : dBEntries) {
			
			String fileName = e.getFileName();
			
			for(FileAlias fA : filesAlias) {
			
				for(String alias : fA.getAlias()) {
					
					if(alias.equals(fileName) || e.getFileName().contains(alias)) {

							fA.setLastFileName(alias);
					}
				}
			}
		}
		
		
		//per ogni DBEntry dotata di alias, imposto il nome del file come l'ultimo tra gli alias con cui è conosciuto
		
		for(DBEntry e : dBEntries) {
			
			String fileName = e.getFileName();
			
			for(FileAlias fA : filesAlias) {
			
				for(String alias : fA.getAlias()) {
					
					if(alias.equals(fileName) || e.getFileName().contains(alias)) {

							e.setFileName(fA.getLastFileName());
					}
				}
			}
		}
		
		
		//calcolo la bugginess di ogni file 
		
		for(Ticket t : tickets) {
			
			if(!t.getAffectedVersions().isEmpty()) {
				
				List<RevCommit> fixedCommits = new ArrayList<>();
				
				retrieveAndSortCommitsForTicket(t,commitList,fixedCommits);
				
				if(!fixedCommits.isEmpty()) {
						
					for(int i=0;i<fixedCommits.size();i++) {
						
						List<DiffEntry> entries = calculateDiffEntries(fixedCommits.get(i), git);
						
						for(AffectedVersion av : t.getAffectedVersions()) {
						
							for( DiffEntry entry : entries) {
								 
								if(entry.getChangeType().toString().equals(TYPE_MODIFY) || entry.getChangeType().toString().equals(TYPE_DELETE)) {
									
									
									String oldPath = entry.getOldPath().substring( entry.getOldPath().indexOf("/")+1);
									oldPath = oldPath.replace("/", "\\");
									
									for(DBEntry e : dBEntries) {
										
										String alias = checkAlias(oldPath,filesAlias);
										String nameToBeUsed = null ;
										
										if( (alias!=null)  ){
											
											nameToBeUsed = alias;
										
										}else { 
										
											nameToBeUsed = oldPath;
										}
										
										if((e.getRelease().getName().equals(av.getName()) ) && 
												( e.getFileName().contains(nameToBeUsed) || e.getFileName().equals(nameToBeUsed))){
											
											e.setBugginess("yes");
										}	
									}	
								}
							}
						}
					}
				}
			}
		}
		
	
		for(Ticket t : tickets) {
			
			if(t.getAffectedVersions().isEmpty()) {
				
				List<RevCommit> fixedCommits = new ArrayList<>();
				
				retrieveAndSortCommitsForTicket(t,commitList,fixedCommits);
				
				if(!fixedCommits.isEmpty()) {
						
					for(int i=0;i<fixedCommits.size();i++) {
						
						List<DiffEntry> entries = calculateDiffEntries(fixedCommits.get(i), git);
	
						for( DiffEntry entry : entries) {
							 
							if(entry.getChangeType().toString().equals(TYPE_MODIFY) || entry.getChangeType().toString().equals(TYPE_DELETE)) {
								
								
								String oldPath = entry.getOldPath().substring( entry.getOldPath().indexOf("/")+1);
								oldPath = oldPath.replace("/", "\\");
								
								for(DBEntry e : dBEntries) {
									
									String alias = checkAlias(oldPath,filesAlias);
									String nameToBeUsed = null ;
									
									if( (alias!=null)  ){
										
										nameToBeUsed = alias;
									
									}else { 
									
										nameToBeUsed = oldPath;
									}
									
									if(( e.getFileName().contains(nameToBeUsed) || e.getFileName().equals(nameToBeUsed)) 
											&& ( (e.isBugginess()!= null) && (e.isBugginess().equals("no")) )){
										
										e.setBugginess(null);
									}	
								}	
							}
						}
					}
				}
			}
		}
		
	
		//riduco di metà le releases
		
		List<Release> newReleases = new ArrayList<>();
		
		for(int i=0; i<releasesSize; i++) {
			newReleases.add(releasesArray.get(i));
		}
		
		
		//ricreo la commitlist con solo i commit aventi data precedente l'ultima release del nuovo array
		
		List<RevCommit> newCommitList = new ArrayList<>();
		
		for (RevCommit commit : commitList) {
			
			LocalDateTime commitDate = Instant.ofEpochSecond(commit.getCommitTime()).atZone(ZoneId.of("UTC")).toLocalDateTime();
			
			for(Release release : newReleases ) {
				if(release.getDate().compareTo(commitDate) > 0 ) {
					release.getReleaseCommits().add(commit);
				}
			}
			if(compareDateToReleasesArrayDateTime(releasesArray,commitDate)!=null) {
				newCommitList.add(commit);
			}
		}
		
		//ordino la lista dei commit temporalmente dal piu vecchio al piu giovane
		
		Collections.sort(newCommitList, (c1, c2) -> {
			
			LocalDateTime commitDate1 = Instant.ofEpochSecond(c1.getCommitTime()).atZone(ZoneId.of("UTC")).toLocalDateTime();
        	LocalDateTime commitDate2 = Instant.ofEpochSecond(c2.getCommitTime()).atZone(ZoneId.of("UTC")).toLocalDateTime();
        	
            return commitDate1.compareTo(commitDate2);
		});
		
		
		//considero le dBEntries relative alla prima meta delle release
		
		List<DBEntry> newDBEntries = new ArrayList<>();
		int releaseIndex = newReleases.get(newReleases.size() - 1).getIndex();
		
		for(DBEntry entry :dBEntries ) {
			
			if( (entry.getRelease().getIndex() <= releaseIndex)  && (entry.isBugginess()!=null) ) {
				
				newDBEntries.add(entry);
				
			}
			
		}
		
		//calcolo delle metriche eccetto Size che è stata calcolata durante il retrieval dei filepaths 
		
		
		calculateNFix(newCommitList,tickets,newDBEntries,git,filesAlias);
		
		
		calculateMetrics(git,newDBEntries,filesAlias);
	

		//create bugginess CSV
		
		 
		 // ordino le DBEntry in base all indice di versione
		 
		 Collections.sort(dBEntries, (e1, e2) -> Integer.compare(e1.getRelease().getIndex(), e2.getRelease().getIndex()));
				
		//Name of CSV for output
		 
		 String outname = projName + "Bugginess.csv";
		 
		 
		 try(FileWriter fileWriter = new FileWriter(outname);) {
	            
	            fileWriter.append("Release , Filename , Size , LocTouched , NR  , NFix , NAuth  , LocAdded , MaxLocAdded , "
	            		+ "AvgLocAdded , Churn , MaxChurn , AvgChurn , ChgSetSize , MaxChgSetSize , AvgChgSetSize , Bugginess");
	            fileWriter.append("\n");
	            for (DBEntry e : newDBEntries) {
	               
	               if( e.isBugginess()!=null) {
	               
	               fileWriter.append(String.valueOf(e.getRelease().getIndex()));
	               fileWriter.append(",");
	               fileWriter.append(e.getFileName());
	               fileWriter.append(",");
	               fileWriter.append(String.valueOf(e.getSize()));
	               fileWriter.append(",");
	               fileWriter.append(String.valueOf(e.getLocTouched()));
	               fileWriter.append(",");
	               fileWriter.append(String.valueOf(e.getnR()));
	               fileWriter.append(",");
	               fileWriter.append(String.valueOf(e.getnFix()));
	               fileWriter.append(",");
	               fileWriter.append(String.valueOf(e.getDistinctAuthors()));
	               fileWriter.append(",");
	               fileWriter.append(String.valueOf(e.getLocAdded()));
	               fileWriter.append(",");
	               fileWriter.append(String.valueOf(e.getMaxLocAdded()));
	               fileWriter.append(",");
	               fileWriter.append(String.valueOf(e.getAvgLocAdded()));
	               fileWriter.append(",");
	               fileWriter.append(String.valueOf(e.getChurn()));
	               fileWriter.append(",");
	               fileWriter.append(String.valueOf(e.getMaxChurn()));
	               fileWriter.append(",");
	               fileWriter.append(String.valueOf(e.getAvgChurn()));
	               fileWriter.append(",");
	               fileWriter.append(String.valueOf(e.getChgSet()));
	               fileWriter.append(",");
	               fileWriter.append(String.valueOf(e.getMaxChgSetSize()));
	               fileWriter.append(",");
	               fileWriter.append(String.valueOf(e.getAvgChgSetSize()));
	               fileWriter.append(",");
	               fileWriter.append(e.isBugginess());
	               fileWriter.append("\n");
	               
	               }

	            }
	         } catch (Exception e) {

	            Log.errorLog("Error in csv writer \n");
		        StringWriter sw = new StringWriter();
		        PrintWriter pw = new PrintWriter(sw);
		        e.printStackTrace(pw);
		        Log.errorLog(sw.toString());
		        
	         }
	   }
	}
	
