/**
 * 
 */
package org.openforis.collect.csv;

import org.openforis.idm.metamodel.TaxonAttributeDefinition;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Field;
import org.openforis.idm.model.TaxonAttribute;

/**
 * @author M. Togna
 * @author S. Ricci
 * 
 * @deprecated replaced with idm-transform api
 */
@Deprecated
public class TaxonColumnProvider extends CompositeAttributeColumnProvider {

	public TaxonColumnProvider(String attributeName) {
		super(attributeName);
	}

	@Override
	protected String[] getFieldsHeadings() {
		return new String[] {
				getFieldHeading(TaxonAttributeDefinition.CODE_FIELD_NAME),
				getFieldHeading(TaxonAttributeDefinition.SCIENTIFIC_NAME_FIELD_NAME),
				getFieldHeading(TaxonAttributeDefinition.VERNACULAR_NAME_FIELD_NAME),
				getFieldHeading(TaxonAttributeDefinition.LANGUAGE_CODE_FIELD_NAME),
				getFieldHeading(TaxonAttributeDefinition.LANGUAGE_VARIETY_FIELD_NAME)
		};
	}

	@Override
	protected Field<?>[] getFieldsToExtract(Attribute<?, ?> attr) {
		TaxonAttribute taxonAttr = (TaxonAttribute) attr;
		return new Field[] { 
				taxonAttr.getCodeField(), 
				taxonAttr.getScientificNameField(), 
				taxonAttr.getVernacularNameField(),
				taxonAttr.getLanguageCodeField(),
				taxonAttr.getLanguageVarietyField()
		};
	}

}
