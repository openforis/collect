package org.openforis.collect.command.handler;

import java.util.List;
import java.util.Locale;

import org.openforis.collect.command.NodeCommand;
import org.openforis.collect.command.RecordCommand;
import org.openforis.collect.event.EventListener;
import org.openforis.collect.event.EventListenerToList;
import org.openforis.collect.event.EventProducer;
import org.openforis.collect.event.EventProducer.EventProducerContext;
import org.openforis.collect.event.RecordEvent;
import org.openforis.collect.manager.MessageSource;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.RecordProvider;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.manager.UserManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.NodeChangeSet;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.concurrency.Job;
import org.openforis.concurrency.JobManager;
import org.openforis.concurrency.Task;

public abstract class RecordCommandHandler<C extends RecordCommand> implements CommandHandler<C> {

	protected JobManager jobManager;
	protected SurveyManager surveyManager;
	protected RecordProvider recordProvider;
	protected RecordManager recordManager;
	protected UserManager userManager;
	protected MessageSource messageSource;

	protected CollectRecord findRecord(NodeCommand command) {
		CollectSurvey survey = surveyManager.getOrLoadSurveyById(command.getSurveyId());
		return recordProvider.provide(survey, command.getRecordId(), Step.fromRecordStep(command.getRecordStep()));
	}

	protected abstract RecordCommandResult executeForResult(C command);

	protected void persistRecord(RecordCommandResult result) {
		CollectRecord record = result.getRecord();
		if (!record.isPreview()) {
			recordManager.save(record);
		}
	}

	protected RecordEvent transformEvent(RecordCommandResult result, RecordEvent event) {
		return event;
	}

	@Override
	public List<RecordEvent> executeSync(C command) {
		RecordCommandResult result = executeForResult(command);
		persistRecord(result);
		EventListenerToList eventListener = new EventListenerToList();
		notifyEvents(result, command, eventListener);
		return eventListener.getList();
	}

	@Override
	public final void execute(final C command, final EventListener eventListener) {
		jobManager.start(new Job() {
			private RecordCommandResult result;

			protected void buildTasks() throws Throwable {
				addTask(new Task() {
					protected void execute() throws Throwable {
						result = executeForResult(command);
						persistRecord(result);
					}
				});
			}

			@Override
			protected void onCompleted() {
				super.onCompleted();
				notifyEvents(result, command, eventListener);
			}

		});

	}

	private void notifyEvents(final RecordCommandResult result, final C command, final EventListener eventListener) {
		if (result.getChangeSet() != null) {
			EventProducerContext context = new EventProducer.EventProducerContext(messageSource, Locale.ENGLISH,
					command.getUsername());
			new EventProducer(context, new EventListener() {
				public void onEvent(RecordEvent event) {
					eventListener.onEvent(transformEvent(result, event));
				}
			}).produceFor(result.getChangeSet());
		} else if (result.getEvent() != null) {
			eventListener.onEvent(transformEvent(result, result.getEvent()));
		}
	}

	public void setJobManager(JobManager jobManager) {
		this.jobManager = jobManager;
	}

	public void setSurveyManager(SurveyManager surveyManager) {
		this.surveyManager = surveyManager;
	}

	public void setRecordManager(RecordManager recordManager) {
		this.recordManager = recordManager;
	}

	public void setRecordProvider(RecordProvider recordProvider) {
		this.recordProvider = recordProvider;
	}

	public void setUserManager(UserManager userManager) {
		this.userManager = userManager;
	}

	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	public static class RecordCommandResult {
		private CollectRecord record;
		private NodeChangeSet changeSet;
		private RecordEvent event;

		public RecordCommandResult() {
		}

		public RecordCommandResult(CollectRecord record, NodeChangeSet changeSet) {
			this.record = record;
			this.changeSet = changeSet;
		}

		public CollectRecord getRecord() {
			return record;
		}

		public NodeChangeSet getChangeSet() {
			return changeSet;
		}

		public RecordEvent getEvent() {
			return event;
		}

		public void setEvent(RecordEvent event) {
			this.event = event;
		}

	}

}
