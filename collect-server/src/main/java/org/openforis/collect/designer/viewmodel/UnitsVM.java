/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import java.util.ArrayList;
import java.util.List;

import org.openforis.collect.designer.form.SurveyObjectFormObject;
import org.openforis.collect.designer.form.UnitFormObject;
import org.openforis.collect.designer.form.UnitFormObject.Dimension;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.Unit;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.select.annotation.VariableResolver;

/**
 * 
 * @author S. Ricci
 *
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class UnitsVM extends SurveyObjectBaseVM<Unit> {

	private static final String UNITS_UPDATED_GLOBAL_COMMAND = "unitsUpdated";

	@Override
	protected List<Unit> getItemsInternal() {
		CollectSurvey survey = getSurvey();
		List<Unit> units = survey.getUnits();
		return units;
	}
	
	@Override
	protected Unit createItemInstance() {
		Unit instance = survey.createUnit();
		return instance;
	}

	@Override
	protected void addNewItemToSurvey() {
		CollectSurvey survey = getSurvey();
		survey.addUnit(editedItem);
	}

	@Override
	protected void deleteItemFromSurvey(Unit item) {
		CollectSurvey survey = getSurvey();
		survey.removeUnit(item);
	}
	
	@Override
	protected void moveSelectedItem(int indexTo) {
		survey.moveUnit(selectedItem, indexTo);
	}

	@Override
	protected SurveyObjectFormObject<Unit> createFormObject() {
		return new UnitFormObject();
	}
	
	@Override
	@Command
	@NotifyChange({"editedItem","selectedItem"})
	public void applyChanges() {
		super.applyChanges();
		BindUtils.postGlobalCommand(null, null, UNITS_UPDATED_GLOBAL_COMMAND, null);
	}
	
	public List<String> getAvailableDimensions() {
		Dimension[] dimensions = Dimension.values();
		List<String> result = new ArrayList<String>(dimensions.length);
		for (Dimension dimension : dimensions) {
			String label = dimension.getLabel();
			result.add(label);
		}
		return result;
	}
	
}
