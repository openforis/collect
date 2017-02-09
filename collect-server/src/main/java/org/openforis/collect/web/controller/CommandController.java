package org.openforis.collect.web.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.PATCH;
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
import org.openforis.collect.command.DeleteEntityCommand;
import org.openforis.collect.command.DeleteRecordCommand;
import org.openforis.collect.command.NodeCommand;
import org.openforis.collect.command.UpdateAttributeCommand;
import org.openforis.collect.command.UpdateBooleanAttributeCommand;
import org.openforis.collect.command.UpdateCodeAttributeCommand;
import org.openforis.collect.command.UpdateDateAttributeCommand;
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
@RequestMapping("command")
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

	@RequestMapping(value="attribute", method=POST, consumes=APPLICATION_JSON_VALUE)
	@Transactional
	public @ResponseBody List<RecordEventView> addAttribute(@RequestBody AddAttributeCommand command) {
		List<RecordEvent> events = commandDispatcher.submit(command);
		return toView(events);
	}
	
	@RequestMapping(value="attribute", method=PATCH, consumes=APPLICATION_JSON_VALUE)
	@Transactional
	public @ResponseBody List<RecordEventView> updateAttribute(@RequestBody UpdateAttributeCommandWrapper commandWrapper) {
		UpdateAttributeCommand command = commandWrapper.toCommand();
		List<RecordEvent> events = commandDispatcher.submit(command);
		return toView(events);
	}
	
	@RequestMapping(value="attribute", method=DELETE, consumes=APPLICATION_JSON_VALUE)
	@Transactional
	public @ResponseBody List<RecordEventView> deleteAttribute(@RequestBody DeleteNodeCommand command) {
		List<RecordEvent> events = commandDispatcher.submit(command);
		return toView(events);
	}
	
	@RequestMapping(value="entity", method=POST, consumes=APPLICATION_JSON_VALUE)
	@Transactional
	public @ResponseBody List<RecordEventView> addEntity(@RequestBody AddEntityCommand command) {
		List<RecordEvent> events = commandDispatcher.submit(command);
		return toView(events);
	}
	
	@RequestMapping(value="entity", method=DELETE, consumes=APPLICATION_JSON_VALUE)
	@Transactional
	public @ResponseBody List<RecordEventView> deleteEntity(@RequestBody DeleteEntityCommand command) {
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
	
	static class UpdateAttributeCommandWrapper {
		
		AttributeType attributeType;
		String username;
		int surveyId;
		int recordId;
		int parentEntityId;
		int attributeDefId;
		Integer attributeId;
		Map<String, Object> value;

		void setValueInCommand(NodeCommand c) {
			switch(attributeType) {
			case BOOLEAN:
				((UpdateBooleanAttributeCommand) c).setValue((Boolean) value.get("value"));
				break;
			case CODE:
				((UpdateCodeAttributeCommand) c).setCode((String) value.get("code"));
				break;
			case DATE:
				((UpdateDateAttributeCommand) c).setValue((Date) value.get("value"));
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

		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		public int getSurveyId() {
			return surveyId;
		}

		public void setSurveyId(int surveyId) {
			this.surveyId = surveyId;
		}

		public int getRecordId() {
			return recordId;
		}

		public void setRecordId(int recordId) {
			this.recordId = recordId;
		}

		public int getParentEntityId() {
			return parentEntityId;
		}

		public void setParentEntityId(int parentEntityId) {
			this.parentEntityId = parentEntityId;
		}

		public int getAttributeDefId() {
			return attributeDefId;
		}

		public void setAttributeDefId(int attributeDefId) {
			this.attributeDefId = attributeDefId;
		}
		
		public Integer getAttributeId() {
			return attributeId;
		}

		public void setAttributeId(Integer attributeId) {
			this.attributeId = attributeId;
		}
		
		public Map<String, Object> getValue() {
			return value;
		}

		public void setValue(Map<String, Object> value) {
			this.value = value;
		}
	}
	
}
