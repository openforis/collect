/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import java.util.List;

import org.openforis.collect.designer.form.FormObject;
import org.openforis.collect.designer.form.SpatialReferenceSystemFormObject;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.SpatialReferenceSystem;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zk.ui.select.annotation.VariableResolver;

/**
 * 
 * @author S. Ricci
 *
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class SpatialReferenceSystemsVM extends SurveyObjectBaseVM<SpatialReferenceSystem> {

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
}
