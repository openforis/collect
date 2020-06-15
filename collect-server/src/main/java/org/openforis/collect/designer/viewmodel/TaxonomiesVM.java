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
import org.openforis.collect.designer.util.Resources;
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
import org.openforis.idm.metamodel.ReferenceDataSchema.ReferenceDataDefinition;
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
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Window;

/**
 * 
 * @author S. Ricci
 *
 */
public class TaxonomiesVM extends SurveyObjectBaseVM<CollectTaxonomy> {

	public static final String EDITING_ATTRIBUTE_PARAM = "editingAttribute";
	public static final String SELECTED_TAXONOMY_PARAM = "selectedTaxonomy";
	private static final String TAXONOMIES_UPDATED_COMMAND = "taxonomiesUpdated";
	public static final String CLOSE_TAXONOMY_IMPORT_POP_UP_COMMAND = "closeTaxonomyImportPopUp";
	private static final int TAXA_PAGE_SIZE = 30;

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

	public static void dispatchTaxonomiesUpdatedCommand() {
		BindUtils.postGlobalCommand(null, null, TAXONOMIES_UPDATED_COMMAND, null);
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
	protected void deleteItemFromSurvey(CollectTaxonomy item) {
		speciesManager.delete(item);
		dispatchTaxonomiesUpdatedCommand();
		SurveyEditVM.dispatchSurveySaveCommand();
	}

	@Override
	protected FormObject<CollectTaxonomy> createFormObject() {
		return new TaxonomyFormObject();
	}

	@Override
	protected void moveSelectedItemInSurvey(int indexTo) {
	}

	@Override
	protected CollectTaxonomy createItemInstance() {
		return null;
	}

	@Override
	protected void addNewItemToSurvey() {
	}

	@Override
	protected void performItemSelection(CollectTaxonomy item) {
		super.performItemSelection(item);
		List<String> fixedColumnNames = new ArrayList<String>();
		// required columns
		fixedColumnNames.addAll(Arrays.asList(SpeciesFileColumn.REQUIRED_COLUMN_NAMES));
		// vernacular language codes
		fixedColumnNames.addAll(speciesManager.loadTaxaVernacularLangCodes(item.getId()));
		referenceDataAttributesEditor = new ReferenceDataAttributesEditor(fixedColumnNames,
				getSurvey().getReferenceDataSchema().getTaxonomyDefinition(item.getName()));
		notifyChange("taxaAttributes");
		taxaPage = 0;
		reloadTaxa();
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
		return survey.getSchema().getTaxonAttributeDefinitions(item.getName());
	}

	@GlobalCommand
	public void taxonomiesUpdated() {
		notifyChange("items");
	}

	@Override
	public void commitChanges(@ContextParam(ContextType.BINDER) Binder binder) {
		super.commitChanges(binder);
		dispatchTaxonomiesUpdatedCommand();
	}

	@Command
	public void openImportPopUp() {
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("taxonomyId", editedItem.getId());
		taxonomyImportPopUp = openPopUp(Resources.Component.TAXONOMY_IMPORT_POPUP.getLocation(), true, args);
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

	public List<TaxonSummary> getTaxa() {
		return isEditingItem() ? taxonSummaries.getItems() : null;
	}

	public int getTaxaTotal() {
		return isEditingItem() ? taxonSummaries.getTotalCount() : 0;
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
		} else if (SpeciesFileColumn.FAMILY.getColumnName().equals(colName)) {
			return taxon.getFamilyName();
		} else if (SpeciesFileColumn.SCIENTIFIC_NAME.getColumnName().equals(colName)) {
			return taxon.getScientificName();
		} else if (Languages.exists(Standard.ISO_639_3, colName)) {
			return taxon.getJointVernacularNames(colName);
		} else {
			return taxon.getInfo(colName);
		}
	}

	@Command
	public void updateTaxaPaging(int newPageIndex) {
		this.taxaPage = newPageIndex;
		reloadTaxa();
	}

	private void reloadTaxa() {
		taxonSummaries = speciesManager.loadTaxonSummaries(getSurvey(), editedItem.getId(), taxaPage * TAXA_PAGE_SIZE,
				TAXA_PAGE_SIZE);
		notifyChange("taxa", "taxaTotal", "taxaPage");
	}

	private static class ReferenceDataAttributesEditor {

		private ReferenceDataDefinition referenceDataDefinition;
		private List<String> fixedColumnNames;
		private ListModelList<AttributeFormObject> attributes;

		public ReferenceDataAttributesEditor(List<String> fixedColumnNames,
				ReferenceDataDefinition referenceDataDefinition) {
			this.fixedColumnNames = fixedColumnNames;
			this.referenceDataDefinition = referenceDataDefinition;
		}

		public List<AttributeFormObject> getAttributes() {
			if (attributes == null) {
				attributes = new ListModelList<AttributeFormObject>();
				for (String colName : fixedColumnNames) {
					attributes.add(new AttributeFormObject(false, attributes.size(), colName));
				}
				List<String> infoAttributeNames = referenceDataDefinition.getAttributeNames();
				for (String infoAttributeName : infoAttributeNames) {
					attributes.add(new AttributeFormObject(true, attributes.size(), infoAttributeName));
				}
			}
			return attributes;
		}

		public void changeAttributeEditableStatus(AttributeFormObject attribute) {
			attribute.setEditingStatus(!attribute.getEditingStatus());
			refreshAttributeColumnTemplate(attribute);
		}

		private void refreshAttributeColumnTemplate(AttributeFormObject attribute) {
			// replace the element in the collection by itself to trigger a model update
			int index = attributes.indexOf(attribute);
			attributes.set(index, attribute);
		}

	}

}
