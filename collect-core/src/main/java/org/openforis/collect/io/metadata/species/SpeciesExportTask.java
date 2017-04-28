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
import org.openforis.idm.metamodel.ReferenceDataSchema.TaxonomyDefinition;
import org.openforis.idm.metamodel.ReferenceDataSchema.ReferenceDataDefinition.Attribute;
import org.springframework.beans.factory.annotation.Autowired;
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
public class SpeciesExportTask extends Task {
	
	private static final String LATIN_LANG_CODE = "lat";
	private static final String VERNACULAR_NAMES_SEPARATOR = " / ";

	@Autowired
	private SpeciesManager speciesManager;
	
	//parameters
	private OutputStream outputStream;
	private CollectSurvey survey;
	private int taxonomyId;
	
	//temporary
	private String taxonomyName;
	private List<String> infoAttributeNames;
	
	@Override
	protected void initializeInternalVariables() throws Throwable {
		super.initializeInternalVariables();
		this.infoAttributeNames = extractInfoAttributeNames();
		CollectTaxonomy taxonomy = speciesManager.loadTaxonomyById(taxonomyId);
		this.taxonomyName = taxonomy.getName();
	}
	
	@Override
	protected void execute() throws Throwable {
		CsvWriter writer = new CsvWriter(outputStream);
		
		TaxonSummaries summaries = speciesManager.loadFullTaxonSummariesOld(survey, taxonomyId);
		
		List<String> vernacularNamesLangCodes = getNotEmptyValues(summaries.getVernacularNamesLanguageCodes());
		vernacularNamesLangCodes.remove(LATIN_LANG_CODE); //consider Latin vernacular name as synonym

		//write headers
		ArrayList<String> colNames = new ArrayList<String>();
		colNames.add(SpeciesFileColumn.NO.getColumnName());
		colNames.add(SpeciesFileColumn.CODE.getColumnName());
		colNames.add(SpeciesFileColumn.FAMILY.getColumnName());
		colNames.add(SpeciesFileColumn.SCIENTIFIC_NAME.getColumnName());
		colNames.add(SpeciesFileColumn.SYNONYMS.getColumnName());
		
		colNames.addAll(vernacularNamesLangCodes);
		colNames.addAll(infoAttributeNames);
		
		writer.writeHeaders(colNames);
		
		for (TaxonSummary item : summaries.getItems()) {
			writeTaxonSummary(writer, vernacularNamesLangCodes, infoAttributeNames, item);
		}
		writer.flush();
	}
	
	private List<String> extractInfoAttributeNames() {
		List<String> colNames = new ArrayList<String>();
		TaxonomyDefinition taxonReferenceDataSchema = survey.getReferenceDataSchema().getTaxonomyDefinition(taxonomyName);
		List<Attribute> infoAttributes = taxonReferenceDataSchema.getAttributes();
		for (Attribute infoAttribute : infoAttributes) {
			colNames.add(infoAttribute.getName());
		}
		return colNames;
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

	public OutputStream getOutputStream() {
		return outputStream;
	}

	public void setOutputStream(OutputStream outputStream) {
		this.outputStream = outputStream;
	}
	
	public CollectSurvey getSurvey() {
		return survey;
	}
	
	public void setSurvey(CollectSurvey survey) {
		this.survey = survey;
	}

	public int getTaxonomyId() {
		return taxonomyId;
	}

	public void setTaxonomyId(int taxonomyId) {
		this.taxonomyId = taxonomyId;
	}

}
