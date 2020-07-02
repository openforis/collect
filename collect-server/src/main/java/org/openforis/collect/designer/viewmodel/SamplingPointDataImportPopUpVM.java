package org.openforis.collect.designer.viewmodel;

import java.util.List;

import org.openforis.collect.designer.util.MessageUtil;
import org.openforis.collect.designer.util.MessageUtil.ConfirmHandler;
import org.openforis.collect.designer.util.Resources;
import org.openforis.collect.designer.viewmodel.JobStatusPopUpVM.JobEndHandler;
import org.openforis.collect.designer.viewmodel.referencedata.ReferenceDataImportErrorsPopUpVM;
import org.openforis.collect.io.metadata.samplingdesign.SamplingPointDataImportJob;
import org.openforis.collect.utils.Files;
import org.openforis.commons.collection.CollectionUtils;
import org.openforis.idm.metamodel.SpatialReferenceSystem;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.Init;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zul.Window;

public class SamplingPointDataImportPopUpVM extends BaseSurveyFileImportVM {

	private static final String EXAMPLE_FILENAME = "sampling-point-data-example.xlsx";
	
	// transient
	private Window jobStatusPopUp;

	public SamplingPointDataImportPopUpVM() {
		super(new String[] { Files.CSV_FILE_EXTENSION, Files.EXCEL_FILE_EXTENSION }, EXAMPLE_FILENAME);
	}

	public static Window openPopUp() {
		return openPopUp(Resources.Component.SAMPLING_POINT_DATA_IMPORT_POPUP.getLocation(), true);
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
	public void importSamplingPointData() {
		MessageUtil.showConfirm(new ConfirmHandler() {
			public void onOk() {
				SamplingPointDataImportJob job = jobManager.createJob(SamplingPointDataImportJob.class);
				job.setSurvey(getSurvey());
				job.setFile(uploadedFile);
				jobManager.start(job);

				monitorImportJob(job);
			}

		}, "survey.sampling_point_data.import_data.confirm_import");
	}

	private void monitorImportJob(SamplingPointDataImportJob job) {
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

							SamplingPointDataVM.notifySamplingPointDataUpdated();
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
