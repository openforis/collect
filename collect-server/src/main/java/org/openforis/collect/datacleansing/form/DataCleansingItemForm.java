package org.openforis.collect.datacleansing.form;

import java.util.Date;
import java.util.UUID;

import org.openforis.collect.datacleansing.DataCleansingItem;
import org.openforis.collect.datacleansing.json.CollectDateSerializer;
import org.openforis.commons.web.PersistedObjectForm;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * 
 * @author S. Ricci
 */
public abstract class DataCleansingItemForm<T extends DataCleansingItem> extends PersistedObjectForm<Integer, T> {

	private UUID uuid;
	private Date creationDate;
	private Date modifiedDate;
	
	public DataCleansingItemForm() {
		super();
	}

	public DataCleansingItemForm(T obj) {
		super(obj);
	}

	@JsonSerialize(using = CollectDateSerializer.class)
	public Date getCreationDate() {
		return creationDate;
	}
	
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}
	
	@JsonSerialize(using = CollectDateSerializer.class)
	public Date getModifiedDate() {
		return modifiedDate;
	}
	
	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}

	public UUID getUuid() {
		return uuid;
	}
	
	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}
}
