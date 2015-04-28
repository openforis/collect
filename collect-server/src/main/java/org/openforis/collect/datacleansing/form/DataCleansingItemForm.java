package org.openforis.collect.datacleansing.form;

import java.util.Date;

import org.openforis.collect.datacleansing.json.CollectDateSerializer;
import org.openforis.commons.web.PersistedObjectForm;
import org.openforis.idm.metamodel.PersistedSurveyObject;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * 
 * @author S. Ricci
 */
public abstract class DataCleansingItemForm<T extends PersistedSurveyObject> extends PersistedObjectForm<T> {

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

}
