package org.openforis.collect.manager;

import org.openforis.collect.model.Imagery;
import org.openforis.collect.persistence.ImageryDao;
import org.springframework.stereotype.Component;

@Component
public class ImageryManager extends AbstractPersistedObjectManager<Imagery, Integer, ImageryDao> {

	public Imagery findByTitle(String title) {
		return dao.findByTitle(title);
	}
	
	
}
