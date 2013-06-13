/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import static org.openforis.collect.designer.model.LabelKeys.DUPLICATED_QUALIFIER;
import static org.openforis.collect.designer.model.LabelKeys.EMPTY_OPTION;
import static org.openforis.collect.designer.model.LabelKeys.RANK_PREFIX;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.openforis.collect.designer.form.TaxonAttributeDefinitionFormObject;
import org.openforis.collect.designer.session.SessionStatus;
import org.openforis.collect.designer.util.MessageUtil;
import org.openforis.collect.manager.SpeciesManager;
import org.openforis.collect.model.CollectTaxonomy;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.TaxonAttributeDefinition;
import org.openforis.idm.model.species.Taxon.TaxonRank;
import org.zkoss.bind.Binder;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.DependsOn;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.select.annotation.WireVariable;

/**
 * @author S. Ricci
 *
 */
public class TaxonAttributeVM extends AttributeVM<TaxonAttributeDefinition> {

	private static final String QUALIFIERS_FIELD = "qualifiers";
	
	@WireVariable
	private SpeciesManager speciesManager;
	
	private List<String> qualifiers;
	private String selectedQualifier;

	@Init(superclass=false)
	public void init(@ExecutionArgParam("parentEntity") EntityDefinition parentEntity, 
			@ExecutionArgParam("item") TaxonAttributeDefinition attributeDefn, 
			@ExecutionArgParam("newItem") Boolean newItem) {
		super.init(parentEntity, attributeDefn, newItem);
	}
	
	@Command
	@NotifyChange("qualifiers")
	public void addQualifier() {
		if ( qualifiers == null ) {
			initQualifiersList();
		}
		if ( qualifiers.contains("") ) {
			MessageUtil.showWarning(DUPLICATED_QUALIFIER);
		} else {
			qualifiers.add("");
		}
	}
	
	@Command
	@NotifyChange({"selectedQualifier","qualifiers"})
	public void updateQualifier(@ContextParam(ContextType.BINDER) Binder binder, @BindingParam("text") String text) {
		int index = qualifiers.indexOf(selectedQualifier);
		if ( qualifiers.contains(text) && ! selectedQualifier.equals(text) ) {
			MessageUtil.showWarning(DUPLICATED_QUALIFIER);
		} else {
			qualifiers.set(index, text);
			dispatchApplyChangesCommand(binder);
		}
	}
	
	@Command
	@NotifyChange({"selectedQualifier","qualifiers"})
	public void deleteQualifier() {
		qualifiers.remove(selectedQualifier);
		selectedQualifier = null;
	}
	
	@Command
	@NotifyChange("selectedQualifier")
	public void selectQualifier(@BindingParam("qualifier") String qualifier) {
		selectedQualifier = qualifier;
	}
	
	protected void initQualifiersList() {
		if ( qualifiers == null ) {
			qualifiers = new ArrayList<String>();
			tempFormObject.setField(QUALIFIERS_FIELD, qualifiers);
			((TaxonAttributeDefinitionFormObject) formObject).setQualifiers(qualifiers);
		}
	}
	
	@Override
	public void setEditedItem(TaxonAttributeDefinition editedItem) {
		super.setEditedItem(editedItem);
		if ( editedItem != null ) {
			qualifiers = ((TaxonAttributeDefinitionFormObject) formObject).getQualifiers();
			tempFormObject.setField(QUALIFIERS_FIELD, qualifiers);
		}
	}
	
	public List<String> getRanks() {
		List<String> result = new ArrayList<String>();
		result.add(Labels.getLabel(EMPTY_OPTION));
		result.addAll(Arrays.asList(TaxonRank.names()));
		return result;
	}
	
	public String getRankLabel(String name) {
		String labelKey = RANK_PREFIX + name.toLowerCase();
		String label = Labels.getLabel(labelKey);
		return label;
	}
	
	@DependsOn("surveyId")
	public List<String> getTaxonomyNames() {
		Integer surveyId = getSurveyId();
		List<CollectTaxonomy> taxonomies;
		if  (surveyId == null ) {
			SessionStatus sessionStatus = getSessionStatus();
			Integer publishedSurveyId = sessionStatus.getPublishedSurveyId();
			if ( publishedSurveyId == null ) {
				return Collections.emptyList();
			} else {
				taxonomies = speciesManager.loadTaxonomiesBySurvey(publishedSurveyId);
			}
		} else {
			taxonomies = speciesManager.loadTaxonomiesBySurveyWork(surveyId);
		}
		List<String> result = new ArrayList<String>();
		for (CollectTaxonomy taxonomy : taxonomies) {
			result.add(taxonomy.getName());
		}
		return result;
	}
	
	public List<String> getQualifiers() {
		return qualifiers;
	}

	public String getSelectedQualifier() {
		return selectedQualifier;
	}

	public void setSelectedQualifier(String selectedQualifier) {
		this.selectedQualifier = selectedQualifier;
	}

}
