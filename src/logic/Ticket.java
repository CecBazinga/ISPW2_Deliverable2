package logic;

import java.util.ArrayList;
import java.util.List;

public class Ticket {

	private String expand;
	private String id;
	private String key;
	private String createdDate;
	private String resolutionDate;
	private List<AffectedVersion> affectedVersions;


	public void initializeAV() {
		
		this.affectedVersions = new ArrayList<>();
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getResolutionDate() {
		return resolutionDate;
	}

	public void setResolutionDate(String resolutionDate) {
		this.resolutionDate = resolutionDate;
	}

	public String getCreated() {
		return createdDate;
	}

	public void setCreated(String createdDate) {
		this.createdDate = createdDate;
	}

	public List<AffectedVersion> getAffectedVersions() {
		return affectedVersions;
	}

	public void setAffectedVersions(List<AffectedVersion> affectedVersions) {
		this.affectedVersions = affectedVersions;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getExpand() {
		return expand;
	}

	public void setExpand(String expand) {
		this.expand = expand;
	}
	
}
