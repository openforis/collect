package org.openforis.collect.io.metadata;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.openforis.collect.io.metadata.samplingdesign.SamplingDesignExportTask;
import org.openforis.collect.io.metadata.species.SpeciesExportTask;
import org.openforis.collect.manager.SpeciesManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.CollectTaxonomy;
import org.openforis.concurrency.Job;
import org.openforis.concurrency.WorkerStatusChangeEvent;
import org.openforis.concurrency.WorkerStatusChangeListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * 
 * @author S. Ricci
 *
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SurveyBackupJob extends Job {

	public static final String SURVEY_XML_ENTRY_NAME = "idml.xml";
	public static final String SAMPLING_DESIGN_ENTRY_NAME = "sampling_design/sampling_design.csv";
	public static final String SPECIES_ENTRY_FORMAT = "species/%s.csv";
	
	private CollectSurvey survey;
	private File outputFile;
	private ZipOutputStream zipOutputStream;
	
	@Autowired
	private SpeciesManager speciesManager;
	
	@Override
	public void init() {
		try {
			outputFile = File.createTempFile("collect", "survey_export.zip");
			zipOutputStream = new ZipOutputStream(new FileOutputStream(outputFile));
		} catch ( Exception e ) {
			throw new RuntimeException(e);
		}
		addIdmlExportTask();
		addSamplingDesignExportTask();
		addSpeciesExportTask();
		
		super.init();
	}
	
	@Override
	protected void execute() throws Throwable {
		super.execute();
		IOUtils.closeQuietly(zipOutputStream);
	}
	
	private void addIdmlExportTask() {
		IdmlExportTask task = createTask(IdmlExportTask.class);
		task.setSurvey(survey);
		task.setOutputStream(zipOutputStream);
		task.addStatusChangeListener(new SurveyBackupTaskStatusChangeListener(SURVEY_XML_ENTRY_NAME));
		addTask(task);
	}
	
	private void addSamplingDesignExportTask() {
		SamplingDesignExportTask task = createTask(SamplingDesignExportTask.class);
		task.setSurvey(survey);
		task.setOutputStream(zipOutputStream);
		task.addStatusChangeListener(new SurveyBackupTaskStatusChangeListener(SAMPLING_DESIGN_ENTRY_NAME));
		addTask(task);
	}

	private void addSpeciesExportTask() {
		List<CollectTaxonomy> taxonomies;
		if (survey.isWork()) {
			taxonomies = speciesManager.loadTaxonomiesBySurveyWork(survey.getId());
		} else {
			taxonomies = speciesManager.loadTaxonomiesBySurvey(survey.getId());
		}
		for (CollectTaxonomy taxonomy : taxonomies) {
			SpeciesExportTask task = createTask(SpeciesExportTask.class);
			task.setOutputStream(zipOutputStream);
			task.setTaxonomyId(taxonomy.getId());
			String entryName = String.format(SPECIES_ENTRY_FORMAT, taxonomy.getName());
			task.addStatusChangeListener(new SurveyBackupTaskStatusChangeListener(entryName));
			addTask(task);
		}
	}
	
	public CollectSurvey getSurvey() {
		return survey;
	}

	public void setSurvey(CollectSurvey survey) {
		this.survey = survey;
	}
	
	public File getOutputFile() {
		return outputFile;
	}
	
	private class SurveyBackupTaskStatusChangeListener implements WorkerStatusChangeListener {
		
		private String entryName;

		public SurveyBackupTaskStatusChangeListener(String entryName) {
			this.entryName = entryName;
		}
		
		@Override
		public void statusChanged(WorkerStatusChangeEvent event) {
			try {
				switch ( event.getTo() ) {
				case RUNNING:
					zipOutputStream.putNextEntry(new ZipEntry(entryName));
					break;
				case COMPLETED:
					zipOutputStream.closeEntry();
					break;
				default:
					break;
				}
			} catch ( IOException e ) {
				throw new RuntimeException("Error creating or closing the zip entry: " + e.getMessage(), e);
			}
		}

	}
}
