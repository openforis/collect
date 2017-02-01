package org.openforis.collect.web.controller;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

import org.openforis.collect.command.CommandQueue;
import org.openforis.collect.command.UpdateBooleanAttributeCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("command")
public class CommandController {

	@Autowired
	private CommandQueue commandQueue;
	
	@RequestMapping(value="attribute/boolean", method=POST)
	public @ResponseBody Boolean send(@ModelAttribute UpdateBooleanAttributeCommand command) {
		commandQueue.add(command);
		return true;
	}
	
}
