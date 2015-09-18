package org.openforis.collect.datacleansing.form;

import java.util.ArrayList;
import java.util.List;

import org.openforis.collect.datacleansing.DataErrorQuery;
import org.openforis.collect.datacleansing.DataErrorQueryGroup;
import org.openforis.commons.web.Forms;

/**
 * 
 * @author S. Ricci
 *
 */
public class DataErrorQueryGroupForm extends DataCleansingItemForm<DataErrorQueryGroup> {

	private String title;
	private String description;
	private List<DataErrorQueryForm> queries;
	private List<Integer> queryIds;
	
	public DataErrorQueryGroupForm() {
		super();
		this.queries = new ArrayList<DataErrorQueryForm>();
		this.queryIds = new ArrayList<Integer>();
	}
	
	public DataErrorQueryGroupForm(DataErrorQueryGroup group) {
		super(group);
		List<DataErrorQuery> queries = group.getQueries();
		this.queries = Forms.toForms(queries, DataErrorQueryForm.class);
		this.queryIds = new ArrayList<Integer>(queries.size());
		for (DataErrorQuery query : queries) {
			this.queryIds.add(query.getId());
		}
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
	
	public List<DataErrorQueryForm> getQueries() {
		return queries;
	}
	
	public List<Integer> getQueryIds() {
		return queryIds;
	}
	
	public void setQueryIds(List<Integer> queryIds) {
		this.queryIds = queryIds;
	}
	
}
