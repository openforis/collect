package org.openforis.collect.designer.viewmodel;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.designer.form.AttributeFormObject;
import org.openforis.collect.designer.util.MessageUtil;
import org.openforis.collect.designer.util.MessageUtil.ConfirmHandler;
import org.openforis.collect.io.metadata.ReferenceDataExportOutputFormat;
import org.openforis.collect.io.metadata.samplingdesign.SamplingDesignExportJob;
import org.openforis.collect.io.metadata.samplingdesign.SamplingDesignFileColumn;
import org.openforis.collect.manager.SamplingDesignManager;
import org.openforis.collect.utils.Dates;
import org.openforis.collect.utils.MediaTypes;
import org.openforis.idm.metamodel.ReferenceDataSchema.ReferenceDataDefinition.Attribute;
import org.openforis.idm.metamodel.ReferenceDataSchema.SamplingPointDefinition;
import org.openforis.idm.metamodel.Survey;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Window;

public class SamplingPointDataVM extends SurveyBaseVM {

	public static final String SAMPLING_POINT_DATA_QUEUE = "samplingPointData";
	public static final String SAMPLING_POINT_DATA_UPDATED_COMMAND = "samplingPointDataUpdated";
	public static final String CLOSE_SAMPLING_POINT_DATA_IMPORT_POPUP_COMMAND = "closeSamplingPointDataImportPopUp";

	private static final String ERROR_ATTRIBUTE_NAME_REQUIRED = "survey.sampling_point_data.attribute.validation.name_required";
	private static final String ERROR_ATTRIBUTE_NAME_INVALID = "global.validation.internal_name.invalid_value";
	private static final String ERROR_ATTRIBUTE_NAME_DUPLICATE = "global.item.validation.name_already_defined";

	private Window samplingPointDataImportPopUp;

	@WireVariable
	private SamplingDesignManager samplingDesignManager;

	private ListModelList<AttributeFormObject> attributes;

	@Init(superclass = false)
	public void init() {
		super.init();
	}

	public List<AttributeFormObject> getAttributes() {
		if (attributes == null) {
			attributes = new ListModelList<>();
			for (String colName : SamplingDesignFileColumn.ALL_COLUMN_NAMES) {
				attributes.add(new AttributeFormObject(false, attributes.getSize(), colName));
			}
			List<String> infoAttributeNames = getSamplingPointDefinition().getAttributeNames();
			for (String infoAttributeName : infoAttributeNames) {
				attributes.add(new AttributeFormObject(true, attributes.getSize(), infoAttributeName));
			}
		}
		return attributes;
	}

	public boolean isSamplingPointDataEmpty() {
		return samplingDesignManager.countBySurvey(getSurveyId()) == 0;
	}

	@Command
	public void openImportPopUp() {
		samplingPointDataImportPopUp = SamplingPointDataImportPopUpVM.openPopUp();
	}

	@GlobalCommand
	public void closeSamplingPointDataImportPopUp() {
		closePopUp(samplingPointDataImportPopUp);
	}

	@GlobalCommand
	public void samplingPointDataUpdated() {
		notifyChange("columnNames");
		notifyChange("samplingPointDataEmpty");
	}

	@Command
	public void exportToCsv() throws IOException {
		export(ReferenceDataExportOutputFormat.CSV);
	}

	@Command
	public void exportToExcel() throws IOException {
		export(ReferenceDataExportOutputFormat.EXCEL);
	}

	@Command
	public void deleteAllItems() {
		MessageUtil.showConfirm(new ConfirmHandler() {
			public void onOk() {
				samplingDesignManager.deleteBySurvey(getSurveyId());
				notifySamplingPointDataUpdated();
			}
		}, "survey.sampling_point_data.confirm_delete_all_items");
	}

	private void export(ReferenceDataExportOutputFormat outputFormat) throws IOException {
		SamplingDesignExportJob job = jobManager.createJob(SamplingDesignExportJob.class);
		job.setSurvey(getSurvey());
		job.setOutputFormat(outputFormat);
		jobManager.start(job, false);

		String mediaType = outputFormat == ReferenceDataExportOutputFormat.CSV ? MediaTypes.CSV_CONTENT_TYPE
				: MediaTypes.XLSX_CONTENT_TYPE;
		String extension = outputFormat.getFileExtesion();
		String fileName = String.format("%s_sampling_point_data_%s.%s", getSurvey().getName(), Dates.formatCompactNow(),
				extension);
		Filedownload.save(new FileInputStream(job.getOutputFile()), mediaType, fileName);
	}

	@Command
	public void confirmAttributeUpdate(@BindingParam("attribute") AttributeFormObject attribute) {
		if (validateAttribute(attribute)) {
			changeAttributeEditableStatus(attribute);
			int infoAttributeIndex = attribute.getIndex() - SamplingDesignFileColumn.ALL_COLUMNS.length;
			getSamplingPointDefinition().setAttribute(infoAttributeIndex, new Attribute(attribute.getName()));
			dispatchSurveyChangedCommand();
		}
	}

	@Command
	public void changeAttributeEditableStatus(@BindingParam("attribute") AttributeFormObject attribute) {
		attribute.setEditingStatus(!attribute.getEditingStatus());
		refreshAttributeColumnTemplate(attribute);
	}

	private boolean validateAttribute(AttributeFormObject attribute) {
		String name = attribute.getName();
		String error = null;
		if (StringUtils.isBlank(name)) {
			error = ERROR_ATTRIBUTE_NAME_REQUIRED;
		} else if (!name.matches(Survey.INTERNAL_NAME_REGEX)) {
			error = ERROR_ATTRIBUTE_NAME_INVALID;
		} else {
			// validate name uniqueness
			for (int index = 0; index < attributes.size(); index++) {
				AttributeFormObject attributeFormObject = attributes.get(index);
				if (name.equals(attributeFormObject.getName()) && attribute.getIndex() != index) {
					error = ERROR_ATTRIBUTE_NAME_DUPLICATE;
					break;
				}
			}
		}
		if (error == null) {
			return true;
		} else {
			MessageUtil.showError(error);
			return false;
		}
	}

	private SamplingPointDefinition getSamplingPointDefinition() {
		return getSurvey().getReferenceDataSchema().getSamplingPointDefinition();
	}

	private void refreshAttributeColumnTemplate(AttributeFormObject attribute) {
		// replace the element in the collection by itself to trigger a model update
		int index = attributes.indexOf(attribute);
		attributes.set(index, attribute);
		notifyChange("attributes");
	}

	public static void notifySamplingPointDataUpdated() {
		BindUtils.postGlobalCommand(null, null, SAMPLING_POINT_DATA_UPDATED_COMMAND, null);
		// To be handled by composer
		BindUtils.postGlobalCommand(SAMPLING_POINT_DATA_QUEUE, null, SAMPLING_POINT_DATA_UPDATED_COMMAND, null);
	}

	public static void dispatchSamplingPointDataImportPopUpCloseCommand() {
		BindUtils.postGlobalCommand(null, null, CLOSE_SAMPLING_POINT_DATA_IMPORT_POPUP_COMMAND, null);
	}

}
