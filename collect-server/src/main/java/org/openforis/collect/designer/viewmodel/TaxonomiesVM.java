/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openforis.collect.designer.form.AttributeFormObject;
import org.openforis.collect.designer.form.FormObject;
import org.openforis.collect.designer.form.TaxonomyFormObject;
import org.openforis.collect.designer.util.MessageUtil;
import org.openforis.collect.io.metadata.ReferenceDataExportOutputFormat;
import org.openforis.collect.io.metadata.species.SpeciesExportJob;
import org.openforis.collect.io.metadata.species.SpeciesFileColumn;
import org.openforis.collect.manager.SpeciesManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.metamodel.TaxonSummaries;
import org.openforis.collect.metamodel.TaxonSummary;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.CollectTaxonomy;
import org.openforis.collect.utils.Dates;
import org.openforis.collect.utils.MediaTypes;
import org.openforis.idm.metamodel.Languages;
import org.openforis.idm.metamodel.Languages.Standard;
import org.openforis.idm.metamodel.ReferenceDataSchema;
import org.openforis.idm.metamodel.ReferenceDataSchema.TaxonomyDefinition;
import org.openforis.idm.metamodel.TaxonAttributeDefinition;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.Binder;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Window;

/**
 * 
 * @author S. Ricci
 *
 */
public class TaxonomiesVM extends SurveyObjectBaseVM<CollectTaxonomy> {

	public static final String EDITING_ATTRIBUTE_PARAM = "editingAttribute";
	public static final String SELECTED_TAXONOMY_PARAM = "selectedTaxonomy";
	private static final String TAXONOMY_UPDATED_COMMAND = "taxonomyUpdated";
	private static final String CLOSE_TAXONOMY_IMPORT_POP_UP_COMMAND = "closeTaxonomyImportPopUp";
	private static final int TAXA_PAGE_SIZE = 30;
	private static final String RANK_COL_NAME = "rank";
	private static final String LAT_LANG_CODE = "lat";

	@WireVariable
	private SurveyManager surveyManager;
	@WireVariable
	private SpeciesManager speciesManager;

	protected boolean editingAttribute;

	private Window taxonomyImportPopUp;
	private Window dataImportErrorPopUp;
	private Window referencedNodesPopUp;
	private ReferenceDataAttributesEditor referenceDataAttributesEditor;
	private int taxaPage;
	private TaxonSummaries taxonSummaries;

	public static void dispatchTaxonomyUpdatedCommand(int taxonomyId) {
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("taxonomyId", taxonomyId);
		BindUtils.postGlobalCommand(null, null, TAXONOMY_UPDATED_COMMAND, args);
	}

	public static void dispatchCloseTaxonomyImportPopUpCommand() {
		BindUtils.postGlobalCommand(null, null, CLOSE_TAXONOMY_IMPORT_POP_UP_COMMAND, null);
	}

	public TaxonomiesVM() {
		super();
		formObject = createFormObject();
		fieldLabelKeyPrefixes.addAll(0, Arrays.asList("survey.taxonomy"));
	}

	@Init(superclass = false)
	public void init(@ExecutionArgParam(EDITING_ATTRIBUTE_PARAM) Boolean editingAttribute,
			@ExecutionArgParam(SELECTED_TAXONOMY_PARAM) CollectTaxonomy selectedTaxonomy) {
		super.init();
		if (selectedTaxonomy != null) {
			selectionChanged(selectedTaxonomy);
		}
		this.editingAttribute = editingAttribute != null && editingAttribute.booleanValue();
	}

	@Override
	protected List<CollectTaxonomy> getItemsInternal() {
		CollectSurvey survey = getSurvey();
		if (survey == null) {
			// TODO session expired
			return Collections.emptyList();
		}
		return speciesManager.loadTaxonomiesBySurvey(survey);
	}

	@Override
	protected FormObject<CollectTaxonomy> createFormObject() {
		return new TaxonomyFormObject();
	}

	@Override
	protected void moveSelectedItemInSurvey(int indexTo) {
		// DO NOT ALLOW MOVE
	}

	@Override
	protected CollectTaxonomy createItemInstance() {
		CollectTaxonomy taxonomy = new CollectTaxonomy();
		taxonomy.setSurvey(getSurvey());
		return taxonomy;
	}

	@Override
	protected void addNewItemToSurvey() {
		ReferenceDataSchema referenceDataSchema = getSurvey().getReferenceDataSchema();
		referenceDataSchema.addTaxonomyDefinition(new TaxonomyDefinition(editedItem.getName()));
		speciesManager.save(editedItem);
		dispatchTaxonomiesUpdatedCommand();
		SurveyEditVM.dispatchSurveySaveCommand();
	}

	@Override
	protected void performNewItemCreation(Binder binder) {
		super.performNewItemCreation(binder);
		resetTaxa();
	}

	@Override
	protected void deleteItemFromSurvey(CollectTaxonomy item) {
		speciesManager.delete(item);
		getSurvey().getReferenceDataSchema().removeTaxonomyDefinition(item.getName());
		dispatchTaxonomiesUpdatedCommand();
	}

	@Override
	protected void performDeleteItem(CollectTaxonomy item) {
		super.performDeleteItem(item);
		SurveyEditVM.dispatchSurveySaveCommand();
	}

	@Override
	protected void performItemSelection(CollectTaxonomy item) {
		super.performItemSelection(item);
		List<String> fixedColumnNames = new ArrayList<String>();
		// required columns
		fixedColumnNames.addAll(Arrays.asList(SpeciesFileColumn.CODE.getColumnName(), RANK_COL_NAME,
				SpeciesFileColumn.SCIENTIFIC_NAME.getColumnName(), SpeciesFileColumn.SYNONYMS.getColumnName()));
		// vernacular language codes (excluding lat = synonyms)
		List<String> vernacularLangCodes = speciesManager.loadTaxaVernacularLangCodes(item.getId());
		vernacularLangCodes.remove(LAT_LANG_CODE);
		fixedColumnNames.addAll(vernacularLangCodes);
		referenceDataAttributesEditor = new ReferenceDataAttributesEditor(fixedColumnNames,
				getSurvey().getReferenceDataSchema().getTaxonomyDefinition(item.getName()));
		notifyChange("taxaAttributes");
		taxaPage = 0;
		loadTaxa();
	}

	@Command
	public void deleteTaxonomy(@BindingParam("item") final CollectTaxonomy item) {
		List<TaxonAttributeDefinition> references = getReferences(item);
		if (!references.isEmpty()) {
			String title = Labels.getLabel("global.message.title.warning");
			String message = Labels.getLabel("survey.taxonomy.alert.cannot_delete_used_taxonomy");
			referencedNodesPopUp = SurveyErrorsPopUpVM.openPopUp(title, message, references,
					new MessageUtil.ConfirmHandler() {
						public void onOk() {
							closePopUp(referencedNodesPopUp);
							referencedNodesPopUp = null;
						}
					}, true);
		} else {
			super.deleteItem(item);
		}
	}

	protected List<TaxonAttributeDefinition> getReferences(CollectTaxonomy item) {
		return getSurvey().getSchema().getTaxonAttributeDefinitions(item.getName());
	}

	@GlobalCommand
	public void taxonomiesUpdated() {
		notifyChange("items");
	}

	@GlobalCommand
	public void taxonomyUpdated(@BindingParam("taxonomyId") int taxonomyId) {
		if (isEditingItem() && editedItem.getId() == taxonomyId) {
			// refresh taxonomy information
			performItemSelection(editedItem);
		}
		dispatchTaxonomiesUpdatedCommand();
	}

	@Override
	public void commitChanges(@ContextParam(ContextType.BINDER) Binder binder) {
		String oldName = editedItem.getName();
		List<TaxonAttributeDefinition> references = oldName == null ? null : getReferences(editedItem);
		super.commitChanges(binder);
		if (oldName != null) {
			// update survey reference data
			getSurvey().getReferenceDataSchema().updateTaxonomyDefinitionName(oldName, editedItem.getName());
			// update selected taxonomy in survey schema node definitions
			for (TaxonAttributeDefinition taxonAttributeDefinition : references) {
				taxonAttributeDefinition.setTaxonomy(editedItem.getName());
			}
		}
		speciesManager.save(editedItem);
		dispatchTaxonomiesUpdatedCommand();
		SurveyEditVM.dispatchSurveySaveCommand();
	}

	@Command
	public void openImportPopUp() {
		taxonomyImportPopUp = TaxonomyImportPopUpVM.openPopUp(editedItem.getId());
	}

	@Command
	public void exportToCsv() throws IOException {
		export(ReferenceDataExportOutputFormat.CSV);
	}

	@Command
	public void exportToExcel() throws IOException {
		export(ReferenceDataExportOutputFormat.EXCEL);
	}

	private void export(ReferenceDataExportOutputFormat outputFormat) throws IOException {
		SpeciesExportJob job = jobManager.createJob(SpeciesExportJob.class);
		job.setSurvey(getSurvey());
		job.setTaxonomyId(editedItem.getId());
		job.setOutputFormat(outputFormat);
		jobManager.start(job, false);
		String contentType = outputFormat == ReferenceDataExportOutputFormat.CSV ? MediaTypes.CSV_CONTENT_TYPE
				: MediaTypes.XLSX_CONTENT_TYPE;
		String fileName = String.format("%s_species_list_%s_%s", getSurvey().getName(), editedItem.getName(),
				Dates.formatCompactNow());
		Filedownload.save(new FileInputStream(job.getOutputFile()), contentType, fileName);
	}

	@GlobalCommand
	public void closeReferenceDataImportErrorPopUp() {
		closePopUp(dataImportErrorPopUp);
		dataImportErrorPopUp = null;
	}

	@GlobalCommand
	public void closeTaxonomyImportPopUp() {
		closePopUp(taxonomyImportPopUp);
		taxonomyImportPopUp = null;
	}

	@GlobalCommand
	public void closeTaxonomyManagerPopUp() {
		resetEditedItem();
		taxonomiesUpdated();
	}

	@GlobalCommand
	public void taxonomyAssigned(@BindingParam("list") CollectTaxonomy taxonomy,
			@BindingParam("oldTaxonomy") CollectTaxonomy oldTaxonomy) {
		BindUtils.postNotifyChange(null, null, taxonomy, ".");
		BindUtils.postNotifyChange(null, null, oldTaxonomy, ".");
	}

	@Command
	public void close(@ContextParam(ContextType.TRIGGER_EVENT) Event event) {
		event.stopPropagation();
		checkCanLeaveForm(new CanLeaveFormConfirmHandler() {
			public void onOk(boolean confirmed) {
				Map<String, Object> params = new HashMap<String, Object>();
				params.put("editingAttribute", editingAttribute);
				params.put("selectedTaxonomy", selectedItem);
				BindUtils.postGlobalCommand(null, null, "closeTaxonomyManagerPopUp", params);
			}
		});
	}

	public boolean hasWarnings(CollectTaxonomy taxonomy) {
		return getWarnings(taxonomy) != null;
	}

	public String getWarnings(CollectTaxonomy taxonomy) {
		String messageKey;
		List<TaxonAttributeDefinition> referencingDefs = getReferences(taxonomy);
		if (referencingDefs.isEmpty()) {
			messageKey = "survey.validation.error.unused_taxonomy";
		} else if (!speciesManager.hasTaxons(taxonomy)) {
			messageKey = "survey.validation.error.empty_taxonomy";
		} else {
			messageKey = null;
		}
		return messageKey == null ? null : Labels.getLabel(messageKey);
	}

	public List<AttributeFormObject> getTaxaAttributes() {
		return isEditingItem() ? referenceDataAttributesEditor.getAttributes() : null;
	}

	public String getTaxaAttributeLabel(AttributeFormObject attribute) {
		String name = attribute.getName();
		if (Languages.exists(Standard.ISO_639_3, name)) {
			return String.format("%s (%s)", name, Labels.getLabel(name));
		} else {
			return name;
		}
	}

	@Command
	@NotifyChange("taxaAttributes")
	public void changeAttributeEditableStatus(@BindingParam("attribute") AttributeFormObject attribute) {
		referenceDataAttributesEditor.changeAttributeEditableStatus(attribute);
	}

	@Command
	@NotifyChange("taxaAttributes")
	public void confirmAttributeUpdate(@BindingParam("attribute") AttributeFormObject attribute) {
		if (referenceDataAttributesEditor.confirmAttributeUpdate(attribute)) {
			dispatchSurveyChangedCommand();
		}
	}

	public List<TaxonSummary> getTaxa() {
		return isEditingItem() && taxonSummaries != null ? taxonSummaries.getItems() : null;
	}

	public int getTaxaTotal() {
		return isEditingItem() && taxonSummaries != null ? taxonSummaries.getTotalCount() : 0;
	}

	public int getTaxaPage() {
		return taxaPage;
	}

	public int getTaxaPageSize() {
		return TAXA_PAGE_SIZE;
	}

	public String getTaxonAttribute(TaxonSummary taxon, String colName) {
		if (SpeciesFileColumn.CODE.getColumnName().equals(colName)) {
			return taxon.getCode();
		} else if (RANK_COL_NAME.equals(colName)) {
			return Labels.getLabel("survey.taxonomy.rank." + taxon.getRank().getName());
		} else if (SpeciesFileColumn.SCIENTIFIC_NAME.getColumnName().equals(colName)) {
			return taxon.getScientificName();
		} else if (SpeciesFileColumn.SYNONYMS.getColumnName().equals(colName)) {
			return taxon.getJointSynonyms();
		} else if (Languages.exists(Standard.ISO_639_3, colName)) {
			return taxon.getJointVernacularNames(colName);
		} else {
			return taxon.getInfo(colName);
		}
	}

	@Command
	public void updateTaxaPaging(int newPageIndex) {
		this.taxaPage = newPageIndex;
		loadTaxa();
	}

	private void resetTaxa() {
		this.taxaPage = 0;
		this.taxonSummaries = null;
		notifyChange("taxa", "taxaTotal", "taxaPage");
	}

	private void loadTaxa() {
		taxonSummaries = speciesManager.loadTaxonSummaries(getSurvey(), editedItem.getId(), taxaPage * TAXA_PAGE_SIZE,
				TAXA_PAGE_SIZE);
		notifyChange("taxa", "taxaTotal", "taxaPage");
	}

}
