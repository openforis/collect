/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openforis.collect.designer.form.FormObject;
import org.openforis.collect.designer.form.SpatialReferenceSystemFormObject;
import org.openforis.collect.geospatial.GeoToolsCoordinateOperations;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.SpatialReferenceSystem;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.Binder;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.Init;

/**
 * 
 * @author S. Ricci
 *
 */
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
		return new SpatialReferenceSystem();
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
	protected void moveSelectedItemInSurvey(int indexTo) {
		survey.moveSpatialReferenceSystem(selectedItem, indexTo);
	}
	
	public List<String> getAvailablePredefinedSRSs() {
		List<SpatialReferenceSystem> currentSRSs = survey.getSpatialReferenceSystems();
		List<String> insertedSRSCodes = new ArrayList<String>();
		for (SpatialReferenceSystem srs : currentSRSs) {
			insertedSRSCodes.add(srs.getId());
		}
		GeoToolsCoordinateOperations coordinateOperations = 
				(GeoToolsCoordinateOperations) survey.getContext().getCoordinateOperations();
		Set<String> availableSRSs = coordinateOperations.getAvailableSRSs();
		
		List<String> result = new ArrayList<String>(availableSRSs);
		result.removeAll(insertedSRSCodes);
		Collections.sort(result);
		
		return result;
	}
	
	@Command
	public void addPredefinedSrs() {
		checkCanLeaveForm(new CanLeaveFormConfirmHandler() {
			@Override
			public void onOk(boolean confirmed) {
				Set<String> languages = new HashSet<String>(survey.getLanguages());
				
				GeoToolsCoordinateOperations coordinateOperations = 
						(GeoToolsCoordinateOperations) survey.getContext().getCoordinateOperations();
				SpatialReferenceSystem srs = coordinateOperations.fetchSRS(selectedPredefinedSrsCode, languages);
				
				survey.addSpatialReferenceSystem(srs);
				selectedPredefinedSrsCode = null;
				notifyChange("items", "selectedPredefinedSrsCode", "availablePredefinedSRSs");
				dispatchSurveyChangedCommand();
			}
		});
	}

	public String getSelectedPredefinedSrsCode() {
		return selectedPredefinedSrsCode;
	}
	
	public void setSelectedPredefinedSrsCode(String selectedPredefinedSrsCode) {
		this.selectedPredefinedSrsCode = selectedPredefinedSrsCode;
	}
	
	@Command
	public void apply(@ContextParam(ContextType.BINDER) final Binder binder) {
		checkCanLeaveForm(new CanLeaveFormConfirmHandler() {
			@Override
			public void onOk(boolean confirmed) {
				BindUtils.postGlobalCommand(null, null, "closeSRSManagerPopUp", null);
			}
		});
	}
}
