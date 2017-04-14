/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import java.util.HashMap;
import java.util.Map;

import org.openforis.collect.manager.CodeListManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.idm.metamodel.CodeList;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.Binder;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.IdSpace;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.select.annotation.WireVariable;

/**
 * 
 * @author S. Ricci
 *
 */
public class CodeListsPopUpVM extends SurveyBaseVM {

	public static final String EDITING_ATTRIBUTE_PARAM = "editingAttribute";
	public static final String SELECTED_CODE_LIST_PARAM = "selectedCodeList";
	public static final String CLOSE_CODE_LIST_ITEM_POP_UP_COMMAND = "closeCodeListItemPopUp";
	public static final String CLOSE_CODE_LIST_IMPORT_POP_UP_COMMAND = "closeCodeListImportPopUp";
	
	private boolean editingAttribute;
	
	@WireVariable
	private SurveyManager surveyManager;
	@WireVariable
	private CodeListManager codeListManager;
	
	@Init(superclass=false)
	public void init(@ExecutionArgParam(EDITING_ATTRIBUTE_PARAM) Boolean editingAttribute, 
			@ExecutionArgParam(SELECTED_CODE_LIST_PARAM) CodeList selectedCodeList) {
		super.init();
		this.editingAttribute = editingAttribute != null && editingAttribute.booleanValue();
	}
	
	@Command
	public void apply(@ContextParam(ContextType.VIEW) final Component view,
			@ContextParam(ContextType.BINDER) final Binder binder) {
		checkCanLeaveForm(new CanLeaveFormConfirmHandler() {
			@Override
			public void onOk(boolean confirmed) {
				if (confirmed) {
					undoLastChanges(view);
				}
				postCodeListsManagerPopUpCommand(getSelectedCodeList(binder));
			}
		});
	}
	
	@Command
	public void close(@ContextParam(ContextType.TRIGGER_EVENT) Event event) {
		event.stopPropagation();
		dispatchCurrentFormValidatedCommand();
		postCodeListsManagerPopUpCommand(null);
	}
	
	private void postCodeListsManagerPopUpCommand(CodeList selectedCodeList) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("editingAttribute", editingAttribute);
		params.put("selectedCodeList", selectedCodeList);
		BindUtils.postGlobalCommand((String) null, (String) null, "closeCodeListsManagerPopUp", params);		
	}

	private CodeList getSelectedCodeList(Binder binder) {
		Component view = binder.getView();
		IdSpace spaceOwner = view.getSpaceOwner();
		Component innerInclude = spaceOwner.getFellow("codeListsInclude");
		Component managerContainer = innerInclude.getSpaceOwner().getFellow("codeListsManagerContainer");
		CodeListsVM vm = (CodeListsVM) managerContainer.getAttribute("vm");
		CodeList codeList = vm.getSelectedItem();
		return codeList;
	}
	
}
