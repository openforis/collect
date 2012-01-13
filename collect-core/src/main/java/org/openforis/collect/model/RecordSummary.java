/**
 * 
 */
package org.openforis.collect.model;

import java.util.Date;
import java.util.List;

/**
 * @author M. Togna
 * 
 */
public class RecordSummary {

	private String id;
	private String createdBy;
	private Date creationDate;
	private int errorCount;
	private String modifiedBy;
	private Date modifiedDate;
	private int warningCount;
	private int step;
	private List<String> rootEntityKeys;

	public RecordSummary(String id, int errorCount, int warningCount, String createdBy, Date creationDate, String modifiedBy, Date modifiedDate, int step) {
		this.id = id;
		this.errorCount = errorCount;
		this.warningCount = warningCount;
		this.createdBy = createdBy;
		this.creationDate = creationDate;
		this.modifiedBy = modifiedBy;
		this.modifiedDate = modifiedDate;
		this.step = step;
	}

	public String getCreatedBy() {
		return this.createdBy;
	}

	public Date getCreationDate() {
		return this.creationDate;
	}

	public int getErrorCount() {
		return this.errorCount;
	}

	public String getId() {
		return this.id;
	}

	public String getModifiedBy() {
		return this.modifiedBy;
	}

	public Date getModifiedDate() {
		return this.modifiedDate;
	}

	public int getWarningCount() {
		return this.warningCount;
	}

	public int getStep() {
		return step;
	}

	public List<String> getRootEntityKeys() {
		return rootEntityKeys;
	}

}
