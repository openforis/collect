package org.openforis.collect.designer.viewmodel;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.openforis.collect.designer.form.AttributeFormObject;
import org.openforis.collect.designer.util.MessageUtil;
import org.openforis.collect.designer.util.MessageUtil.ConfirmHandler;
import org.openforis.collect.io.metadata.ReferenceDataExportOutputFormat;
import org.openforis.collect.io.metadata.samplingdesign.SamplingDesignExportJob;
import org.openforis.collect.io.metadata.samplingdesign.SamplingDesignFileColumn;
import org.openforis.collect.manager.SamplingDesignManager;
import org.openforis.collect.model.SamplingDesignItem;
import org.openforis.collect.model.SamplingDesignSummaries;
import org.openforis.collect.utils.Dates;
import org.openforis.collect.utils.MediaTypes;
import org.openforis.idm.metamodel.ReferenceDataSchema.SamplingPointDefinition;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Window;

public class SamplingPointDataVM extends SurveyBaseVM {

	public static final String SAMPLING_POINT_DATA_UPDATED_COMMAND = "samplingPointDataUpdated";
	public static final String CLOSE_SAMPLING_POINT_DATA_IMPORT_POPUP_COMMAND = "closeSamplingPointDataImportPopUp";
	private static final int ITEMS_PAGE_SIZE = 20;

	private Window samplingPointDataImportPopUp;

	@WireVariable
	private SamplingDesignManager samplingDesignManager;

	private ReferenceDataAttributesEditor referenceDataAttributesEditor;

	private int itemsPage = 0;
	private SamplingDesignSummaries itemsSummaries;

	@Init(superclass = false)
	public void init() {
		super.init();
		loadItems();
	}

	public List<AttributeFormObject> getAttributes() {
		if (referenceDataAttributesEditor == null) {
			initReferenceDataAttributesEditor();
		}
		return referenceDataAttributesEditor.getAttributes();
	}

	@Command
	@NotifyChange("attributes")
	public void changeAttributeEditableStatus(@BindingParam("attribute") AttributeFormObject attribute) {
		referenceDataAttributesEditor.changeAttributeEditableStatus(attribute);
	}

	@Command
	public void updateItemsPaging(int newPageIndex) {
		this.itemsPage = newPageIndex;
		notifyChange("itemsPage");
		loadItems();
	}

	public String getAttributeValue(AttributeFormObject attribute, SamplingDesignItem item) {
		int fixedColumnsLength = SamplingDesignFileColumn.ALL_COLUMN_NAMES.length;
		int index = attribute.getIndex();
		if (index >= fixedColumnsLength) {
			// info column
			return item.getInfoAttribute(index - fixedColumnsLength);
		} else {
			SamplingDesignFileColumn column = SamplingDesignFileColumn.ALL_COLUMNS[index];
			if (column.isLevelColumn()) {
				int level = column.getLevel();
				return item.getLevelCodes().size() >= level ? item.getLevelCode(level) : null;
			} else if (column == SamplingDesignFileColumn.X) {
				Double value = item.getX();
				return value == null ? null : value.toString();
			} else if (column == SamplingDesignFileColumn.Y) {
				Double value = item.getY();
				return value == null ? null : value.toString();
			} else if (column == SamplingDesignFileColumn.SRS_ID) {
				return item.getSrsId();
			} else {
				return null;
			}
		}
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
		initReferenceDataAttributesEditor();
		notifyChange("attributes");
		updateItemsPaging(0);
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
				resetItems();
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
		if (referenceDataAttributesEditor.confirmAttributeUpdate(attribute)) {
			dispatchSurveyChangedCommand();
		}
	}
	
	public List<SamplingDesignItem> getItems() {
		return itemsSummaries == null ? null : itemsSummaries.getRecords();
	}

	public int getItemsTotal() {
		return itemsSummaries == null ? 0 : itemsSummaries.getTotalCount();
	}

	public boolean isSamplingPointDataEmpty() {
		return getItemsTotal() == 0;
	}

	public int getItemsPage() {
		return itemsPage;
	}

	public int getItemsPageSize() {
		return ITEMS_PAGE_SIZE;
	}


	private void initReferenceDataAttributesEditor() {
		SamplingPointDefinition samplingPointDefinition = getSurvey().getReferenceDataSchema().getSamplingPointDefinition();
		referenceDataAttributesEditor = new ReferenceDataAttributesEditor(
				Arrays.asList(SamplingDesignFileColumn.ALL_COLUMN_NAMES), samplingPointDefinition);
	}

	private void loadItems() {
		itemsSummaries = samplingDesignManager.loadBySurvey(getSurveyId(), itemsPage * ITEMS_PAGE_SIZE,
				ITEMS_PAGE_SIZE);
		notifyChange("itemsTotal", "items");
	}
	
	private void resetItems() {
		itemsSummaries = null;
		itemsPage = 0;
		notifyChange("itemsTotal", "items", "itemsPage");
	}

	public static void notifySamplingPointDataUpdated() {
		BindUtils.postGlobalCommand(null, null, SAMPLING_POINT_DATA_UPDATED_COMMAND, null);
	}

	public static void dispatchSamplingPointDataImportPopUpCloseCommand() {
		BindUtils.postGlobalCommand(null, null, CLOSE_SAMPLING_POINT_DATA_IMPORT_POPUP_COMMAND, null);
	}

}
