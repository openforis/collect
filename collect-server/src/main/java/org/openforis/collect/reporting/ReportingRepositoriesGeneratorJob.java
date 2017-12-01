package org.openforis.collect.reporting;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

import org.openforis.collect.concurrency.SurveyLockingJob;
import org.openforis.concurrency.Progress;
import org.openforis.concurrency.ProgressListener;
import org.openforis.concurrency.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(SCOPE_PROTOTYPE)
public class ReportingRepositoriesGeneratorJob extends SurveyLockingJob {
	
	@Autowired
	private ReportingRepositories reportingRepositories;
	
	private Input input;
	
	@Override
	protected void buildTasks() throws Throwable {
		addTask(new Task() {
			protected void execute() throws Throwable {
				reportingRepositories.createRepositories(survey.getName(), input.getLanguage(), new ProgressListener() {
					public void progressMade(Progress progress) {
						setProcessedItems(progress.getProcessedItems());
						setTotalItems(progress.getTotalItems());
					}
				});
			}
		});
	}
	
	public Input getInput() {
		return input;
	}
	
	public void setInput(Input input) {
		this.input = input;
	}
	
	public static class Input {
		
		private String language;

		public Input(String language) {
			super();
			this.language = language;
		}

		public String getLanguage() {
			return language;
		}
	}

}
