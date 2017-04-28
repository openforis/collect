package org.openforis.collect.manager.dataexport.species;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openforis.collect.io.metadata.species.SpeciesFileColumn;
import org.openforis.collect.manager.SpeciesManager;
import org.openforis.collect.metamodel.TaxonSummaries;
import org.openforis.collect.metamodel.TaxonSummary;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.CollectTaxonomy;
import org.openforis.commons.io.csv.CsvWriter;

/**
 * 
 * @author S. Ricci
 *
 */
public class SpeciesExportProcess {
	
	private static final String LATIN_LANG_CODE = "lat";
	private static final String VERNACULAR_NAMES_SEPARATOR = " / ";

	private final Log log = LogFactory.getLog(SpeciesExportProcess.class);
	
	private SpeciesManager speciesManager;
	private String taxonomyName;
	
	public SpeciesExportProcess(SpeciesManager speciesManager) {
		super();
		this.speciesManager = speciesManager;
	}

	public void exportToCSV(OutputStream out, CollectSurvey survey, int taxonomyId) {
		CsvWriter writer = null;
		try {
			writer = new CsvWriter(out);
			CollectTaxonomy taxonomy = speciesManager.loadTaxonomyById(taxonomyId);
			taxonomyName = taxonomy.getName();
			TaxonSummaries summaries = speciesManager.loadFullTaxonSummariesOld(survey, taxonomyId);
			ArrayList<String> colNames = new ArrayList<String>();
			colNames.add(SpeciesFileColumn.NO.getColumnName());
			colNames.add(SpeciesFileColumn.CODE.getColumnName());
			colNames.add(SpeciesFileColumn.FAMILY.getColumnName());
			colNames.add(SpeciesFileColumn.SCIENTIFIC_NAME.getColumnName());
			colNames.add(SpeciesFileColumn.SYNONYMS.getColumnName());
			List<String> vernacularNamesLangCodes = extractVernacularNameLanguageCodes(summaries);
			colNames.addAll(vernacularNamesLangCodes);
			List<String> infoAttributeNames = survey.getReferenceDataSchema().getTaxonomyDefinition(taxonomyName).getAttributeNames();
			colNames.addAll(infoAttributeNames);
			
			writer.writeHeaders(colNames);
			List<TaxonSummary> items = summaries.getItems();
			for (TaxonSummary item : items) {
				writeTaxonSummary(writer, vernacularNamesLangCodes, infoAttributeNames, item);
			}
		} catch (Exception e) {
			log.error(e);
		} finally {
			IOUtils.closeQuietly(writer);
		}
	}

	private List<String> extractVernacularNameLanguageCodes(TaxonSummaries summaries) {
		List<String> vernacularNamesLangCodes = getNotEmptyValues(summaries.getVernacularNamesLanguageCodes());
		vernacularNamesLangCodes.remove(LATIN_LANG_CODE); //consider Latin vernacular name as synonym
		return vernacularNamesLangCodes;
	}

	protected List<String> getNotEmptyValues(List<String> values) {
		List<String> result = new ArrayList<String>();
		if( values != null ) {
			for (String langCode : values) {
				if ( StringUtils.isNotBlank(langCode) ) {
					result.add(langCode);
				}
			}
		}
		return result;
	}

	protected void writeTaxonSummary(CsvWriter writer,
			List<String> vernacularNamesLangCodes, List<String> infoAttributes, TaxonSummary item) {
		List<String> lineValues = new ArrayList<String>();
		lineValues.add(item.getTaxonId() == null ? null: item.getTaxonId().toString());
		lineValues.add(item.getCode());
		lineValues.add(item.getFamilyName());
		lineValues.add(item.getScientificName());
		lineValues.add(item.getJointSynonyms(VERNACULAR_NAMES_SEPARATOR));
		for (String langCode : vernacularNamesLangCodes) {
			String jointVernacularNames = item.getJointVernacularNames(langCode, VERNACULAR_NAMES_SEPARATOR);
			lineValues.add(jointVernacularNames);
		}
		for (String infoAttribute : infoAttributes) {
			lineValues.add(item.getInfo(infoAttribute));
		}
		writer.writeNext(lineValues);
	}

}
