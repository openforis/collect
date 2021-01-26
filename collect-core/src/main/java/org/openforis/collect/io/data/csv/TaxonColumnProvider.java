/**
 * 
 */
package org.openforis.collect.io.data.csv;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openforis.collect.metamodel.CollectAnnotations;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.commons.collection.CollectionUtils;
import org.openforis.commons.collection.Predicate;
import org.openforis.idm.metamodel.TaxonAttributeDefinition;

/**
 * @author M. Togna
 * @author S. Ricci
 * 
 */
public class TaxonColumnProvider extends CompositeAttributeColumnProvider<TaxonAttributeDefinition> {

	private static final String[] FAMILY_FIELDS = new String[] {
			TaxonAttributeDefinition.FAMILY_CODE_FIELD_NAME,
			TaxonAttributeDefinition.FAMILY_SCIENTIFIC_NAME_FIELD_NAME
	};

	public TaxonColumnProvider(CSVDataExportParameters config, TaxonAttributeDefinition defn) {
		super(config, defn);
	}

	@Override
	protected String[] getFieldNames() {
		CollectSurvey survey = attributeDefinition.getSurvey();
		CollectAnnotations annotations = survey.getAnnotations();
		final List<String> visibleFieldNames = new ArrayList<String>(Arrays.asList(
				survey.getUIOptions().getVisibleFields(attributeDefinition)));
		if (! visibleFieldNames.contains(TaxonAttributeDefinition.CODE_FIELD_NAME)) {
			//always include CODE field
			visibleFieldNames.add(0, TaxonAttributeDefinition.CODE_FIELD_NAME);
		}
		if (annotations.isShowFamily(attributeDefinition)) {
			// add family fields first (only if not in visible fields already)
			List<String> familyFieldsToInclude = new ArrayList<String>(Arrays.asList(FAMILY_FIELDS));
			CollectionUtils.filter(familyFieldsToInclude, new Predicate<String>() {
				public boolean evaluate(String familyField) {
					return !visibleFieldNames.contains(familyField);
				}
			});
			visibleFieldNames.addAll(familyFieldsToInclude);
		} else {
			visibleFieldNames.removeAll(Arrays.asList(FAMILY_FIELDS));
		}
		return visibleFieldNames.toArray(new String[visibleFieldNames.size()]);
	}
}
