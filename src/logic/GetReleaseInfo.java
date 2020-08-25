package logic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Collections;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.json.JSONException;
import org.json.JSONObject;

import entity.Release;

import org.json.JSONArray;
import java.nio.charset.StandardCharsets;

public class GetReleaseInfo {
	
    protected static HashMap<LocalDateTime, String> releaseNames;
    protected static HashMap<LocalDateTime, String> releaseID;
    protected static ArrayList<LocalDateTime> releases;
    

	private GetReleaseInfo() {
		   
	}
	   
	public static void getProjectReleases(String project, List<Release> releasesArray) throws IOException, JSONException {
		   
		   String projName = project;
		   //Fills the arraylist with releases dates and orders them
		   //Ignores releases with missing dates
		   releases = new ArrayList<>();
		         Integer i;
		         String url = "https://issues.apache.org/jira/rest/api/2/project/" + projName;
		         JSONObject json = readJsonFromUrl(url);
		         JSONArray versions = json.getJSONArray("versions");
		         releaseNames = new HashMap<>();
		         releaseID = new HashMap<> ();
		         
		        
	        	 
		         for (i = 0; i < versions.length(); i++ ) {
		            String name = "";
		            String id = "";
		            if(versions.getJSONObject(i).has("releaseDate")) {
		               if (versions.getJSONObject(i).has("name"))
		                  name = versions.getJSONObject(i).get("name").toString();
		               if (versions.getJSONObject(i).has("id"))
		                  id = versions.getJSONObject(i).get("id").toString();
		               addRelease(versions.getJSONObject(i).get("releaseDate").toString(),
		                          name,id);
		            }
		         }
	         
		         
		         // order releases by date
		         Collections.sort(releases, (o1, o2) ->  o1.compareTo(o2));
		       
		         if (releases.size() < 6)
		            return;
		         
	             
	             for ( i = 0; i < releases.size(); i++) {
	                Integer index = i + 1;
	                Release release = new Release();
	                release.setIndex(index);
	                release.setId(releaseID.get(releases.get(i)));
	                release.setName(releaseNames.get(releases.get(i)));
	                release.setDate(releases.get(i));
	                releasesArray.add(release);
	             }
		   }
 
	
	   public static void addRelease(String strDate, String name, String id) {
		      LocalDate date = LocalDate.parse(strDate);
		      LocalDateTime dateTime = date.atStartOfDay();
		      if (!releases.contains(dateTime))
		         releases.add(dateTime);
		      releaseNames.put(dateTime, name);
		      releaseID.put(dateTime, id);
		   }


	   public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
	     
	      try(InputStream is = new URL(url).openStream()) {
	         BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8.name()));
	         String jsonText = readAll(rd);
	         
	         return  new JSONObject(jsonText);
	         
	       } 
		
	   }
	   
	   private static String readAll(Reader rd) throws IOException {
		      StringBuilder sb = new StringBuilder();
		      int cp;
		      while ((cp = rd.read()) != -1) {
		         sb.append((char) cp);
		      }
		      return sb.toString();
		   }

	
}