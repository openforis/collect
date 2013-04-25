/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import java.util.HashMap;
import java.util.Map;

import org.openforis.collect.designer.util.ComponentUtil;
import org.openforis.collect.designer.util.Resources;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;

/**
 * 
 * @author S. Ricci
 *
 */
public class CodeListImportVM extends SurveyBaseVM {

	private int codeListId;

	@Init(superclass=false)
	public void init(@ExecutionArgParam("codeListId") int codeListId) {
		super.init();
		this.codeListId = codeListId;
	}
	
	@Command
	public void close() {
		checkCanLeaveForm(new CanLeaveFormConfirmHandler() {
			@Override
			public void onOk(boolean confirmed) {
				Map<String, Object> args = new HashMap<String, Object>();
				args.put("undoChanges", confirmed);
				BindUtils.postGlobalCommand(null, null, CodeListsVM.CLOSE_CODE_LIST_IMPORT_POP_UP_COMMAND, args);
			}
		});
	}
	
	public String getCodeListImportModuleUrl() {
		Map<String, String> queryParams = createBasicModuleParameters();
		queryParams.put("code_list_import", "true");
		queryParams.put("code_list_id", Integer.toString(codeListId));
		String url = ComponentUtil.createUrl(Resources.Page.COLLECT_SWF.getLocation(), queryParams);
		return url;
	}
	
}
