package org.openforis.collect.designer.viewmodel;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.openforis.collect.designer.util.MessageUtil;
import org.openforis.collect.designer.util.Resources;
import org.openforis.collect.designer.viewmodel.JobStatusPopUpVM.JobEndHandler;
import org.openforis.collect.io.metadata.collectearth.RandomGridGenerationJob;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.SurveyFile;
import org.openforis.collect.utils.Files;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.Binder;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Window;

/**
 * 
 * @author S. Ricci
 *
 */
public class SurveyFileRandomGridGenerationParametersPopUpVM extends SurveyBaseVM {

	private static final String SURVEY_FILE_RANDOM_GRID_GENERATION_COMPLETE_GLOBAL_COMMAND = "surveyFileRandomGridGenerationComplete";
	private static final String CLOSE_SURVEY_FILE_RANDOM_GRID_GENERATION_GLOBAL_COMMAND = "closeRandomGridGenerationPopUp";

	public static final String PERCENTAGE_FIELD = "percentage";
	public static final String NEXT_MEASUREMENT_FIELD = "nextMeasurement";

	@WireVariable 
	private SurveyManager surveyManager;
	
	//input
	private SurveyFile sourceGridFile;
	
	//temporary variables
	private Map<String, Object> form;
	private Window jobStatusPopUp;

	
	public static Window openPopUp(SurveyFile sourceGridFile) {
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("sourceGridFile", sourceGridFile);
		return openPopUp(Resources.Component.SURVEY_FILE_RANDOM_GRID_GENERATION_PARAMETERS_POPUP.getLocation(), true, args);
	}

	@Init(superclass = false)
	public void init(@ExecutionArgParam("sourceGridFile") SurveyFile sourceGridFile) {
		super.init();
		this.sourceGridFile = sourceGridFile; 
		this.form = new HashMap<String, Object>();
		this.form.put(PERCENTAGE_FIELD, 5);
	}
	
	@Command
	public void close() {
		BindUtils.postGlobalCommand(null, null, CLOSE_SURVEY_FILE_RANDOM_GRID_GENERATION_GLOBAL_COMMAND, null);
	}
	
	@Command
	public void start(@ContextParam(ContextType.BINDER) Binder binder) {
		try {
			AttributeDefinition measurementKeyDef = survey.getFirstMeasurementKeyDef();
			String measurementAttrName = measurementKeyDef.getName();
			
			Double percentage = getFormFieldValue(binder, PERCENTAGE_FIELD);
			String nextMeasurement = getFormFieldValue(binder, NEXT_MEASUREMENT_FIELD);
			String outputSurveyFileName = FilenameUtils.getBaseName(sourceGridFile.getFilename()) + "_" + measurementAttrName + "_" + nextMeasurement + ".csv"; 
	
			RandomGridGenerationJob job = jobManager.createJob(RandomGridGenerationJob.class);
			job.setSurvey(survey);
			byte[] fileContent = surveyManager.loadSurveyFileContent(sourceGridFile);
			File file = Files.witeToTempFile(fileContent, "source_grid", ".csv");
			job.setSurveyManager(surveyManager);
			job.setFile(file);
			job.setSurveyFileName(outputSurveyFileName);
			job.setPercentage(percentage);
			job.setNewMeasurement(nextMeasurement);
			jobManager.start(job);
			jobStatusPopUp = JobStatusPopUpVM.openPopUp("survey.file.random_grid_generation.title", job, true, 
					new JobEndHandler<RandomGridGenerationJob>() {
				public void onJobEnd(RandomGridGenerationJob job) {
					closeJobStatusPopUp();
					switch(job.getStatus()) {
					case COMPLETED:
						MessageUtil.showInfo("survey.file.random_grid_generation.complete_successfully", outputSurveyFileName);
						Map<String, Object> args = new HashMap<String, Object>();
						args.put("outputSurveyFileName", outputSurveyFileName);
						BindUtils.postGlobalCommand(null, null, SURVEY_FILE_RANDOM_GRID_GENERATION_COMPLETE_GLOBAL_COMMAND, args);
						break;
					case FAILED:
						MessageUtil.showError("survey.file.random_grid_generation.error", job.getErrorMessage());
						break;
					default:
					}
				}
			});
		} catch (Exception e) {
			MessageUtil.showError("survey.file.random_grid_generation.error", e.getMessage());
		}
		
	}
	
	private void closeJobStatusPopUp() {
		closePopUp(jobStatusPopUp);
	}

	public Map<String, Object> getForm() {
		return form;
	}
	
	public void setForm(Map<String, Object> form) {
		this.form = form;
	}
	
}
