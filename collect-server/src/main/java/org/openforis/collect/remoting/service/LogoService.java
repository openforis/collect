/**
 * 
 */
package org.openforis.collect.remoting.service;

import java.util.Locale;

import org.openforis.collect.manager.LogoManager;
import org.openforis.collect.model.Logo;
import org.openforis.collect.model.LogoPosition;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author E. Wibowo
 * @author S. Ricci
 */
public class LogoService {

	@Autowired
	private LogoManager logoManager;

	public Logo loadLogo(String position) {
		return logoManager.loadLogo(LogoPosition.valueOf(position.toUpperCase(Locale.ENGLISH)));
	}
	
	public void deleteLogo(String position) {
		Logo logo = logoManager.loadLogo(LogoPosition.valueOf(position.toUpperCase(Locale.ENGLISH)));
		if ( logo != null ) {
			logoManager.delete(logo);
		}
	}

	public Logo saveLogo(Logo logo) {
		logoManager.save(logo);
		return logo;
	}
	
}
