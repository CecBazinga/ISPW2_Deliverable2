package logic;

import java.util.ArrayList;
import java.util.List;

public class FileAlias {

	
	private String LastFileName;
	private List<String> alias = new ArrayList<String>();
	
	public List<String> getAlias() {
		return alias;
	}
	public void setAlias(List<String> alias) {
		this.alias = alias;
	}
	public String getLastFileName() {
		return LastFileName;
	}
	public void setLastFileName(String lastFileName) {
		LastFileName = lastFileName;
	}
	
	public boolean checkAlias(String fileName) {
		
		for(String a : alias) {
			
			if(a.equals(fileName)) {
				return false;
			}
		}
		
		return true;
	}
	
}
