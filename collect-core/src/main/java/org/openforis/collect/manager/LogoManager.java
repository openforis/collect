/**
 * 
 */
package org.openforis.collect.manager;

import org.openforis.collect.model.Logo;
import org.openforis.collect.model.LogoPosition;
import org.openforis.collect.persistence.LogoDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author E. Wibowo
 * 
 */
@Transactional(readOnly=true, propagation=Propagation.SUPPORTS)
public class LogoManager {
	
	@Autowired
	private LogoDao logoDao;
	
	public Logo loadLogo(LogoPosition position) {
		Logo logo = logoDao.loadByPosition(position);
		return logo;
	}
	
	@Transactional
	public Logo save(Logo logo) {
		//Logo oldLogo = logoDao.loadById(logo.getId());
		Logo oldLogo = logoDao.loadByPosition(logo.getPosition());
		if ( oldLogo == null ) {
			logoDao.insert(logo);
		} else {
			logoDao.update(logo);
		}
		return logo;
	}
	
	@Transactional
	public void delete(Logo logo) {
		logoDao.deleteByPosition(logo.getPosition());
	}
}
