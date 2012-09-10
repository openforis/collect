/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import java.util.List;

import org.openforis.collect.designer.form.ItemFormObject;
import org.openforis.collect.designer.form.SpatialReferenceSystemFormObject;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.SpatialReferenceSystem;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zkplus.databind.BindingListModelList;

/**
 * 
 * @author S. Ricci
 *
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class SurveySRSEditVM extends SurveyItemEditVM<SpatialReferenceSystem> {

	@Override
	public BindingListModelList<SpatialReferenceSystem> getItems() {
		CollectSurvey survey = getSurvey();
		List<SpatialReferenceSystem> spatialReferenceSystems = survey.getSpatialReferenceSystems();
		return new BindingListModelList<SpatialReferenceSystem>(spatialReferenceSystems, false);
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
	protected ItemFormObject<SpatialReferenceSystem> createFormObject() {
		return new SpatialReferenceSystemFormObject();
	}
}
