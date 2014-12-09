package org.openforis.collect.io.metadata.species;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.manager.SpeciesManager;
import org.openforis.collect.metamodel.TaxonSummaries;
import org.openforis.collect.metamodel.TaxonSummary;
import org.openforis.commons.io.csv.CsvWriter;
import org.openforis.concurrency.Task;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * 
 * @author S. Ricci
 *
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SpeciesBackupExportTask extends Task {
	
	private static final String LATIN_LANG_CODE = "lat";
	private static final String VERNACULAR_NAMES_SEPARATOR = " / ";

	private SpeciesManager speciesManager;
	
	//parameters
	private OutputStream outputStream;
	private int taxonomyId;
	
	@Override
	protected void execute() throws Throwable {
		CsvWriter writer = new CsvWriter(outputStream);
		
		TaxonSummaries summaries = speciesManager.loadFullTaxonSummaries(taxonomyId);
		
		List<String> vernacularNamesLangCodes = getNotEmptyValues(summaries.getVernacularNamesLanguageCodes());
		vernacularNamesLangCodes.remove(LATIN_LANG_CODE); //consider Latin vernacular name as synonym

		//write headers
		writeHeaders(writer, vernacularNamesLangCodes);
		
		for (TaxonSummary item : summaries.getItems()) {
			writeTaxonSummary(writer, vernacularNamesLangCodes, item);
		}
		writer.flush();
	}

	private void writeHeaders(CsvWriter writer,	List<String> vernacularNamesLangCodes) {
		ArrayList<String> colNames = new ArrayList<String>();
		colNames.add(SpeciesBackupFileColumn.ID.getColumnName());
		colNames.add(SpeciesBackupFileColumn.PARENT_ID.getColumnName());
		colNames.add(SpeciesBackupFileColumn.RANK.getColumnName());
		colNames.add(SpeciesBackupFileColumn.NO.getColumnName());
		colNames.add(SpeciesBackupFileColumn.CODE.getColumnName());
		colNames.add(SpeciesBackupFileColumn.SCIENTIFIC_NAME.getColumnName());
		colNames.add(SpeciesBackupFileColumn.SYNONYMS.getColumnName());
		colNames.addAll(vernacularNamesLangCodes);
		
		writer.writeHeaders(colNames.toArray(new String[0]));
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
		lineValues.add(Integer.toString(item.getTaxonSystemId()));
		lineValues.add(item.getParentSystemId() == null ? null : item.getParentSystemId().toString());
		lineValues.add(item.getRank().getName());
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

	public SpeciesManager getSpeciesManager() {
		return speciesManager;
	}
	
	public void setSpeciesManager(SpeciesManager speciesManager) {
		this.speciesManager = speciesManager;
	}
	
	public OutputStream getOutputStream() {
		return outputStream;
	}

	public void setOutputStream(OutputStream outputStream) {
		this.outputStream = outputStream;
	}

	public int getTaxonomyId() {
		return taxonomyId;
	}

	public void setTaxonomyId(int taxonomyId) {
		this.taxonomyId = taxonomyId;
	}

}
