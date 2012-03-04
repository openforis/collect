package org.openforis.collect.persistence;

import static org.openforis.collect.persistence.jooq.Sequences.OFC_TAXON_VERNACULAR_NAME_ID_SEQ;
import static org.openforis.collect.persistence.jooq.tables.OfcTaxonVernacularName.OFC_TAXON_VERNACULAR_NAME;

import java.sql.Connection;
import java.util.List;

import org.jooq.Record;
import org.jooq.UpdatableRecord;
import org.openforis.collect.persistence.jooq.MappingJooqDaoSupport;
import org.openforis.collect.persistence.jooq.MappingJooqFactory;
import org.openforis.idm.model.species.TaxonVernacularName;

/**
 * @author G. Miceli
 */
public class TaxonVernacularNameDao extends MappingJooqDaoSupport<TaxonVernacularName, TaxonVernacularNameDao.JooqFactory> {
	public TaxonVernacularNameDao() {
		super(TaxonVernacularNameDao.JooqFactory.class);
	}

	public List<TaxonVernacularName> findByVernacularName(String searchString, int maxResults) {
		return findContaining(OFC_TAXON_VERNACULAR_NAME.VERNACULAR_NAME, searchString, maxResults);
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
			t.setTaxonId(r.getValue(OFC_TAXON_VERNACULAR_NAME.TAXON_ID));
			t.setStep(r.getValue(OFC_TAXON_VERNACULAR_NAME.STEP));
		}
		
		@Override
		public void toRecord(TaxonVernacularName t, UpdatableRecord<?> r) {
			r.setValue(OFC_TAXON_VERNACULAR_NAME.ID, t.getId());
			r.setValue(OFC_TAXON_VERNACULAR_NAME.VERNACULAR_NAME, t.getVernacularName());
			r.setValue(OFC_TAXON_VERNACULAR_NAME.LANGUAGE_CODE, t.getLanguageCode());
			r.setValue(OFC_TAXON_VERNACULAR_NAME.LANGUAGE_VARIETY, t.getLanguageVariety());
			r.setValue(OFC_TAXON_VERNACULAR_NAME.TAXON_ID, t.getTaxonId());			
			r.setValue(OFC_TAXON_VERNACULAR_NAME.STEP, t.getStep());
		}

		@Override
		protected void setId(TaxonVernacularName t, int id) {
			t.setId(id);
		}

		@Override
		protected int getId(TaxonVernacularName t) {
			return t.getId();
		}
	}
}
