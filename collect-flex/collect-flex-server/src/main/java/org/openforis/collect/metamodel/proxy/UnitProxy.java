/**
 * 
 */
package org.openforis.collect.metamodel.proxy;

import java.util.ArrayList;
import java.util.List;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.Proxy;
import org.openforis.idm.metamodel.Unit;

/**
 * @author M. Togna
 * 
 */
public class UnitProxy implements Proxy {

	private transient Unit unit;

	public UnitProxy(Unit unit) {
		super();
		this.unit = unit;
	}

	static List<UnitProxy> fromList(List<Unit> units) {
		List<UnitProxy> proxies = new ArrayList<UnitProxy>();
		if (units != null) {
			for (Unit unit : units) {
				proxies.add(new UnitProxy(unit));
			}
		}
		return proxies;
	}

	@ExternalizedProperty
	public String getName() {
		return unit.getName();
	}

	@ExternalizedProperty
	public String getDimension() {
		return unit.getDimension();
	}

	@ExternalizedProperty
	public Number getConversionFactor() {
		return unit.getConversionFactor();
	}

	@ExternalizedProperty
	public List<LanguageSpecificTextProxy> getLabels() {
		return LanguageSpecificTextProxy.fromList(unit.getLabels());
	}

	@ExternalizedProperty
	public List<LanguageSpecificTextProxy> getAbbreviations() {
		return LanguageSpecificTextProxy.fromList(unit.getAbbreviations());
	}

}
