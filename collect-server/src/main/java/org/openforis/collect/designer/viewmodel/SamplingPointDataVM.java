package org.openforis.collect.designer.viewmodel;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.openforis.collect.designer.util.Resources;
import org.openforis.collect.io.metadata.samplingdesign.SamplingDesignExportJob;
import org.openforis.collect.io.metadata.samplingdesign.SamplingDesignExportTask.OutputFormat;
import org.openforis.collect.manager.SamplingDesignManager;
import org.openforis.collect.model.SamplingDesignItem;
import org.openforis.collect.utils.Dates;
import org.openforis.collect.utils.MediaTypes;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Window;

public class SamplingPointDataVM extends SurveyBaseVM {

	public static final String CLOSE_DATA_IMPORT_POP_UP_COMMAND = "closeSamplingPointDataImportPopUp";

	@WireVariable
	private SamplingDesignManager samplingDesignManager;
	
	private Window samplingPointDataImportPopUp;
	
	@Init(superclass=false)
	public void init() {
		super.init();
	}
	
	public List<SamplingDesignItem> getSamplingPointItems() {
		Integer surveyId = getSurveyId();
		if (surveyId == null) {
			//TODO session expired
			return Collections.emptyList();
		}
		int offset = 0;
		int maxRecords = 30;
		return samplingDesignManager.loadBySurvey(surveyId, offset, maxRecords).getRecords();
	}
	
	public String getLevelCode(SamplingDesignItem item, int level) {
		return item.getLevelCodes().size() >= level
			? item.getLevelCode(level)
			: null;
	}
	
	@Command
	public void openImportPopUp() {
		samplingPointDataImportPopUp = openPopUp(Resources.Component.SAMPLING_POINT_DATA_IMPORT_POPUP.getLocation(), true);
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

		String mediaType = outputFormat == OutputFormat.CSV 
				? MediaTypes.CSV_CONTENT_TYPE 
				: MediaTypes.XLSX_CONTENT_TYPE;
		String extension = outputFormat == OutputFormat.CSV ? "csv" : "xlsx";
		String fileName = String.format("%s_sampling_point_data_%s.%s", 
				getSurvey().getName(), Dates.formatCompactNow(), extension);
		Filedownload.save(new FileInputStream(job.getOutputFile()), mediaType, fileName);
	}

}
