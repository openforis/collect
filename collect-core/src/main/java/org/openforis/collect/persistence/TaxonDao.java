package org.openforis.collect.persistence;

import static org.openforis.collect.persistence.jooq.Sequences.OFC_TAXON_ID_SEQ;
import static org.openforis.collect.persistence.jooq.tables.OfcTaxon.OFC_TAXON;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.jooq.BatchBindStep;
import org.jooq.Configuration;
import org.jooq.Field;
import org.jooq.Insert;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.Select;
import org.jooq.SelectConditionStep;
import org.jooq.SelectQuery;
import org.jooq.SortField;
import org.jooq.StoreQuery;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.openforis.collect.persistence.jooq.MappingDSLContext;
import org.openforis.collect.persistence.jooq.MappingJooqDaoSupport;
import org.openforis.collect.persistence.jooq.tables.records.OfcTaxonRecord;
import org.openforis.idm.model.species.Taxon;
import org.openforis.idm.model.species.Taxon.TaxonRank;

/**
 * @author G. Miceli
 * @author S. Ricci
 */
public class TaxonDao extends MappingJooqDaoSupport<Taxon, TaxonDao.TaxonDSLContext> {
	
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
		super(TaxonDao.TaxonDSLContext.class);
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
		return findStartingWith(OFC_TAXON.CODE, taxonomyId, null, searchString, maxResults);
	}

	public List<Taxon> findByCode(int taxonomyId, TaxonRank rank, String searchString, int maxResults) {
		return findStartingWith(OFC_TAXON.CODE, taxonomyId, rank, searchString, maxResults);
	}
	
	public List<Taxon> findByScientificName(int taxonomyId, String searchString, int maxResults) {
		return findStartingWith(OFC_TAXON.SCIENTIFIC_NAME, taxonomyId, null, searchString, maxResults);
	}

	protected List<Taxon> findStartingWith(TableField<?,String> field, int taxonomyId, TaxonRank rank, String searchString, int maxResults) {
		TaxonDSLContext dsl = dsl();
		searchString = searchString.toLowerCase(Locale.ENGLISH) + "%";
		SelectConditionStep<Record> query = dsl.select()
			.from(OFC_TAXON)
			.where(OFC_TAXON.TAXONOMY_ID.equal(taxonomyId)
				.and(DSL.lower(field).like(searchString)));
		if (rank != null) {
			query.and(OFC_TAXON.TAXON_RANK.equal(rank.name()));
		}
		query.limit(maxResults);
		Result<?> result = query.fetch();
		List<Taxon> entities = dsl.fromResult(result);
		return entities;
	}
	
	public int countTaxons(int taxonomyId) {
		SelectQuery<?> q = dsl().selectCountQuery();
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
		TaxonDSLContext dsl = dsl();
		SelectQuery<Record> q = dsl.selectQuery();	
		q.addFrom(OFC_TAXON);
		q.addConditions(OFC_TAXON.TAXONOMY_ID.equal(taxonomyId));

		q.addOrderBy(sortField);
		
		q.addLimit(offset, maxRecords);
		
		Result<Record> result = q.fetch();
		
		return dsl.fromResult(result);
	}
	
	public void deleteByTaxonomy(int taxonomyId) {
		dsl().delete(OFC_TAXON)
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
			TaxonDSLContext dsl = dsl();
			int id = dsl.nextId(OFC_TAXON.ID, OFC_TAXON_ID_SEQ);
			int maxId = id;
			Insert<OfcTaxonRecord> query = dsl.createInsertStatement();
			BatchBindStep batch = dsl.batch(query);
			for (Taxon item : items) {
				if ( item.getSystemId() == null ) {
					item.setSystemId(id++);
				}
				Object[] values = dsl.extractValues(item);
				batch.bind(values);
				maxId = Math.max(maxId, item.getSystemId());
			}
			batch.execute();
			dsl.restartSequence(OFC_TAXON_ID_SEQ, maxId + 1);
		}
	}

	public int duplicateTaxons(int oldTaxonomyId, Integer newTaxonomyId) {
		TaxonDSLContext dsl = dsl();
		int nextId = dsl.nextId(OFC_TAXON.ID, OFC_TAXON_ID_SEQ);
		int minId = loadMinId(dsl, oldTaxonomyId);
		int idShift = nextId - minId;
		Field<?>[] selectFields = {
				OFC_TAXON.ID.add(idShift),
				OFC_TAXON.PARENT_ID.add(idShift),
				OFC_TAXON.CODE,
				OFC_TAXON.SCIENTIFIC_NAME,
				OFC_TAXON.STEP,
				OFC_TAXON.TAXON_ID,
				OFC_TAXON.TAXON_RANK,
				DSL.val(newTaxonomyId)
			};
		Select<?> select = dsl.select(selectFields)
				.from(OFC_TAXON)
				.where(OFC_TAXON.TAXONOMY_ID.equal(oldTaxonomyId))
				.orderBy(OFC_TAXON.PARENT_ID, OFC_TAXON.ID);
		Insert<OfcTaxonRecord> insert = dsl.insertInto(OFC_TAXON, TAXON_FIELDS).select(select);
		int insertedCount = insert.execute();
		nextId = nextId + insertedCount;
		dsl.restartSequence(OFC_TAXON_ID_SEQ, nextId);
		return idShift;
	}
	
	protected int loadMinId(TaxonDSLContext jf, int taxonomyId) {
		Integer minId = jf.select(DSL.min(OFC_TAXON.ID))
				.from(OFC_TAXON)
				.where(OFC_TAXON.TAXONOMY_ID.equal(taxonomyId))
				.fetchOne(0, Integer.class);
		return minId == null ? 0: minId.intValue();
	}

	public int nextId() {
		return dsl().nextId(OFC_TAXON.ID, OFC_TAXON_ID_SEQ);
	}

	protected static class TaxonDSLContext extends MappingDSLContext<Taxon> {

		private static final long serialVersionUID = 1L;

		public TaxonDSLContext(Configuration config) {
			super(config, OFC_TAXON.ID, OFC_TAXON_ID_SEQ, Taxon.class);
		}

		@Override
		public void fromRecord(Record r, Taxon t) {
			t.setSystemId(r.getValue(OFC_TAXON.ID));
			t.setTaxonId(r.getValue(OFC_TAXON.TAXON_ID));
			t.setParentId(r.getValue(OFC_TAXON.PARENT_ID));
			t.setCode(r.getValue(OFC_TAXON.CODE));
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

