package org.openforis.collect.manager.validation;

import java.util.List;

import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.process.AbstractProcess;
import org.openforis.collect.manager.process.ProcessStatus;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectRecordSummary;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.RecordFilter;
import org.openforis.collect.model.User;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.Schema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author S. Ricci
 *
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class RecordValidationProcess extends AbstractProcess<Void, ProcessStatus> {

//	private static Log LOG = LogFactory.getLog(RecordValidationProcess.class);
	
	@Autowired
	private RecordManager recordManager;
	
	private CollectSurvey survey;
	private User user;
	private String sessionId;

	@Override
	protected void initStatus() {
		status = new ProcessStatus();
	}

	@Override
	@Transactional(propagation=Propagation.SUPPORTS)
	public void startProcessing() throws Exception {
		super.startProcessing();
		validateParameters();
		status.setTotal(recordManager.countRecords(new RecordFilter(survey)));
		Schema schema = survey.getSchema();
		List<EntityDefinition> rootEntities = schema.getRootEntityDefinitions();
		for (EntityDefinition rootEntityDef : rootEntities) {
			RecordFilter filter = new RecordFilter(survey, rootEntityDef.getId());
			List<CollectRecordSummary> summaries = recordManager.loadSummaries(filter);
			for (CollectRecordSummary summary : summaries) {
				//long start = System.currentTimeMillis();
				//print(outputStream, "Start validating record: " + recordKey);
				if ( status.isRunning() ) {
					Step step = summary.getStep();
					Integer recordId = summary.getId();
					recordManager.validateAndSave(survey, user, sessionId, recordId, step);
					status.incrementProcessed();
					//long elapsedMillis = System.currentTimeMillis() - start;
					//print(outputStream, "Validation of record " + recordKey + " completed in " + elapsedMillis + " millis");
				}
			}
		}
		if ( status.isRunning() ) {
			status.complete();
		}
	}

	private void validateParameters() {
		if ( survey == null || user == null || sessionId == null ) {
			throw new IllegalStateException("Survey, user and sessionId must be specified before starting the process");
		}
	}

	public CollectSurvey getSurvey() {
		return survey;
	}

	public void setSurvey(CollectSurvey survey) {
		this.survey = survey;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	
}
