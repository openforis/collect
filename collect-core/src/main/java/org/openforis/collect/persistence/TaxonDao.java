package org.openforis.collect.persistence;

import static org.openforis.collect.persistence.jooq.Sequences.OFC_TAXON_ID_SEQ;
import static org.openforis.collect.persistence.jooq.tables.OfcTaxon.OFC_TAXON;

import java.sql.Connection;
import java.util.Arrays;
import java.util.List;

import org.jooq.BatchBindStep;
import org.jooq.Field;
import org.jooq.Insert;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.Select;
import org.jooq.SelectQuery;
import org.jooq.SortField;
import org.jooq.StoreQuery;
import org.jooq.TableField;
import org.jooq.impl.Factory;
import org.openforis.collect.persistence.jooq.MappingJooqDaoSupport;
import org.openforis.collect.persistence.jooq.MappingJooqFactory;
import org.openforis.collect.persistence.jooq.tables.records.OfcTaxonRecord;
import org.openforis.idm.model.species.Taxon;
import org.openforis.idm.model.species.Taxon.TaxonRank;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author G. Miceli
 * @author S. Ricci
 */
@Transactional
public class TaxonDao extends MappingJooqDaoSupport<Taxon, TaxonDao.JooqFactory> {
	
	protected static Field<?>[] TAXON_FIELDS = {
			OFC_TAXON.ID,
			OFC_TAXON.PARENT_ID,
			OFC_TAXON.CODE,
			OFC_TAXON.SCIENTIFIC_NAME,
			OFC_TAXON.STEP,
			OFC_TAXON.TAXON_ID,
			OFC_TAXON.TAXON_RANK,
			OFC_TAXON.TAXONOMY_ID};
	
	public TaxonDao() {
		super(TaxonDao.JooqFactory.class);
	}

	@Override
	public Taxon loadById(int id) {
		return super.loadById(id);
	}

	@Override
	public void insert(Taxon entity) {
		super.insert(entity);
	}
	
	@Override
	public void update(Taxon entity) {
		super.update(entity);
	}

	@Override
	public void delete(int id) {
		super.delete(id);
	}

	public List<Taxon> findByCode(int taxonomyId, String searchString, int maxResults) {
		return findStartingWith(OFC_TAXON.CODE, taxonomyId, searchString, maxResults);
	}

	public List<Taxon> findByScientificName(int taxonomyId, String searchString, int maxResults) {
		return findStartingWith(OFC_TAXON.SCIENTIFIC_NAME, taxonomyId, searchString, maxResults);
	}

	protected List<Taxon> findStartingWith(TableField<?,String> field, int taxonomyId, String searchString, int maxResults) {
		JooqFactory jf = getMappingJooqFactory();
		searchString = searchString.toUpperCase() + "%";
		Select<?> query = 
			jf.select()
			.from(OFC_TAXON)
			.where(OFC_TAXON.TAXONOMY_ID.equal(taxonomyId)
				.and(JooqFactory.upper(field).like(searchString)))
			.limit(maxResults);
		Result<?> result = query.fetch();
		List<Taxon> entities = jf.fromResult(result);
		return entities;
	}
	
	public int countTaxons(int taxonomyId) {
		JooqFactory f = getMappingJooqFactory();
		SelectQuery q = f.selectCountQuery();
		q.addConditions(OFC_TAXON.TAXONOMY_ID.equal(taxonomyId));
		Record r = q.fetchOne();
		return (Integer) r.getValue(0);
	}
	
	public List<Taxon> loadTaxons(int taxonomyId, int offset, int maxRecords) {
		return loadTaxons(taxonomyId, offset, maxRecords, OFC_TAXON.SCIENTIFIC_NAME.asc());
	}
	
	public List<Taxon> loadTaxonsForTreeBuilding(int taxonomyId) {
		return loadTaxons(taxonomyId, 0, Integer.MAX_VALUE, OFC_TAXON.PARENT_ID.asc().nullsFirst());
	}
	
	public List<Taxon> loadTaxons(int taxonomyId, int offset, int maxRecords, SortField<?> sortField) {
		JooqFactory jf = getMappingJooqFactory();
		SelectQuery q = jf.selectQuery();	
		q.addFrom(OFC_TAXON);
		q.addConditions(OFC_TAXON.TAXONOMY_ID.equal(taxonomyId));
		//always order by SCIENTIFIC_NAME to avoid pagination issues
		q.addOrderBy(sortField);
		
		//add limit
		q.addLimit(offset, maxRecords);
		
		//fetch results
		Result<Record> result = q.fetch();
		
		return jf.fromResult(result);
	}
	
	public void deleteByTaxonomy(int taxonomyId) {
		JooqFactory jf = getMappingJooqFactory();
		jf.delete(OFC_TAXON)
			.where(OFC_TAXON.TAXONOMY_ID.equal(taxonomyId))
			.execute();
	}
	
	/**
	 * Inserts the items in batch.
	 * 
	 * @param taxa
	 */
	public void insert(List<Taxon> items) {
		if ( items != null && ! items.isEmpty() ) {
			JooqFactory jf = getMappingJooqFactory();
			int id = jf.nextId(OFC_TAXON.ID, OFC_TAXON_ID_SEQ);
			int maxId = id;
			Insert<OfcTaxonRecord> query = jf.createInsertStatement();
			BatchBindStep batch = jf.batch(query);
			for (Taxon item : items) {
				if ( item.getSystemId() == null ) {
					item.setSystemId(id++);
				}
				Object[] values = jf.extractValues(item);
				batch.bind(values);
				maxId = Math.max(maxId, item.getSystemId());
			}
			batch.execute();
			jf.restartSequence(OFC_TAXON_ID_SEQ, maxId + 1);
		}
	}

	public int duplicateTaxons(int oldTaxonomyId, Integer newTaxonomyId) {
		JooqFactory jf = getMappingJooqFactory();
		int nextId = jf.nextId(OFC_TAXON.ID, OFC_TAXON_ID_SEQ);
		int minId = loadMinId(jf, oldTaxonomyId);
		int idShift = nextId - minId;
		Field<?>[] selectFields = {
				OFC_TAXON.ID.add(idShift),
				OFC_TAXON.PARENT_ID.add(idShift),
				OFC_TAXON.CODE,
				OFC_TAXON.SCIENTIFIC_NAME,
				OFC_TAXON.STEP,
				OFC_TAXON.TAXON_ID,
				OFC_TAXON.TAXON_RANK,
				Factory.val(newTaxonomyId)
			};
		Select<?> select = jf.select(selectFields)
				.from(OFC_TAXON)
				.where(OFC_TAXON.TAXONOMY_ID.equal(oldTaxonomyId))
				.orderBy(OFC_TAXON.PARENT_ID, OFC_TAXON.ID);
		Insert<OfcTaxonRecord> insert = jf.insertInto(OFC_TAXON, TAXON_FIELDS).select(select);
		int insertedCount = insert.execute();
		nextId = nextId + insertedCount;
		jf.restartSequence(OFC_TAXON_ID_SEQ, nextId);
		return idShift;
	}
	
	protected int loadMinId(JooqFactory jf, int taxonomyId) {
		Integer minId = jf.select(Factory.min(OFC_TAXON.ID))
				.from(OFC_TAXON)
				.where(OFC_TAXON.TAXONOMY_ID.equal(taxonomyId))
				.fetchOne(0, Integer.class);
		return minId == null ? 0: minId.intValue();
	}

	public int nextId() {
		JooqFactory jf = getMappingJooqFactory();
		return jf.nextId(OFC_TAXON.ID, OFC_TAXON_ID_SEQ);
	}

	protected static class JooqFactory extends MappingJooqFactory<Taxon> {

		private static final long serialVersionUID = 1L;

		public JooqFactory(Connection connection) {
			super(connection, OFC_TAXON.ID, OFC_TAXON_ID_SEQ, Taxon.class);
		}

		@Override
		public void fromRecord(Record r, Taxon t) {
			t.setSystemId(r.getValue(OFC_TAXON.ID));
			t.setTaxonId(r.getValue(OFC_TAXON.TAXON_ID));
			t.setParentId(r.getValue(OFC_TAXON.PARENT_ID));
			t.setCode(r.getValueAsString(OFC_TAXON.CODE));
			t.setScientificName(r.getValue(OFC_TAXON.SCIENTIFIC_NAME));
			String taxonRankName = r.getValue(OFC_TAXON.TAXON_RANK);
			TaxonRank taxonRank = TaxonRank.fromName(taxonRankName);
			t.setTaxonRank(taxonRank);
			t.setTaxonomyId(r.getValue(OFC_TAXON.TAXONOMY_ID));
			t.setStep(r.getValue(OFC_TAXON.STEP));
		}

		@Override
		public void fromObject(Taxon t, StoreQuery<?> q) {
			q.addValue(OFC_TAXON.ID, t.getSystemId());
			q.addValue(OFC_TAXON.TAXON_ID, t.getTaxonId());
			q.addValue(OFC_TAXON.PARENT_ID, t.getParentId());
			q.addValue(OFC_TAXON.CODE, t.getCode());
			q.addValue(OFC_TAXON.SCIENTIFIC_NAME, t.getScientificName());
			TaxonRank taxonRank = t.getTaxonRank();
			q.addValue(OFC_TAXON.TAXON_RANK, taxonRank != null ? taxonRank.getName(): null);
			q.addValue(OFC_TAXON.TAXONOMY_ID, t.getTaxonomyId());
			q.addValue(OFC_TAXON.STEP, t.getStep());
		}

		protected Insert<OfcTaxonRecord> createInsertStatement() {
			Object[] valuesPlaceholders = new String[TAXON_FIELDS.length];
			Arrays.fill(valuesPlaceholders, "?");
			return insertInto(OFC_TAXON, TAXON_FIELDS).values(valuesPlaceholders);
		}
		
		protected Object[] extractValues(Taxon item) {
			Object[] values = {
					item.getSystemId(), 
					item.getParentId(),
					item.getCode(),
					item.getScientificName(),
					item.getStep(),
					item.getTaxonId(),
					item.getTaxonRank() == null ? null: item.getTaxonRank().getName(),
					item.getTaxonomyId() 
					};
			return values;
		}

		@Override
		protected void setId(Taxon t, int id) {
			t.setSystemId(id);
		}

		@Override
		protected Integer getId(Taxon t) {
			return t.getSystemId();
		}
	}

}

