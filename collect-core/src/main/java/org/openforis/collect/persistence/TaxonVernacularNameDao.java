package org.openforis.collect.persistence;

import static org.openforis.collect.persistence.jooq.Sequences.OFC_TAXON_VERNACULAR_NAME_ID_SEQ;
import static org.openforis.collect.persistence.jooq.Tables.OFC_USER_ROLE;
import static org.openforis.collect.persistence.jooq.tables.OfcTaxonVernacularName.OFC_TAXON_VERNACULAR_NAME;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SimpleSelectQuery;
import org.jooq.StoreQuery;
import org.openforis.collect.persistence.jooq.MappingJooqDaoSupport;
import org.openforis.collect.persistence.jooq.MappingJooqFactory;
import org.openforis.collect.persistence.jooq.tables.records.OfcUserRoleRecord;
import org.openforis.idm.model.species.TaxonVernacularName;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author G. Miceli
 * @author E. Wibowo
 */
@Transactional
public class TaxonVernacularNameDao
		extends
		MappingJooqDaoSupport<TaxonVernacularName, TaxonVernacularNameDao.JooqFactory> {
	public TaxonVernacularNameDao() {
		super(TaxonVernacularNameDao.JooqFactory.class);
	}

	public List<TaxonVernacularName> findByVernacularName(String searchString,
			int maxResults) {
		return findContaining(OFC_TAXON_VERNACULAR_NAME.VERNACULAR_NAME,
				searchString, maxResults);
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

	protected static class JooqFactory extends
			MappingJooqFactory<TaxonVernacularName> {

		private static final long serialVersionUID = 1L;

		public JooqFactory(Connection connection) {
			super(connection, OFC_TAXON_VERNACULAR_NAME.ID,
					OFC_TAXON_VERNACULAR_NAME_ID_SEQ, TaxonVernacularName.class);
		}

		@Override
		public void fromRecord(Record r, TaxonVernacularName t) {
			t.setId(r.getValue(OFC_TAXON_VERNACULAR_NAME.ID));
			t.setVernacularName(r
					.getValue(OFC_TAXON_VERNACULAR_NAME.VERNACULAR_NAME));
			t.setLanguageCode(r
					.getValue(OFC_TAXON_VERNACULAR_NAME.LANGUAGE_CODE));
			t.setLanguageVariety(r
					.getValue(OFC_TAXON_VERNACULAR_NAME.LANGUAGE_VARIETY));
			t.setTaxonSystemId(r.getValue(OFC_TAXON_VERNACULAR_NAME.TAXON_ID));
			t.setStep(r.getValue(OFC_TAXON_VERNACULAR_NAME.STEP));
			List<String> q = new ArrayList<String>();
			if(r.getValue(OFC_TAXON_VERNACULAR_NAME.QUALIFIER1)!=null) q.add(r.getValue(OFC_TAXON_VERNACULAR_NAME.QUALIFIER1));				
			if(r.getValue(OFC_TAXON_VERNACULAR_NAME.QUALIFIER2)!=null) q.add(r.getValue(OFC_TAXON_VERNACULAR_NAME.QUALIFIER2));
			if(r.getValue(OFC_TAXON_VERNACULAR_NAME.QUALIFIER3)!=null) q.add(r.getValue(OFC_TAXON_VERNACULAR_NAME.QUALIFIER3));
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
			
			if(t.getQualifiers()!=null)
			{
				for(int i=0;i<t.getQualifiers().size();i++)
				{
					if(i==0) q.addValue(OFC_TAXON_VERNACULAR_NAME.QUALIFIER1, t.getQualifiers().get(0));
					else if(i==1) q.addValue(OFC_TAXON_VERNACULAR_NAME.QUALIFIER2, t.getQualifiers().get(0));
					else if(i==2) q.addValue(OFC_TAXON_VERNACULAR_NAME.QUALIFIER3, t.getQualifiers().get(0));
				}
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
