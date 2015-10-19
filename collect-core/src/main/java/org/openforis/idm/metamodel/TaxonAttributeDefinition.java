/**
 * 
 */
package org.openforis.idm.metamodel;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.TaxonAttribute;
import org.openforis.idm.model.TaxonOccurrence;
import org.openforis.idm.model.Value;
import org.openforis.idm.model.species.Taxon.TaxonRank;

/**
 * @author G. Miceli
 * @author M. Togna
 * @author S. Ricci
 * @author W. Eko
 */
public class TaxonAttributeDefinition extends AttributeDefinition {

	private static final long serialVersionUID = 1L;
	
	public static final String CODE_FIELD_NAME = "code";
	public static final String SCIENTIFIC_NAME_FIELD_NAME = "scientific_name";
	public static final String VERNACULAR_NAME_FIELD_NAME = "vernacular_name";
	public static final String LANGUAGE_CODE_FIELD_NAME = "language_code";
	public static final String LANGUAGE_VARIETY_FIELD_NAME = "language_variety";
	
	public static final String QUALIFIER_SEPARATOR = ",";

	private String qualifiers;
	
	private final FieldDefinitionMap fieldDefinitionByName = new FieldDefinitionMap(
		new FieldDefinition<String>(CODE_FIELD_NAME, "c", "code", String.class, this), 
		new FieldDefinition<String>(SCIENTIFIC_NAME_FIELD_NAME, "s", "name", String.class, this), 
		new FieldDefinition<String>(VERNACULAR_NAME_FIELD_NAME, "v", "vn", String.class, this),
		new FieldDefinition<String>(LANGUAGE_CODE_FIELD_NAME, "l", "lang", String.class, this),
		new FieldDefinition<String>(LANGUAGE_VARIETY_FIELD_NAME, "lv", "lang_var", String.class, this)
	);
	
	private String taxonomy;
	private TaxonRank highestTaxonRank;
	
	TaxonAttributeDefinition(Survey survey, int id) {
		super(survey, id);
	}
	
	TaxonAttributeDefinition(TaxonAttributeDefinition obj, int id) {
		super(obj, id);
	}
	
	@Override
	public Node<?> createNode() {
		return new TaxonAttribute(this);
	}

	@Override
	@SuppressWarnings("unchecked")
	public TaxonOccurrence createValue(String string) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public TaxonOccurrence createValue(Object val) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	protected FieldDefinitionMap getFieldDefinitionMap() {
		return fieldDefinitionByName;
	}
	
	@Override
	public Class<? extends Value> getValueType() {
		return TaxonOccurrence.class;
	}

	public String getTaxonomy() {
		return taxonomy;
	}

	public void setTaxonomy(String taxonomy) {
		this.taxonomy = taxonomy;
	}

	@Deprecated
	public String getHighestRank() {
		return highestTaxonRank == null ? null: highestTaxonRank.getName();
	}
	
	@Deprecated
	public void setHighestRank(String highestRank) {
		TaxonRank rank = TaxonRank.fromName(highestRank);
		setHighestTaxonRank(rank);
	}

	public TaxonRank getHighestTaxonRank() {
		return highestTaxonRank;
	}
	
	public void setHighestTaxonRank(TaxonRank highestTaxonRank) {
		this.highestTaxonRank = highestTaxonRank;
	}
	
	public List<String> getQualifiers() {
		if ( qualifiers != null ) {
			String[] exprs = qualifiers.split(QUALIFIER_SEPARATOR);
			return Collections.unmodifiableList(Arrays.asList(exprs));
		} else {
			return Collections.emptyList();
		}
	}

	public void setQualifiers(String qualifiers) {
		this.qualifiers = qualifiers != null && qualifiers.length() > 0 ? qualifiers: null;
	}

	public void setQualifiers(List<String> qualifiers) {
		setQualifiers(StringUtils.join(qualifiers, QUALIFIER_SEPARATOR));
	}
	
	@Override
	public boolean hasMainField() {
		return false;
	}
	
	@Override
	public String getMainFieldName() {
		throw new IllegalArgumentException("Main field not defined");
	}

	@Override
	public boolean deepEquals(Object obj) {
		if (this == obj)
			return true;
		if (!super.deepEquals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		TaxonAttributeDefinition other = (TaxonAttributeDefinition) obj;
		if (highestTaxonRank != other.highestTaxonRank)
			return false;
		if (qualifiers == null) {
			if (other.qualifiers != null)
				return false;
		} else if (!qualifiers.equals(other.qualifiers))
			return false;
		if (taxonomy == null) {
			if (other.taxonomy != null)
				return false;
		} else if (!taxonomy.equals(other.taxonomy))
			return false;
		return true;
	}
	
}
