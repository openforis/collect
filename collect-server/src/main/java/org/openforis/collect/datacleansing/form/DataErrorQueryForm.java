package org.openforis.collect.datacleansing.form;

import javax.validation.constraints.NotNull;

import org.openforis.collect.datacleansing.DataErrorQuery;

/**
 * 
 * @author S. Ricci
 *
 */
public class DataErrorQueryForm extends DataCleansingItemForm<DataErrorQuery> {

	@NotNull
	private Integer typeId;
	@NotNull
	private Integer queryId;
	
	//calculated members
	private String typeCode;
	private String queryTitle;
	private String queryDescription;

	public DataErrorQueryForm() {
		super();
	}
	
	public DataErrorQueryForm(DataErrorQuery query) {
		super(query);
		typeCode = query == null ? null: query.getType() == null ? null: query.getType().getCode();
		queryTitle = query == null ? null: query.getQuery().getTitle();
		queryDescription = query == null ? null: query.getQuery().getDescription();
	}
	
	public String getTypeCode() {
		return typeCode;
	}
	
	public String getQueryTitle() {
		return queryTitle;
	}
	
	public String getQueryDescription() {
		return queryDescription;
	}
	
	public Integer getTypeId() {
		return typeId;
	}
	
	public void setTypeId(Integer typeId) {
		this.typeId = typeId;
	}
	
	public Integer getQueryId() {
		return queryId;
	}
	
	public void setQueryId(Integer queryId) {
		this.queryId = queryId;
	}
	
}
