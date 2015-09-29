/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Stack;

import org.openforis.collect.designer.form.SurveyObjectFormObject;
import org.openforis.collect.designer.form.UnitFormObject;
import org.openforis.collect.designer.util.MessageUtil;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.NumericAttributeDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.Unit;
import org.openforis.idm.metamodel.Unit.Dimension;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.Binder;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Window;

/**
 * 
 * @author S. Ricci
 *
 */
public class UnitsVM extends SurveyObjectBaseVM<Unit> {

	private static final String UNITS_UPDATED_GLOBAL_COMMAND = "unitsUpdated";
	
	private Window confirmDeleteItemWithErrorsPopUp;

	public static String getDimensionLabel(Dimension dimension) {
		String labelKey = "survey.unit.dimension." + dimension.name().toLowerCase(Locale.ENGLISH);
		String label = Labels.getLabel(labelKey);
		return label;
	}
	
	@Override
	@Init(superclass=false)
	public void init() {
		super.init();
	}
	
	@Override
	@AfterCompose
	public void doAfterCompose(@ContextParam(ContextType.VIEW) Component view){
		super.doAfterCompose(view);
	}
	
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
	protected void moveSelectedItemInSurvey(int indexTo) {
		survey.moveUnit(selectedItem, indexTo);
	}

	@Override
	protected SurveyObjectFormObject<Unit> createFormObject() {
		return new UnitFormObject();
	}
	
	@Override
	@Command
	@NotifyChange({"editedItem","selectedItem"})
	public void applyChanges(@ContextParam(ContextType.BINDER) Binder binder) {
		super.applyChanges(binder);
		dispatchUnitsUpdatedCommand();
	}

	protected void dispatchUnitsUpdatedCommand() {
		BindUtils.postGlobalCommand(null, null, UNITS_UPDATED_GLOBAL_COMMAND, null);
	}
	
	@Command
	public void deleteUnit(@BindingParam("item") final Unit item) {
		List<NodeDefinition> references = getReferences(item);
		if ( references.isEmpty() ) {
			super.deleteItem(item);
		} else {
			String title = Labels.getLabel("survey.unit.delete.confirm_title");
			String message = Labels.getLabel("survey.unit.delete.confirm_in_use");
			confirmDeleteItemWithErrorsPopUp = SurveyErrorsPopUpVM.openPopUp(title, message, 
					references, new MessageUtil.CompleteConfirmHandler() {
				@Override
				public void onOk() {
					performDeleteItem(item);
					closeErrorsInNodesPopUp();
				}
				@Override
				public void onCancel() {
					closeErrorsInNodesPopUp();
				}
			});
		}
	}
	
	@Override
	protected void performDeleteItem(Unit item) {
		super.performDeleteItem(item);
		dispatchUnitsUpdatedCommand();
	}
	
	protected void closeErrorsInNodesPopUp() {
		closePopUp(confirmDeleteItemWithErrorsPopUp);
		confirmDeleteItemWithErrorsPopUp = null;
	}
	
	protected List<NodeDefinition> getReferences(Unit item) {
		List<NodeDefinition> references = new ArrayList<NodeDefinition>();
		Schema schema = survey.getSchema();
		List<EntityDefinition> rootEntities = schema.getRootEntityDefinitions();
		Stack<NodeDefinition> stack = new Stack<NodeDefinition>();
		stack.addAll(rootEntities);
		while ( ! stack.isEmpty() ) {
			NodeDefinition defn = stack.pop();
			if ( defn instanceof EntityDefinition ) {
				stack.addAll(((EntityDefinition) defn).getChildDefinitions());
			} else if ( defn instanceof NumericAttributeDefinition ) {
				List<Unit> units = ((NumericAttributeDefinition) defn).getUnits();
				if ( units.contains(item) ) {
					references.add(defn);
				}
			}
		}
		return references;
	}
	
	public List<String> getAvailableDimensions() {
		Dimension[] dimensions = Dimension.values();
		List<String> result = new ArrayList<String>(dimensions.length);
		for (Dimension dimension : dimensions) {
			String label = getDimensionLabel(dimension);
			result.add(label);
		}
		return result;
	}
	
	@Command
	public void apply(@ContextParam(ContextType.BINDER) final Binder binder) {
		checkCanLeaveForm(new CanLeaveFormConfirmHandler() {
			@Override
			public void onOk(boolean confirmed) {
				BindUtils.postGlobalCommand(null, null, "closeUnitsManagerPopUp", null);
			}
		});
	}
	
	
}
