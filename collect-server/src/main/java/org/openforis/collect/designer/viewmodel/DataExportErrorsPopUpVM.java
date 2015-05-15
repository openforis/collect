package org.openforis.collect.designer.viewmodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openforis.collect.designer.util.PopUpUtil;
import org.openforis.collect.designer.util.Resources;
import org.openforis.collect.io.data.DataBackupError;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zul.Window;

/**
 * 
 * @author S. Ricci
 *
 */
public class DataExportErrorsPopUpVM {

	private List<DataBackupError> errors;

	public static Window showPopUp(final List<DataBackupError> errors) {
		@SuppressWarnings("serial")
		Map<?, ?> args = new HashMap<String, Object>(){{
			this.put("errors", errors);
		}};
		return PopUpUtil.openPopUp(Resources.Component.DATA_EXPORT_ERRORS_POPUP.getLocation(), true, args);
	}
	
	@Init
	public void init(@ExecutionArgParam("errors") List<DataBackupError> errors) {
		this.errors = errors;
	}
	
	public List<DataBackupError> getErrors() {
		return new ArrayList<DataBackupError>(errors);
	}
}
