/**
 * 
 */
package org.openforis.idm.metamodel;

/**
 * @author S. Ricci
 *
 */
public class LanguageSpecificTextMap extends LanguageSpecificTextAbstractMap<LanguageSpecificText> {

	private static final long serialVersionUID = 1L;

	public LanguageSpecificTextMap() {
		super();
	}
	
	public LanguageSpecificTextMap(LanguageSpecificTextMap obj) {
		super(obj);
	}
	
	public String getText(String languageCode, String defaultLanguageCode) {
		if (languageCode == null) {
			languageCode = defaultLanguageCode;
		}
		String text = getText(languageCode);
		if (text == null && defaultLanguageCode != null 
				&& !defaultLanguageCode.equals(languageCode)) {
			return getText(defaultLanguageCode);
		} else {
			return text;
		}
	}
	
	public static void assignDefaultToVoidLanguageText(LanguageSpecificTextMap labels, String defaultLanguageCode) {
		if ( labels != null ) {
			String voidLanguageText = labels.getText(null);
			if ( voidLanguageText != null ) {
				labels.setText(defaultLanguageCode, voidLanguageText);
				labels.remove(null);
			}
		}
	}
}
