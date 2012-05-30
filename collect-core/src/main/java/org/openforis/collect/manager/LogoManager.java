/**
 * 
 */
package org.openforis.collect.manager;

import org.openforis.collect.model.Logo;
import org.openforis.collect.persistence.LogoDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author E. Wibowo
 * 
 */
public class LogoManager {
	
	@Autowired
	private LogoDao logoDao;
	
	@Transactional
	public byte[] loadLogo(int id) {
		Logo logo = logoDao.loadById(id);
		return logo.getImage();
	}
}
