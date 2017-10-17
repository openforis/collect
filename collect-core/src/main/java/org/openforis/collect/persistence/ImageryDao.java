package org.openforis.collect.persistence;

import static org.openforis.collect.persistence.jooq.tables.OfcImagery.OFC_IMAGERY;

import java.util.List;

import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.openforis.collect.model.Imagery;
import org.openforis.collect.persistence.jooq.tables.daos.OfcImageryDao;

public class ImageryDao extends OfcImageryDao implements PersistedObjectDao<Imagery, Integer> {

	public ImageryDao(Configuration configuration) {
		super(configuration);
	}
	
	@Override
	public Imagery loadById(Integer id) {
		return new Imagery(fetchOneById(id));
	}

	@Override
	public List<Imagery> loadAll() {
		return dsl()
			.selectFrom(OFC_IMAGERY)
			.orderBy(OFC_IMAGERY.TITLE)
			.fetchInto(Imagery.class);
	}

	public void save(Imagery imagery) {
		if (imagery.getId() == null) {
			insert(imagery);
		} else {
			update(imagery);
		}
	}
	
	@Override
	public void insert(Imagery imagery) {
		super.insert(imagery);
	}
	
	@Override
	public void update(Imagery item) {
		super.update(item);
	}

	@Override
	public void delete(Integer id) {
		super.deleteById(id);
	}

	private DSLContext dsl() {
		return DSL.using(configuration());
	}

}
