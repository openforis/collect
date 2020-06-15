package org.openforis.collect.io.metadata.species;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.io.metadata.ReferenceDataExportTask;
import org.openforis.collect.manager.SpeciesManager;
import org.openforis.collect.metamodel.TaxonSummaries;
import org.openforis.collect.metamodel.TaxonSummary;
import org.openforis.collect.model.CollectTaxonomy;
import org.openforis.commons.collection.CollectionUtils;
import org.openforis.commons.collection.Predicate;
import org.openforis.idm.metamodel.ReferenceDataSchema.ReferenceDataDefinition.Attribute;
import org.openforis.idm.metamodel.ReferenceDataSchema.TaxonomyDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * 
 * @author S. Ricci
 *
 */
@Component
@Scope(SCOPE_PROTOTYPE)
public class SpeciesExportTask extends ReferenceDataExportTask {

	private static final String LATIN_LANG_CODE = "lat";
	private static final String VERNACULAR_NAMES_SEPARATOR = " / ";

	@Autowired
	private SpeciesManager speciesManager;

	// parameters
	private int taxonomyId;

	// temporary
	private List<String> infoAttributeNames;
	private CollectTaxonomy taxonomy;
	private TaxonSummaries summaries;
	private List<String> vernacularNamesLangCodes;

	@Override
	protected void initializeInternalVariables() throws Throwable {
		super.initializeInternalVariables();
		this.taxonomy = speciesManager.loadTaxonomyById(survey, taxonomyId);
		this.summaries = speciesManager.loadFullTaxonSummariesOld(taxonomy);
		this.vernacularNamesLangCodes = getNotEmptyValues(summaries.getVernacularNamesLanguageCodes());
		this.vernacularNamesLangCodes.remove(LATIN_LANG_CODE); // consider Latin vernacular name as synonym
		this.infoAttributeNames = extractInfoAttributeNames();
	}

	@Override
	protected long countTotalItems() {
		return this.summaries.getTotalCount();
	}
	
	private List<String> extractInfoAttributeNames() {
		List<String> colNames = new ArrayList<String>();
		TaxonomyDefinition taxonReferenceDataSchema = survey.getReferenceDataSchema()
				.getTaxonomyDefinition(this.taxonomy.getName());
		List<Attribute> infoAttributes = taxonReferenceDataSchema.getAttributes();
		for (Attribute infoAttribute : infoAttributes) {
			colNames.add(infoAttribute.getName());
		}
		return colNames;
	}

	private List<String> getNotEmptyValues(List<String> values) {
		List<String> result = new ArrayList<String>(values);
		CollectionUtils.filter(result, new Predicate<String>() {
			public boolean evaluate(String langCode) {
				return StringUtils.isNotBlank(langCode);
			}
		});
		return result;
	}

	@Override
	protected List<String> getHeaders() {
		ArrayList<String> colNames = new ArrayList<String>();
		colNames.addAll(Arrays.asList(SpeciesFileColumn.NO.getColumnName(), SpeciesFileColumn.CODE.getColumnName(),
				SpeciesFileColumn.FAMILY.getColumnName(), SpeciesFileColumn.SCIENTIFIC_NAME.getColumnName(),
				SpeciesFileColumn.SYNONYMS.getColumnName()));

		colNames.addAll(vernacularNamesLangCodes);
		colNames.addAll(infoAttributeNames);

		return colNames;
	}

	@Override
	protected void writeItems() {
		for (TaxonSummary item : summaries.getItems()) {
			List<String> lineValues = new ArrayList<String>();
			lineValues.add(item.getTaxonId() == null ? null : item.getTaxonId().toString());
			lineValues.add(item.getCode());
			lineValues.add(item.getFamilyName());
			lineValues.add(item.getScientificName());
			lineValues.add(item.getJointSynonyms(VERNACULAR_NAMES_SEPARATOR));
			for (String langCode : vernacularNamesLangCodes) {
				String jointVernacularNames = item.getJointVernacularNames(langCode, VERNACULAR_NAMES_SEPARATOR);
				lineValues.add(jointVernacularNames);
			}
			for (String infoAttribute : infoAttributeNames) {
				lineValues.add(item.getInfo(infoAttribute));
			}
			writer.writeNext(lineValues);
			incrementProcessedItems();
		}
	}

	public void setTaxonomyId(int taxonomyId) {
		this.taxonomyId = taxonomyId;
	}

}
