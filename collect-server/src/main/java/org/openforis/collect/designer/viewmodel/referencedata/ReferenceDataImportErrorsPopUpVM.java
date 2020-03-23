/**
 * 
 */
package org.openforis.collect.designer.viewmodel.referencedata;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.openforis.collect.designer.util.PopUpUtil;
import org.openforis.collect.designer.util.Resources;
import org.openforis.collect.designer.viewmodel.BaseVM;
import org.openforis.collect.io.metadata.parsing.ParsingError;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;
import org.zkoss.util.resource.Labels;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Window;

/**
 * @author S. Ricci
 *
 */
public class ReferenceDataImportErrorsPopUpVM extends BaseVM {
	
	private static final String ERRORS_PARAM = "errors";
	private static final String TITLE_PARAM = "title";
	
	private List<ParsingError> errors;
	private String title;

	public static Window showPopUp(List<ParsingError> errors, String title) {
		Map<String, Object> args = new HashMap<String, Object>();
		args.put(ERRORS_PARAM, errors);
		args.put(TITLE_PARAM, title);
		return PopUpUtil.openPopUp(Resources.Component.REFERENCE_DATA_IMPORT_ERRORS_POPUP.getLocation(), true, args);
	}
	
	@Init
	public void init(@ExecutionArgParam(ERRORS_PARAM) List<ParsingError> errors, 
			@ExecutionArgParam(TITLE_PARAM) String title) {
		this.errors = errors;
		this.title = title;
	}

	public List<ParsingError> getErrors() {
		return new ListModelList<ParsingError>(errors);
	}
	
	public String getTitle() {
		return title;
	}
	
	public String getErrorTypeLabel(ParsingError error) {
		return Labels.getLabel(String.format("survey.reference_data.import_error.type.%s", 
				error.getErrorType().name().toLowerCase(Locale.ENGLISH)));
	}
	

}
