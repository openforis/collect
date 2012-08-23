/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import java.util.List;

import org.apache.commons.beanutils.BeanUtils;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.Unit;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Textbox;

/**
 * 
 * @author S. Ricci
 *
 */
public class SurveyUnitsEditVM {
	
//	@WireVariable
//	private SurveyManager surveyManager;
	
	private CollectSurvey survey;
	
	private Unit selectedUnit;
	
	private Unit editedUnit;
	
	@Wire
	Textbox labelTextBox;

	public SurveyUnitsEditVM() {
		survey = new CollectSurvey();
	}
	
	@Listen("onChange=#labelTextBox")
    public void onChangeLabel() {
		String text = labelTextBox.getValue();
		editedUnit.setLabel("eng", text);
	}
	
	@NotifyChange({"editingUnit","editedUnit","selectedUnit"})
	@Command
	public void newUnit() {
		setSelectedUnit(null);
		setEditedUnit(new Unit());
	}
	
	public List<Unit> getUnits() {
		return survey.getUnits();
	}
	
	@NotifyChange("units")
	@Command
	public void saveUnit() {
		if ( selectedUnit == null ) {
			survey.addUnit(editedUnit);
			setSelectedUnit(editedUnit);
		} else {
			try {
				BeanUtils.copyProperties(selectedUnit, editedUnit);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	@NotifyChange("units")
	@Command
	public void deleteUnit(@BindingParam("unit") Unit unit) {
		survey.removeUnit(unit);
	}
	
	public CollectSurvey getSurvey() {
		return survey;
	}

	public Unit getSelectedUnit() {
		return selectedUnit;
	}

	@NotifyChange("editedUnit")
	public void setSelectedUnit(Unit unit) {
		selectedUnit = unit;
		if ( unit != null ) {
			try {
				setEditedUnit((Unit) BeanUtils.cloneBean(unit));
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	public Unit getEditedUnit() {
		return editedUnit;
	}

	@NotifyChange("editingUnit")
	public void setEditedUnit(Unit editedUnit) {
		this.editedUnit = editedUnit;
	}
	
	public boolean isEditingUnit() {
		return this.editedUnit != null;
	}

	
}
