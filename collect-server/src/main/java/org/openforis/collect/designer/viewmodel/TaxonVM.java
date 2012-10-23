/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import static org.openforis.collect.designer.model.Labels.EMPTY_OPTION;

import java.util.ArrayList;
import java.util.List;

import org.openforis.collect.designer.form.TaxonAttributeDefinitionFormObject;
import org.openforis.idm.metamodel.TaxonAttributeDefinition;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;

/**
 * @author S. Ricci
 *
 */
public class TaxonVM extends AttributeVM<TaxonAttributeDefinition> {

	private static final String RANK_LABEL_KEY_PREFIX = "survey.schema.attribute.taxon.rank.";
	private static final String QUALIFIERS_FIELD = "qualifiers";
	
	public enum Rank {
		FAMILY, GENUS, SPECIES
	}
	
	private List<String> qualifiers;
	private String selectedQualifier;

	@AfterCompose
	@Override
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view) {
		//necessary because of not inheritance of AfterCompose behaviour
		super.afterCompose(view);
	}
	
	@Init(superclass=false)
	@Override
	public void init(@ExecutionArgParam("item") TaxonAttributeDefinition attributeDefn, 
			@ExecutionArgParam("newItem") Boolean newItem) {
		super.init(attributeDefn, newItem);
	}
	
	protected void initAttributeDefaultsList() {
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
	
	public List<String> getQualifiers() {
		return qualifiers;
	}

	public List<String> getRanks() {
		List<String> result = new ArrayList<String>();
		String emptyOption = Labels.getLabel(EMPTY_OPTION);
		result.add(emptyOption);
		Rank[] ranks = Rank.values();
		for (Rank rank : ranks) {
			String labelKey = RANK_LABEL_KEY_PREFIX + rank.name().toLowerCase();
			String label = Labels.getLabel(labelKey);
			result.add(label);
		}
		return result;
	}
	
	public String getSelectedQualifier() {
		return selectedQualifier;
	}

	public void setSelectedQualifier(String selectedQualifier) {
		this.selectedQualifier = selectedQualifier;
	}

}
