package org.openforis.collect.dataview;

import java.util.ArrayList;
import java.util.List;

import org.openforis.collect.event.RecordStep;

public class QueryDto {
	
	private String surveyName;
	private RecordStep recordStep;
	private int contextEntityDefinitionId;
	private List<QueryColumnDto> columns = new ArrayList<QueryColumnDto>();
	private List<QueryFilter> filter = new ArrayList<QueryFilter>();
	private int page;
	private int recordsPerPage;
	
	public String getSurveyName() {
		return surveyName;
	}

	public void setSurveyName(String surveyName) {
		this.surveyName = surveyName;
	}

	public RecordStep getRecordStep() {
		return recordStep;
	}

	public void setRecordStep(RecordStep recordStep) {
		this.recordStep = recordStep;
	}
	
	public int getContextEntityDefinitionId() {
		return contextEntityDefinitionId;
	}
	
	public void setContextEntityDefinitionId(int contextEntityDefinitionId) {
		this.contextEntityDefinitionId = contextEntityDefinitionId;
	}
	
	public List<QueryColumnDto> getColumns() {
		return columns;
	}
	
	public void setColumns(List<QueryColumnDto> columns) {
		this.columns = columns;
	}
	
	public List<QueryFilter> getFilter() {
		return filter;
	}
	
	public void setFilter(List<QueryFilter> filter) {
		this.filter = filter;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public int getRecordsPerPage() {
		return recordsPerPage;
	}

	public void setRecordsPerPage(int recordsPerPage) {
		this.recordsPerPage = recordsPerPage;
	}
}