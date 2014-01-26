/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openforis.collect.designer.form.FormObject;
import org.openforis.collect.designer.form.SpatialReferenceSystemFormObject;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.SpatialReferenceSystem;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zk.ui.select.annotation.VariableResolver;

/**
 * 
 * @author S. Ricci
 *
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class SpatialReferenceSystemsVM extends SurveyObjectBaseVM<SpatialReferenceSystem> {

	private String selectedPredefinedSrsCode;
	
	@Override
	@Init(superclass=false)
	public void init() {
		super.init();
	}
	
	@Override
	public List<SpatialReferenceSystem> getItemsInternal() {
		CollectSurvey survey = getSurvey();
		List<SpatialReferenceSystem> spatialReferenceSystems = survey.getSpatialReferenceSystems();
		return spatialReferenceSystems;
	}
	
	@Override
	protected SpatialReferenceSystem createItemInstance() {
		SpatialReferenceSystem instance = new SpatialReferenceSystem();
		return instance;
	}

	@Override
	protected void addNewItemToSurvey() {
		CollectSurvey survey = getSurvey();
		survey.addSpatialReferenceSystem(editedItem);
	}

	@Override
	protected void deleteItemFromSurvey(SpatialReferenceSystem item) {
		CollectSurvey survey = getSurvey();
		survey.removeSpatialReferenceSystem(item);
	}
	
	@Override
	protected FormObject<SpatialReferenceSystem> createFormObject() {
		return new SpatialReferenceSystemFormObject();
	}
	
	@Override
	protected void moveSelectedItem(int indexTo) {
		survey.moveSpatialReferenceSystem(selectedItem, indexTo);
	}
	
	public List<String> getAvailablePredefinedSRSs() {
		List<SpatialReferenceSystem> currentSRSs = survey.getSpatialReferenceSystems();
		List<String> currentSRSCodes = new ArrayList<String>();
		for (SpatialReferenceSystem srs : currentSRSs) {
			currentSRSCodes.add(srs.getId());
		}
		//TODO call service to get srs list
		Set<String> availableSRSs = new HashSet<String>(Arrays.asList("EPSG:21035", "EPSG:21036", "EPSG:21037", "EPSG:21038", "EPSG:21039", "EPSG:21040"));
		
		List<String> result = new ArrayList<String>(availableSRSs);
		result.removeAll(currentSRSCodes);
		Collections.sort(result);
		return result;
	}
	
	@Command
	public void addPredefinedSrs() {
		//TODO call service to get srs details from EPSG authority
		SpatialReferenceSystem srs = new SpatialReferenceSystem();
		srs.setId(selectedPredefinedSrsCode);
		
		survey.addSpatialReferenceSystem(srs);
		selectedPredefinedSrsCode = null;
		notifyChange("items", "selectedPredefinedSrsCode", "availablePredefinedSRSs");
	}

	public String getSelectedPredefinedSrsCode() {
		return selectedPredefinedSrsCode;
	}
	
	public void setSelectedPredefinedSrsCode(String selectedPredefinedSrsCode) {
		this.selectedPredefinedSrsCode = selectedPredefinedSrsCode;
	}
}
