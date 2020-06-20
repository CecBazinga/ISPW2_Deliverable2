package logic;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.revwalk.RevCommit;

public class Release {

	
	private int index;
	private String id;
	private String name;
	private LocalDateTime date;
	private List<RevCommit> releaseCommits = new ArrayList<RevCommit>();
	
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public LocalDateTime getDate() {
		return date;
	}
	public void setDate(LocalDateTime date) {
		this.date = date;
	}
	public List<RevCommit> getReleaseCommits() {
		return releaseCommits;
	}
	public void setReleaseCommits(List<RevCommit> releaseCommits) {
		this.releaseCommits = releaseCommits;
	}
	
}
