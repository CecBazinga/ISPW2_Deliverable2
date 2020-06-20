package logic;

public class GraphMonth {

	private String monthDate;
	private int fixedBugs;
	

	public  GraphMonth(String month, int bugs) {
		this.monthDate = month ;
		this.fixedBugs = bugs ;
	}
	
	public String getDate() {
		return this.monthDate;
	}
	
	public int getBugsNumber() {
		return this.fixedBugs;
	}
	
	public void setDate(String month) {
		this.monthDate = month ;
	}
	
	public void setFixedBugs(int bugs) {
		this.fixedBugs = bugs;
	}
}
