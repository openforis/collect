package org.openforis.idm.metamodel.xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.openforis.idm.metamodel.CodeListService;
import org.openforis.idm.metamodel.PersistedCodeListItem;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.xml.internal.unmarshal.SurveyCodeListImporterPR;

/**
 * Load code lists inside a XML file and store them into the database
 * using a ExternalCodeListPersister
 * 
 * @author S. Ricci
 */
public class CodeListImporter {
	
	private static final String UTF8_ENCODING = "UTF-8";

	private static final int BATCH_SIZE = 1000;
	
	private CodeListService service;

	private List<PersistedCodeListItem> itemsToPersistBuffer;
	private long nextItemId;
	
	public CodeListImporter(CodeListService service) {
		this(service, 1);
	}
	
	public CodeListImporter(CodeListService service, long nextItemSystemId) {
		this.service = service;
		this.nextItemId = nextItemSystemId;
		this.itemsToPersistBuffer = new ArrayList<PersistedCodeListItem>();
	}

	public void importCodeLists(Survey survey, InputStream is) throws IdmlParseException {
		try {
			SurveyCodeListImporterPR persister = new SurveyCodeListImporterPR(this, survey);
			persister.parse(is, UTF8_ENCODING);
			flushItemsToPersistBuffer();
		} catch (XmlParseException e) {
			throw new IdmlParseException(e);
		} catch (IOException e) {
			throw new IdmlParseException(e);
		}
	}
	
	private void flushItemsToPersistBuffer() {
		service.save(itemsToPersistBuffer);
		itemsToPersistBuffer.clear();
	}

	public void persistItem(PersistedCodeListItem item)  {
		itemsToPersistBuffer.add(item);
		if ( itemsToPersistBuffer.size() > BATCH_SIZE) {
			flushItemsToPersistBuffer();
		}
	}
	
	public CodeListService getService() {
		return service;
	}

	public long nextItemId() {
		return nextItemId++;
	}

}
