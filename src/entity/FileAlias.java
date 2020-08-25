package entity;

import java.util.ArrayList;
import java.util.List;

public class FileAlias {

	
	private String lastFileName;
	private List<String> alias = new ArrayList<>();
	
	public List<String> getAlias() {
		return alias;
	}
	public void setAlias(List<String> alias) {
		this.alias = alias;
	}
	public String getLastFileName() {
		return lastFileName;
	}
	public void setLastFileName(String lastFileName) {
		this.lastFileName = lastFileName;
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
