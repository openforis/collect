package org.openforis.collect.metamodel;

import java.util.ArrayList;
import java.util.List;

import org.openforis.collect.persistence.xml.CeoApplicationOptions;

public class SurveyView {
	
	private Integer id;
	private String name;
	private boolean temporary;
	private SurveyTarget target;
	List<CodeListView> codeLists = new ArrayList<CodeListView>();
	private List<EntityDefView> rootEntities = new ArrayList<EntityDefView>();
	CeoApplicationOptions ceoApplicationOptions;
	
	public SurveyView(Integer id, String name, boolean temporary, SurveyTarget target) {
		this.id = id;
		this.name = name;
		this.temporary = temporary;
		this.target = target;
	}
	
	public Integer getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public boolean isTemporary() {
		return temporary;
	}
	
	public void setTemporary(boolean temporary) {
		this.temporary = temporary;
	}
	
	public SurveyTarget getTarget() {
		return target;
	}
	
	public void setTarget(SurveyTarget target) {
		this.target = target;
	}
	
	public void addRootEntity(EntityDefView rootEntity) {
		rootEntities.add(rootEntity);
	}
	
	public List<EntityDefView> getRootEntities() {
		return rootEntities;
	}
	
	public List<CodeListView> getCodeLists() {
		return codeLists;
	}
	
	public CeoApplicationOptions getCeoApplicationOptions() {
		return ceoApplicationOptions;
	}
	
	public void setCeoApplicationOptions(CeoApplicationOptions ceoApplicationOptions) {
		this.ceoApplicationOptions = ceoApplicationOptions;
	}
}