package org.openforis.collect.web.controller;

import org.openforis.collect.manager.CollectRDBPublisher;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.relational.CollectRdbException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 
 * @author S. Ricci
 *
 */
@Controller
public class RDBPublishController {

	@Autowired
	private CollectRDBPublisher rdbPublisher;
	
	@RequestMapping(value = "/submitPublishToRdb", method = RequestMethod.POST)
	public @ResponseBody String publish(
			@RequestParam("survey") String surveyName, 
			@RequestParam("root_entity") String rootEntityName, 
			@RequestParam("schema") String schemaName/*,
			@RequestParam("url") String targetUrl,
			@RequestParam("user") String targetUser,
			@RequestParam("pass") String targetPassword*/) throws CollectRdbException {
		
		//Connection targetConn = DriverManager.getConnection(targetUrl, targetUser, targetPassword);
		//DriverManager.getConnection("jdbc:postgresql://localhost:5433/archenland1", "postgres","postgres"),
	
		rdbPublisher.export(
					surveyName,
					rootEntityName,
					Step.ANALYSIS,
					schemaName/*,
					targetConn*/);
		return "Publishing, please check log files for progress updates.";
	}
	
}
