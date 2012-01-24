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
	private Integer errors;
	private Integer missing;
	private String modifiedBy;
	private Date modifiedDate;
	private Integer skipped;
	private Integer warnings;
	private int step;
	private Map<String, String> rootEntityKeys;
	private Map<String, Integer> entityCounts;

	public RecordSummary(Integer id, Map<String, String> rootEntityKeys,  Map<String, Integer> entityCounts, String createdBy, Date creationDate, String modifiedBy, Date modifiedDate, int step, 
			Integer skippedCount, Integer missing, Integer errors, Integer warnings) {
		this.id = id;
		this.rootEntityKeys = rootEntityKeys;
		this.entityCounts = entityCounts;
		this.createdBy = createdBy;
		this.creationDate = creationDate;
		this.modifiedBy = modifiedBy;
		this.modifiedDate = modifiedDate;
		this.step = step;
		this.skipped = skippedCount;
		this.missing = missing;
		this.errors = errors;
		this.warnings = warnings;
	}

	public String getCreatedBy() {
		return this.createdBy;
	}

	public Date getCreationDate() {
		return this.creationDate;
	}

	public Integer getErrors() {
		return this.errors;
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

	public Integer getWarnings() {
		return this.warnings;
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

	public Integer getMissing() {
		return missing;
	}

	public Integer getSkipped() {
		return skipped;
	}

}
