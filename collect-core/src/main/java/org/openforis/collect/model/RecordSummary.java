/**
 * 
 */
package org.openforis.collect.model;

import java.util.Date;
import java.util.Map;

/**
 * @author M. Togna
 * 
 */
public class RecordSummary {

	private Integer id;
	private String createdBy;
	private Date creationDate;
	private int errorCount;
	private int missingCount;
	private String modifiedBy;
	private Date modifiedDate;
	private int skippedCount;
	private int warningCount;
	private int step;
	private Map<String, String> rootEntityKeys;
	private Map<String, Integer> entityCounts;

	public RecordSummary(Integer id, Map<String, String> rootEntityKeys,  Map<String, Integer> entityCounts, String createdBy, Date creationDate, String modifiedBy, Date modifiedDate, int step, 
			int skippedCount, int missingCount, int errorCount, int warningCount) {
		this.id = id;
		this.rootEntityKeys = rootEntityKeys;
		this.entityCounts = entityCounts;
		this.createdBy = createdBy;
		this.creationDate = creationDate;
		this.modifiedBy = modifiedBy;
		this.modifiedDate = modifiedDate;
		this.step = step;
		this.skippedCount = skippedCount;
		this.missingCount = missingCount;
		this.errorCount = errorCount;
		this.warningCount = warningCount;
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

	public Integer getId() {
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

	public Map<String, String> getRootEntityKeys() {
		return rootEntityKeys;
	}

	public Map<String, Integer> getEntityCounts() {
		return entityCounts;
	}

	public int getMissingCount() {
		return missingCount;
	}

	public int getSkippedCount() {
		return skippedCount;
	}

}
