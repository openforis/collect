package org.openforis.collect.persistence;

import static org.openforis.collect.persistence.jooq.Sequences.OFC_TAXON_ID_SEQ;
import static org.openforis.collect.persistence.jooq.tables.OfcTaxon.OFC_TAXON;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.ArrayUtils;
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
import org.openforis.collect.model.CollectTaxonomy;
import org.openforis.collect.persistence.jooq.MappingDSLContext;
import org.openforis.collect.persistence.jooq.MappingJooqDaoSupport;
import org.openforis.collect.persistence.jooq.tables.records.OfcTaxonRecord;
import org.openforis.commons.collection.CollectionUtils;
import org.openforis.idm.metamodel.ReferenceDataSchema.TaxonomyDefinition;
import org.openforis.idm.model.species.Taxon;
import org.openforis.idm.model.species.Taxon.TaxonRank;

/**
 * @author G. Miceli
 * @author S. Ricci
 */
public class TaxonDao extends MappingJooqDaoSupport<Taxon, TaxonDao.TaxonDSLContext> {
	
	@SuppressWarnings("rawtypes")
	private static Field[] GENERIC_FIELDS = {
			OFC_TAXON.ID,
			OFC_TAXON.PARENT_ID,
			OFC_TAXON.CODE,
			OFC_TAXON.SCIENTIFIC_NAME,
			OFC_TAXON.STEP,
			OFC_TAXON.TAXON_ID,
			OFC_TAXON.TAXON_RANK,
			OFC_TAXON.TAXONOMY_ID};
	
	@SuppressWarnings("rawtypes")
	private static Field[] INFO_FIELDS = {
			OFC_TAXON.INFO01,
			OFC_TAXON.INFO02,
			OFC_TAXON.INFO03,
			OFC_TAXON.INFO04,
			OFC_TAXON.INFO05,
			OFC_TAXON.INFO06,
			OFC_TAXON.INFO07,
			OFC_TAXON.INFO08,
			OFC_TAXON.INFO09,
			OFC_TAXON.INFO10,
			OFC_TAXON.INFO11,
			OFC_TAXON.INFO12,
			OFC_TAXON.INFO13,
			OFC_TAXON.INFO14,
			OFC_TAXON.INFO15,
			OFC_TAXON.INFO16,
			OFC_TAXON.INFO17,
			OFC_TAXON.INFO18,
			OFC_TAXON.INFO19,
			OFC_TAXON.INFO20
	};

	private static Field<?>[] ALL_FIELDS = ArrayUtils.addAll(GENERIC_FIELDS, INFO_FIELDS);

	public TaxonDao() {
		super(TaxonDao.TaxonDSLContext.class);
	}

	public Taxon loadById(CollectTaxonomy taxonomy, int taxonId) {
		return super.loadById(dsl(taxonomy), taxonId);
	}

	@Override
	public Taxon loadById(int id) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void insert(Taxon entity) {
		dsl((CollectTaxonomy) entity.getTaxonomy()).insertQuery(entity).execute();
	}
	
	@Override
	public void update(Taxon entity) {
		dsl((CollectTaxonomy) entity.getTaxonomy()).updateQuery(entity).execute();
	}

	public void delete(Taxon entity) {
		dsl((CollectTaxonomy) entity.getTaxonomy()).deleteQuery(entity.getSystemId()).execute();
	}

	public List<Taxon> findByCode(CollectTaxonomy taxonomy, TaxonRank highestRank, String searchString, int maxResults) {
		return findStartingWith(taxonomy, highestRank, OFC_TAXON.CODE, searchString, maxResults);
	}
	
	public List<Taxon> findByScientificName(CollectTaxonomy taxonomy, TaxonRank highestRank, String searchString, int maxResults) {
		return findStartingWith(taxonomy, highestRank, OFC_TAXON.SCIENTIFIC_NAME, searchString, maxResults);
	}

	protected List<Taxon> findStartingWith(CollectTaxonomy taxonomy, TaxonRank highestRank, TableField<?,String> field, String searchString, int maxResults) {
		TaxonDSLContext dsl = dsl(taxonomy);
		searchString = searchString.toLowerCase(Locale.ENGLISH) + "%";
		SelectConditionStep<Record> query = dsl.select()
			.from(OFC_TAXON)
			.where(OFC_TAXON.TAXONOMY_ID.equal(taxonomy.getId())
				.and(DSL.lower(field).like(searchString)));
		if (highestRank != null) {
			List<String> selfAndDescendantRankNames = CollectionUtils.project(highestRank.getSelfAndDescendants(), "name");
			query.and(OFC_TAXON.TAXON_RANK.in(selfAndDescendantRankNames));
		}
		query.orderBy(field, OFC_TAXON.SCIENTIFIC_NAME, OFC_TAXON.CODE);
		query.limit(maxResults);
		Result<?> result = query.fetch();
		List<Taxon> entities = dsl.fromResult(result);
		return entities;
	}
	
	public int countTaxons(CollectTaxonomy taxonomy) {
		SelectQuery<?> q = dsl(taxonomy).selectCountQuery();
		q.addConditions(OFC_TAXON.TAXONOMY_ID.equal(taxonomy.getId()));
		Record r = q.fetchOne();
		return (Integer) r.getValue(0);
	}
	
	public List<Taxon> loadTaxons(CollectTaxonomy taxonomy, int offset, int maxRecords) {
		return loadTaxons(taxonomy, offset, maxRecords, OFC_TAXON.SCIENTIFIC_NAME.asc());
	}
	
	public List<Taxon> loadTaxonsForTreeBuilding(CollectTaxonomy taxonomy) {
		return loadTaxons(taxonomy, 0, Integer.MAX_VALUE, OFC_TAXON.PARENT_ID.asc().nullsFirst());
	}
	
	public List<Taxon> loadTaxons(CollectTaxonomy taxonomy, int offset, int maxRecords, SortField<?> sortField) {
		TaxonDSLContext dsl = dsl(taxonomy);
		SelectQuery<Record> q = dsl.selectQuery();	
		q.addFrom(OFC_TAXON);
		q.addConditions(OFC_TAXON.TAXONOMY_ID.equal(taxonomy.getId()));

		q.addOrderBy(sortField);
		
		q.addLimit(offset, maxRecords);
		
		Result<Record> result = q.fetch();
		
		return dsl.fromResult(result);
	}
	
	public void deleteByTaxonomy(CollectTaxonomy taxonomy) {
		dsl(taxonomy).delete(OFC_TAXON)
			.where(OFC_TAXON.TAXONOMY_ID.equal(taxonomy.getId()))
			.execute();
	}
	
	/**
	 * Inserts the items in batch.
	 */
	public void insert(CollectTaxonomy taxonomy, List<Taxon> items) {
		if ( items != null && ! items.isEmpty() ) {
			TaxonDSLContext dsl = dsl(taxonomy);
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

	public int duplicateTaxons(CollectTaxonomy oldTaxonomy, CollectTaxonomy newTaxonomy) {
		TaxonDSLContext dsl = dsl(oldTaxonomy);
		int nextId = dsl.nextId(OFC_TAXON.ID, OFC_TAXON_ID_SEQ);
		int minId = loadMinId(dsl, oldTaxonomy.getId());
		int idShift = nextId - minId;
		
		List<Field<?>> selectFields = new ArrayList<Field<?>>();
		selectFields.addAll(Arrays.<Field<?>>asList(
				OFC_TAXON.ID.add(idShift),
				OFC_TAXON.PARENT_ID.add(idShift),
				OFC_TAXON.CODE,
				OFC_TAXON.SCIENTIFIC_NAME,
				OFC_TAXON.STEP,
				OFC_TAXON.TAXON_ID,
				OFC_TAXON.TAXON_RANK,
				DSL.val(newTaxonomy.getId())
		));
		selectFields.addAll(Arrays.<Field<?>>asList(INFO_FIELDS));
		
		Select<?> select = dsl.select(selectFields)
				.from(OFC_TAXON)
				.where(OFC_TAXON.TAXONOMY_ID.equal(oldTaxonomy.getId()))
				.orderBy(OFC_TAXON.PARENT_ID, OFC_TAXON.ID);
		
		Insert<OfcTaxonRecord> insert = dsl.insertInto(OFC_TAXON, ALL_FIELDS).select(select);
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

	public int nextId(CollectTaxonomy taxonomy) {
		return dsl(taxonomy).nextId(OFC_TAXON.ID, OFC_TAXON_ID_SEQ);
	}
	
	@Override
	protected TaxonDSLContext dsl() {
		throw new UnsupportedOperationException();
	}
	
	private TaxonDSLContext dsl(CollectTaxonomy taxonomy) {
		return new TaxonDSLContext(getConfiguration(), taxonomy);
	}

	protected static class TaxonDSLContext extends MappingDSLContext<Taxon> {

		private static final long serialVersionUID = 1L;
		private CollectTaxonomy taxonomy;

		public TaxonDSLContext(Configuration config, CollectTaxonomy taxonomy) {
			super(config, OFC_TAXON.ID, OFC_TAXON_ID_SEQ, Taxon.class);
			this.taxonomy = taxonomy;
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
			t.setInfoAttributes(extractInfoAttributes(r));
			t.setTaxonomy(taxonomy);
		}

		@SuppressWarnings("unchecked")
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
			addFieldValues(q, INFO_FIELDS, t.getInfoAttributes());
		}

		@SuppressWarnings("unchecked")
		private List<String> extractInfoAttributes(Record r) {
			List<String> infoAttributes = extractFields(r, (Field<String>[]) INFO_FIELDS);
			TaxonomyDefinition taxonomyDef = taxonomy.getSurvey().getReferenceDataSchema().getTaxonomyDefinition(taxonomy.getName());
			return infoAttributes.subList(0, taxonomyDef.getAttributes().size());
		}

		protected Insert<OfcTaxonRecord> createInsertStatement() {
			Object[] valuesPlaceholders = new String[ALL_FIELDS.length];
			Arrays.fill(valuesPlaceholders, "?");
			return insertInto(OFC_TAXON, ALL_FIELDS).values(valuesPlaceholders);
		}
		
		protected Object[] extractValues(Taxon item) {
			@SuppressWarnings("unchecked")
			List<Object> values = new ArrayList<Object>(Arrays.asList(
					item.getSystemId(), 
					item.getParentId(),
					item.getCode(),
					item.getScientificName(),
					item.getStep(),
					item.getTaxonId(),
					item.getTaxonRank() == null ? null: item.getTaxonRank().getName(),
					item.getTaxonomyId()
					));
			values.addAll(CollectionUtils.copyAndFillWithNulls(item.getInfoAttributes(), INFO_FIELDS.length));
			return values.toArray(new Object[values.size()]);
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

