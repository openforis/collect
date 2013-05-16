/**
 * 
 */
package org.openforis.collect.csv;

import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Field;

/**
 * @author M. Togna
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
		return new String[] { "species_code", "species_scientific_name", "species_vernacular_name", "species_language" };
	}

	@Override
	protected Field<?>[] getFieldsToExtract(Attribute<?, ?> attr) {
		return new Field[] { attr.getField(0), attr.getField(1), attr.getField(2), attr.getField(3) };
	}

}
