package org.openforis.collect.io.metadata.species;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openforis.collect.io.exception.ParsingException;
import org.openforis.collect.io.metadata.ReferenceDataImportTask;
import org.openforis.collect.io.metadata.parsing.ParsingError;
import org.openforis.collect.manager.SpeciesManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.CollectTaxonomy;
import org.openforis.collect.model.TaxonTree;
import org.openforis.collect.model.TaxonTree.Node;
import org.openforis.collect.persistence.SurveyStoreException;
import org.openforis.idm.metamodel.ReferenceDataSchema.ReferenceDataDefinition;
import org.openforis.idm.metamodel.ReferenceDataSchema.ReferenceDataDefinition.Attribute;
import org.openforis.idm.metamodel.ReferenceDataSchema.TaxonomyDefinition;
import org.openforis.idm.model.species.Taxon;
import org.openforis.idm.model.species.TaxonVernacularName;
import org.openforis.idm.model.species.Taxonomy;
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
public class SpeciesBackupImportTask extends ReferenceDataImportTask<ParsingError> {

	private static final String TAXONOMY_NOT_FOUND_ERROR_MESSAGE_KEY = "speciesImport.error.taxonomyNotFound";
	
	private SpeciesManager speciesManager;
	private SurveyManager surveyManager;

	//input
	private CollectSurvey survey;
	private String taxonomyName;
	private boolean overwriteAll;
	private transient File file;
	
	//runtime instance variables
	private transient TaxonTree taxonTree;
	private transient SpeciesBackupCSVReader reader;
	
	@Override
	protected void validateInput() throws Throwable {
		super.validateInput();
		if ( survey == null ) {
			throw new RuntimeException("Survey not specified");
		}
		Taxonomy taxonomy = loadTaxonomy();
		
		if ( taxonomyName == null || ! overwriteAll && taxonomy == null ) {
			changeStatus(Status.FAILED);
			setErrorMessage(TAXONOMY_NOT_FOUND_ERROR_MESSAGE_KEY);
		}
	}
	
	@Override
	protected void createInternalVariables() throws Throwable {
		reader = new SpeciesBackupCSVReader(file);
		reader.init();
		
		List<String> infoColumnNames = reader.getInfoColumnNames();
		List<ReferenceDataDefinition.Attribute> attributes = ReferenceDataDefinition.Attribute.fromNames(infoColumnNames);
		TaxonomyDefinition taxonDefinition = new TaxonomyDefinition(taxonomyName);
		taxonDefinition.setAttributes(attributes);
		survey.getReferenceDataSchema().addTaxonomyDefinition(taxonDefinition);

		taxonTree = new TaxonTree(taxonDefinition);

		super.createInternalVariables();
	}
	
	@Override
	protected void execute() throws Throwable {
		if ( isRunning() ) {
			addProcessedRow(1);
			while ( isRunning() ) {
				SpeciesBackupLine line = reader.readNextLine();
				if ( line != null ) {
					processLine(line);
					addProcessedRow(line.getLineNumber());
				}
				if ( ! reader.isReady() ) {
					break;
				}
			}
			if ( isRunning() && ! hasErrors() ) {
				persistTaxa();
			} else {
				throw new RuntimeException("Cannot proceed, errors in file");
			}
		}
	}
	
	@Override
	protected long countTotalItems() {
		try {
			return reader.size();
		} catch (IOException e) {
			throw new RuntimeException("Error counting total items: " + e.getMessage(), e);
		}
	}

	protected void processLine(SpeciesBackupLine line) throws ParsingException {
		Taxon taxon = new Taxon();
		taxon.setSystemId(line.getId());
		taxon.setParentId(line.getParentId());
		taxon.setTaxonRank(line.getRank());
		taxon.setTaxonId(line.getNo());
		taxon.setCode(line.getCode());
		taxon.setScientificName(line.getScientificName());
		List<Attribute> taxonAttributes = survey.getReferenceDataSchema().getTaxonomyDefinition(taxonomyName).getAttributes();
		for (Attribute taxonAttribute : taxonAttributes) {
			taxon.addInfoAttribute(line.getInfoAttribute(taxonAttribute.getName()));
		}
		Taxon parent = findParentTaxon(line);
		taxonTree.addNode(parent, taxon);
		processVernacularNames(line, taxon);
	}

	protected void processVernacularNames(SpeciesBackupLine line, Taxon taxon) {
		Map<String, List<String>> langToVernacularName = line.getLanguageToVernacularNames();
		Set<String> vernacularLangCodes = langToVernacularName.keySet();
		for (String langCode : vernacularLangCodes) {
			List<String> vernacularNames = langToVernacularName.get(langCode);
			for (String vernacularName : vernacularNames) {
				TaxonVernacularName taxonVN = new TaxonVernacularName();
				taxonVN.setLanguageCode(langCode);
				taxonVN.setVernacularName(vernacularName);
				taxonTree.addVernacularName(taxon, taxonVN);
			}
		}
	}

	private CollectTaxonomy loadTaxonomy() {
		CollectTaxonomy taxonomy = taxonomyName == null ? null:
			speciesManager.loadTaxonomyByName(survey.getId(), taxonomyName);
		return taxonomy;
	}
	
	private void persistTaxa() throws SurveyStoreException {
		CollectTaxonomy taxonomy = loadTaxonomy();
		if ( taxonomy == null ) {
			if ( overwriteAll ) {
				//create and store taxonomy taxonomy
				taxonomy = new CollectTaxonomy();
				taxonomy.setName(taxonomyName);
				taxonomy.setSurveyId(survey.getId());
				speciesManager.save(taxonomy);
			} else {
				throw new RuntimeException("Cannot insert new taxonomy: overwriteAll parameter not specified");
			}
		}
		saveSurvey(); //taxonomy definition changed
		
		speciesManager.insertTaxons(taxonomy.getId(), taxonTree, overwriteAll);
	}

	@SuppressWarnings("deprecation")
	private void saveSurvey() throws SurveyStoreException {
		if ( survey.isTemporary() ) {
			surveyManager.save(survey);
		} else {
			surveyManager.updateModel(survey);
		}
	}

	private Taxon findParentTaxon(SpeciesBackupLine line) throws ParsingException {
		Node parentNode = line.getParentId() == null ? null : taxonTree.getNodeBySystemId(line.getParentId());
		Taxon parent = parentNode == null ? null : parentNode.getTaxon();
		return parent;
	}
	
	public void setSpeciesManager(SpeciesManager speciesManager) {
		this.speciesManager = speciesManager;
	}
	
	public void setSurveyManager(SurveyManager surveyManager) {
		this.surveyManager = surveyManager;
	}
	
	public File getFile() {
		return file;
	}
	
	public void setFile(File file) {
		this.file = file;
	}

	public void setOverwriteAll(boolean overwriteAll) {
		this.overwriteAll = overwriteAll;
	}

	public void setSurvey(CollectSurvey survey) {
		this.survey = survey;
	}

	public void setTaxonomyName(String taxonomyName) {
		this.taxonomyName = taxonomyName;
	}

}
