/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import org.openforis.collect.designer.form.ItemFormObject;
import org.openforis.collect.designer.form.SpatialReferenceSystemFormObject;
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
		return new BindingListModelList<SpatialReferenceSystem>(survey.getSpatialReferenceSystems(), false);
	}

	@Override
	protected void addNewItemToSurvey() {
		survey.addSpatialReferenceSystem(editedItem);
	}

	@Override
	protected void deleteItemFromSurvey(SpatialReferenceSystem item) {
		survey.removeSpatialReferenceSystem(item);
	}
	
	@Override
	protected ItemFormObject<SpatialReferenceSystem> createFormObject() {
		return new SpatialReferenceSystemFormObject();
	}
}
