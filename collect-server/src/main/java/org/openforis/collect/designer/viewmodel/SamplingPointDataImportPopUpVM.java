package org.openforis.collect.designer.viewmodel;

import java.io.InputStream;
import java.util.List;

import javax.servlet.ServletContext;

import org.openforis.collect.designer.util.MessageUtil;
import org.openforis.collect.designer.viewmodel.JobStatusPopUpVM.JobEndHandler;
import org.openforis.collect.designer.viewmodel.referencedata.ReferenceDataImportErrorsPopUpVM;
import org.openforis.collect.io.metadata.samplingdesign.SamplingPointDataImportJob;
import org.openforis.collect.utils.Files;
import org.openforis.collect.utils.MediaTypes;
import org.openforis.commons.collection.CollectionUtils;
import org.openforis.idm.metamodel.SpatialReferenceSystem;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.Init;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Window;

public class SamplingPointDataImportPopUpVM extends BaseSurveyFileImportVM {

	// transient
	private Window jobStatusPopUp;

	public SamplingPointDataImportPopUpVM() {
		super(new String[] { Files.CSV_FILE_EXTENSION, Files.EXCEL_FILE_EXTENSION });
	}

	@Init(superclass = false)
	public void init() {
		super.init();
	}

	@Command
	public void close(@ContextParam(ContextType.TRIGGER_EVENT) Event event) {
		if (event != null) {
			event.stopPropagation();
		}
		SamplingPointDataVM.dispatchSamplingPointDataImportPopUpCloseCommand();
	}

	public String getAvailableSrsIds() {
		List<SpatialReferenceSystem> spatialReferenceSystems = getSurvey().getSpatialReferenceSystems();
		return String.join(", ", CollectionUtils.project(spatialReferenceSystems, "id"));
	}

	@Command
	public void downloadExample() {
		ServletContext context = getSession().getWebApp().getServletContext();
		String fileName = "sampling-point-data-example.xlsx";
		InputStream is = context.getResourceAsStream("/WEB-INF/resources/io/" + fileName);
		Filedownload.save(is, MediaTypes.XLSX_CONTENT_TYPE, fileName);
	}

	@Command
	public void importSamplingPointData() {
		SamplingPointDataImportJob job = jobManager.createJob(SamplingPointDataImportJob.class);
		job.setSurvey(getSurvey());
		job.setFile(uploadedFile);
		jobManager.start(job);

		String messagePrefix = "survey.sampling_point_data.import_data.";
		jobStatusPopUp = JobStatusPopUpVM.openPopUp(messagePrefix + "title", job, true,
				new JobEndHandler<SamplingPointDataImportJob>() {
					@Override
					public void onJobEnd(SamplingPointDataImportJob job) {
						closePopUp(jobStatusPopUp);
						switch (job.getStatus()) {
						case COMPLETED:
							MessageUtil.showInfo(messagePrefix + "completed");
							// Survey has been updated: save it!
							SurveyEditVM.dispatchSurveySaveCommand();

							SamplingPointDataVM.notifySamplingPointDataUpdate();
							close(null);
							break;
						case FAILED:
							String title = Labels.getLabel(messagePrefix + "error_popup.title",
									new String[] { getUploadedFileName() });
							ReferenceDataImportErrorsPopUpVM.showPopUp(job.getErrors(), title);
							break;
						default:
						}
					}
				});
	}

}
