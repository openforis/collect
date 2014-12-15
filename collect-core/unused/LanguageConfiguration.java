/**
 * 
 */
package org.openforis.collect.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.openforis.idm.metamodel.Configuration;
import org.openforis.idm.metamodel.Languages;
import org.openforis.idm.util.CollectionUtil;

/**
 * @author S. Ricci
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "languageCodes" })
@XmlRootElement(name = "language")
public class LanguageConfiguration implements Configuration {

	@XmlElement(name = "languageCode")
	private List<String> languageCodes;

	public List<String> getLanguageCodes() {
		return CollectionUtil.unmodifiableList(languageCodes);
	}
	
	public void addLanguageCode(String code) {
		if ( Languages.contains(code) ) { 
			if ( languageCodes == null ) {
				languageCodes = new ArrayList<String>();
			}
			if ( ! languageCodes.contains(code) ) {
				languageCodes.add(code);
			}
		} else {
			throw new IllegalArgumentException("Unsupported language code: " + code);
		}
	}
	
	public void removeLanguageCode(String code) {
		languageCodes.remove(code);
	}

	public void addLanguageCodes(List<String> codes) {
		for (String code : codes) {
			addLanguageCode(code);
		}
	}
	
}
