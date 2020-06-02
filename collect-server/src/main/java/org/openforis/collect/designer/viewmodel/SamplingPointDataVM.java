package org.openforis.collect.designer.viewmodel;

import java.io.FileInputStream;
import java.io.IOException;

import org.openforis.collect.designer.util.Resources;
import org.openforis.collect.io.metadata.samplingdesign.SamplingDesignExportJob;
import org.openforis.collect.io.metadata.samplingdesign.SamplingDesignExportTask.OutputFormat;
import org.openforis.collect.utils.Dates;
import org.openforis.collect.utils.MediaTypes;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Window;

public class SamplingPointDataVM extends SurveyBaseVM {

	public static final String SAMPLING_POINT_DATA_QUEUE = "samplingPointData";

	protected static final String SAMPLING_POINT_DATA_UPDATED_COMMAND = "samplingPointDataUpdated";

	public static final String CLOSE_SAMPLING_POINT_DATA_IMPORT_POPUP_COMMAND = "closeSamplingPointDataImportPopUp";

	private Window samplingPointDataImportPopUp;

	@Init(superclass = false)
	public void init() {
		super.init();
	}

	@Command
	public void openImportPopUp() {
		samplingPointDataImportPopUp = openPopUp(Resources.Component.SAMPLING_POINT_DATA_IMPORT_POPUP.getLocation(),
				true);
	}

	@GlobalCommand
	public void closeSamplingPointDataImportPopUp() {
		closePopUp(samplingPointDataImportPopUp);
	}

	@Command
	public void exportToCsv() throws IOException {
		export(OutputFormat.CSV);
	}

	@Command
	public void exportToExcel() throws IOException {
		export(OutputFormat.EXCEL);
	}

	private void export(OutputFormat outputFormat) throws IOException {
		SamplingDesignExportJob job = jobManager.createJob(SamplingDesignExportJob.class);
		job.setSurvey(getSurvey());
		job.setOutputFormat(outputFormat);
		jobManager.start(job, false);

		String mediaType = outputFormat == OutputFormat.CSV ? MediaTypes.CSV_CONTENT_TYPE
				: MediaTypes.XLSX_CONTENT_TYPE;
		String extension = outputFormat == OutputFormat.CSV ? "csv" : "xlsx";
		String fileName = String.format("%s_sampling_point_data_%s.%s", getSurvey().getName(), Dates.formatCompactNow(),
				extension);
		Filedownload.save(new FileInputStream(job.getOutputFile()), mediaType, fileName);
	}

	public static void notifySamplingPointDataUpdate() {
		BindUtils.postGlobalCommand(SAMPLING_POINT_DATA_QUEUE, null,
				SamplingPointDataVM.SAMPLING_POINT_DATA_UPDATED_COMMAND, null);
	}

	public static void dispatchSamplingPointDataImportPopUpCloseCommand() {
		BindUtils.postGlobalCommand(null, null, CLOSE_SAMPLING_POINT_DATA_IMPORT_POPUP_COMMAND, null);
	}

}
