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
		if ( logo == null ) {
			return null;
		} else {
			return logo.getImage();
		}
	}
	
	@Transactional
	public void save(Logo logo) {
		int position = logo.getPosition();
		Logo oldLogo = logoDao.loadById(position);
		if ( oldLogo == null ) {
			logoDao.insert(logo);
		} else {
			logoDao.update(logo);
		}
	}
	
	@Transactional
	public void delete(Logo logo) {
		int position = logo.getPosition();
		logoDao.delete(position);
	}
}
