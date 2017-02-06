package org.openforis.collect.web.controller;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.Date;
import java.util.Map;

import org.openforis.collect.command.Command;
import org.openforis.collect.command.CommandBrokerCommandQueue;
import org.openforis.collect.command.UpdateBooleanAttributeCommand;
import org.openforis.collect.command.UpdateCodeAttributeCommand;
import org.openforis.collect.command.UpdateDateAttributeCommand;
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
	private CommandBrokerCommandQueue commandQueue;
	
	@RequestMapping(value="attribute", method=POST, consumes=MediaType.APPLICATION_JSON_VALUE)
	@Transactional
	public @ResponseBody Boolean send(@RequestBody UpdateAttributeCommandWrapper commandWrapper) {
		Command command = commandWrapper.toCommand();
		commandQueue.publish(command);
		return true;
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
		
		public Command toCommand() {
			Command c;
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
