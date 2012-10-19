package org.openforis.collect.metamodel.proxy;

import java.util.ArrayList;
import java.util.List;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.Proxy;
import org.openforis.idm.metamodel.Precision;
import org.openforis.idm.metamodel.Unit;

/**
 * 
 * @author S. Ricci
 *
 */
public class PrecisionProxy implements Proxy {

	private transient Precision precision;

	public PrecisionProxy(Precision precision) {
		super();
		this.precision = precision;
	}

	@ExternalizedProperty
	public UnitProxy getUnit() {
		if(precision.getUnit() != null) {
			return new UnitProxy(precision.getUnit());
		} else {
			return null;
		}
	}

	static List<PrecisionProxy> fromList(List<Precision> list) {
		List<PrecisionProxy> proxies = new ArrayList<PrecisionProxy>();
		if (list != null) {
			for (Precision v : list) {
				proxies.add(new PrecisionProxy(v));
			}
		}
		return proxies;
	}

	@ExternalizedProperty
	public String getUnitName() {
		Unit unit = precision.getUnit();
		return unit != null ? unit.getName(): null;
	}
	
	@ExternalizedProperty
	public Integer getDecimalDigits() {
		return precision.getDecimalDigits();
	}

	@ExternalizedProperty
	public boolean isDefaultPrecision() {
		return precision.isDefaultPrecision();
	}

}
