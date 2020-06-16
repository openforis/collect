package org.openforis.collect.designer.viewmodel;

import java.util.HashMap;
import java.util.Map;

import org.openforis.collect.designer.util.MessageUtil;
import org.openforis.collect.designer.util.MessageUtil.ConfirmHandler;
import org.openforis.collect.designer.util.Resources;
import org.openforis.collect.designer.viewmodel.JobStatusPopUpVM.JobEndHandler;
import org.openforis.collect.designer.viewmodel.referencedata.ReferenceDataImportErrorsPopUpVM;
import org.openforis.collect.io.metadata.species.SpeciesImportJob;
import org.openforis.collect.utils.Files;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zul.Window;

public class TaxonomyImportPopUpVM extends BaseSurveyFileImportVM {

	private static final String EXAMPLE_FILENAME = "species-list-example.xlsx";
	// transient
	private Window jobStatusPopUp;
	private int taxonomyId;

	public TaxonomyImportPopUpVM() {
		super(new String[] { Files.CSV_FILE_EXTENSION, Files.EXCEL_FILE_EXTENSION }, EXAMPLE_FILENAME);
	}

	@Init(superclass = false)
	public void init(@ExecutionArgParam("taxonomyId") int taxonomyId) {
		super.init();
		this.taxonomyId = taxonomyId;
	}

	public static Window openPopUp(int taxonomyId) {
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("taxonomyId", taxonomyId);
		return openPopUp(Resources.Component.TAXONOMY_IMPORT_POPUP.getLocation(), true, args);
	}

	@Command
	public void close(@ContextParam(ContextType.TRIGGER_EVENT) Event event) {
		if (event != null) {
			event.stopPropagation();
		}
		TaxonomiesVM.dispatchCloseTaxonomyImportPopUpCommand();
	}

	@Command
	public void importData() {
		MessageUtil.showConfirm(new ConfirmHandler() {
			public void onOk() {
				SpeciesImportJob job = jobManager.createJob(SpeciesImportJob.class);
				job.setSurvey(getSurvey());
				job.setFile(uploadedFile);
				job.setTaxonomyId(taxonomyId);
				jobManager.start(job);

				monitorImportJob(job);
			}

		}, "survey.taxonomy.import_data.confirm_import");
	}

	private void monitorImportJob(SpeciesImportJob job) {
		String messagePrefix = "survey.taxonomy.import_data.";
		jobStatusPopUp = JobStatusPopUpVM.openPopUp(messagePrefix + "title", job, true,
				new JobEndHandler<SpeciesImportJob>() {
					public void onJobEnd(SpeciesImportJob job) {
						closePopUp(jobStatusPopUp);
						switch (job.getStatus()) {
						case COMPLETED:
							MessageUtil.showInfo(messagePrefix + "completed");
							// Survey has been updated: save it!
							SurveyEditVM.dispatchSurveySaveCommand();

							TaxonomiesVM.dispatchTaxonomyUpdatedCommand(taxonomyId);

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
