package org.openforis.collect.web.controller;

import java.io.Writer;
import java.util.List;

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openforis.collect.manager.SessionManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.RecordSummarySortField;
import org.openforis.collect.persistence.RecordDao;
import org.openforis.collect.web.session.SessionState;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.model.expression.InvalidExpressionException;
import org.openforis.idm.transform.AutomaticColumnProvider;
import org.openforis.idm.transform.ColumnProvider;
import org.openforis.idm.transform.ColumnProviderChain;
import org.openforis.idm.transform.DataTransformation;
import org.openforis.idm.transform.csv.ModelCsvWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author S. Ricci
 * 
 */
@Controller
public class DataExportController {
	private static Log LOG = LogFactory.getLog(DataExportController.class);
	
	@Autowired
	private SessionManager sessionManager;
	
	@Autowired
	private RecordDao recordDao;
	
	public DataExportController() {
		
	}
	
	@RequestMapping(value = "/exportEntity.htm", method = RequestMethod.GET)
	public @ResponseBody String exportEntity(HttpServletRequest request, HttpServletResponse response, 
			@RequestParam("rootEntityName") String rootEntityName, 
			@RequestParam("entityId") Integer entityId,
			@RequestParam("stepNumber") Integer stepNumber ) {
		try {
			String outputFileName = "data.csv";
			String contentType = MimetypesFileTypeMap.getDefaultFileTypeMap().getContentType(outputFileName);
			
			response.setContentType(contentType);      
			response.setHeader("Content-Disposition", "attachment; filename=" + outputFileName);

			SessionState sessionState = getSessionState(request);
			CollectSurvey survey = sessionState.getActiveSurvey();
			
			DataTransformation transform = getTransform(survey, rootEntityName, entityId);
			extractData(response.getWriter(), transform, survey, rootEntityName, Step.valueOf(stepNumber));
			return "ok";
		}
		catch (Exception e) {
			LOG.error(e);
			response.setContentType("text/html");      
			return "Error during export. See log files.";
		}
	}
	
	private DataTransformation getTransform(Survey survey, String rootEntityName, Integer entityId) throws InvalidExpressionException {
		Schema schema = survey.getSchema();
		EntityDefinition entityDefn = (EntityDefinition) schema.getById(entityId);
		String axisPath = entityDefn.getPath();
		EntityDefinition rowDefn = (EntityDefinition) schema.getByPath(axisPath);
		
		ColumnProvider provider = new ColumnProviderChain(
				/*
				new PivotExpressionColumnProvider("parent()/parent()", 
						new SingleAttributeColumnProvider("id", "cluster_id"),
						new SingleAttributeColumnProvider("region", "cluster_region"),
						new SingleAttributeColumnProvider("district", "cluster_district")),
				new PivotExpressionColumnProvider("parent()", 
						new SingleAttributeColumnProvider("no","plot_no"),	
						new SingleAttributeColumnProvider("subplot", "subplot")),
				//new TaxonColumnProvider("species"),
				 */
				new AutomaticColumnProvider(rowDefn)
				);
		return new DataTransformation(axisPath, provider);
	}
	
	private void extractData(Writer output, DataTransformation xform, CollectSurvey survey, String rootEntityName, Step step) throws Exception {
		ModelCsvWriter writer = null;
		try {
			writer = new ModelCsvWriter(output, xform);
			writer.printColumnHeadings();
			
			// Cycle over data files
			int rowsCount = 0;
			long read = 0;
			long start = System.currentTimeMillis();
			List<CollectRecord> summaries = recordDao.loadSummaries(survey, rootEntityName, 0, Integer.MAX_VALUE, (List<RecordSummarySortField>) null, (String) null);
			for (CollectRecord s : summaries) {
				if ( s.getStep().getStepNumber() <= step.getStepNumber() ) {
					CollectRecord record = recordDao.load(survey, s.getId(), step.getStepNumber());
					rowsCount += writer.printData(record);
					read++;
				}
			}
			//System.out.println("Exported "+rowsCount+" rows from "+read+" records in "+(duration/1000)+"s ("+(duration/rowsCount)+"ms/row).");
		} finally {
			if ( writer!=null ) {
				writer.flush();
				writer.close();
			}
		}
	}

	private SessionState getSessionState(HttpServletRequest request) {
		HttpSession session = request.getSession();
		if(session != null) {
			SessionState sessionState = (SessionState) session.getAttribute(SessionState.SESSION_ATTRIBUTE_NAME);
			return sessionState;
		}
		return null;
	}
	
}
