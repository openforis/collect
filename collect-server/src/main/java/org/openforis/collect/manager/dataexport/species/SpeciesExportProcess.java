package org.openforis.collect.manager.dataexport.species;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openforis.collect.manager.SpeciesManager;
import org.openforis.collect.manager.speciesimport.SpeciesFileColumn;
import org.openforis.collect.metamodel.TaxonSummaries;
import org.openforis.collect.metamodel.TaxonSummary;
import org.openforis.commons.io.csv.CsvWriter;

/**
 * 
 * @author S. Ricci
 *
 */
public class SpeciesExportProcess {
	
	private final Log log = LogFactory.getLog(SpeciesExportProcess.class);
	
	private static final String SYNOMYMS_COL_NAME = "synomyms";
	private static final String VERNACULAR_NAMES_SEPARATOR = " / ";

	private SpeciesManager speciesManager;
	
	public SpeciesExportProcess(SpeciesManager speciesManager) {
		super();
		this.speciesManager = speciesManager;
	}

	public void exportToCSV(OutputStream out, int taxonomyId) {
		CsvWriter writer = null;
		try {
			writer = new CsvWriter(out);
			TaxonSummaries summaries = speciesManager.loadTaxonSummaries(taxonomyId);
			ArrayList<String> colNames = new ArrayList<String>();
			colNames.add(SpeciesFileColumn.NO.getColumnName());
			colNames.add(SpeciesFileColumn.CODE.getColumnName());
			//colNames.add(SpeciesFileColumn.FAMILY.getColumnName());
			colNames.add(SpeciesFileColumn.SCIENTIFIC_NAME.getColumnName());
			colNames.add(SYNOMYMS_COL_NAME);
			List<String> vernacularNamesLangCodes = getNotEmptyValues(summaries.getVernacularNamesLanguageCodes());
			colNames.addAll(vernacularNamesLangCodes);
			writer.writeHeaders(colNames.toArray(new String[0]));
			List<TaxonSummary> items = summaries.getItems();
			for (TaxonSummary item : items) {
				writeTaxonSummary(writer, vernacularNamesLangCodes, item);
			}
		} catch (Exception e) {
			log.error(e);
		} finally {
			IOUtils.closeQuietly(writer);
		}
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
			List<String> vernacularNamesLangCodes, TaxonSummary item) {
		List<String> lineValues = new ArrayList<String>();
		lineValues.add(item.getTaxonId() == null ? null: item.getTaxonId().toString());
		lineValues.add(item.getCode());
		lineValues.add(item.getScientificName());
		lineValues.add(item.getJointSynonyms(VERNACULAR_NAMES_SEPARATOR));
		for (String langCode : vernacularNamesLangCodes) {
			String jointVernacularNames = item.getJointVernacularNames(langCode, VERNACULAR_NAMES_SEPARATOR);
			lineValues.add(jointVernacularNames);
		}
		writer.writeNext(lineValues.toArray(new String[0]));
	}

}
