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
