/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openforis.collect.designer.form.CodeListItemFormObject;
import org.openforis.collect.designer.form.FormObject;
import org.openforis.idm.metamodel.CodeListItem;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;

/**
 * 
 * @author S. Ricci
 *
 */
public class CodeListItemVM extends SurveyObjectBaseVM<CodeListItem> {

	@Init(superclass=false)
	public void init(@ExecutionArgParam("item") CodeListItem item) {
		super.init();
		setEditedItem(item);
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
	protected void moveSelectedItem(int indexTo) {
		//do nothing
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
	public void close() {
		checkCanLeaveForm(new CanLeaveFormConfirmHandler() {
			@Override
			public void onOk(boolean confirmed) {
				Map<String, Object> args = new HashMap<String, Object>();
				args.put("undoChanges", confirmed);
				BindUtils.postGlobalCommand(null, null, CodeListsVM.CLOSE_CODE_LIST_ITEM_POP_UP_COMMAND, args);
			}
		});
	}
	
}
