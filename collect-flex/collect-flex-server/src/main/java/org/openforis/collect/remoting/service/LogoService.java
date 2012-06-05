/**
 * 
 */
package org.openforis.collect.remoting.service;

import org.openforis.collect.manager.LogoManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author E. Wibowo
 */
public class LogoService {

	@Autowired
	private LogoManager logoManager;

	public byte[] loadLogo(int id) {
		return logoManager.loadLogo(id);
	}

}
