package org.openforis.collect.datacleansing.form;

import java.util.ArrayList;
import java.util.List;

import org.openforis.collect.datacleansing.DataQuery;
import org.openforis.collect.datacleansing.DataQueryGroup;
import org.openforis.commons.collection.CollectionUtils;
import org.openforis.commons.web.Forms;

/**
 * 
 * @author S. Ricci
 *
 */
public class DataQueryGroupForm extends DataCleansingItemForm<DataQueryGroup> {

	private String title;
	private String description;
	private List<Integer> queryIds;
	private transient List<DataQueryForm> queries;
	
	public DataQueryGroupForm() {
		super();
		this.queries = new ArrayList<DataQueryForm>();
		this.queryIds = new ArrayList<Integer>();
	}
	
	public DataQueryGroupForm(DataQueryGroup group) {
		super(group);
		List<DataQuery> queries = group.getQueries();
		this.queries = Forms.toForms(queries, DataQueryForm.class);
		this.queryIds = CollectionUtils.project(queries, "id");
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
	
	public List<DataQueryForm> getQueries() {
		return queries;
	}
	
	public List<Integer> getQueryIds() {
		return queryIds;
	}
	
	public void setQueryIds(List<Integer> queryIds) {
		this.queryIds = queryIds;
	}
	
}
