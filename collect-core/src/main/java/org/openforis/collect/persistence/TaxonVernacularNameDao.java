package org.openforis.collect.persistence;

import static org.openforis.collect.persistence.jooq.Sequences.OFC_TAXON_VERNACULAR_NAME_ID_SEQ;
import static org.openforis.collect.persistence.jooq.tables.OfcTaxon.OFC_TAXON;
import static org.openforis.collect.persistence.jooq.tables.OfcTaxonVernacularName.OFC_TAXON_VERNACULAR_NAME;

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
import org.jooq.StoreQuery;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.openforis.collect.persistence.jooq.MappingDSLContext;
import org.openforis.collect.persistence.jooq.MappingJooqDaoSupport;
import org.openforis.collect.persistence.jooq.tables.records.OfcTaxonVernacularNameRecord;
import org.openforis.idm.model.species.TaxonVernacularName;

/**
 * @author G. Miceli
 * @author S. Ricci
 * @author E. Wibowo
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class TaxonVernacularNameDao extends MappingJooqDaoSupport<Long, TaxonVernacularName, TaxonVernacularNameDao.TaxonVernacularNameDSLContext> {
	
	private static final TableField[] QUALIFIER_FIELDS = {OFC_TAXON_VERNACULAR_NAME.QUALIFIER1, OFC_TAXON_VERNACULAR_NAME.QUALIFIER2, OFC_TAXON_VERNACULAR_NAME.QUALIFIER3};

	protected static Field<?>[] TAXON_VERNACULAR_NAME_FIELDS;
	static {
		Field<?>[] fields = {
				OFC_TAXON_VERNACULAR_NAME.ID,
				OFC_TAXON_VERNACULAR_NAME.TAXON_ID,
				OFC_TAXON_VERNACULAR_NAME.VERNACULAR_NAME,
				OFC_TAXON_VERNACULAR_NAME.LANGUAGE_CODE,
				OFC_TAXON_VERNACULAR_NAME.LANGUAGE_VARIETY,
				OFC_TAXON_VERNACULAR_NAME.STEP};
		fields = ArrayUtils.addAll(fields, QUALIFIER_FIELDS);
		TAXON_VERNACULAR_NAME_FIELDS = fields;
	}

	public TaxonVernacularNameDao() {
		super(TaxonVernacularNameDao.TaxonVernacularNameDSLContext.class);
	}

	public List<TaxonVernacularName> findByVernacularName(int taxonomyId, String searchString, int maxResults) {
		return findByVernacularName(taxonomyId, searchString, null, maxResults);
	}	
	
	public List<TaxonVernacularName> findByVernacularName(int taxonomyId, String searchString, String[] qualifierValues, int maxResults) {
		TaxonVernacularNameDSLContext dsl = dsl();
		//find containing
		searchString = "%" + searchString.toLowerCase(Locale.ENGLISH) + "%";
		
		SelectConditionStep selectConditionStep = dsl.select(OFC_TAXON_VERNACULAR_NAME.fields())
			.from(OFC_TAXON_VERNACULAR_NAME)
			.join(OFC_TAXON).on(OFC_TAXON.ID.equal(OFC_TAXON_VERNACULAR_NAME.TAXON_ID))
			.where(OFC_TAXON.TAXONOMY_ID.equal(taxonomyId)
				.and(DSL.lower(OFC_TAXON_VERNACULAR_NAME.VERNACULAR_NAME).like(searchString)));
		
		if ( qualifierValues != null ) {
			for (int i = 0; i < qualifierValues.length; i++) {
				String value = qualifierValues[i];
				if ( value != null ) {
					TableField field = QUALIFIER_FIELDS[i];
					selectConditionStep.and(field.equal(value));
				}
			}
		}
		selectConditionStep.limit(maxResults);
		Result<?> result = selectConditionStep.fetch();
		List<TaxonVernacularName> entities = dsl.fromResult(result);
		return entities;
	}

	public List<TaxonVernacularName> findByTaxon(long taxonSystemId) {
		TaxonVernacularNameDSLContext dsl = dsl();
		
		SelectConditionStep selectConditionStep = dsl.select(OFC_TAXON_VERNACULAR_NAME.fields())
			.from(OFC_TAXON_VERNACULAR_NAME)
			.where(OFC_TAXON_VERNACULAR_NAME.TAXON_ID.equal(taxonSystemId));
		
		Result<?> result = selectConditionStep.fetch();
		List<TaxonVernacularName> entities = dsl.fromResult(result);
		return entities;
	}
	
	public Map<Integer, List<TaxonVernacularName>> findByTaxon(long taxonSystemId) {
		TaxonVernacularNameDSLContext dsl = dsl();
		
		SelectConditionStep selectConditionStep = dsl.select(OFC_TAXON_VERNACULAR_NAME.fields())
			.from(OFC_TAXON_VERNACULAR_NAME)
			.where(OFC_TAXON_VERNACULAR_NAME.TAXON_ID.equal(taxonSystemId));
		
		Result<?> result = selectConditionStep.fetch();
		List<TaxonVernacularName> entities = dsl.fromResult(result);
		return entities;
	}

	public List<String> loadVernacularLangCodes(int taxonomyId) {
		return dsl()
				.selectDistinct(OFC_TAXON_VERNACULAR_NAME.LANGUAGE_CODE)
				.from(OFC_TAXON_VERNACULAR_NAME)
				.where(OFC_TAXON_VERNACULAR_NAME.TAXON_ID.in(
						dsl.select(OFC_TAXON.ID)
							.from(OFC_TAXON)
							.where(OFC_TAXON.TAXONOMY_ID.eq(taxonomyId)))
				).orderBy(OFC_TAXON_VERNACULAR_NAME.LANGUAGE_CODE)
				.fetch(OFC_TAXON_VERNACULAR_NAME.LANGUAGE_CODE);
	}
	
	public void deleteByTaxonomy(int taxonomyId) {
		TaxonVernacularNameDSLContext dsl = dsl();
		SelectConditionStep selectTaxonIds = dsl
				.select(OFC_TAXON.ID)
				.from(OFC_TAXON)
				.where(OFC_TAXON.TAXONOMY_ID.equal(taxonomyId));
		dsl.delete(OFC_TAXON_VERNACULAR_NAME)
			.where(OFC_TAXON_VERNACULAR_NAME.TAXON_ID.in(selectTaxonIds))
			.execute();
	}
	
	public void insert(List<TaxonVernacularName> items) {
		if ( items != null && ! items.isEmpty() ) {
			TaxonVernacularNameDSLContext dsl = dsl();
			long id = dsl.nextId(OFC_TAXON_VERNACULAR_NAME.ID, OFC_TAXON_VERNACULAR_NAME_ID_SEQ);
			long maxId = id;
			Insert<OfcTaxonVernacularNameRecord> query = dsl.createInsertStatement();
			BatchBindStep batch = dsl.batch(query);
			for (TaxonVernacularName item : items) {
				if ( item.getId() == null ) {
					item.setId(id++);
				}
				Object[] values = dsl.extractValues(item);
				batch.bind(values);
				maxId = Math.max(maxId, item.getId());
			}
			batch.execute();
			dsl.restartSequence(OFC_TAXON_VERNACULAR_NAME_ID_SEQ, maxId + 1);
		}
	}

	public void duplicateVernacularNames(int oldTaxonomyId,
			long taxonIdGap) {
		TaxonVernacularNameDSLContext dsl = dsl();
		long nextId = dsl.nextId(OFC_TAXON_VERNACULAR_NAME.ID, OFC_TAXON_VERNACULAR_NAME_ID_SEQ);
		long minId = loadMinId(dsl, oldTaxonomyId);
		long idGap = nextId - minId;
		Field<?>[] selectFields = {
				OFC_TAXON_VERNACULAR_NAME.ID.add(idGap),
				OFC_TAXON_VERNACULAR_NAME.TAXON_ID.add(taxonIdGap),
				OFC_TAXON_VERNACULAR_NAME.VERNACULAR_NAME,
				OFC_TAXON_VERNACULAR_NAME.LANGUAGE_CODE,
				OFC_TAXON_VERNACULAR_NAME.LANGUAGE_VARIETY,
				OFC_TAXON_VERNACULAR_NAME.STEP};
		selectFields = ArrayUtils.addAll(selectFields, QUALIFIER_FIELDS);
		Select<?> select = dsl.select(selectFields)
				.from(OFC_TAXON_VERNACULAR_NAME)
					.join(OFC_TAXON)
					.on(OFC_TAXON.ID.equal(OFC_TAXON_VERNACULAR_NAME.TAXON_ID))
				.where(OFC_TAXON.TAXONOMY_ID.equal(oldTaxonomyId))
				.orderBy(OFC_TAXON_VERNACULAR_NAME.TAXON_ID, OFC_TAXON_VERNACULAR_NAME.ID);
		Insert<?> insert = dsl.insertInto(OFC_TAXON_VERNACULAR_NAME, TAXON_VERNACULAR_NAME_FIELDS).select(select);
		int insertedCount = insert.execute();
		nextId = nextId + insertedCount;
		dsl.restartSequence(OFC_TAXON_VERNACULAR_NAME_ID_SEQ, nextId);
	}
	
	protected long loadMinId(TaxonVernacularNameDSLContext jf, int taxonomyId) {
		Long minId = jf.select(DSL.min(OFC_TAXON_VERNACULAR_NAME.ID))
				.from(OFC_TAXON_VERNACULAR_NAME)
					.join(OFC_TAXON)
					.on(OFC_TAXON.ID.equal(OFC_TAXON_VERNACULAR_NAME.TAXON_ID))
				.where(OFC_TAXON.TAXONOMY_ID.equal(taxonomyId))
				.fetchOne(0, Long.class);
		return minId == null ? 0: minId.longValue();
	}

	public long nextId() {
		return dsl().nextId(OFC_TAXON_VERNACULAR_NAME.ID, OFC_TAXON_VERNACULAR_NAME_ID_SEQ);
	}

	protected static class TaxonVernacularNameDSLContext extends MappingDSLContext<Long, TaxonVernacularName> {

		private static final long serialVersionUID = 1L;

		public TaxonVernacularNameDSLContext(Configuration config) {
			super(config, OFC_TAXON_VERNACULAR_NAME.ID, OFC_TAXON_VERNACULAR_NAME_ID_SEQ, TaxonVernacularName.class);
		}

		@Override
		public void fromRecord(Record r, TaxonVernacularName t) {
			t.setId(r.getValue(OFC_TAXON_VERNACULAR_NAME.ID));
			t.setVernacularName(r.getValue(OFC_TAXON_VERNACULAR_NAME.VERNACULAR_NAME));
			t.setLanguageCode(r.getValue(OFC_TAXON_VERNACULAR_NAME.LANGUAGE_CODE));
			t.setLanguageVariety(r.getValue(OFC_TAXON_VERNACULAR_NAME.LANGUAGE_VARIETY));
			t.setTaxonSystemId(r.getValue(OFC_TAXON_VERNACULAR_NAME.TAXON_ID));
			t.setStep(r.getValue(OFC_TAXON_VERNACULAR_NAME.STEP));
			List<String> q = new ArrayList<String>();
			for ( TableField field : QUALIFIER_FIELDS ) {
				q.add((String) r.getValue(field));
			}
			t.setQualifiers(q);
		}

		@Override
		public void fromObject(TaxonVernacularName t, StoreQuery<?> q) {
			q.addValue(OFC_TAXON_VERNACULAR_NAME.ID, t.getId());
			q.addValue(OFC_TAXON_VERNACULAR_NAME.VERNACULAR_NAME,
					t.getVernacularName());
			q.addValue(OFC_TAXON_VERNACULAR_NAME.LANGUAGE_CODE,
					t.getLanguageCode());
			q.addValue(OFC_TAXON_VERNACULAR_NAME.LANGUAGE_VARIETY,
					t.getLanguageVariety());
			q.addValue(OFC_TAXON_VERNACULAR_NAME.TAXON_ID, t.getTaxonSystemId());
			q.addValue(OFC_TAXON_VERNACULAR_NAME.STEP, t.getStep());
			
			List<String> qualifiers = t.getQualifiers();
			for (int i = 0; i < qualifiers.size(); i++) {
				q.addValue(QUALIFIER_FIELDS[i], qualifiers.get(i));
			}
		}
		
		protected Insert<OfcTaxonVernacularNameRecord> createInsertStatement() {
			Object[] valuesPlaceholders = new String[TAXON_VERNACULAR_NAME_FIELDS.length];
			Arrays.fill(valuesPlaceholders, "?");
			return insertInto(OFC_TAXON_VERNACULAR_NAME, TAXON_VERNACULAR_NAME_FIELDS).values(valuesPlaceholders);
		}
		
		protected Object[] extractValues(TaxonVernacularName item) {
			Object[] values = {
					item.getId(), 
					item.getTaxonSystemId(),
					item.getVernacularName(),
					item.getLanguageCode(),
					item.getLanguageVariety(),
					item.getStep()
					};
			Object[] qualifierValues = extractQualifierValues(item);
			values = ArrayUtils.addAll(values, qualifierValues);
			return values;
		}

		protected String[] extractQualifierValues(TaxonVernacularName item) {
			String[] result = new String[QUALIFIER_FIELDS.length];
			List<String> itemQualifiers = item.getQualifiers();
			for (int i = 0; i < QUALIFIER_FIELDS.length; i++) {
				String value = i < itemQualifiers.size() ? itemQualifiers.get(i): null;
				result[i] = value;
			}
			return result;
		}

		@Override
		protected void setId(TaxonVernacularName t, Long id) {
			t.setId(id);
		}

		@Override
		protected Long getId(TaxonVernacularName t) {
			return t.getId();
		}
	}

}
