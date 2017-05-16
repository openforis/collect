package org.openforis.collect.io.metadata.species;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.manager.SpeciesManager;
import org.openforis.collect.metamodel.TaxonSummaries;
import org.openforis.collect.metamodel.TaxonSummary;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.CollectTaxonomy;
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
	private CollectSurvey survey;
	private int taxonomyId;
	
	//temporary variables
	private String taxonomyName;
	private List<String> infoAttributeNames;
	private List<String> vernacularNamesLangCodes;
	
	@Override
	protected void initializeInternalVariables() throws Throwable {
		super.initializeInternalVariables();
		CollectTaxonomy taxonomy = speciesManager.loadTaxonomyById(survey, taxonomyId);
		this.taxonomyName = taxonomy.getName();
		this.infoAttributeNames = survey.getReferenceDataSchema().getTaxonomyDefinition(taxonomyName).getAttributeNames();
	}
	
	@Override
	protected void execute() throws Throwable {
		CsvWriter writer = new CsvWriter(outputStream);
		
		CollectTaxonomy taxonomy = speciesManager.loadTaxonomyById(survey, taxonomyId);
		TaxonSummaries summaries = speciesManager.loadFullTaxonSummaries(taxonomy);
		
		List<String> vernacularNamesLangCodes = getNotEmptyValues(summaries.getVernacularNamesLanguageCodes());
		vernacularNamesLangCodes.remove(LATIN_LANG_CODE); //consider Latin vernacular name as synonym
		this.vernacularNamesLangCodes = vernacularNamesLangCodes;
		
		//write headers
		writeHeaders(writer);
		
		for (TaxonSummary item : summaries.getItems()) {
			writeTaxonSummary(writer, item);
		}
		writer.flush();
	}

	private void writeHeaders(CsvWriter writer) {
		ArrayList<String> colNames = new ArrayList<String>();
		colNames.add(SpeciesBackupFileColumn.ID.getColumnName());
		colNames.add(SpeciesBackupFileColumn.PARENT_ID.getColumnName());
		colNames.add(SpeciesBackupFileColumn.RANK.getColumnName());
		colNames.add(SpeciesBackupFileColumn.NO.getColumnName());
		colNames.add(SpeciesBackupFileColumn.CODE.getColumnName());
		colNames.add(SpeciesBackupFileColumn.SCIENTIFIC_NAME.getColumnName());
		colNames.add(SpeciesBackupFileColumn.SYNONYMS.getColumnName());
		colNames.addAll(vernacularNamesLangCodes);
		colNames.addAll(infoAttributeNames);
		
		writer.writeHeaders(colNames);
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

	protected void writeTaxonSummary(CsvWriter writer, TaxonSummary item) {
		List<String> lineValues = new ArrayList<String>();
		lineValues.add(Integer.toString(item.getTaxonSystemId()));
		lineValues.add(item.getParentSystemId() == null ? null : item.getParentSystemId().toString());
		lineValues.add(item.getRank().getName());
		lineValues.add(item.getTaxonId() == null ? null: item.getTaxonId().toString());
		lineValues.add(item.getCode());
		lineValues.add(item.getScientificName());
		lineValues.add(item.getJointSynonyms(VERNACULAR_NAMES_SEPARATOR));
		//write vernacular names
		for (String langCode : vernacularNamesLangCodes) {
			String jointVernacularNames = item.getJointVernacularNames(langCode, VERNACULAR_NAMES_SEPARATOR);
			lineValues.add(jointVernacularNames);
		}
		//write info attributes
		for (String infoAttribute : infoAttributeNames) {
			lineValues.add(item.getInfo(infoAttribute));
		}
		writer.writeNext(lineValues);
	}

	public void setSpeciesManager(SpeciesManager speciesManager) {
		this.speciesManager = speciesManager;
	}
	
	public void setOutputStream(OutputStream outputStream) {
		this.outputStream = outputStream;
	}
	
	public void setSurvey(CollectSurvey survey) {
		this.survey = survey;
	}

	public void setTaxonomyId(int taxonomyId) {
		this.taxonomyId = taxonomyId;
	}

}
