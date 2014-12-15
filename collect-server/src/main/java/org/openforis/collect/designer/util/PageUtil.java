package org.openforis.collect.designer.util;

import org.zkoss.zk.ui.util.Clients;

/**
 * 
 * @author S. Ricci
 *
 */
public abstract class PageUtil {
	
	public static void confirmClose(String message) {
		Clients.confirmClose(message);
	}
	
	public static void clearConfirmClose() {
		Clients.confirmClose(null);
	}

}
