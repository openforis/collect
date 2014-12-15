package org.openforis.collect.web.controller;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.relational.CollectRDBPublisher;
import org.openforis.collect.relational.CollectRdbException;
import org.openforis.collect.relational.model.RelationalSchemaConfig;
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
	
	@RequestMapping(value = "/rdbpublish", method = RequestMethod.GET)
	public @ResponseBody String publish(
			@RequestParam("survey") String surveyName, 
			@RequestParam("root_entity") String rootEntityName, 
			@RequestParam("targeturl") String targetUrl,
			@RequestParam("targetuser") String targetUser,
			@RequestParam("targetpass") String targetPassword,
			@RequestParam("targetschema") String schemaName
			) throws CollectRdbException, SQLException {
		
		Connection targetConn = DriverManager.getConnection(targetUrl, targetUser, targetPassword);
//		DriverManager.getConnection("jdbc:postgresql://localhost:5433/archenland1", "postgres","postgres"),
	
		rdbPublisher.export(
					surveyName,
					rootEntityName,
					Step.ANALYSIS,
					schemaName,
					targetConn,
					RelationalSchemaConfig.createDefault());
		return "Publishing, please check log files in order to monitor the process.";
	}
	
}
