/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import static org.openforis.collect.designer.model.LabelKeys.DUPLICATED_QUALIFIER;
import static org.openforis.collect.designer.model.LabelKeys.EMPTY_OPTION;
import static org.openforis.collect.designer.model.LabelKeys.RANK_PREFIX;

import java.util.ArrayList;
import java.util.List;

import org.openforis.collect.designer.form.TaxonAttributeDefinitionFormObject;
import org.openforis.collect.designer.util.MessageUtil;
import org.openforis.idm.metamodel.TaxonAttributeDefinition;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.util.resource.Labels;

/**
 * @author S. Ricci
 *
 */
public class TaxonVM extends AttributeVM<TaxonAttributeDefinition> {

	private static final String QUALIFIERS_FIELD = "qualifiers";
	
	public enum Rank {
		FAMILY, GENUS, SPECIES
	}
	
	private List<String> qualifiers;
	private String selectedQualifier;

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
	public void updateQualifier(@BindingParam("text") String text) {
		int index = qualifiers.indexOf(selectedQualifier);
		if ( qualifiers.contains(text) && ! selectedQualifier.equals(text) ) {
			MessageUtil.showWarning(DUPLICATED_QUALIFIER);
		} else {
			qualifiers.set(index, text);
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
		String emptyOption = Labels.getLabel(EMPTY_OPTION);
		result.add(emptyOption);
		Rank[] ranks = Rank.values();
		for (Rank rank : ranks) {
			String labelKey = RANK_PREFIX + rank.name().toLowerCase();
			String label = Labels.getLabel(labelKey);
			result.add(label);
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
