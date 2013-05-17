package org.openforis.collect.manager;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openforis.collect.manager.process.AbstractProcess;
import org.openforis.collect.manager.process.ProcessStatus;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.User;
import org.openforis.collect.spring.MessageContextHolder;
import org.openforis.collect.util.ValidationMessageBuilder;
import org.openforis.commons.io.csv.CsvWriter;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.validation.Check.Flag;
import org.openforis.idm.metamodel.validation.ValidationResultFlag;
import org.openforis.idm.metamodel.validation.ValidationResults;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.NodeVisitor;

/**
 * 
 * @author S. Ricci
 *
 */
public class ValidationReportProcess extends AbstractProcess<Void, ProcessStatus> {

	private static final String[] VALIDATION_REPORT_HEADERS = new String[] {"Record","Phase","Field","Error message"};

	private static Log LOG = LogFactory.getLog(ValidationReportProcess.class);
	
	private OutputStream outputStream;
	private RecordManager recordManager;
	private ReportType reportType;
	private CollectSurvey survey;
	private String rootEntityName;
	private User user;
	private String sessionId;
	private boolean includeConfirmedErrors;

	private ValidationMessageBuilder validationMessageBuilder;
	private CsvWriter csvWriter;
	
	public enum ReportType {
		CSV
	}

	public ValidationReportProcess(OutputStream outputStream,
			RecordManager recordManager,
			MessageContextHolder messageContextHolder,
			ReportType reportType, 
			User user, String sessionId, 
			CollectSurvey survey, String rootEntityName, boolean includeConfirmedErrors) {
		super();
		this.outputStream = outputStream;
		this.recordManager = recordManager;
		this.reportType = reportType;
		this.user = user;
		this.sessionId = sessionId;
		this.survey = survey;
		this.rootEntityName = rootEntityName;
		this.includeConfirmedErrors = includeConfirmedErrors;
		validationMessageBuilder = ValidationMessageBuilder.createInstance(messageContextHolder);
	}
	
	@Override
	protected void initStatus() {
		status = new ProcessStatus();
	}

	@Override
	public Void call() throws Exception {
		status.start();
		List<CollectRecord> summaries = recordManager.loadSummaries(survey, rootEntityName, (String) null);
		if ( summaries != null ) {
			status.setTotal(summaries.size());
			try {
				initWriter();
				writeHeader();
				for (CollectRecord summary : summaries) {
					//long start = System.currentTimeMillis();
					//print(outputStream, "Start validating record: " + recordKey);
					if ( status.isRunning() ) {
						Step step = summary.getStep();
						Integer recordId = summary.getId();
						try {
							final CollectRecord record = recordManager.checkout(survey, user, recordId, step.getStepNumber(), sessionId, true);
							writeValidationReport(record);
							status.incrementProcessed();
						} finally {
							recordManager.releaseLock(recordId);
						}
						//long elapsedMillis = System.currentTimeMillis() - start;
						//print(outputStream, "Validation of record " + recordKey + " completed in " + elapsedMillis + " millis");
					}
				}
				closeWriter();
				if ( status.isRunning() ) {
					status.complete();
				}
			} catch (IOException e) {
				status.error();
				String message = e.getMessage();
				status.setErrorMessage(message);
				if ( LOG.isErrorEnabled() ) {
					LOG.error(message, e);
				}
			}
		}
		return null;
	}

	protected void initWriter() {
		switch (reportType) {
		case CSV:
			csvWriter = new CsvWriter(outputStream);
			break;
		}
	}
	
	private void closeWriter() throws IOException {
		switch (reportType) {
		case CSV:
			csvWriter.close();
			break;
		}
		
	}

	protected void writeValidationReport(final CollectRecord record) throws IOException {
		final ModelVersion version = record.getVersion();
		Entity rootEntity = record.getRootEntity();
		record.addEmptyNodes(rootEntity);
		rootEntity.traverse(new NodeVisitor() {
			@Override
			public void visit(Node<? extends NodeDefinition> node, int idx) {
				if ( node instanceof Attribute ) {
					Attribute<?,?> attribute = (Attribute<?, ?>) node;
					ValidationResults validationResults = attribute.validateValue();
					boolean errorConfirmed = record.isErrorConfirmed(attribute);
					if ( validationResults.hasErrors() || (includeConfirmedErrors && validationResults.hasWarnings() && errorConfirmed) ) {
						writeErrors(attribute, validationResults);
					}
				} else if ( node instanceof Entity ) {
					Entity entity = (Entity) node;
					EntityDefinition definition = entity.getDefinition();
					List<NodeDefinition> childDefinitions = definition.getChildDefinitions();
					for (NodeDefinition childDefinition : childDefinitions) {
						if ( version == null || version.isApplicable(childDefinition) ) {
							String childName = childDefinition.getName();
							ValidationResultFlag validateMaxCount = entity.validateMaxCount( childName );
							if ( validateMaxCount.isError() ) {
								writeMaxCountError(entity, childName);
							}
							ValidationResultFlag validateMinCount = entity.validateMinCount( childName );
							boolean missingApproved = record.isMissingApproved(entity, childName);
							if ( validateMinCount.isError() || (includeConfirmedErrors && validateMinCount.isWarning() && missingApproved) ) {
								writeMinCountError(entity, childName);
							}
						}
					}
				}
			}
		});
	}
	
	protected void writeHeader() throws IOException {
		switch (reportType) {
		case CSV:
			csvWriter.writeHeaders(VALIDATION_REPORT_HEADERS);
		break;
		}
	}

	protected void writeErrors(Attribute<?,?> attribute, ValidationResults validationResults) {
		CollectRecord record = (CollectRecord) attribute.getRecord();
		List<String> messages = validationMessageBuilder.getValidationMessages(attribute, validationResults, Flag.ERROR);
		if ( messages.isEmpty() && record.isErrorConfirmed(attribute) ) {
			messages = validationMessageBuilder.getValidationMessages(attribute, validationResults, Flag.WARN);
		}
		if ( ! messages.isEmpty() ) {
			String path = validationMessageBuilder.getPrettyFormatPath(attribute);
			for (String message : messages) {
				writeValidationReportLine(record, path, message);
			}
		}
	}
	
	private void writeMinCountError(Entity parentEntity, String childName) {
		writeCountError(true, parentEntity, childName);
	}

	private void writeMaxCountError(Entity parentEntity, String childName) {
		writeCountError(false, parentEntity, childName);
	}
	
	protected void writeCountError(boolean min, Entity parentEntity, String childName) {
		EntityDefinition parentEntityDefn = parentEntity.getDefinition();
		NodeDefinition childDefn = parentEntityDefn.getChildDefinition(childName);
		String message;
		if ( min ) {
			message = validationMessageBuilder.getMinCountValidationMessage(parentEntity, childName); 
		} else {
			message = validationMessageBuilder.getMaxCountValidationMessage(childDefn);
		}
		String path = validationMessageBuilder.getPrettyFormatPath(parentEntity, childName);
		CollectRecord record = (CollectRecord) parentEntity.getRecord();
		writeValidationReportLine(record, path, message);
	}

	protected void writeValidationReportLine(CollectRecord record, String path, String message) {
		String recordKey = validationMessageBuilder.getRecordKey(record);
		String phase = record.getStep().name();
		String[] line = new String[]{recordKey, phase, path, message};
		switch (reportType) {
		case CSV:
			csvWriter.writeNext(line);
			break;
		}
	}

}
