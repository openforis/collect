/**
 * 
 */
package org.openforis.collect.designer.viewmodel.referencedata;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.openforis.collect.designer.util.MessageUtil;
import org.openforis.collect.designer.util.PopUpUtil;
import org.openforis.collect.designer.util.Resources;
import org.openforis.collect.designer.viewmodel.BaseVM;
import org.openforis.collect.io.metadata.parsing.ParsingError;
import org.openforis.collect.io.metadata.parsing.ParsingError.ErrorType;
import org.openforis.commons.io.excel.ExcelFlatValuesWriter;
import org.openforis.commons.io.flat.FlatDataWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;
import org.zkoss.util.resource.Labels;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Window;

/**
 * @author S. Ricci
 *
 */
public class ReferenceDataImportErrorsPopUpVM extends BaseVM {
	
	private static final Logger logger = LoggerFactory.getLogger(ReferenceDataImportErrorsPopUpVM.class);
	
	private static final String ERRORS_PARAM = "errors";
	private static final String TITLE_PARAM = "title";
	private static final String MESSAGE_KEY_DUPLICATE_VALUE = "survey.reference_data.import_error.type.duplicate_value.message";

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
	
	@Command
	public void exportErrorsToExcel() {
		try {
			File outputFile = File.createTempFile("collect_reference_data_import_errors", ".xlsx");
			FileOutputStream out = new FileOutputStream(outputFile); 
			final FlatDataWriter csvWriter = new ExcelFlatValuesWriter(out);
			csvWriter.writeHeaders(Arrays.asList("row", "columns", "type", "message"));
			for (ParsingError error : this.errors) {
				csvWriter.writeNext(Arrays.asList(String.valueOf(error.getRow()), error.getColumnsString(),
						getErrorTypeLabel(error), getErrorMessageLabel(error)));
			}
			csvWriter.close();
			String fileName = "data_import_errors.xlsx";
			String contentType = URLConnection.guessContentTypeFromName(fileName);
			FileInputStream is = new FileInputStream(outputFile);
			Filedownload.save(is, contentType, fileName);
		} catch (Exception e) {
			logger.error("Error exporting list of errors", e);
			MessageUtil.showError("survey.schema.export_summary.error", e.getMessage());
		}
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

	public String getErrorMessageLabel(ParsingError error) {
		String label = Labels.getLabel(error.getMessage(), error.getMessageArgs());
		if (label == null && error.getErrorType() == ErrorType.DUPLICATE_VALUE) {
			label = Labels.getLabel(MESSAGE_KEY_DUPLICATE_VALUE, error.getMessageArgs());
		}
		return label == null ? error.getMessage() : label;
	}

}
