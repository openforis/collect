package org.openforis.collect.relational;

import java.util.List;

import org.openforis.collect.event.EventQueue;
import org.openforis.collect.event.RecordStep;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.relational.event.InitializeRDBEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * 
 * @author D. Wiell
 * @author S. Ricci
 *
 */
public class CollectRDBMonitor {

	@Autowired
    private PlatformTransactionManager transactionManager;
	@Autowired
	private EventQueue eventQueue;
	@Autowired
	private SurveyManager surveyManager;
	@Autowired
	private CollectLocalRDBStorageManager localRDBStorageManager;

	public void init() {
		if (eventQueue.isEnabled()) {
			runInTransaction(new Runnable() {
				public void run() {
					List<CollectSurvey> surveys = surveyManager.getAll();
					for (CollectSurvey survey : surveys) {
						for (RecordStep step : RecordStep.values()) {
							if (rdbMissing(survey, step)) {
								eventQueue.publish(new InitializeRDBEvent(survey.getName(),
										step));
							}
						}
					}
				}
			});
		}
	}

	private void runInTransaction(final Runnable task) {
		TransactionTemplate tmpl = new TransactionTemplate(transactionManager);
        tmpl.execute(new TransactionCallbackWithoutResult() {
        	@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				task.run();
			}
        });
	}

	private boolean rdbMissing(CollectSurvey survey, RecordStep step) {
		return ! localRDBStorageManager.existsRDBFile(survey.getName(), step);
	}

}
