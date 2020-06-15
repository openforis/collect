package org.openforis.collect.io.metadata.samplingdesign;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

import org.openforis.collect.io.metadata.ReferenceDataExportJob;
import org.openforis.collect.manager.SamplingDesignManager;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(SCOPE_PROTOTYPE)
public class SamplingDesignExportJob extends ReferenceDataExportJob {

	private SamplingDesignManager samplingDesignManager;

	public SamplingDesignExportJob() {
		this.tempFilePrefix = "sampling_point_data_export";
	}

	@Override
	protected void buildTasks() throws Throwable {
		SamplingDesignExportTask t = createTask(SamplingDesignExportTask.class);
		t.setSamplingDesignManager(samplingDesignManager);
		t.setSurvey(survey);
		t.setOutputStream(outputStream);
		t.setOutputFormat(outputFormat);
		addTask(t);
	}

	public void setSamplingDesignManager(SamplingDesignManager samplingDesignManager) {
		this.samplingDesignManager = samplingDesignManager;
	}

}
