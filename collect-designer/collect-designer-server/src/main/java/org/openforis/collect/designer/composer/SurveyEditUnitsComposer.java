package org.openforis.collect.designer.composer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openforis.collect.designer.viewmodel.SurveyVM;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.Unit;
import org.zkoss.bind.BindComposer;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Textbox;

@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class SurveyEditUnitsComposer extends BindComposer<Component> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static final String DATE_FORMAT = "dd/MM/yyyy";
	
	@Wire
	Textbox versionLabelTextBox;
	
	@Wire
	Listbox unitsList;
	
	private Unit currentUnit;
	
	@WireVariable
	Session _sess;

	@Listen("onSelect=#unitsList")
    public void onSelectUnitsList() {
		Listitem selectedItem = unitsList.getSelectedItem();
		
	}

	public Unit getCurrentUnit() {
		return currentUnit;
	}

	public void setCurrentUnit(Unit currentUnit) {
		this.currentUnit = currentUnit;
	}
	
}
