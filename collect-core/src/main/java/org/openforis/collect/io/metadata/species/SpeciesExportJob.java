package org.openforis.collect.io.metadata.species;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

import org.openforis.collect.io.metadata.ReferenceDataExportJob;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(SCOPE_PROTOTYPE)
public class SpeciesExportJob extends ReferenceDataExportJob {

	private int taxonomyId;

	public SpeciesExportJob() {
		this.tempFilePrefix = "species_export";
	}

	@Override
	protected void buildTasks() throws Throwable {
		SpeciesExportTask t = createTask(SpeciesExportTask.class);
		t.setSurvey(survey);
		t.setTaxonomyId(taxonomyId);
		t.setOutputStream(outputStream);
		t.setOutputFormat(outputFormat);
		addTask(t);
	}

	public void setTaxonomyId(int taxonomyId) {
		this.taxonomyId = taxonomyId;
	}

}
