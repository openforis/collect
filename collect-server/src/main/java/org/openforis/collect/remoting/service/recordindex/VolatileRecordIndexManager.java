package org.openforis.collect.remoting.service.recordindex;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.openforis.collect.manager.RecordIndexException;
import org.openforis.collect.manager.RecordIndexManager;
import org.openforis.collect.model.CollectRecord;

/**
 * 
 * @author S. Ricci
 *
 */
public class VolatileRecordIndexManager extends RecordIndexManager {

	private static final long serialVersionUID = 1L;

	@Override
	public synchronized boolean init() throws RecordIndexException {
		initIndexDirectory();
		initialized = true;
		return initialized;
	}
	
	@Override
	protected Directory createIndexDirectory() throws RecordIndexException {
		RAMDirectory directory = new RAMDirectory();
		return directory;
	}
	
	@Override
	public void index(CollectRecord record) throws RecordIndexException {
		IndexWriter indexWriter = null;
		try {
			indexWriter = createIndexWriter();
			clean(indexWriter); //temporary index is relative only to one record
			index(indexWriter, record);
		} catch (Exception e) {
			throw new RecordIndexException(e);
		} finally {
			close(indexWriter);
		}
	}

	private void clean(IndexWriter indexWriter) {
		try {
			indexWriter.deleteAll();
		} catch (Throwable t) {
			//DO NOTHING
		}
	}

}
