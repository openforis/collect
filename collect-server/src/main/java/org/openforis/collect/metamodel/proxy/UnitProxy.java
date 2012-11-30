/**
 * 
 */
package org.openforis.collect.metamodel.proxy;

import java.util.ArrayList;
import java.util.List;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.idm.metamodel.Unit;

/**
 * @author M. Togna
 * @author S. Ricci
 * 
 */
public class UnitProxy extends IdentifiableSurveyObjectProxy {

	private transient Unit unit;

	public UnitProxy(Unit unit) {
		super(unit);
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
	public Double getConversionFactor() {
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
