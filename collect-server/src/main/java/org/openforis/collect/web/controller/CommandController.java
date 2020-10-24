package org.openforis.collect.web.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;
import org.openforis.collect.command.AddAttributeCommand;
import org.openforis.collect.command.AddEntityCommand;
import org.openforis.collect.command.Command;
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
import org.openforis.collect.command.UpdateRealAttributeCommand;
import org.openforis.collect.command.UpdateTaxonAttributeCommand;
import org.openforis.collect.command.UpdateTextAttributeCommand;
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
import org.openforis.collect.utils.Files;
import org.openforis.collect.web.manager.SessionRecordProvider;
import org.openforis.collect.web.ws.AppWS;
import org.openforis.collect.web.ws.AppWS.RecordEventMessage;
import org.openforis.commons.web.Response;
import org.openforis.idm.metamodel.BooleanAttributeDefinition;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CoordinateAttributeDefinition;
import org.openforis.idm.metamodel.DateAttributeDefinition;
import org.openforis.idm.metamodel.FileAttributeDefinition;
import org.openforis.idm.metamodel.NumberAttributeDefinition;
import org.openforis.idm.metamodel.NumericAttributeDefinition.Type;
import org.openforis.idm.metamodel.TaxonAttributeDefinition;
import org.openforis.idm.metamodel.TextAttributeDefinition;
import org.openforis.idm.metamodel.Unit;
import org.openforis.idm.model.BooleanValue;
import org.openforis.idm.model.Code;
import org.openforis.idm.model.Coordinate;
import org.openforis.idm.model.Date;
import org.openforis.idm.model.File;
import org.openforis.idm.model.FileAttribute;
import org.openforis.idm.model.IntegerValue;
import org.openforis.idm.model.RealValue;
import org.openforis.idm.model.TaxonOccurrence;
import org.openforis.idm.model.TextValue;
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
	@Transactional
	public @ResponseBody Response createRecord(@RequestBody CreateRecordCommand command) {
		return submitCommand(command);
	}

	@RequestMapping(value = "record_preview", method = POST, consumes = APPLICATION_JSON_VALUE)
	public @ResponseBody List<RecordEventView> createRecordPreview(@RequestBody CreateRecordPreviewCommand command) {
		return submitCommandSync(command);
	}

	@RequestMapping(value = "record", method = DELETE, consumes = APPLICATION_JSON_VALUE)
	@Transactional
	public @ResponseBody Response deleteRecord(@RequestBody DeleteRecordCommand command) {
		return submitCommand(command);
	}

	@RequestMapping(value = "record/attribute/new", method = POST, consumes = APPLICATION_JSON_VALUE)
	@Transactional
	public @ResponseBody Response addAttribute(@RequestBody AddAttributeCommand command) {
		return submitCommand(command);
	}

	@RequestMapping(value = "record/attributes", method = POST, consumes = APPLICATION_JSON_VALUE)
	@Transactional
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
	@Transactional
	public @ResponseBody Object updateAttribute(@RequestBody UpdateAttributeCommandWrapper commandWrapper) {
		CollectSurvey survey = getSurvey(commandWrapper);
		UpdateAttributeCommand<?> command = commandWrapper.toCommand(survey);
		return submitCommand(command);
	}

	@RequestMapping(value = "record/attribute/file", method = POST, consumes = MULTIPART_FORM_DATA_VALUE)
	@Transactional
	public @ResponseBody Response updateAttributeFile(
			@RequestParam("command") String commandWrapperJsonString,
			@RequestParam("file") MultipartFile multipartFile) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		UpdateAttributeCommandWrapper commandWrapper = objectMapper.readValue(commandWrapperJsonString, UpdateAttributeCommandWrapper.class);
		CollectSurvey survey = getSurvey(commandWrapper);
		UpdateAttributeCommand<Value> command = commandWrapper.toCommand(survey);
		FileAttributeDefinition attrDef = survey.getSchema().getDefinitionById(command.getNodeDefId());
		if (multipartFile.getSize() <= attrDef.getMaxSize()) {
			CollectRecord record = sessionRecordProvider.provide(survey, command.getRecordId(), Step.fromRecordStep(command.getRecordStep()));
			FileAttribute fileAttr = record.findNodeByPath(command.getNodePath());
			File value;
			if (record.isPreview()) {
				java.io.File tempFile = sessionRecordFileManager.saveToTempFile(multipartFile.getInputStream(), multipartFile.getOriginalFilename(), record, fileAttr.getInternalId());
				value = new File(tempFile.getName(), multipartFile.getSize());
			} else {
				java.io.File tempFile = Files.writeToTempFile(multipartFile.getInputStream(), multipartFile.getOriginalFilename(), "ofc_data_entry_file");
				value = recordFileManager.moveFileIntoRepository(fileAttr, tempFile, multipartFile.getOriginalFilename(), false);
			}
			command.setValue(value);
			return submitCommand(command);
		} else {
			throw new IllegalArgumentException(String.format("File size (%d) exceeds expected maximum size: %d", multipartFile.getSize(), attrDef.getMaxSize()));
		}
	}

	@RequestMapping(value = "record/attribute/file/delete", method = POST, consumes = APPLICATION_JSON_VALUE)
	@Transactional
	public @ResponseBody Object deleteAttributeFile(@RequestBody DeleteAttributeCommand command) throws Exception {
		CollectSurvey survey = getSurvey(command);
		CollectRecord record = sessionRecordProvider.provide(survey, command.getRecordId(), Step.fromRecordStep(command.getRecordStep()));
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
	@Transactional
	public @ResponseBody Object deleteAttribute(@RequestBody DeleteAttributeCommand command) {
		return submitCommand(command);
	}

	@RequestMapping(value = "record/entity", method = POST, consumes = APPLICATION_JSON_VALUE)
	@Transactional
	public @ResponseBody Object addEntity(@RequestBody AddEntityCommand command) {
		return submitCommand(command);
	}

	@RequestMapping(value = "record/entity/delete", method = POST, consumes = APPLICATION_JSON_VALUE)
	@Transactional
	public @ResponseBody Object deleteEntity(@RequestBody DeleteEntityCommand command) {
		return submitCommand(command);
	}

	private Response submitCommand(Command command) {
		if (command instanceof RecordCommand) {
			((RecordCommand) command).setUsername(sessionManager.getLoggedUsername());
		}
		commandDispatcher.submit(command, new EventListener() {
			public void onEvent(RecordEvent event) {
				appWS.sendMessage(new RecordEventMessage(new RecordEventView(event)));
			}
		});
		return new Response();
	}

	private List<RecordEventView> submitCommandSync(Command command) {
		if (command instanceof RecordCommand) {
			((RecordCommand) command).setUsername(sessionManager.getLoggedUsername());
		}
		List<RecordEvent> events = commandDispatcher.submitSync(command);

		List<RecordEventView> result = new ArrayList<RecordEventView>(events.size());
		for (RecordEvent event : events) {
			result.add(new RecordEventView(event));
		}
		return result;
	}

	private CollectSurvey getSurvey(RecordCommand command) {
		return surveyManager.getOrLoadSurveyById(command.getSurveyId());
	}

	static class RecordEventView {

		private RecordEvent event;

		public RecordEventView(RecordEvent event) {
			super();
			this.event = event;
		}

		public String getEventType() {
			return event.getClass().getSimpleName();
		}

		public RecordEvent getEvent() {
			return event;
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

	static class UpdateAttributeCommandWrapper extends UpdateAttributeCommand<Value> {

		private static final long serialVersionUID = 1L;

		AttributeType attributeType;
		Type numericType;
		Map<String, Object> valueByField;

		Value extractValue(CollectSurvey survey) {
			if (valueByField == null) {
				return null;
			}
			switch (attributeType) {
			case BOOLEAN:
				return new BooleanValue((Boolean) valueByField.get(BooleanAttributeDefinition.VALUE_FIELD));
			case CODE:
				return new Code((String) valueByField.get(CodeAttributeDefinition.CODE_FIELD),
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
				return new Coordinate(x, y, srsId, altitude, accuracy);
			case DATE:
				return new Date((Integer) valueByField.get(DateAttributeDefinition.YEAR_FIELD_NAME),
						(Integer) valueByField.get(DateAttributeDefinition.MONTH_FIELD_NAME),
						(Integer) valueByField.get(DateAttributeDefinition.DAY_FIELD_NAME));
			case FILE:
				return new File((String) valueByField.get(FileAttributeDefinition.FILE_NAME_FIELD),
						(Long) valueByField.get(FileAttributeDefinition.FILE_SIZE_FIELD));
			case NUMBER:
				Integer unitId = (Integer) valueByField.get(NumberAttributeDefinition.UNIT_FIELD);
				Unit unit = unitId == null ? null : survey.getUnit(unitId);
				Number number = (Number) valueByField.get(NumberAttributeDefinition.VALUE_FIELD);
				return numericType == Type.INTEGER ? new IntegerValue(number == null ? null : number.intValue(), unit)
						: new RealValue(number == null ? null : number.doubleValue(), unit);
			case TAXON:
				String code = (String) valueByField.get(TaxonAttributeDefinition.CODE_FIELD_NAME);
				String scientificName = (String) valueByField.get(TaxonAttributeDefinition.SCIENTIFIC_NAME_FIELD_NAME);
				String vernacularName = (String) valueByField.get(TaxonAttributeDefinition.VERNACULAR_NAME_FIELD_NAME);
				String languageCode = (String) valueByField.get(TaxonAttributeDefinition.LANGUAGE_CODE_FIELD_NAME);
				String languageVariety = (String) valueByField.get(TaxonAttributeDefinition.LANGUAGE_VARIETY_FIELD_NAME);
				return new TaxonOccurrence(code, scientificName, vernacularName, languageCode, languageVariety);
			case TEXT:
				return new TextValue((String) valueByField.get(TextAttributeDefinition.VALUE_FIELD));
			default:
				throw new IllegalStateException("Unsupported command type: " + attributeType);
			}
		}

		@SuppressWarnings("unchecked")
		public UpdateAttributeCommand<Value> toCommand(CollectSurvey survey) {
			UpdateAttributeCommand<Value> c;
			Class<? extends UpdateAttributeCommand<?>> commandType = toCommandType();
			try {
				c = (UpdateAttributeCommand<Value>) commandType.getConstructor().newInstance();
				BeanUtils.copyProperties(this, c, "attributeType", "value");
				c.setValue(extractValue(survey));
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
			case TAXON:
				return UpdateTaxonAttributeCommand.class;
			case TEXT:
				return UpdateTextAttributeCommand.class;
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
	}

}
