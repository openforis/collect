/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import org.openforis.collect.designer.form.ItemFormObject;
import org.openforis.collect.designer.form.UnitFormObject;
import org.openforis.idm.metamodel.Unit;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zkplus.databind.BindingListModelList;

/**
 * 
 * @author S. Ricci
 *
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class SurveyUnitsEditVM extends SurveyItemEditVM<Unit> {

	private static final String UNITS_UPDATED_GLOBAL_COMMAND = "unitsUpdated";

	@Override
	public BindingListModelList<Unit> getItems() {
		return new BindingListModelList<Unit>(survey.getUnits(), false);
	}

	@Override
	protected void addNewItemToSurvey() {
		survey.addUnit(editedItem);
	}

	@Override
	protected void deleteItemFromSurvey(Unit item) {
		survey.removeUnit(item);
	}

	@Override
	protected ItemFormObject<Unit> createFormObject() {
		return new UnitFormObject();
	}
	
	@Override
	@Command
	@NotifyChange({"editedItem","selectedItem"})
	public void applyChanges() {
		super.applyChanges();
		BindUtils.postGlobalCommand(null, null, UNITS_UPDATED_GLOBAL_COMMAND, null);
	}

}
