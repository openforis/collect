/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openforis.collect.designer.session.SessionStatus;
import org.openforis.collect.designer.util.MessageUtil;
import org.openforis.collect.designer.util.Resources;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.NumericAttributeDefinition;
import org.openforis.idm.metamodel.Precision;
import org.openforis.idm.metamodel.Schema;
import org.zkoss.bind.Binder;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.DependsOn;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Window;

/**
 * @author S. Ricci
 *
 */
public class NumericAttributeVM extends AttributeVM<NumericAttributeDefinition> {

	@WireVariable
	private SurveyManager surveyManager;
	
	protected List<Precision> precisions;
	protected Precision selectedPrecision;
	private boolean editingNewPrecision;
	private Precision editedPrecision;
	
	private Window precisionPopUp;
	
	@Init(superclass=false)
	public void init(@ExecutionArgParam("parentEntity") EntityDefinition parentEntity, 
			@ExecutionArgParam("item") NumericAttributeDefinition attributeDefn, 
			@ExecutionArgParam("newItem") Boolean newItem) {
		super.init(parentEntity, attributeDefn, newItem);
	}
	
	@Override
	public void setEditedItem(NumericAttributeDefinition editedItem) {
		super.setEditedItem(editedItem);
		if ( editedItem != null ) {
			precisions = editedItem.getPrecisionDefinitions();
		} else {
			precisions = null;
		}
	}
	
	@Command
	@NotifyChange("precisions")
	public void addPrecision() {
		editingNewPrecision = true;
		editedPrecision = new Precision();
		openPrecisionEditPopUp();
	}
	
	@Command
	public void editPrecision() {
		editingNewPrecision = false;
		editedPrecision = selectedPrecision;
		openPrecisionEditPopUp();
	}
	
	@Command
	@NotifyChange({"selectedPrecision","precisions"})
	public void deletePrecision() {
		MessageUtil.showConfirm(new MessageUtil.ConfirmHandler() {
			@Override
			public void onOk() {
				editedItem.removePrecisionDefinition(selectedPrecision);
				selectedPrecision = null;
				initPrecisions();
				notifyChange("selectedPrecision");
			}
		}, "survey.schema.attribute.numeric.precisions.confirm_delete");
	}
	
	protected void openPrecisionEditPopUp() {
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("parentDefinition", editedItem);
		args.put("newItem", editingNewPrecision);
		args.put("precision", editedPrecision);
		precisionPopUp = openPopUp(Resources.Component.PRECISION_POPUP.getLocation(), true, args);
	}
	
	@Command
	@NotifyChange("selectedPrecision")
	public void selectPrecision(@BindingParam("precision") Precision precision) {
		selectedPrecision = precision;
	}

	@GlobalCommand
	public void closeUnitsManagerPopUp(@ContextParam(ContextType.BINDER) Binder binder) {
		validateForm(binder);
	}
	
	@GlobalCommand
	public void applyChangesToEditedPrecision(@ContextParam(ContextType.BINDER) Binder binder) {
		if ( editedPrecision != null && checkCanLeaveForm() ) {
			if ( editedPrecision.isDefaultPrecision() ) {
				removeDefaultAssignmentToPrecisions();
				editedPrecision.setDefaultPrecision(true);
			}
			closePrecisionEditPopUp(binder);
			editedPrecision = null;
			initPrecisions();
			notifyChange("editedPrecision");
		}
	}

	protected void removeDefaultAssignmentToPrecisions() {
		List<Precision> precisionDefinitions = editedItem.getPrecisionDefinitions();
		for (Precision precision : precisionDefinitions) {
			precision.setDefaultPrecision(false);
		}
	}

	@GlobalCommand
	public void cancelChangesToEditedPrecision(@ContextParam(ContextType.BINDER) Binder binder) {
		//TODO confirm if there are not committed changes 
		if ( editedPrecision != null ) {
			closePrecisionEditPopUp(binder);
			editedPrecision = null;
			notifyChange("editedPrecision");
		}
	}
	
	protected void closePrecisionEditPopUp(Binder binder) {
		closePopUp(precisionPopUp);
		precisionPopUp = null;
		validateForm(binder);
	}
	

	protected void initPrecisions() {
		if ( editedItem != null ) {
			precisions = new ArrayList<Precision>(editedItem.getPrecisionDefinitions());
		} else {
			precisions = null;
		}
		notifyChange("precisions");
	}

	@Command
	@NotifyChange({"precisions"})
	public void moveSelectedPrecisionUp() {
		moveSelectedPrecision(true);
	}
	
	@Command
	@NotifyChange({"precisions"})
	public void moveSelectedPrecisionDown() {
		moveSelectedPrecision(false);
	}
	
	protected void moveSelectedPrecision(boolean up) {
		int indexFrom = getSelectedPrecisionIndex();
		int indexTo = up ? indexFrom - 1: indexFrom + 1;
		moveSelectedPrecision(indexTo);
		initPrecisions();
	}
	
	protected int getSelectedPrecisionIndex() {
		List<?> items = editedItem.getPrecisionDefinitions();
		int index = items.indexOf(selectedPrecision);
		return index;
	}

	protected void moveSelectedPrecision(int indexTo) {
		editedItem.movePrecisionDefinition(selectedPrecision, indexTo);
		initPrecisions();
	}
	
	public boolean isTypeChangeDisabled() {
		NodeDefinition oldNodeDefn = getOldNodeDefinition();
		return oldNodeDefn != null;
	}
	
	protected NodeDefinition getOldNodeDefinition() {
		SessionStatus sessionStatus = getSessionStatus();
		Integer publishedSurveyId = sessionStatus.getPublishedSurveyId();
		if ( publishedSurveyId != null ) {
			CollectSurvey publishedSurvey = surveyManager.getById(publishedSurveyId);
			Schema schema = publishedSurvey.getSchema();
			int nodeId = editedItem.getId();
			NodeDefinition oldDefn = schema.getDefinitionById(nodeId);
			return oldDefn;
		} else {
			return null;
		}
	}
	
	@DependsOn({"precisions","selectedPrecision"})
	public boolean isMoveSelectedPrecisionUpDisabled() {
		return isMoveSelectedPrecisionDisabled(true);
	}
	
	@DependsOn({"precisions","selectedPrecision"})
	public boolean isMoveSelectedPrecisionDownDisabled() {
		return isMoveSelectedPrecisionDisabled(false);
	}
	
	protected boolean isMoveSelectedPrecisionDisabled(boolean up) {
		if ( selectedPrecision == null ) {
			return true;
		} else {
			List<Precision> siblings = editedItem.getPrecisionDefinitions();
			int index = siblings.indexOf(selectedPrecision);
			return up ? index <= 0: index < 0 || index >= siblings.size() - 1;
		}
	}
	
	public List<Precision> getPrecisions() {
		return precisions;
	}
	
	public Precision getSelectedPrecision() {
		return selectedPrecision;
	}

	public void setSelectedPrecision(Precision selectedPrecision) {
		this.selectedPrecision = selectedPrecision;
	}
	
}
