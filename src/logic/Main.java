package logic;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.json.JSONException;

public class Main {

	public static void main(String[] args) {
		
		//String projName = "Bookkeeper" ;
		String projName = "OpenJPA" ;
		
		String path ="C:\\Users\\Utente\\Desktop\\ISPW2\\Falessi\\progetti\\"+projName;
		
		Log.setupLogger();
		int releasesSize = 0;
		
		try {
			EvaluateFixedBugs.evaluate(projName,path);
			
		} catch (IOException | JSONException | GitAPIException e) {
			
			Log.errorLog("Error while calculating fixed bugs per month \n");
	        StringWriter sw = new StringWriter();
	        PrintWriter pw = new PrintWriter(sw);
	        e.printStackTrace(pw);
	        Log.errorLog(sw.toString());
		}
		
		try {
			
			releasesSize = CalculateBugginess.calculateBugginess(projName, path);
			
		} catch (IOException | JSONException | GitAPIException e) {
			
			Log.errorLog("Error while calculating bugginess \n");
	        StringWriter sw = new StringWriter();
	        PrintWriter pw = new PrintWriter(sw);
	        e.printStackTrace(pw);
	        Log.errorLog(sw.toString());
		}
		
		
		try {
			
			TrainClassifiers.train(projName,releasesSize);
			
		} catch (Exception e) {
			
			Log.errorLog("Error while training classifiers \n");
	        StringWriter sw = new StringWriter();
	        e.printStackTrace(new PrintWriter(sw));
	        Log.errorLog(sw.toString());
	        
		}
		
	}

}
