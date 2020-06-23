package logic;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.json.JSONException;

public class Main {

	public static void main(String[] args) {
		
		String projName = "Bookkeeper" ;
		String path ="C:\\Users\\Utente\\Desktop\\ISPW2\\Falessi\\progetti\\"+projName;
		
		Log.setupLogger();
		
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
			
			CalculateBugginess.calculateBugginess(projName, path);
			
		} catch (IOException | JSONException | GitAPIException e) {
			
			Log.errorLog("Error while calculating bugginess \n");
	        StringWriter sw = new StringWriter();
	        PrintWriter pw = new PrintWriter(sw);
	        e.printStackTrace(pw);
	        Log.errorLog(sw.toString());
		}
		
		
		
	}

}
