package org.openforis.collect.designer.util;

import java.util.Map;

import org.zkoss.zk.ui.Executions;
import org.zkoss.zul.Window;

/**
 * 
 * @author S. Ricci
 *
 */
public abstract class PopUpUtil {

	public static Window openPopUp(String url, boolean modal) {
		return openPopUp(url, modal, null);
	}
	
	public static Window openPopUp(String url, boolean modal, Map<?, ?> args) {
		Window result = (Window) Executions.createComponents(
				url, null, args);
		if ( modal ) {
			result.doModal();
		}
		return result;
	}
	
	public static void closePopUp(Window popUp) {
		if ( popUp != null ) {
			popUp.detach();
		}
	}
	
}
