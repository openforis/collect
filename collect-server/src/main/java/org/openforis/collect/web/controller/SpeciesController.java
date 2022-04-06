package org.openforis.collect.web.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.openforis.collect.manager.SessionManager;
import org.openforis.collect.manager.SpeciesManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.manager.TaxonSearchParameters;
import org.openforis.collect.manager.dataexport.species.SpeciesExportProcess;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.CollectTaxonomy;
import org.openforis.collect.model.UserRoles;
import org.openforis.collect.model.proxy.TaxonOccurrenceProxy;
import org.openforis.collect.utils.Controllers;
import org.openforis.collect.utils.MediaTypes;
import org.openforis.collect.utils.Proxies;
import org.openforis.idm.model.TaxonOccurrence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SpeciesController {

	private static final String SPECIES_LIST_CSV_FILE_NAME = "species_list.csv";

	@Autowired
	private SpeciesManager speciesManager;
	@Autowired
	private SurveyManager surveyManager;
	@Autowired
	@Qualifier("sessionManager")
	private SessionManager sessionManager;

	@RequestMapping(value = "api/survey/{surveyId}/taxonomy/{taxonomyId}/export.csv", method = RequestMethod.GET)
	public @ResponseBody String exportSpecies(HttpServletResponse response, @PathVariable("surveyId") Integer surveyId,
			@PathVariable("taxonomyId") Integer taxonomyId) throws IOException {
		Controllers.setOutputContent(response, SPECIES_LIST_CSV_FILE_NAME, MediaTypes.CSV_CONTENT_TYPE);
		ServletOutputStream out = response.getOutputStream();
		CollectSurvey survey = surveyManager.loadSurvey(surveyId);
		SpeciesExportProcess process = new SpeciesExportProcess(speciesManager);
		process.exportToCSV(out, survey, taxonomyId);
		return "ok";
	}

	@Secured(UserRoles.ENTRY)
	@RequestMapping(value = "api/survey/{surveyId}/taxonomy/{taxonomyName}/query", method = RequestMethod.POST)
	public @ResponseBody List<TaxonOccurrenceProxy> findTaxon(@PathVariable("surveyId") int surveyId,
			@PathVariable("taxonomyName") String taxonomyName, @RequestBody TaxonQuery query) {
		CollectTaxonomy taxonomy = loadTaxonomy(surveyId, taxonomyName);
		List<TaxonOccurrence> list;
		switch (query.field) {
		case FAMILY_CODE:
			list = speciesManager.findByFamilyCode(taxonomy, query.searchString, query.maxResults, query.parameters);
			break;
		case FAMILY_SCIENTIFIC_NAME:
			list = speciesManager.findByFamilyScientificName(taxonomy, query.searchString, query.maxResults,
					query.parameters);
			break;
		case CODE:
			list = speciesManager.findByCode(taxonomy, query.searchString, query.maxResults, query.parameters);
			break;
		case SCIENTIFIC_NAME:
			list = speciesManager.findByScientificName(taxonomy, query.searchString, query.maxResults,
					query.parameters);
			break;
		case VERNACULAR_NAME:
			list = speciesManager.findByVernacularName(taxonomy, null, query.searchString, query.maxResults,
					query.parameters);
			break;
		default:
			throw new IllegalArgumentException(String.format("Taxon query: invalid field %s", query.field));
		}
		return Proxies.fromList(list, TaxonOccurrenceProxy.class);
	}

	private CollectTaxonomy loadTaxonomy(int surveyId, String taxonomyName) {
		CollectSurvey survey = surveyManager.getOrLoadSurveyById(surveyId);
		return speciesManager.loadTaxonomyByName(survey, taxonomyName);
	}

	public static class TaxonQuery {
		public enum FIELD {
			FAMILY_CODE, FAMILY_SCIENTIFIC_NAME, CODE, SCIENTIFIC_NAME, VERNACULAR_NAME
		}

		public FIELD field;

		public String searchString;

		public int maxResults = 30;

		public TaxonSearchParameters parameters;

	}

}
