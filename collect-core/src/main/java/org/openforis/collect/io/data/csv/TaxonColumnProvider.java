/**
 * 
 */
package org.openforis.collect.io.data.csv;

import org.apache.commons.lang3.ArrayUtils;
import org.openforis.collect.metamodel.CollectAnnotations;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.TaxonAttributeDefinition;

/**
 * @author M. Togna
 * @author S. Ricci
 * 
 */
public class TaxonColumnProvider extends CompositeAttributeColumnProvider<TaxonAttributeDefinition> {

	private static final String[] DEFAULT_FIELDS = new String[] {
			TaxonAttributeDefinition.CODE_FIELD_NAME,
			TaxonAttributeDefinition.SCIENTIFIC_NAME_FIELD_NAME,
			TaxonAttributeDefinition.VERNACULAR_NAME_FIELD_NAME,
			TaxonAttributeDefinition.LANGUAGE_CODE_FIELD_NAME,
			TaxonAttributeDefinition.LANGUAGE_VARIETY_FIELD_NAME
	};
	
	private static final String[] SHOW_FAMILY_FIELDS = ArrayUtils.addAll(DEFAULT_FIELDS, new String[] {
			TaxonAttributeDefinition.FAMILY_CODE_FIELD_NAME,
			TaxonAttributeDefinition.FAMILY_SCIENTIFIC_NAME_FIELD_NAME
	});

	public TaxonColumnProvider(CSVExportConfiguration config, TaxonAttributeDefinition defn) {
		super(config, defn);
	}

	@Override
	protected String[] getFieldNames() {
		CollectSurvey survey = attributeDefinition.getSurvey();
		CollectAnnotations annotations = survey.getAnnotations();
		boolean showFamily = annotations.isShowFamily(attributeDefinition);
		return showFamily ? SHOW_FAMILY_FIELDS : DEFAULT_FIELDS;
	}

}
