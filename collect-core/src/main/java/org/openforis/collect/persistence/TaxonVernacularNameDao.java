package org.openforis.collect.persistence;

import static org.openforis.collect.persistence.jooq.Sequences.OFC_TAXON_VERNACULAR_NAME_ID_SEQ;
import static org.openforis.collect.persistence.jooq.tables.OfcTaxon.OFC_TAXON;
import static org.openforis.collect.persistence.jooq.tables.OfcTaxonVernacularName.OFC_TAXON_VERNACULAR_NAME;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SelectConditionStep;
import org.jooq.StoreQuery;
import org.jooq.TableField;
import org.openforis.collect.persistence.jooq.MappingJooqDaoSupport;
import org.openforis.collect.persistence.jooq.MappingJooqFactory;
import org.openforis.idm.model.species.TaxonVernacularName;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author G. Miceli
 * @author S. Ricci
 * @author E. Wibowo
 */
@Transactional
@SuppressWarnings({ "unchecked", "rawtypes" })
public class TaxonVernacularNameDao extends MappingJooqDaoSupport<TaxonVernacularName, TaxonVernacularNameDao.JooqFactory> {
	
	private static final TableField[] QUALIFIER_FIELDS = {OFC_TAXON_VERNACULAR_NAME.QUALIFIER1, OFC_TAXON_VERNACULAR_NAME.QUALIFIER2, OFC_TAXON_VERNACULAR_NAME.QUALIFIER3};

	public TaxonVernacularNameDao() {
		super(TaxonVernacularNameDao.JooqFactory.class);
	}

	@Override
	public TaxonVernacularName loadById(int id) {
		return super.loadById(id);
	}

	@Override
	public void insert(TaxonVernacularName entity) {
		super.insert(entity);
	}

	@Override
	public void update(TaxonVernacularName entity) {
		super.update(entity);
	}

	@Override
	public void delete(int id) {
		super.delete(id);
	}
	
	public List<TaxonVernacularName> findByVernacularName(int taxonomyId, String searchString, int maxResults) {
		return findByVernacularName(taxonomyId, searchString, null, maxResults);
	}	
	
	public List<TaxonVernacularName> findByVernacularName(int taxonomyId, String searchString, String[] qualifierValues, int maxResults) {
		JooqFactory jf = getMappingJooqFactory();
		//find containing
		searchString = "%" + searchString.toUpperCase() + "%";
		
		SelectConditionStep selectConditionStep = jf.select(OFC_TAXON_VERNACULAR_NAME.getFields())
			.from(OFC_TAXON_VERNACULAR_NAME)
			.join(OFC_TAXON).on(OFC_TAXON.ID.equal(OFC_TAXON_VERNACULAR_NAME.TAXON_ID))
			.where(OFC_TAXON.TAXONOMY_ID.equal(taxonomyId)
				.and(JooqFactory.upper(OFC_TAXON_VERNACULAR_NAME.VERNACULAR_NAME).like(searchString)));
		
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
		List<TaxonVernacularName> entities = jf.fromResult(result);
		return entities;
	}

	public List<TaxonVernacularName> findByTaxon(int taxonId) {
		JooqFactory jf = getMappingJooqFactory();
		
		SelectConditionStep selectConditionStep = jf.select(OFC_TAXON_VERNACULAR_NAME.getFields())
			.from(OFC_TAXON_VERNACULAR_NAME)
			.where(OFC_TAXON_VERNACULAR_NAME.TAXON_ID.equal(taxonId));
		
		Result<?> result = selectConditionStep.fetch();
		List<TaxonVernacularName> entities = jf.fromResult(result);
		return entities;
	}

	
	public void deleteByTaxonomy(int taxonomyId) {
		JooqFactory jf = getMappingJooqFactory();
		SelectConditionStep selectTaxonIds = jf.select(OFC_TAXON.ID).from(OFC_TAXON).where(OFC_TAXON.TAXONOMY_ID.equal(taxonomyId));
		jf.delete(OFC_TAXON_VERNACULAR_NAME).where(OFC_TAXON_VERNACULAR_NAME.TAXON_ID.in(selectTaxonIds)).execute();
	}
	
	protected static class JooqFactory extends MappingJooqFactory<TaxonVernacularName> {

		private static final long serialVersionUID = 1L;

		public JooqFactory(Connection connection) {
			super(connection, OFC_TAXON_VERNACULAR_NAME.ID, OFC_TAXON_VERNACULAR_NAME_ID_SEQ, TaxonVernacularName.class);
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

		@Override
		protected void setId(TaxonVernacularName t, int id) {
			t.setId(id);
		}

		@Override
		protected Integer getId(TaxonVernacularName t) {
			return t.getId();
		}
	}
}
