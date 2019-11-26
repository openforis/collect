package org.openforis.collect.datacleansing.form;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.datacleansing.DataCleansingStep;
import org.openforis.collect.datacleansing.DataCleansingStep.DataCleansingStepType;
import org.openforis.collect.datacleansing.DataCleansingStepValue;
import org.openforis.collect.datacleansing.DataQuery;

/**
 * 
 * @author S. Ricci
 *
 */
public class DataCleansingStepForm extends DataCleansingItemForm<DataCleansingStep> {

	private Integer queryId;
	private String title;
	private String description;
	private String typeCode;

	private List<DataCleansingStepValue> updateValues;
	
	//calculated members
	private String queryTitle;
	private String queryDescription;
	
	public DataCleansingStepForm() {
		super();
		this.updateValues = new ArrayList<DataCleansingStepValue>();
	}
	
	public DataCleansingStepForm(DataCleansingStep step) {
		super(step);
		DataQuery query = step.getQuery();
		if (query != null) {
			queryTitle = step == null ? null: query.getTitle();
			queryDescription = step == null ? null: query.getDescription();
		}
		List<DataCleansingStepValue> values = step.getUpdateValues();
		this.updateValues = new ArrayList<DataCleansingStepValue>(values);
		this.typeCode = String.valueOf(step.getType().getCode());
	}
	
	public DataCleansingStepType getType() {
		return StringUtils.isBlank(typeCode) 
				? DataCleansingStepType.ATTRIBUTE_UPDATE 
				: DataCleansingStepType.fromCode(typeCode.charAt(0));
	}
	
	public String getQueryTitle() {
		return queryTitle;
	}
	
	public String getQueryDescription() {
		return queryDescription;
	}
	
	public Integer getQueryId() {
		return queryId;
	}
	
	public void setQueryId(Integer queryId) {
		this.queryId = queryId;
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getTypeCode() {
		return typeCode;
	}
	
	public void setTypeCode(String typeCode) {
		this.typeCode = typeCode;
	}
	
	public List<DataCleansingStepValue> getUpdateValues() {
		return updateValues;
	}
	
	public void setUpdateValues(List<DataCleansingStepValue> values) {
		this.updateValues = values;
	}
	
}
