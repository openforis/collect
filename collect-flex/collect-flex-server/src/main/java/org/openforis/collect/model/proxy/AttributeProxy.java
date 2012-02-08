/**
 * 
 */
package org.openforis.collect.model.proxy;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Code;
import org.openforis.idm.model.Coordinate;
import org.openforis.idm.model.Date;
import org.openforis.idm.model.Taxon;
import org.openforis.idm.model.Time;

/**
 * @author M. Togna
 * 
 */
public class AttributeProxy extends NodeProxy {

	private transient Attribute<? extends AttributeDefinition, ?> attribute;

	@SuppressWarnings("unchecked")
	public AttributeProxy(@SuppressWarnings("rawtypes") Attribute attribute) {
		super(attribute);
		this.attribute = attribute;
	}

	@ExternalizedProperty
	public Object getValue(){
		Object val = attribute.getValue();
		if(val != null) {
			if(val instanceof Code) {
				return new CodeProxy((Code) val);
			} else if(val instanceof Coordinate) {
				return new CoordinateProxy((Coordinate) val);
			} else if(val instanceof Date) {
				return new DateProxy((Date) val);
			} else if(val instanceof Taxon) {
				return new TaxonProxy((Taxon) val);
			} else if(val instanceof Time) {
				return new TimeProxy((Time) val);
			} else{
				return val;
			}
		} else {
			return null;
		}
	}
	
}
