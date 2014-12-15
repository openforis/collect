/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openforis.collect.designer.form.CodeListItemFormObject;
import org.openforis.collect.designer.form.FormObject;
import org.openforis.collect.manager.CodeListManager;
import org.openforis.idm.metamodel.CodeListItem;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.Binder;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zk.ui.select.annotation.WireVariable;

/**
 * 
 * @author S. Ricci
 *
 */
public class CodeListItemVM extends SurveyObjectBaseVM<CodeListItem> {

	public static final String ITEM_ARG = "item";
	public static final String PARENT_ITEM_ARG = "parentItem";
	public static final String ENUMERATING_CODE_LIST_ARG = "enumeratingCodeList";

	@WireVariable
	private CodeListManager codeListManager;
	
	@Init(superclass=false)
	public void init(@ExecutionArgParam(ITEM_ARG) CodeListItem item) {
		super.init();
		setEditedItem(item);
		commitChangesOnApply = false;
	}
	
	@Override
	protected void addNewItemToSurvey() {
		//do nothing, performed by CodeListVM
	}
	
	@Override
	protected void deleteItemFromSurvey(CodeListItem item) {
		//do nothing, performed by CodeListVM
	}
	
	@Override
	protected List<CodeListItem> getItemsInternal() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	protected void moveSelectedItemInSurvey(int indexTo) {
		//managed by CodeListsVM
	}
	
	@Override
	protected CodeListItem createItemInstance() {
		//items instantiated in CodeListEditVM
		return null;
	}
	
	@Override
	protected FormObject<CodeListItem> createFormObject() {
		return new CodeListItemFormObject();
	}

	@Command
	public void apply(@ContextParam(ContextType.BINDER) Binder binder) {
		if ( isCurrentFormValid() ) {
			commitChanges();
			postClosePopUpCommand(false);
		} else {
			checkCanLeaveForm(new CanLeaveFormConfirmHandler() {
				@Override
				public void onOk(boolean confirmed) {
					postClosePopUpCommand(confirmed);
				}
			});
		}
	}
	
	@Command
	public void cancel(@ContextParam(ContextType.BINDER) Binder binder) {
		undoLastChanges(binder.getView());
		postClosePopUpCommand(true);
	}
	
	private void postClosePopUpCommand(boolean undoChanges) {
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("undoChanges", undoChanges);
		BindUtils.postGlobalCommand(null, null, CodeListsVM.CLOSE_CODE_LIST_ITEM_POP_UP_COMMAND, args);
	}	

	public CodeListManager getCodeListManager() {
		return codeListManager;
	}
}
