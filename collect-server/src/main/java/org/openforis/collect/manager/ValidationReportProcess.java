package org.openforis.collect.manager;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

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

	private static final String[] VALIDATION_REPORT_HEADERS = new String[] {"Record","Field","Error message"};

	//private static Log LOG = LogFactory.getLog(ValidationReportProcess.class);
	
	private OutputStream outputStream;
	private RecordManager recordManager;
	private ReportType reportType;
	private CollectSurvey survey;
	private String rootEntityName;
	private User user;
	private String sessionId;
	private CsvWriter csvWriter;

	private ValidationMessageBuilder validationMessageBuilder;
	
	public enum ReportType {
		CSV
	}

	public ValidationReportProcess(OutputStream outputStream,
			RecordManager recordManager,
			MessageContextHolder messageContextHolder,
			ReportType reportType, 
			User user, String sessionId, 
			CollectSurvey survey, String rootEntityName) {
		super();
		this.outputStream = outputStream;
		this.recordManager = recordManager;
		this.reportType = reportType;
		this.user = user;
		this.sessionId = sessionId;
		this.survey = survey;
		this.rootEntityName = rootEntityName;
		validationMessageBuilder = ValidationMessageBuilder.createInstance(messageContextHolder);
	}
	
	@Override
	protected void initStatus() {
		status = new ProcessStatus();
	}

	@Override
	public Void call() throws Exception {
		List<CollectRecord> summaries = recordManager.loadSummaries(survey, rootEntityName, (String) null);
		if ( summaries != null ) {
			initWriter();
			writeHeader();
			for (CollectRecord summary : summaries) {
				//long start = System.currentTimeMillis();
				//print(outputStream, "Start validating record: " + recordKey);
				Integer id = summary.getId();
				Step step = summary.getStep();
				final CollectRecord record = recordManager.checkout(survey, user, id, step.getStepNumber(), sessionId, true);
				printValidationReport(record);
				recordManager.releaseLock(id);
				//long elapsedMillis = System.currentTimeMillis() - start;
				//print(outputStream, "Validation of record " + recordKey + " completed in " + elapsedMillis + " millis");
			}
			closeWriter();
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

	protected void printValidationReport(final CollectRecord record) throws IOException {
		final ModelVersion version = record.getVersion();
		Entity rootEntity = record.getRootEntity();
		recordManager.addEmptyNodes(rootEntity);
		rootEntity.traverse(new NodeVisitor() {
			@Override
			public void visit(Node<? extends NodeDefinition> node, int idx) {
				if ( node instanceof Attribute ) {
					Attribute<?,?> attribute = (Attribute<?, ?>) node;
					ValidationResults validationResults = attribute.validateValue();
					if ( validationResults.hasErrors() ) {
						writeValidationResults(attribute, validationResults);
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
							if ( validateMinCount.isError() ) {
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

	protected void writeValidationResults(Attribute<?,?> attribute, ValidationResults validationResults) {
		List<String> messages = validationMessageBuilder.getValidationMessages(attribute, validationResults);
		if ( ! messages.isEmpty() ) {
			CollectRecord record = (CollectRecord) attribute.getRecord();
			String recordKey = validationMessageBuilder.getRecordKey(record);
			String path = validationMessageBuilder.getPrettyFormatPath(attribute);
			for (String message : messages) {
				writeValidationReportLine(recordKey, path, message);
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
		String message = min ? validationMessageBuilder.getMinCountValidationMessage(childDefn) : validationMessageBuilder.getMaxCountValidationMessage(childDefn);
		String path = validationMessageBuilder.getPrettyFormatPath(parentEntity, childName);
		CollectRecord record = (CollectRecord) parentEntity.getRecord();
		String recordKey = validationMessageBuilder.getRecordKey(record);
		writeValidationReportLine(recordKey, path, message);
	}

	protected void writeValidationReportLine(String recordKey, String path, String message) {
		String[] line = new String[]{recordKey, path, message};
		switch (reportType) {
		case CSV:
			csvWriter.writeNext(line);
			break;
		}
	}

}
