package org.openforis.collect.web.controller;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.openforis.collect.command.CommandDispatcher;
import org.openforis.collect.command.CreateRecordCommand;
import org.openforis.collect.command.UpdateAttributeCommand;
import org.openforis.collect.command.UpdateBooleanAttributeCommand;
import org.openforis.collect.command.UpdateCodeAttributeCommand;
import org.openforis.collect.command.UpdateDateAttributeCommand;
import org.openforis.collect.event.RecordEvent;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
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

	@RequestMapping(value="record", method=POST, consumes=MediaType.APPLICATION_JSON_VALUE)
	@Transactional
	public @ResponseBody List<RecordEventView> createRecord(@RequestBody CreateRecordCommand command) {
		List<RecordEvent> events = commandDispatcher.submit(command);
		return toView(events);
	}

	@RequestMapping(value="attribute", method=POST, consumes=MediaType.APPLICATION_JSON_VALUE)
	@Transactional
	public @ResponseBody List<RecordEvent> updateAttribute(@RequestBody UpdateAttributeCommandWrapper commandWrapper) {
		UpdateAttributeCommand command = commandWrapper.toCommand();
		List<RecordEvent> result = commandDispatcher.submit(command);
		return result;
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
		
		public enum Type {
			BOOLEAN, CODE, DATE
		}
		
		private Type type;
		private String username;
		private int surveyId;
		private int recordId;
		private int parentEntityId;
		private int attributeDefId;
		private Map<String, Object> value;
		
		public UpdateAttributeCommand toCommand() {
			UpdateAttributeCommand c;
			switch(type) {
			case BOOLEAN:
				c = new UpdateBooleanAttributeCommand();
				BeanUtils.copyProperties(this, c, "value");
				((UpdateBooleanAttributeCommand) c).setValue((Boolean) value.get("value"));
				break;
			case CODE:
				c = new UpdateCodeAttributeCommand();
				BeanUtils.copyProperties(this, c, "value");
				((UpdateCodeAttributeCommand) c).setCode((String) value.get("code"));
				break;
			case DATE:
				c = new UpdateDateAttributeCommand();
				BeanUtils.copyProperties(this, c, "value");
				((UpdateDateAttributeCommand) c).setValue((Date) value.get("value"));
				break;
			default:
				throw new IllegalStateException("Unsupported command type: " + type);
			}
			return c;
		}
		
		public Type getType() {
			return type;
		}

		public void setType(Type type) {
			this.type = type;
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

		public Map<String, Object> getValue() {
			return value;
		}

		public void setValue(Map<String, Object> value) {
			this.value = value;
		}
		
	}
	
	
}
