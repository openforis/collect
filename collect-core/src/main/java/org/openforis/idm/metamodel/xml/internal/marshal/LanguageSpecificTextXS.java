package org.openforis.idm.metamodel.xml.internal.marshal;

import static org.openforis.idm.metamodel.xml.IdmlConstants.XML_LANG_ATTRIBUTE;
import static org.openforis.idm.metamodel.xml.IdmlConstants.XML_NAMESPACE_URI;

import java.io.IOException;
import java.util.List;

import org.openforis.idm.metamodel.LanguageSpecificText;

/**
 * 
 * @author G. Miceli
 *
 */
abstract class LanguageSpecificTextXS<P> extends XmlSerializerSupport<LanguageSpecificText, P>{

	public LanguageSpecificTextXS(String tag) {
		super(tag);
	}

	@Override
	protected void body(LanguageSpecificText txt) throws IOException {
		text(txt.getText());
	}
	
	@Override
	protected void marshal(List<? extends LanguageSpecificText> txts) throws IOException {
		marshal(txts, (String) null);
	}
	
	protected void marshal(List<? extends LanguageSpecificText> txts, String defaultLanguage) throws IOException {
		if ( txts == null || txts.isEmpty() ) {
			super.marshal(txts);
		} else {
			startList();
			for (LanguageSpecificText txt : txts) {
				//do not include default language in marshalled XML
				boolean includeLang = txt.getLanguage() != null && ! txt.getLanguage().equals(defaultLanguage);
				marshal(txt, includeLang);
			}
			endList();
		}
	}
	
	@Override
	protected void marshal(LanguageSpecificText txt) throws IOException {
		marshal(txt, true);
	}

	protected void marshal(LanguageSpecificText txt, boolean includeLanguage) throws IOException {
		if ( isIncludeEmpty() || txt != null ) {
			start(txt);
			attributes(txt, includeLanguage);
			body(txt);
			end(txt);
		}	
	}

	@Override
	protected void attributes(LanguageSpecificText txt) throws IOException {
		attributes(txt, true);
	}
	
	protected void attributes(LanguageSpecificText txt, boolean includeLanguage) throws IOException {
		String lang = txt.getLanguage();
		
		if ( includeLanguage && lang != null ) {
			attribute(XML_NAMESPACE_URI, XML_LANG_ATTRIBUTE, lang);
		}
	}
	
}
