package org.openforis.collect.persistence;

import static org.openforis.collect.persistence.jooq.tables.OfcImagery.OFC_IMAGERY;

import java.util.List;

import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.openforis.collect.model.Imagery;
import org.openforis.collect.persistence.jooq.tables.daos.OfcImageryDao;
import org.openforis.collect.persistence.jooq.tables.pojos.OfcImagery;
import org.openforis.collect.persistence.jooq.tables.records.OfcImageryRecord;

public class ImageryDao extends OfcImageryDao implements PersistedObjectDao<Imagery, Integer> {

	public ImageryDao(Configuration configuration) {
		super(configuration);
	}
	
	@Override
	public Imagery loadById(Integer id) {
		return new Imagery(fetchOneById(id));
	}

	public Imagery findByTitle(String title) {
		List<OfcImagery> result = super.fetchByTitle(title);
		if (result.isEmpty()) {
			return null;
		} else {
			return new Imagery(result.get(0));
		}
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
		OfcImageryRecord result = dsl().insertInto(OFC_IMAGERY).columns(
				OFC_IMAGERY.ATTRIBUTION, 
				OFC_IMAGERY.EXTENT, 
				OFC_IMAGERY.SOURCE_CONFIG, 
				OFC_IMAGERY.TITLE)
			.values(imagery.getAttribution(),
					imagery.getExtent(),
					imagery.getSourceConfig(),
					imagery.getTitle())
			.returning(OFC_IMAGERY.ID)
			.fetchOne();
		imagery.setId(result.getId());
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
