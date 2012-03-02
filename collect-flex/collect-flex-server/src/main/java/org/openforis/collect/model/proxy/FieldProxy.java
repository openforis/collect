/**
 * 
 */
package org.openforis.collect.model.proxy;

import java.util.ArrayList;
import java.util.List;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.Proxy;
import org.openforis.collect.model.FieldSymbol;
import org.openforis.idm.model.Code;
import org.openforis.idm.model.Coordinate;
import org.openforis.idm.model.Date;
import org.openforis.idm.model.AttributeField;
import org.openforis.idm.model.TaxonOccurrence;
import org.openforis.idm.model.Time;

/**
 * @author S. Ricci
 *
 */
public class FieldProxy implements Proxy {

	private transient AttributeField<?> attributeField;

	public FieldProxy(AttributeField<?> field) {
		super();
		this.attributeField = field;
	}


	public static List<FieldProxy> fromList(List<AttributeField<?>> list) {
		List<FieldProxy> proxies = new ArrayList<FieldProxy>();
		if (list != null) {
			for (AttributeField<?> item : list) {
				FieldProxy proxy = new FieldProxy(item);
				proxies.add(proxy);
			}
		}
		return proxies;
	}
	@ExternalizedProperty
	public Object getValue(){
		Object val = attributeField.getValue();
		if(val != null) {
			if(val instanceof Code) {
				return new CodeProxy((Code) val);
			} else if(val instanceof Coordinate) {
				return new CoordinateProxy((Coordinate) val);
			} else if(val instanceof Date) {
				return new DateProxy((Date) val);
			} else if(val instanceof TaxonOccurrence) {
				return new TaxonOccurrenceProxy((TaxonOccurrence) val);
			} else if(val instanceof Time) {
				return new TimeProxy((Time) val);
			} else{
				return val;
			}
		} else {
			return null;
		}
	}
	
	@ExternalizedProperty
	public String getRemarks() {
		return attributeField.getRemarks();
	}

	@ExternalizedProperty
	public FieldSymbol getSymbol() {
		if(attributeField.getSymbol() != null) {
			return FieldSymbol.valueOf(attributeField.getSymbol());
		} else {
			return null;
		}
	}
	
	
	
}
