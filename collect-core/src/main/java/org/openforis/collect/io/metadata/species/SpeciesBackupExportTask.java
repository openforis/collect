package org.openforis.collect.io.metadata.species;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.io.metadata.ReferenceDataExportTask;
import org.openforis.collect.manager.SpeciesManager;
import org.openforis.collect.metamodel.TaxonSummaries;
import org.openforis.collect.metamodel.TaxonSummary;
import org.openforis.collect.model.CollectTaxonomy;
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
public class SpeciesBackupExportTask extends ReferenceDataExportTask {

	private static final String LATIN_LANG_CODE = "lat";
	private static final String VERNACULAR_NAMES_SEPARATOR = " / ";

	@Autowired
	private SpeciesManager speciesManager;

	// parameters
	private int taxonomyId;

	// temporary variables
	private CollectTaxonomy taxonomy;
	private List<String> infoAttributeNames;
	private List<String> vernacularNamesLangCodes;
	private TaxonSummaries summaries;

	@Override
	protected void initializeInternalVariables() throws Throwable {
		super.initializeInternalVariables();
		this.taxonomy = speciesManager.loadTaxonomyById(survey, taxonomyId);
		this.vernacularNamesLangCodes = speciesManager.loadTaxaVernacularLangCodes(taxonomyId);
		this.vernacularNamesLangCodes.remove(LATIN_LANG_CODE); // consider Latin vernacular name as synonym
		this.infoAttributeNames = survey.getReferenceDataSchema().getTaxonomyDefinition(taxonomy.getName())
				.getAttributeNames();
		this.summaries = speciesManager.loadFullTaxonSummaries(taxonomy);
	}

	@Override
	protected long countTotalItems() {
		return this.summaries.getTotalCount();
	}
	
	@Override
	protected List<String> getHeaders() {
		List<String> colNames = new ArrayList<String>(Arrays.asList(
			SpeciesBackupFileColumn.ID.getColumnName(),
			SpeciesBackupFileColumn.PARENT_ID.getColumnName(), 
			SpeciesBackupFileColumn.RANK.getColumnName(),
			SpeciesBackupFileColumn.NO.getColumnName(), 
			SpeciesBackupFileColumn.CODE.getColumnName(),
			SpeciesBackupFileColumn.SCIENTIFIC_NAME.getColumnName(),
			SpeciesBackupFileColumn.SYNONYMS.getColumnName()
		));
		colNames.addAll(vernacularNamesLangCodes);
		colNames.addAll(infoAttributeNames);
		return colNames;
	}

	@Override
	protected void writeItems() {
		for (TaxonSummary item : summaries.getItems()) {
			List<String> lineValues = new ArrayList<String>();
			lineValues.add(Long.toString(item.getTaxonSystemId()));
			lineValues.add(item.getParentSystemId() == null ? null : item.getParentSystemId().toString());
			lineValues.add(item.getRank().getName());
			lineValues.add(item.getTaxonId() == null ? null : item.getTaxonId().toString());
			lineValues.add(item.getCode());
			lineValues.add(item.getScientificName());
			lineValues.add(item.getJointSynonyms(VERNACULAR_NAMES_SEPARATOR));
			// write vernacular names
			for (String langCode : vernacularNamesLangCodes) {
				String jointVernacularNames = item.getJointVernacularNames(langCode, VERNACULAR_NAMES_SEPARATOR);
				lineValues.add(jointVernacularNames);
			}
			// write info attributes
			for (String infoAttribute : infoAttributeNames) {
				lineValues.add(item.getInfo(infoAttribute));
			}
			writer.writeNext(lineValues);
		}
	}

	protected List<String> getNotEmptyValues(List<String> values) {
		List<String> result = new ArrayList<String>();
		if (values != null) {
			for (String langCode : values) {
				if (StringUtils.isNotBlank(langCode)) {
					result.add(langCode);
				}
			}
		}
		return result;
	}

	public void setSpeciesManager(SpeciesManager speciesManager) {
		this.speciesManager = speciesManager;
	}

	public void setTaxonomyId(int taxonomyId) {
		this.taxonomyId = taxonomyId;
	}

}
