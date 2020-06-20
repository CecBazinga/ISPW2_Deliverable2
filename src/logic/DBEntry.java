package logic;

import java.io.File;

public class DBEntry {
	
	private Release release;
	private File file;
	private String fileName;
	private String bugginess;
	private int size;
	private int nFix ;
	private int nR;
	private int distinctAuthors;
	private int locAdded;
	private int maxLocAdded ;
	private double avgLocAdded ;
	private int locTouched;
	private int churn;
	private int maxChurn;
	private double avgChurn;
	private int chgSet;
	private int maxChgSetSize;
	private double avgChgSetSize;
	
	
	public Release getRelease() {
		return release;
	}
	public void setRelease(Release release) {
		this.release = release;
	}
	public String isBugginess() {
		return bugginess;
	}
	
	public void resetBugginess() {
		this.bugginess = null;
	}
	public void setBugginess(String bugginess) {
		this.bugginess = bugginess;
	}
	public File getFile() {
		return file;
	}
	public void setFile(File file) {
		this.file = file;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public int getnFix() {
		return nFix;
	}
	public void setnFix(int nFix) {
		this.nFix = nFix;
	}
	public int getnR() {
		return nR;
	}
	public void setnR(int nR) {
		this.nR = nR;
	}
	public int getDistinctAuthors() {
		return distinctAuthors;
	}
	public void setDistinctAuthors(int distinctAuthors) {
		this.distinctAuthors = distinctAuthors;
	}
	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;
	}
	public int getLocAdded() {
		return locAdded;
	}
	public void setLocAdded(int locAdded) {
		this.locAdded = locAdded;
	}
	public int getLocTouched() {
		return locTouched;
	}
	public void setLocTouched(int locTouched) {
		this.locTouched = locTouched;
	}
	public int getChurn() {
		return churn;
	}
	public void setChurn(int churn) {
		this.churn = churn;
	}
	public int getMaxLocAdded() {
		return maxLocAdded;
	}
	public void setMaxLocAdded(int maxLocAdded) {
		this.maxLocAdded = maxLocAdded;
	}
	public double getAvgLocAdded() {
		return avgLocAdded;
	}
	public void setAvgLocAdded(double avgLocAdded) {
		this.avgLocAdded = avgLocAdded;
	}
	public int getMaxChurn() {
		return maxChurn;
	}
	public void setMaxChurn(int maxChurn) {
		this.maxChurn = maxChurn;
	}
	public double getAvgChurn() {
		return avgChurn;
	}
	public void setAvgChurn(double avgChurn) {
		this.avgChurn = avgChurn;
	}
	public int getChgSet() {
		return chgSet;
	}
	public void setChgSet(int chgSet) {
		this.chgSet = chgSet;
	}
	public double getAvgChgSetSize() {
		return avgChgSetSize;
	}
	public void setAvgChgSetSize(double avgChgSetSize) {
		this.avgChgSetSize = avgChgSetSize;
	}
	public int getMaxChgSetSize() {
		return maxChgSetSize;
	}
	public void setMaxChgSetSize(int maxChgSetSize) {
		this.maxChgSetSize = maxChgSetSize;
	}
	
	

}
