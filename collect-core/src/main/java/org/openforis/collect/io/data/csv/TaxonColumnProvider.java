/**
 * 
 */
package org.openforis.collect.io.data.csv;

import org.openforis.idm.metamodel.TaxonAttributeDefinition;

/**
 * @author M. Togna
 * @author S. Ricci
 * 
 */
public class TaxonColumnProvider extends CompositeAttributeColumnProvider<TaxonAttributeDefinition> {

	public TaxonColumnProvider(TaxonAttributeDefinition defn) {
		super(defn);
	}

	@Override
	protected String[] getFieldNames() {
		return new String[] {
				TaxonAttributeDefinition.CODE_FIELD_NAME,
				TaxonAttributeDefinition.SCIENTIFIC_NAME_FIELD_NAME,
				TaxonAttributeDefinition.VERNACULAR_NAME_FIELD_NAME,
				TaxonAttributeDefinition.LANGUAGE_CODE_FIELD_NAME,
				TaxonAttributeDefinition.LANGUAGE_VARIETY_FIELD_NAME
		};
	}

}
