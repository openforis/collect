/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.openforis.collect.designer.form.SurveyObjectFormObject;
import org.openforis.collect.designer.form.UnitFormObject;
import org.openforis.collect.designer.form.UnitFormObject.Dimension;
import org.openforis.collect.designer.util.MessageUtil;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.NumericAttributeDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.Unit;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zul.Window;

/**
 * 
 * @author S. Ricci
 *
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class UnitsVM extends SurveyObjectBaseVM<Unit> {

	private static final String UNITS_UPDATED_GLOBAL_COMMAND = "unitsUpdated";
	
	private Window confirmDeleteItemWithErrorsPopUp;

	@Override
	@Init(superclass=false)
	public void init() {
		super.init();
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
		dispatchUnitsUpdatedCommand();
	}

	protected void dispatchUnitsUpdatedCommand() {
		BindUtils.postGlobalCommand(null, null, UNITS_UPDATED_GLOBAL_COMMAND, null);
	}
	
	@Override
	@Command
	public void deleteItem(@BindingParam("item") final Unit item) {
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
			String label = dimension.getLabel();
			result.add(label);
		}
		return result;
	}
	
}
