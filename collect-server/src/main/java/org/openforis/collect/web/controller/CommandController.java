package org.openforis.collect.web.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.openforis.collect.command.AddAttributeCommand;
import org.openforis.collect.command.AddEntityCommand;
import org.openforis.collect.command.CommandDispatcher;
import org.openforis.collect.command.CreateRecordCommand;
import org.openforis.collect.command.CreateRecordPreviewCommand;
import org.openforis.collect.command.DeleteAttributeCommand;
import org.openforis.collect.command.DeleteEntityCommand;
import org.openforis.collect.command.DeleteRecordCommand;
import org.openforis.collect.command.RecordCommand;
import org.openforis.collect.command.UpdateAttributeCommand;
import org.openforis.collect.command.UpdateBooleanAttributeCommand;
import org.openforis.collect.command.UpdateCodeAttributeCommand;
import org.openforis.collect.command.UpdateCoordinateAttributeCommand;
import org.openforis.collect.command.UpdateDateAttributeCommand;
import org.openforis.collect.command.UpdateFileAttributeCommand;
import org.openforis.collect.command.UpdateIntegerAttributeCommand;
import org.openforis.collect.command.UpdateIntegerRangeAttributeCommand;
import org.openforis.collect.command.UpdateMultipleAttributeCommand;
import org.openforis.collect.command.UpdateRealAttributeCommand;
import org.openforis.collect.command.UpdateRealRangeAttributeCommand;
import org.openforis.collect.command.UpdateTaxonAttributeCommand;
import org.openforis.collect.command.UpdateTextAttributeCommand;
import org.openforis.collect.command.UpdateTimeAttributeCommand;
import org.openforis.collect.designer.metamodel.AttributeType;
import org.openforis.collect.event.EventListener;
import org.openforis.collect.event.RecordEvent;
import org.openforis.collect.manager.RecordFileManager;
import org.openforis.collect.manager.SessionManager;
import org.openforis.collect.manager.SessionRecordFileManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.utils.ExceptionHandler;
import org.openforis.collect.utils.Files;
import org.openforis.collect.web.manager.SessionRecordProvider;
import org.openforis.collect.web.ws.AppWS;
import org.openforis.collect.web.ws.AppWS.RecordEventMessage;
import org.openforis.collect.web.ws.AppWS.RecordUpdateErrorMessage;
import org.openforis.commons.web.Response;
import org.openforis.idm.metamodel.BooleanAttributeDefinition;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CoordinateAttributeDefinition;
import org.openforis.idm.metamodel.DateAttributeDefinition;
import org.openforis.idm.metamodel.FileAttributeDefinition;
import org.openforis.idm.metamodel.NumberAttributeDefinition;
import org.openforis.idm.metamodel.NumericAttributeDefinition.Type;
import org.openforis.idm.metamodel.RangeAttributeDefinition;
import org.openforis.idm.metamodel.TaxonAttributeDefinition;
import org.openforis.idm.metamodel.TextAttributeDefinition;
import org.openforis.idm.metamodel.TimeAttributeDefinition;
import org.openforis.idm.model.BooleanValue;
import org.openforis.idm.model.Code;
import org.openforis.idm.model.Coordinate;
import org.openforis.idm.model.Date;
import org.openforis.idm.model.File;
import org.openforis.idm.model.FileAttribute;
import org.openforis.idm.model.IntegerRange;
import org.openforis.idm.model.IntegerValue;
import org.openforis.idm.model.RealRange;
import org.openforis.idm.model.RealValue;
import org.openforis.idm.model.TaxonOccurrence;
import org.openforis.idm.model.TextValue;
import org.openforis.idm.model.Time;
import org.openforis.idm.model.Value;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
@Scope(WebApplicationContext.SCOPE_SESSION)
@RequestMapping("api/command")
@Transactional
public class CommandController {

	@Autowired
	private transient SurveyManager surveyManager;
	@Autowired
	private transient RecordFileManager recordFileManager;
	@Autowired
	private SessionRecordProvider sessionRecordProvider;
	@Autowired
	private SessionRecordFileManager sessionRecordFileManager;
	@Autowired
	private transient CommandDispatcher commandDispatcher;
	@Autowired
	private transient SessionManager sessionManager;
	@Autowired
	private transient AppWS appWS;

	@RequestMapping(value = "record", method = POST, consumes = APPLICATION_JSON_VALUE)
	public @ResponseBody Response createRecord(@RequestBody CreateRecordCommand command) {
		return submitCommand(command);
	}

	@RequestMapping(value = "record_preview", method = POST, consumes = APPLICATION_JSON_VALUE)
	public @ResponseBody List<RecordEventView> createRecordPreview(@RequestBody CreateRecordPreviewCommand command)
			throws Exception {
		return submitCommandSync(command);
	}

	@RequestMapping(value = "record", method = DELETE, consumes = APPLICATION_JSON_VALUE)
	public @ResponseBody Response deleteRecord(@RequestBody DeleteRecordCommand command) {
		return submitCommand(command);
	}

	@RequestMapping(value = "record/attribute/new", method = POST, consumes = APPLICATION_JSON_VALUE)
	public @ResponseBody Response addAttribute(@RequestBody AddAttributeCommand command) {
		return submitCommand(command);
	}

	@RequestMapping(value = "record/attributes", method = POST, consumes = APPLICATION_JSON_VALUE)
	public @ResponseBody Response addOrUpdateAttributes(@RequestBody UpdateAttributesCommandWrapper commandsWrapper) {
		List<UpdateAttributeCommandWrapper> commands = commandsWrapper.getCommands();
		if (!commands.isEmpty()) {
			final CollectSurvey survey = getSurvey(commands.get(0));
			commands.forEach(c -> {
				UpdateAttributeCommand<?> command = c.toCommand(survey);
				submitCommand(command);
			});
		}
		return new Response();
	}

	@RequestMapping(value = "record/attribute", method = POST, consumes = APPLICATION_JSON_VALUE)
	public @ResponseBody Object updateAttribute(@RequestBody UpdateAttributeCommandWrapper commandWrapper) {
		CollectSurvey survey = getSurvey(commandWrapper);
		UpdateAttributeCommand<?> command = commandWrapper.toCommand(survey);
		return submitCommand(command);
	}

	@RequestMapping(value = "record/attribute/file", method = POST, consumes = MULTIPART_FORM_DATA_VALUE)
	public @ResponseBody Response updateAttributeFile(@RequestParam("command") String commandWrapperJsonString,
			@RequestParam("file") MultipartFile multipartFile) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		UpdateAttributeCommandWrapper commandWrapper = objectMapper.readValue(commandWrapperJsonString,
				UpdateAttributeCommandWrapper.class);
		CollectSurvey survey = getSurvey(commandWrapper);
		UpdateAttributeCommand<Value> command = commandWrapper.toCommand(survey);
		FileAttributeDefinition attrDef = survey.getSchema().getDefinitionById(command.getNodeDefId());
		if (multipartFile.getSize() <= attrDef.getMaxSize()) {
			CollectRecord record = provideRecord(command);
			FileAttribute fileAttr = record.findNodeByPath(command.getNodePath());
			File value;
			if (record.isPreview()) {
				java.io.File tempFile = sessionRecordFileManager.saveToTempFile(multipartFile.getInputStream(),
						multipartFile.getOriginalFilename(), record, fileAttr.getInternalId());
				value = new File(tempFile.getName(), multipartFile.getSize());
			} else {
				java.io.File tempFile = Files.writeToTempFile(multipartFile.getInputStream(),
						multipartFile.getOriginalFilename(), "ofc_data_entry_file");
				value = recordFileManager.moveFileIntoRepository(fileAttr, tempFile,
						multipartFile.getOriginalFilename(), false);
			}
			command.setValue(value);
			return submitCommand(command);
		} else {
			throw new IllegalArgumentException(String.format("File size (%d) exceeds expected maximum size: %d",
					multipartFile.getSize(), attrDef.getMaxSize()));
		}
	}

	@RequestMapping(value = "record/attribute/file/delete", method = POST, consumes = APPLICATION_JSON_VALUE)
	public @ResponseBody Object deleteAttributeFile(@RequestBody DeleteAttributeCommand command) throws Exception {
		CollectRecord record = provideRecord(command);
		FileAttribute fileAttr = record.findNodeByPath(command.getNodePath());
		if (record.isPreview()) {
			sessionRecordFileManager.deleteTempFile(record, fileAttr.getInternalId());
		} else {
			recordFileManager.deleteRepositoryFile(fileAttr);
		}
		UpdateFileAttributeCommand updateAttributeCommand = new UpdateFileAttributeCommand();
		PropertyUtils.copyProperties(updateAttributeCommand, command);
		updateAttributeCommand.setValue(null);
		return submitCommand(updateAttributeCommand);
	}

	@RequestMapping(value = "record/attribute/delete", method = POST, consumes = APPLICATION_JSON_VALUE)
	public @ResponseBody Object deleteAttribute(@RequestBody DeleteAttributeCommand command) {
		return submitCommand(command);
	}

	@RequestMapping(value = "record/entity", method = POST, consumes = APPLICATION_JSON_VALUE)
	public @ResponseBody Object addEntity(@RequestBody AddEntityCommand command) {
		return submitCommand(command);
	}

	@RequestMapping(value = "record/entity/delete", method = POST, consumes = APPLICATION_JSON_VALUE)
	public @ResponseBody Object deleteEntity(@RequestBody DeleteEntityCommand command) {
		return submitCommand(command);
	}

	private Response submitCommand(RecordCommand command) {
		command.setUsername(sessionManager.getLoggedUsername());
		CollectRecord record = provideRecord(command);
		commandDispatcher.submit(command, new EventListener() {
			public void onEvent(RecordEvent event) {
				appWS.sendMessage(new RecordEventMessage(new RecordEventView(event, record)));
			}
		}, new ExceptionHandler() {
			public void onException(Exception e) {
				appWS.sendMessage(new RecordUpdateErrorMessage(e.getMessage(), ExceptionUtils.getStackTrace(e)));
			}
		});
		return new Response();
	}

	private List<RecordEventView> submitCommandSync(RecordCommand command) throws Exception {
		command.setUsername(sessionManager.getLoggedUsername());
		List<RecordEvent> events = commandDispatcher.submitSync(command);
		CollectRecord record = provideRecord(command);
		List<RecordEventView> result = new ArrayList<RecordEventView>(events.size());
		for (RecordEvent event : events) {
			result.add(new RecordEventView(event, record));
		}
		return result;
	}

	private CollectSurvey getSurvey(RecordCommand command) {
		return surveyManager.getOrLoadSurveyById(command.getSurveyId());
	}

	private CollectRecord provideRecord(RecordCommand command) {
		CollectSurvey survey = getSurvey(command);
		return sessionRecordProvider.provide(survey, command.getRecordId(),
				Step.fromRecordStep(command.getRecordStep()));
	}

	static class RecordEventView {

		private RecordEvent event;
		private int recordErrorsInvalidValues;
		private int recordErrorsMissingValues;
		private int recordWarnings;
		private int recordWarningsMissingValues;

		public RecordEventView(RecordEvent event, CollectRecord record) {
			super();
			this.event = event;
			this.recordErrorsInvalidValues = record.getErrors();
			this.recordErrorsMissingValues = record.getMissingErrors();
			this.recordWarnings = record.getWarnings();
			this.recordWarningsMissingValues = record.getMissingWarnings();
		}

		public String getEventType() {
			return event.getClass().getSimpleName();
		}

		public RecordEvent getEvent() {
			return event;
		}

		public Integer getRecordErrorsInvalidValues() {
			return recordErrorsInvalidValues;
		}

		public Integer getRecordErrorsMissingValues() {
			return recordErrorsMissingValues;
		}

		public Integer getRecordWarnings() {
			return recordWarnings;
		}

		public Integer getRecordWarningsMissingValues() {
			return recordWarningsMissingValues;
		}
	}

	static class UpdateAttributesCommandWrapper {

		List<UpdateAttributeCommandWrapper> commands = new ArrayList<UpdateAttributeCommandWrapper>();

		public List<UpdateAttributeCommandWrapper> getCommands() {
			return commands;
		}

		public void setCommands(List<UpdateAttributeCommandWrapper> commands) {
			this.commands = commands;
		}
	}

	static class ValueExtractor {
		@SuppressWarnings("unchecked")
		static <V extends Value> V extractValue(AttributeType attributeType, Type numericType,
				Map<String, Object> valueByField) {
			if (valueByField == null) {
				return null;
			}
			switch (attributeType) {
			case BOOLEAN:
				return (V) new BooleanValue((Boolean) valueByField.get(BooleanAttributeDefinition.VALUE_FIELD));
			case CODE:
				return (V) new Code((String) valueByField.get(CodeAttributeDefinition.CODE_FIELD),
						(String) valueByField.get(CodeAttributeDefinition.QUALIFIER_FIELD));
			case COORDINATE:
				Number xValue = (Number) valueByField.get(CoordinateAttributeDefinition.X_FIELD_NAME);
				Number yValue = (Number) valueByField.get(CoordinateAttributeDefinition.Y_FIELD_NAME);
				Number altitudeValue = (Number) valueByField.get(CoordinateAttributeDefinition.ALTITUDE_FIELD_NAME);
				Number accuracyValue = (Number) valueByField.get(CoordinateAttributeDefinition.ACCURACY_FIELD_NAME);
				String srsId = (String) valueByField.get(CoordinateAttributeDefinition.SRS_FIELD_NAME);
				Double x = xValue == null ? null : xValue.doubleValue();
				Double y = yValue == null ? null : yValue.doubleValue();
				Double altitude = altitudeValue == null ? null : altitudeValue.doubleValue();
				Double accuracy = accuracyValue == null ? null : accuracyValue.doubleValue();
				return (V) new Coordinate(x, y, srsId, altitude, accuracy);
			case DATE:
				return (V) new Date((Integer) valueByField.get(DateAttributeDefinition.YEAR_FIELD_NAME),
						(Integer) valueByField.get(DateAttributeDefinition.MONTH_FIELD_NAME),
						(Integer) valueByField.get(DateAttributeDefinition.DAY_FIELD_NAME));
			case FILE:
				return (V) new File((String) valueByField.get(FileAttributeDefinition.FILE_NAME_FIELD),
						(Long) valueByField.get(FileAttributeDefinition.FILE_SIZE_FIELD));
			case NUMBER: {
				Integer unitId = (Integer) valueByField.get(NumberAttributeDefinition.UNIT_FIELD);
				Number number = (Number) valueByField.get(NumberAttributeDefinition.VALUE_FIELD);
				return (V) (numericType == Type.INTEGER
						? new IntegerValue(number == null ? null : number.intValue(), unitId)
						: new RealValue(number == null ? null : number.doubleValue(), unitId));
			}
			case RANGE: {
				Integer unitId = (Integer) valueByField.get(RangeAttributeDefinition.UNIT_FIELD);
				Number from = (Number) valueByField.get(RangeAttributeDefinition.FROM_FIELD);
				Number to = (Number) valueByField.get(RangeAttributeDefinition.TO_FIELD);
				return (V) (numericType == Type.INTEGER
						? new IntegerRange(from == null ? null : from.intValue(), to == null ? null : to.intValue(),
								unitId)
						: new RealRange(from == null ? null : from.doubleValue(), to == null ? null : to.doubleValue(),
								unitId));
			}
			case TAXON:
				String code = (String) valueByField.get(TaxonAttributeDefinition.CODE_FIELD_NAME);
				String scientificName = (String) valueByField.get(TaxonAttributeDefinition.SCIENTIFIC_NAME_FIELD_NAME);
				String vernacularName = (String) valueByField.get(TaxonAttributeDefinition.VERNACULAR_NAME_FIELD_NAME);
				String languageCode = (String) valueByField.get(TaxonAttributeDefinition.LANGUAGE_CODE_FIELD_NAME);
				String languageVariety = (String) valueByField
						.get(TaxonAttributeDefinition.LANGUAGE_VARIETY_FIELD_NAME);
				String familyCode = (String) valueByField.get(TaxonAttributeDefinition.FAMILY_CODE_FIELD_NAME);
				String familyScientificName = (String) valueByField
						.get(TaxonAttributeDefinition.FAMILY_SCIENTIFIC_NAME_FIELD_NAME);
				TaxonOccurrence taxonOccurrence = new TaxonOccurrence(code, scientificName, vernacularName,
						languageCode, languageVariety);
				taxonOccurrence.setFamilyCode(familyCode);
				taxonOccurrence.setFamilyScientificName(familyScientificName);
				return (V) taxonOccurrence;
			case TEXT:
				return (V) new TextValue((String) valueByField.get(TextAttributeDefinition.VALUE_FIELD));
			case TIME:
				Integer hour = (Integer) valueByField.get(TimeAttributeDefinition.HOUR_FIELD);
				Integer minute = (Integer) valueByField.get(TimeAttributeDefinition.MINUTE_FIELD);
				return (V) new Time(hour, minute);
			default:
				throw new IllegalStateException("Unsupported command type: " + attributeType);
			}
		}
	}

	static class UpdateAttributeCommandWrapper extends UpdateAttributeCommand<Value> {

		private static final long serialVersionUID = 1L;

		AttributeType attributeType;
		Type numericType;
		Map<String, Object> valueByField;
		List<Map<String, Object>> valuesByField;

		<V extends Value> V extractValue(CollectSurvey survey) {
			return ValueExtractor.extractValue(attributeType, numericType, valueByField);
		}

		private <V extends Value> List<V> extractValues(CollectSurvey survey) {
			if (valuesByField == null) {
				return Collections.emptyList();
			}
			List<V> values = new ArrayList<V>(valuesByField.size());
			for (Map<String, Object> valueByField : valuesByField) {
				values.add(ValueExtractor.extractValue(attributeType, numericType, valueByField));
			}
			return values;
		}

		@SuppressWarnings("unchecked")
		public <V extends Value> UpdateAttributeCommand<V> toCommand(CollectSurvey survey) {
			UpdateAttributeCommand<V> c;
			Class<? extends UpdateAttributeCommand<?>> commandType = toCommandType();
			try {
				c = (UpdateAttributeCommand<V>) commandType.getConstructor().newInstance();
				BeanUtils.copyProperties(this, c, "attributeType", "value", "values");
				c.setValue(extractValue(survey));

				if (c instanceof UpdateMultipleAttributeCommand) {
					((UpdateMultipleAttributeCommand<V>) c).setValues(extractValues(survey));
				}
				return c;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		private Class<? extends UpdateAttributeCommand<?>> toCommandType() {
			switch (attributeType) {
			case BOOLEAN:
				return UpdateBooleanAttributeCommand.class;
			case CODE:
				return UpdateCodeAttributeCommand.class;
			case COORDINATE:
				return UpdateCoordinateAttributeCommand.class;
			case DATE:
				return UpdateDateAttributeCommand.class;
			case FILE:
				return UpdateFileAttributeCommand.class;
			case NUMBER:
				return numericType == Type.INTEGER ? UpdateIntegerAttributeCommand.class
						: UpdateRealAttributeCommand.class;
			case RANGE:
				return numericType == Type.INTEGER ? UpdateIntegerRangeAttributeCommand.class
						: UpdateRealRangeAttributeCommand.class;
			case TAXON:
				return UpdateTaxonAttributeCommand.class;
			case TEXT:
				return UpdateTextAttributeCommand.class;
			case TIME:
				return UpdateTimeAttributeCommand.class;
			default:
				throw new IllegalStateException("Unsupported command type: " + attributeType);
			}
		}

		public AttributeType getAttributeType() {
			return attributeType;
		}

		public void setAttributeType(AttributeType attributeType) {
			this.attributeType = attributeType;
		}

		public Type getNumericType() {
			return numericType;
		}

		public void setNumericType(Type numericType) {
			this.numericType = numericType;
		}

		public Map<String, Object> getValueByField() {
			return valueByField;
		}

		public void setValueByField(Map<String, Object> valueByField) {
			this.valueByField = valueByField;
		}

		public List<Map<String, Object>> getValuesByField() {
			return valuesByField;
		}

		public void setValuesByField(List<Map<String, Object>> valuesByField) {
			this.valuesByField = valuesByField;
		}
	}

}
