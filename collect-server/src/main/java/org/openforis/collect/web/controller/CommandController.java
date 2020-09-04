package org.openforis.collect.web.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.openforis.collect.command.AddAttributeCommand;
import org.openforis.collect.command.AddEntityCommand;
import org.openforis.collect.command.CommandDispatcher;
import org.openforis.collect.command.CreateRecordCommand;
import org.openforis.collect.command.DeleteNodeCommand;
import org.openforis.collect.command.DeleteRecordCommand;
import org.openforis.collect.command.NodeCommand;
import org.openforis.collect.command.UpdateAttributeCommand;
import org.openforis.collect.command.UpdateBooleanAttributeCommand;
import org.openforis.collect.command.UpdateCodeAttributeCommand;
import org.openforis.collect.command.UpdateDateAttributeCommand;
import org.openforis.collect.command.UpdateTextAttributeCommand;
import org.openforis.collect.designer.metamodel.AttributeType;
import org.openforis.collect.event.RecordEvent;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("api/command")
public class CommandController {

	@Autowired
	private CommandDispatcher commandDispatcher;
//	@Autowired
//	private MessageSource messageSource;
//	@Autowired
//	private SurveyContext surveyContext;

	@RequestMapping(value="record", method=POST, consumes=APPLICATION_JSON_VALUE)
	@Transactional
	public @ResponseBody List<RecordEventView> createRecord(@RequestBody CreateRecordCommand command) {
		List<RecordEvent> events = commandDispatcher.submit(command);
		return toView(events);
	}

	@RequestMapping(value="record", method=DELETE, consumes=APPLICATION_JSON_VALUE)
	@Transactional
	public @ResponseBody List<RecordEventView> deleteRecord(@RequestBody DeleteRecordCommand command) {
		RecordEvent events = commandDispatcher.submit(command);
		return toView(Arrays.asList(events));
	}

	@RequestMapping(value="record/attribute/new", method=POST, consumes=APPLICATION_JSON_VALUE)
	@Transactional
	public @ResponseBody List<RecordEventView> addAttribute(@RequestBody AddAttributeCommand command) {
		List<RecordEvent> events = commandDispatcher.submit(command);
		return toView(events);
	}
	
	@RequestMapping(value="record/attributes", method=POST, consumes=APPLICATION_JSON_VALUE)
	@Transactional
	public @ResponseBody List<RecordEventView> addOrUpdateAttributes(@RequestBody UpdateAttributesCommandWrapper commandsWrapper) {
		List<RecordEvent> events = new ArrayList<RecordEvent>(); 
		commandsWrapper.commands.forEach(c -> {
			UpdateAttributeCommand command = c.toCommand();
			events.addAll(commandDispatcher.submit(command));
		});
		return toView(events);
	}
	
	@RequestMapping(value="record/attribute", method=POST, consumes=APPLICATION_JSON_VALUE)
	@Transactional
	public @ResponseBody List<RecordEventView> updateAttribute(@RequestBody UpdateAttributeCommandWrapper commandWrapper) {
		UpdateAttributeCommand command = commandWrapper.toCommand();
		List<RecordEvent> events = commandDispatcher.submit(command);
		return toView(events);
	}
	
	@RequestMapping(value="record/entity", method=POST, consumes=APPLICATION_JSON_VALUE)
	@Transactional
	public @ResponseBody List<RecordEventView> addEntity(@RequestBody AddEntityCommand command) {
		List<RecordEvent> events = commandDispatcher.submit(command);
		return toView(events);
	}
	
	@RequestMapping(value="record/node", method=DELETE, consumes=APPLICATION_JSON_VALUE)
	@Transactional
	public @ResponseBody List<RecordEventView> deleteNode(@RequestBody DeleteNodeCommand command) {
		List<RecordEvent> events = commandDispatcher.submit(command);
		return toView(events);
	}
	
	private List<RecordEventView> toView(List<RecordEvent> events) {
		List<RecordEventView> result = new ArrayList<RecordEventView>(events.size());
		for (RecordEvent recordEvent : events) {
			result.add(new RecordEventView(recordEvent));
		}
		return result;
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
	
	static class UpdateAttributeCommandWrapper extends UpdateAttributeCommand {
		
		private static final long serialVersionUID = 1L;
		
		AttributeType attributeType;
		Map<String, Object> valueByField;

		void setValueInCommand(NodeCommand c) {
			switch(attributeType) {
			case BOOLEAN:
				((UpdateBooleanAttributeCommand) c).setValue((Boolean) valueByField.get("value"));
				break;
			case CODE:
				((UpdateCodeAttributeCommand) c).setCode((String) valueByField.get("code"));
				break;
			case DATE:
				((UpdateDateAttributeCommand) c).setValue((Date) valueByField.get("value"));
				break;
			case TEXT:
				((UpdateTextAttributeCommand) c).setValue((String) valueByField.get("value"));
				break;
			default:
				throw new IllegalStateException("Unsupported command type: " + attributeType);
			}
		}
		
		public UpdateAttributeCommand toCommand() {
			UpdateAttributeCommand c;
			Class<? extends UpdateAttributeCommand> commandType = toCommandType();
			try {
				c = commandType.getConstructor().newInstance();
				BeanUtils.copyProperties(this, c, "attributeType", "value");
				setValueInCommand(c);
				return c;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		private Class<? extends UpdateAttributeCommand> toCommandType() {
			switch(attributeType) {
			case BOOLEAN:
				return UpdateBooleanAttributeCommand.class;
			case CODE:
				return UpdateCodeAttributeCommand.class;
			case DATE:
				return UpdateDateAttributeCommand.class;
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

		public Map<String, Object> getValueByField() {
			return valueByField;
		}
		
		public void setValueByField(Map<String, Object> valueByField) {
			this.valueByField = valueByField;
		}
	}
	
}
