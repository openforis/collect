package org.openforis.collect.web.controller;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

import javax.validation.Valid;

import org.openforis.collect.dataview.QueryDto;
import org.openforis.collect.dataview.QueryExecutor;
import org.openforis.collect.dataview.QueryResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.WebApplicationContext;

@Controller
@Scope(value=WebApplicationContext.SCOPE_SESSION)
@RequestMapping(value = "api")
public class QueryController {

	@Autowired
	private QueryExecutor queryExecutor;
	
	@RequestMapping(value="survey/{surveyId}/data/query", method=GET)
	public @ResponseBody QueryResult getQueryResult(@Valid QueryDto query) {
		return queryExecutor.runQuery(query);
	}
}
